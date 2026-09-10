package com.aa.msw.api.station;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.database.repository.dao.StationDao;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Sample;
import com.aa.msw.model.Station;
import com.aa.msw.source.french.vigicrues.historical.lastThirty.FrenchLast30DaysSampleFetchService;
import com.aa.msw.source.french.vigicrues.stations.FrenchStationFetchService;
import com.aa.msw.source.german.bw.sample.BwSampleFetchService;
import com.aa.msw.source.german.bw.stations.DeBwStationFetchService;
import com.aa.msw.source.rivermap.sample.RivermapSampleFetchService;
import com.aa.msw.source.rivermap.stations.RivermapStationFetchService;
import com.aa.msw.source.swiss.existenz.sample.SwissSampleFetchService;
import com.aa.msw.source.swiss.hydrodaten.stations.SwissStationFetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Profile("!test")
@Service
public class StationApiServiceImpl implements StationApiService {
    private static final Logger LOG = LoggerFactory.getLogger(StationApiServiceImpl.class);
    // holds the stations in-memory for faster access - but also in db for fast startup (mostly for dev purposes)

    private final StationDao stationDao;
    private final SwissStationFetchService swissStationFetchService;
    private final FrenchStationFetchService frenchStationFetchService;
    private final DeBwStationFetchService deBwStationFetchService;
    private final RivermapStationFetchService rivermapStationFetchService;
    private final SampleDao sampleDao;
    private final SwissSampleFetchService swissSampleFetchService;
    private final FrenchLast30DaysSampleFetchService frenchLast30DaysSampleFetchService;
    private final BwSampleFetchService bwSampleFetchService;
    private final RivermapSampleFetchService rivermapSampleFetchService;
    /**
     * A Rivermap station without a reading younger than this is considered down and is deleted.
     */
    static final Period RIVERMAP_MAX_READING_AGE = Period.ofMonths(3);

    private Set<Station> stations = new HashSet<>();

    public StationApiServiceImpl(SwissStationFetchService swissStationFetchService, StationDao stationDao, FrenchStationFetchService frenchStationFetchService, DeBwStationFetchService deBwStationFetchService, RivermapStationFetchService rivermapStationFetchService, SampleDao sampleDao, SwissSampleFetchService swissSampleFetchService, FrenchLast30DaysSampleFetchService frenchLast30DaysSampleFetchService, BwSampleFetchService bwSampleFetchService, RivermapSampleFetchService rivermapSampleFetchService) {
        this.swissStationFetchService = swissStationFetchService;
        this.stationDao = stationDao;
        this.frenchStationFetchService = frenchStationFetchService;
        this.deBwStationFetchService = deBwStationFetchService;
        this.rivermapStationFetchService = rivermapStationFetchService;
        this.sampleDao = sampleDao;
        this.swissSampleFetchService = swissSampleFetchService;
        this.frenchLast30DaysSampleFetchService = frenchLast30DaysSampleFetchService;
        this.bwSampleFetchService = bwSampleFetchService;
        this.rivermapSampleFetchService = rivermapSampleFetchService;
    }

    @Override
    @Transactional
    public Set<Station> getStations() {
        if (!stations.isEmpty()) {
            return stations;
        }
        Set<Station> stationsFromDb = stationDao.getStations();
        if (stationsFromDb.isEmpty()) {
            fetchStationsAndSaveToDb();
        } else {
            stations = stationsFromDb;
        }
        return stations;
    }

    @Scheduled(cron = "0 0 23 * * *") // Runs at 23:00 every day
    @Transactional
    @Override
    public void fetchStationsAndSaveToDb() {
        Set<Station> fetchedStations = fetchStations();
        if (!fetchedStations.isEmpty()) {
            Set<Station> existingStations = stationDao.getStations();

            Set<Station> onlyNewStations = getOnlyNewStations(fetchedStations, existingStations);

            persistStationsToDb(onlyNewStations);
            stations = stationDao.getStations();
        }
    }

    private Set<Station> getOnlyNewStations(Set<Station> fetchedStations, Set<Station> existingStations) {
        Set<Station> newStations = new HashSet<>(fetchedStations);
        newStations.removeIf(s ->
                existingStations.stream().anyMatch(existing -> s.stationId().equals(existing.stationId())));
        return newStations;
    }

    private void persistStationsToDb(Set<Station> fetchedStations) {
        for (Station station : fetchedStations) {
            stationDao.persist(station);
        }
    }

    private Set<Station> fetchStations() {
        Set<Station> stations = frenchStationFetchService.fetchStations();
        stations.addAll(swissStationFetchService.fetchStations());
        stations.addAll(deBwStationFetchService.fetchStations());
        Set<Station> rivermapStations = rivermapStationFetchService.fetchStations();
        stations.addAll(rivermapStations);
        Optional<Set<ApiStationId>> rivermapStationsWithReadings = fetchRivermapStationsWithRecentReadings(rivermapStations);
        return stations.stream()
                .map(station -> processFetchedStations(station, rivermapStationsWithReadings))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    /**
     * Probing the readings of every single Rivermap station would cost one (rate limited) request each, so the readings
     * of all Rivermap stations are fetched with one request and the validation checks against that set.
     * <p>
     * The readings endpoint answers with the <b>latest</b> reading of a station even when it lies outside the requested
     * window (gauges that stopped reporting years ago are still listed), so being part of the answer is not enough:
     * only stations whose newest reading is younger than {@link #RIVERMAP_MAX_READING_AGE} count as alive.
     *
     * @return the Rivermap stations that delivered a reading recently - empty if the request failed (no readings for
     * any of the stations means the API did not answer, not that all gauges are dead)
     */
    private Optional<Set<ApiStationId>> fetchRivermapStationsWithRecentReadings(Set<Station> rivermapStations) {
        if (rivermapStations.isEmpty()) {
            return Optional.of(Set.of());
        }
        Set<ApiStationId> stationIds = rivermapStations.stream().map(Station::stationId).collect(Collectors.toSet());
        List<Sample> samples = rivermapSampleFetchService.fetchSamples(stationIds, RivermapSampleFetchService.MAX_WINDOW_MINUTES);
        if (samples.isEmpty()) {
            LOG.warn("Got no Rivermap readings at all for {} stations - keeping all of them.", stationIds.size());
            return Optional.empty();
        }
        OffsetDateTime oldestAcceptedReading = OffsetDateTime.now().minus(RIVERMAP_MAX_READING_AGE);
        Set<ApiStationId> withRecentReadings = samples.stream()
                .filter(sample -> sample.getTimestamp().isAfter(oldestAcceptedReading))
                .map(Sample::getStationId)
                .collect(Collectors.toSet());
        LOG.info("{} of {} Rivermap stations delivered a reading within the last {} - dropping the others.",
                withRecentReadings.size(), stationIds.size(), RIVERMAP_MAX_READING_AGE);
        return Optional.of(withRecentReadings);
    }

    /**
     * @param station - the station to process
     * @return Station
     * This method checks if the given station is a valid station (has data)
     * - if valid -> return station
     * - if invalid -> return empty and delete the station from db (if exists)
     */
    private Optional<Station> processFetchedStations(Station station, Optional<Set<ApiStationId>> rivermapStationsWithReadings) {
        if (isValidStation(station, rivermapStationsWithReadings)) {
            return Optional.of(station);
        } else {
            stationDao.deleteByStationId(station.stationId());
            return Optional.empty();
        }
    }

    @Transactional
    @Override
    public Station getStation(ApiStationId id) throws NoSuchElementException {
        return getStations().stream()
                .filter(s -> s.stationId().equals(id))
                .findFirst().orElseThrow();
    }

    private boolean isValidStation(Station station, Optional<Set<ApiStationId>> rivermapStationsWithReadings) {
        return isValidSampleInDbForStation(station) || canFetchData(station, rivermapStationsWithReadings);
    }

    private boolean isValidSampleInDbForStation(Station station) {
        List<ApiMeasurementType> necessaryMeasurementTypes = List.of(ApiMeasurementType.FLOW, ApiMeasurementType.HEIGHT);
        for (ApiMeasurementType type : necessaryMeasurementTypes) {
            try {
                if (sampleDao.getCurrentSample(station.stationId(), type)
                        .getTimestamp()
                        .isAfter(OffsetDateTime.now().minusDays(1))) {
                    return true;
                }
            } catch (NoDataAvailableException e) {
                // try next type
            }
        }
        return false;
    }

    private boolean canFetchData(Station station, Optional<Set<ApiStationId>> rivermapStationsWithReadings) {
        ApiStationId stationId = station.stationId();
        return switch (station.provider()) {
            case HYDRODATEN -> canFetchDataForCh(stationId);
            case VIGICRUES -> canFetchDataForFr(stationId);
            case HVZ_BW -> canFetchDataForBw(stationId);
            case RIVERMAP -> rivermapStationsWithReadings
                    .map(withReadings -> withReadings.contains(stationId))
                    .orElse(true);
        };
    }

    private boolean canFetchDataForCh(ApiStationId stationId) {
        try {
            List<Sample> samples = swissSampleFetchService.fetchSamples(Set.of(stationId));
            // A CH station is fetchable if existenz returns at least one measurement. Stations that
            // expose BOTH flow and temperature return 2 samples, so "== 1" wrongly dropped them
            // (e.g. 2243 Zürich, 2473 St. Gallen). Match the DE_BW check: keep if any data exists.
            return !samples.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean canFetchDataForFr(ApiStationId stationId) {
        try {
            List<Sample> samples = frenchLast30DaysSampleFetchService.fetchLatestSamples(Set.of(stationId));
            return samples.size() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean canFetchDataForBw(ApiStationId stationId) {
        try {
            List<Sample> samples = bwSampleFetchService.fetchSamples(Set.of(stationId));
            return !samples.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}

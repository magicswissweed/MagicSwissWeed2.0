package com.aa.msw.api.station;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.database.repository.dao.StationDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.LastFewDays;
import com.aa.msw.model.Sample;
import com.aa.msw.model.Station;
import com.aa.msw.source.french.vigicrues.historical.lastThirty.FrenchLast30DaysSampleFetchService;
import com.aa.msw.source.french.vigicrues.stations.FrenchStationFetchService;
import com.aa.msw.source.swiss.existenz.sample.SwissSampleFetchService;
import com.aa.msw.source.swiss.hydrodaten.stations.SwissStationFetchService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Profile("!test")
@Service
public class StationApiServiceImpl implements StationApiService {
    // holds the stations in-memory for faster access - but also in db for fast startup (mostly for dev purposes)

    private final StationDao stationDao;
    private final SwissStationFetchService swissStationFetchService;
    private final FrenchStationFetchService frenchStationFetchService;
    private final SampleDao sampleDao;
    private final SwissSampleFetchService swissSampleFetchService;
    private final FrenchLast30DaysSampleFetchService frenchLast30DaysSampleFetchService;
    private Set<Station> stations = new HashSet<>();

    public StationApiServiceImpl(SwissStationFetchService swissStationFetchService, StationDao stationDao, FrenchStationFetchService frenchStationFetchService, SampleDao sampleDao, SwissSampleFetchService swissSampleFetchService, FrenchLast30DaysSampleFetchService frenchLast30DaysSampleFetchService) {
        this.swissStationFetchService = swissStationFetchService;
        this.stationDao = stationDao;
        this.frenchStationFetchService = frenchStationFetchService;
        this.sampleDao = sampleDao;
        this.swissSampleFetchService = swissSampleFetchService;
        this.frenchLast30DaysSampleFetchService = frenchLast30DaysSampleFetchService;
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
        return stations.stream()
                .map(this::processFetchedStations)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    /**
     * @param station - the station to process
     * @return Station
     * This method checks if the given station is a valid station (has data)
     * - if valid -> return station
     * - if invalid -> return empty and delete the station from db (if exists)
     */
    private Optional<Station> processFetchedStations(Station station) {
        if (isValidStation(station)) {
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

    private boolean isValidStation(Station station) {
        return isValidSampleInDbForStation(station) || canFetchData(station);
    }

    private boolean isValidSampleInDbForStation(Station station) {
        try {
            return sampleDao.
                    getCurrentSample(station.stationId())
                    .getTimestamp()
                    .isAfter(OffsetDateTime.now().minusDays(1));
        } catch (NoDataAvailableException e) {
            return false;
        }
    }

    private boolean canFetchData(Station station) {
        ApiStationId stationId = station.stationId();
        return switch (stationId.getCountry()) {
            case CH -> canFetchDataForCh(stationId);
            case FR -> canFetchDataForFr(stationId);
        };
    }

    private boolean canFetchDataForCh(ApiStationId stationId) {
        try {
            List<Sample> samples = swissSampleFetchService.fetchSamples(Set.of(stationId));
            return samples.size() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean canFetchDataForFr(ApiStationId stationId) {
        try {
            Set<LastFewDays> dataSet = frenchLast30DaysSampleFetchService.fetchLast30DaysSamples(Set.of(stationId));
            return dataSet.size() == 1;
        } catch (Exception e) {
            return false;
        }
    }
}

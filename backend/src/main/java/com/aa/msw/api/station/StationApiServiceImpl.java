package com.aa.msw.api.station;

import com.aa.msw.database.exceptions.NoSampleAvailableException;
import com.aa.msw.database.repository.dao.StationDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Sample;
import com.aa.msw.model.Station;
import com.aa.msw.source.InputDataFetcherService;
import com.aa.msw.source.french.vigicrues.stations.FrenchStationFetchService;
import com.aa.msw.source.swiss.hydrodaten.stations.SwissStationFetchService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Profile("!test")
@Service
public class StationApiServiceImpl implements StationApiService {
    // holds the stations in-memory for faster access - but also in db for fast startup (mostly for dev purposes)

    private final StationDao stationDao;
    private final SwissStationFetchService swissStationFetchService;
    private final InputDataFetcherService inputDataFetcherService;
    private final FrenchStationFetchService frenchStationFetchService;
    private Set<Station> stations = new HashSet<>();

    public StationApiServiceImpl(SwissStationFetchService swissStationFetchService, InputDataFetcherService inputDataFetcherService, StationDao stationDao, FrenchStationFetchService frenchStationFetchService) {
        this.swissStationFetchService = swissStationFetchService;
        this.inputDataFetcherService = inputDataFetcherService;
        this.stationDao = stationDao;
        this.frenchStationFetchService = frenchStationFetchService;
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
            stationDao.deleteAll();
            persistStationsToDb(fetchedStations);
            stations = fetchedStations;
        }
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
                .filter(this::isValidStation)
                .collect(Collectors.toSet());
    }

    @Transactional
    @Override
    public Station getStation(ApiStationId id) throws NoSuchElementException {
        return getStations().stream()
                .filter(s -> s.stationId().equals(id))
                .findFirst().orElseThrow();
    }

    private boolean isValidStation(Station station) {
        try {
            // TODO: implement also for france
            if (station.stationId().getCountry().equals(CountryEnum.FR)) {
                return true;
            }

            List<Sample> samples = inputDataFetcherService.fetchForStationId(station.stationId());
            return samples.size() == 1;
        } catch (NoSampleAvailableException e) {
            return false;
        }
    }
}

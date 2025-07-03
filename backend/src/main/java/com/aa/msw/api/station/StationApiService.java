package com.aa.msw.api.station;

import com.aa.msw.database.exceptions.NoSampleAvailableException;
import com.aa.msw.database.repository.dao.StationDao;
import com.aa.msw.model.Sample;
import com.aa.msw.model.Station;
import com.aa.msw.source.InputDataFetcherService;
import com.aa.msw.source.hydrodaten.stations.StationFetchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StationApiService {
    // holds the stations in-memory for faster access - but also in db for fast startup (mostly for dev purposes)

    private final StationDao stationDao;
    private final StationFetchService stationFetchService;
    private final InputDataFetcherService inputDataFetcherService;
    private Set<Station> stations = new HashSet<>();

    public StationApiService(StationFetchService stationFetchService, InputDataFetcherService inputDataFetcherService, StationDao stationDao) {
        this.stationFetchService = stationFetchService;
        this.inputDataFetcherService = inputDataFetcherService;
        this.stationDao = stationDao;
    }

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
        return stationFetchService
                .fetchStations()
                .stream()
                .filter(this::isValidStation)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Station getStation(Integer id) throws NoSuchElementException {
        return getStations().stream()
                .filter(s -> s.stationId().equals(id))
                .findFirst().orElseThrow();
    }

    private boolean isValidStation(Station station) {
        try {
            List<Sample> samples = inputDataFetcherService.fetchForStationId(station.stationId());
            return samples.size() == 1;
        } catch (NoSampleAvailableException e) {
            return false;
        }
    }
}

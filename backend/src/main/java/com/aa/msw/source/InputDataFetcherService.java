package com.aa.msw.source;

import com.aa.msw.database.exceptions.NoSampleAvailableException;
import com.aa.msw.database.repository.dao.ForecastDao;
import com.aa.msw.database.repository.dao.Last40DaysDao;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.database.repository.dao.StationDao;
import com.aa.msw.database.services.SpotDbService;
import com.aa.msw.model.Forecast;
import com.aa.msw.model.Last40Days;
import com.aa.msw.model.Sample;
import com.aa.msw.model.Station;
import com.aa.msw.source.existenz.sample.SampleFetchService;
import com.aa.msw.source.hydrodaten.forecast.ForecastFetchService;
import com.aa.msw.source.hydrodaten.historical.lastfourty.Last40DaysSampleFetchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InputDataFetcherService {
    private final SampleFetchService sampleFetchService;
    private final ForecastFetchService forecastFetchService;
    private final StationDao stationDao;
    private final SampleDao sampleDao;
    private final ForecastDao forecastDao;
    private final SpotDbService spotDbService;
    private final Last40DaysSampleFetchService last40DaysSampleFetchService;
    private final Last40DaysDao last40DaysDao;
    private Set<Integer> stationIds;

    public InputDataFetcherService(SampleFetchService sampleFetchService, ForecastFetchService forecastFetchService, StationDao stationDao, SampleDao sampleDao, ForecastDao forecastDao, SpotDbService spotDbService, Last40DaysSampleFetchService last40DaysSampleFetchService, Last40DaysDao last40DaysDao) {
        this.sampleFetchService = sampleFetchService;
        this.forecastFetchService = forecastFetchService;
        this.stationDao = stationDao;
        this.sampleDao = sampleDao;
        this.forecastDao = forecastDao;
        stationIds = getAllStationIds();
        this.spotDbService = spotDbService;
        this.last40DaysSampleFetchService = last40DaysSampleFetchService;
        this.last40DaysDao = last40DaysDao;
    }

    public void updateStationIds() {
        this.stationIds = getAllStationIds();
    }

    public List<Sample> fetchForStationId(Integer stationId) throws NoSampleAvailableException {
        List<Sample> samples;
        try {
            samples = sampleFetchService.fetchSamples(Set.of(stationId));
        } catch (IOException | URISyntaxException e) {
            throw new NoSampleAvailableException(e.getMessage());
        }
        return samples;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 minutes in milliseconds
    public void fetchDataAndWriteToDb() throws IOException, URISyntaxException {
        fetchAndWriteSamples();
        fetchAndWriteForecasts();
        fetchAndWriteLast40Days();
        spotDbService.updateCurrentInfoForAllSpotsOfStations(stationIds);
    }

    private void fetchAndWriteSamples() throws IOException, URISyntaxException {
        List<Sample> samples = sampleFetchService.fetchSamples(stationIds);
        sampleDao.persistSamplesIfNotExist(samples);
    }

    private void fetchAndWriteForecasts() throws URISyntaxException {
        List<Forecast> forecasts = forecastFetchService.fetchForecasts(stationIds);
        forecastDao.persistForecastsIfNotExist(forecasts);
    }

    public void fetchAndWriteLast40Days() throws URISyntaxException {
        Set<Last40Days> fetchedLast40DaysSamples = last40DaysSampleFetchService.fetchLast40DaysSamples(stationIds);
        if (!fetchedLast40DaysSamples.isEmpty()) {
            last40DaysDao.deleteAll();
            last40DaysDao.persistLast40DaysSamples(fetchedLast40DaysSamples);
        }
    }

    private Set<Integer> getAllStationIds() {
        return stationDao.getStations().stream()
                .map(Station::stationId)
                .collect(Collectors.toSet());
    }
}

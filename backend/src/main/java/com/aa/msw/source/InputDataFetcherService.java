package com.aa.msw.source;

import com.aa.msw.database.exceptions.NoSampleAvailableException;
import com.aa.msw.database.repository.dao.ForecastDao;
import com.aa.msw.database.repository.dao.Last40DaysDao;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.database.repository.dao.StationDao;
import com.aa.msw.database.services.SpotDbService;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Forecast;
import com.aa.msw.model.Last40Days;
import com.aa.msw.model.Sample;
import com.aa.msw.model.Station;
import com.aa.msw.notifications.NotificationService;
import com.aa.msw.notifications.NotificationSpotInfo;
import com.aa.msw.source.existenz.sample.SwissSampleFetchService;
import com.aa.msw.source.hydrodaten.forecast.SwissForecastFetchService;
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
    private final SwissSampleFetchService swissSampleFetchService;
    private final SwissForecastFetchService swissForecastFetchService;
    private final StationDao stationDao;
    private final SampleDao sampleDao;
    private final ForecastDao forecastDao;
    private final SpotDbService spotDbService;
    private final Last40DaysSampleFetchService last40DaysSampleFetchService;
    private final Last40DaysDao last40DaysDao;
    private final NotificationService notificationService;

    public InputDataFetcherService(SwissSampleFetchService swissSampleFetchService, SwissForecastFetchService swissForecastFetchService, StationDao stationDao, SampleDao sampleDao, ForecastDao forecastDao, SpotDbService spotDbService, Last40DaysSampleFetchService last40DaysSampleFetchService, Last40DaysDao last40DaysDao, NotificationService notificationService) {
        this.swissSampleFetchService = swissSampleFetchService;
        this.swissForecastFetchService = swissForecastFetchService;
        this.stationDao = stationDao;
        this.sampleDao = sampleDao;
        this.forecastDao = forecastDao;
        this.spotDbService = spotDbService;
        this.last40DaysSampleFetchService = last40DaysSampleFetchService;
        this.last40DaysDao = last40DaysDao;
        this.notificationService = notificationService;
    }

    public List<Sample> fetchForStationId(ApiStationId stationId) throws NoSampleAvailableException {
        List<Sample> samples;
        try {
            samples = swissSampleFetchService.fetchSamples(Set.of(stationId));
        } catch (IOException | URISyntaxException e) {
            throw new NoSampleAvailableException(e.getMessage());
        }
        return samples;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 minutes in milliseconds
    public void fetchDataAndWriteToDb() throws IOException, URISyntaxException {
        Set<ApiStationId> stationIds = getAllStationIds();
        fetchAndWriteSamples(stationIds);
        fetchAndWriteForecasts(stationIds);
        fetchAndWriteLast40Days(stationIds);
        Set<NotificationSpotInfo> spotsThatImproved = spotDbService.updateCurrentInfoForAllSpotsOfStations(stationIds);
        notificationService.sendNotificationsForSpots(spotsThatImproved);
    }

    private void fetchAndWriteSamples(Set<ApiStationId> stationIds) throws IOException, URISyntaxException {
        List<Sample> samples = swissSampleFetchService.fetchSamples(stationIds);
        sampleDao.persistSamplesIfNotExist(samples);
    }

    private void fetchAndWriteForecasts(Set<ApiStationId> stationIds) throws URISyntaxException {
        List<Forecast> forecasts = swissForecastFetchService.fetchForecasts(stationIds);
        forecastDao.persistForecastsIfNotExist(forecasts);
    }

    public void fetchAndWriteLast40Days(Set<ApiStationId> stationIds) throws URISyntaxException {
        Set<Last40Days> fetchedLast40DaysSamples = last40DaysSampleFetchService.fetchLast40DaysSamples(stationIds);
        if (!fetchedLast40DaysSamples.isEmpty()) {
            last40DaysDao.persistLast40DaysSamples(fetchedLast40DaysSamples);
        }
    }

    private Set<ApiStationId> getAllStationIds() {
        return stationDao.getStations().stream()
                .map(Station::stationId)
                .collect(Collectors.toSet());
    }
}

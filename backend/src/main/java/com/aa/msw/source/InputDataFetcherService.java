package com.aa.msw.source;

import com.aa.msw.database.repository.dao.ForecastDao;
import com.aa.msw.database.repository.dao.LastFewDaysDao;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.database.repository.dao.StationDao;
import com.aa.msw.database.services.SpotDbService;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Forecast;
import com.aa.msw.model.LastFewDays;
import com.aa.msw.model.Sample;
import com.aa.msw.model.Station;
import com.aa.msw.notifications.NotificationService;
import com.aa.msw.notifications.NotificationSpotInfo;
import com.aa.msw.source.french.vigicrues.historical.lastThirty.FrenchLast30DaysSampleFetchService;
import com.aa.msw.source.swiss.existenz.sample.SwissSampleFetchService;
import com.aa.msw.source.swiss.hydrodaten.forecast.SwissForecastFetchService;
import com.aa.msw.source.swiss.hydrodaten.historical.lastfourty.SwissLast40DaysSampleFetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class InputDataFetcherService {
    private static final Logger LOG = LoggerFactory.getLogger(InputDataFetcherService.class);

    private final SwissSampleFetchService swissSampleFetchService;
    private final SwissForecastFetchService swissForecastFetchService;
    private final SwissLast40DaysSampleFetchService swissLast40DaysSampleFetchService;
    private final StationDao stationDao;
    private final SampleDao sampleDao;
    private final ForecastDao forecastDao;
    private final SpotDbService spotDbService;
    private final LastFewDaysDao lastFewDaysDao;
    private final NotificationService notificationService;
    private final FrenchLast30DaysSampleFetchService frenchLast30DaysSampleFetchService;

    private boolean fetchedDataSinceRestart = false;

    private final AtomicBoolean isFetching = new AtomicBoolean(false);

    public InputDataFetcherService(SwissSampleFetchService swissSampleFetchService, SwissForecastFetchService swissForecastFetchService, StationDao stationDao, SampleDao sampleDao, ForecastDao forecastDao, SpotDbService spotDbService, SwissLast40DaysSampleFetchService swissLast40DaysSampleFetchService, LastFewDaysDao lastFewDaysDao, NotificationService notificationService, FrenchLast30DaysSampleFetchService frenchLast30DaysSampleFetchService) {
        this.swissSampleFetchService = swissSampleFetchService;
        this.swissForecastFetchService = swissForecastFetchService;
        this.stationDao = stationDao;
        this.sampleDao = sampleDao;
        this.forecastDao = forecastDao;
        this.spotDbService = spotDbService;
        this.swissLast40DaysSampleFetchService = swissLast40DaysSampleFetchService;
        this.lastFewDaysDao = lastFewDaysDao;
        this.notificationService = notificationService;
        this.frenchLast30DaysSampleFetchService = frenchLast30DaysSampleFetchService;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 minutes in milliseconds
    public void fetchDataAndWriteToDb() throws IOException, URISyntaxException {
        if (isFetching.compareAndSet(false, true)) {
            try {
//        We do this very explicitly. This way it's easy to see which data is fetched for which country.
//        If we fetch data from a lot of countries, we should refactor this in the future.
                Set<ApiStationId> stationIds = getAllStationIds();
                Set<ApiStationId> swissStationIds = stationIds.stream()
                        .filter(stationId -> stationId.getCountry().equals(CountryEnum.CH))
                        .collect(Collectors.toSet());
                Set<ApiStationId> frenchStationIds = stationIds.stream()
                        .filter(stationId -> stationId.getCountry().equals(CountryEnum.FR))
                        .collect(Collectors.toSet());
                fetchAndWriteFrenchLast30DaysAndSample(frenchStationIds);
                fetchAndWriteSwissSamples(swissStationIds);
                fetchAndWriteSwissForecasts(swissStationIds);
                fetchAndWriteSwissLast40Days(swissStationIds);
                Set<NotificationSpotInfo> spotsThatImproved = spotDbService.updateCurrentInfoForAllSpotsOfStations(stationIds);
                notificationService.sendNotificationsForSpots(spotsThatImproved);
            } finally {
                isFetching.set(false);
            }
            fetchedDataSinceRestart = true;
        } else {
            LOG.warn("Fetch already in progress, skipping this trigger.");
        }
    }

    public boolean hasFetchedDataSinceRestart() {
        return fetchedDataSinceRestart;
    }

    private void fetchAndWriteFrenchLast30DaysAndSample(Set<ApiStationId> stationIds) throws URISyntaxException {
//        France does not have a call for the latest sample, so we fetch the last 30 days and use the newest as our current sample
        Set<LastFewDays> lastFewDaysSet = frenchLast30DaysSampleFetchService.fetchLast30DaysSamples(stationIds);
        if (!lastFewDaysSet.isEmpty()) {
            lastFewDaysDao.persistLastFewDaysSamples(lastFewDaysSet);

            List<Sample> currentSamples = lastFewDaysSet.stream()
                    .map(LastFewDays::getLatestMeasurementAsSample)
                    .filter(Objects::nonNull)
                    .toList();

            sampleDao.persistSamplesIfNotExist(currentSamples);
        }
    }

    private void fetchAndWriteSwissSamples(Set<ApiStationId> stationIds) throws IOException, URISyntaxException {
        List<Sample> samples = swissSampleFetchService.fetchSamples(stationIds);
        sampleDao.persistSamplesIfNotExist(samples);
    }

    private void fetchAndWriteSwissForecasts(Set<ApiStationId> stationIds) throws URISyntaxException {
        List<Forecast> forecasts = swissForecastFetchService.fetchForecasts(stationIds);
        forecastDao.persistForecastsIfNotExist(forecasts);
    }

    public void fetchAndWriteSwissLast40Days(Set<ApiStationId> stationIds) throws URISyntaxException {
        Set<LastFewDays> fetchedLastFewDaysSamples = swissLast40DaysSampleFetchService.fetchLast40DaysSamples(stationIds);
        if (!fetchedLastFewDaysSamples.isEmpty()) {
            lastFewDaysDao.persistLastFewDaysSamples(fetchedLastFewDaysSamples);
        }
    }

    private Set<ApiStationId> getAllStationIds() {
        return stationDao.getStations().stream()
                .map(Station::stationId)
                .collect(Collectors.toSet());
    }
}

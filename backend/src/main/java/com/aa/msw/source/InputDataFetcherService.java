package com.aa.msw.source;

import com.aa.msw.database.exceptions.NoDataAvailableException;
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
import java.time.Duration;
import java.time.OffsetDateTime;
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

    private final AtomicBoolean isFetchingSwissData = new AtomicBoolean(false);
    private final AtomicBoolean isFetchingFrenchData = new AtomicBoolean(false);

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

    public void fetchDataAndWriteToDb() throws IOException, URISyntaxException {
        fetchSwissDataAndWriteToDb();
        fetchFrenchDataAndWriteToDb();
    }

    @Scheduled(cron = "0 1/10 * * * *") // 01, 11, 21, ...
    private void fetchSwissDataAndWriteToDb() {
        if (isFetchingSwissData.compareAndSet(false, true)) {
            LOG.info("Fetching swiss data...");
            try {
                Set<ApiStationId> stationIds = getAllStationIds();

                try {
                    Set<ApiStationId> filteredStationIds = stationIds.stream()
                            .filter(stationId -> stationId.getCountry().equals(CountryEnum.CH))
                            .collect(Collectors.toSet());
                    fetchAndWriteSwissData(filteredStationIds);
                } catch (Exception e) {
                    LOG.error("Error while fetching data for country {}. We will ignore this so that the other countries data can be fetched.", CountryEnum.CH, e);
                }

                Set<NotificationSpotInfo> spotsThatImproved = spotDbService.updateCurrentInfoForAllSpotsOfStations(stationIds);
                notificationService.sendNotificationsForSpots(spotsThatImproved);

                fetchedDataSinceRestart = true;

                LOG.info("Finished fetching swiss data.");
                try {
                    OffsetDateTime currentSampleTimestamp = sampleDao.getCurrentSample(new ApiStationId(CountryEnum.CH, "2018")).getTimestamp();
                    LOG.info("Current sample timestamp for 2018: {}", currentSampleTimestamp);
                } catch (NoDataAvailableException e) {
                    LOG.error("No sample found for stationId 2018");
                }
            } finally {
                isFetchingSwissData.set(false);
            }
        } else {
            LOG.warn("Fetch already in progress for switzerland, skipping this trigger.");
        }
    }

    // 03, 08, 13, 18, 23, 28, ... in theory... In reality fetching takes a long time, and therefore this runs only every 20 minutes or so
    @Scheduled(cron = "0 3/5 * * * *")
    private void fetchFrenchDataAndWriteToDb() {
        if (isFetchingFrenchData.compareAndSet(false, true)) {
            LOG.info("Fetching french data...");
            try {
                Set<ApiStationId> stationIds = getAllStationIds();

                Duration sampleInterval = Duration.ofMinutes(9);
                try {
                    Set<ApiStationId> filteredStationIds = stationIds.stream()
                            .filter(stationId -> stationId.getCountry().equals(CountryEnum.FR))
                            .filter(stationId -> isLastSampleOlderThan(stationId, sampleInterval))
                            .collect(Collectors.toSet());
                    fetchAndWriteFrenchLast30DaysAndSample(filteredStationIds);
                } catch (Exception e) {
                    LOG.error("Error while fetching data for country {}. We will ignore this so that the other countries data can be fetched.", CountryEnum.FR, e);
                }

                Set<NotificationSpotInfo> spotsThatImproved = spotDbService.updateCurrentInfoForAllSpotsOfStations(stationIds);
                notificationService.sendNotificationsForSpots(spotsThatImproved);

                fetchedDataSinceRestart = true;
                LOG.info("Finished fetching french data.");
            } finally {
                isFetchingFrenchData.set(false);
            }
        } else {
            LOG.warn("Fetch already in progress for france, skipping this trigger.");
        }
    }

    private boolean isLastSampleOlderThan(ApiStationId stationId, Duration sampleInterval) {
        try {
            return sampleDao.getCurrentSample(stationId).getTimestamp()
                    .isBefore(OffsetDateTime.now().minus(sampleInterval));
        } catch (NoDataAvailableException e) {
            return true;
        }
    }

    private void fetchAndWriteSwissData(Set<ApiStationId> swissStationIds) {
        fetchAndWriteSwissSamples(swissStationIds);
        fetchAndWriteSwissForecasts(swissStationIds);
        fetchAndWriteSwissLast40Days(swissStationIds);
    }

    public boolean hasFetchedDataSinceRestart() {
        return fetchedDataSinceRestart;
    }

    private void fetchAndWriteFrenchLast30DaysAndSample(Set<ApiStationId> stationIds) {
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

    private void fetchAndWriteSwissSamples(Set<ApiStationId> stationIds) {
        List<Sample> samples = swissSampleFetchService.fetchSamples(stationIds);
        sampleDao.persistSamplesIfNotExist(samples);
    }

    private void fetchAndWriteSwissForecasts(Set<ApiStationId> stationIds) {
        List<Forecast> forecasts = swissForecastFetchService.fetchForecasts(stationIds);
        forecastDao.persistForecastsIfNotExist(forecasts);
    }

    public void fetchAndWriteSwissLast40Days(Set<ApiStationId> stationIds) {
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

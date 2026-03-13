package com.aa.msw.source;

import com.aa.msw.database.repository.dao.*;
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
import com.aa.msw.source.german.bw.sample.BwSampleFetchService;
import com.aa.msw.source.swiss.existenz.sample.SwissSampleFetchService;
import com.aa.msw.source.swiss.hydrodaten.forecast.SwissForecastFetchService;
import com.aa.msw.source.swiss.hydrodaten.historical.lastfourty.SwissLast40DaysSampleFetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class InputDataFetcherService {
    private static final Logger LOG = LoggerFactory.getLogger(InputDataFetcherService.class);

    private final SwissSampleFetchService swissSampleFetchService;
    private final SwissForecastFetchService swissForecastFetchService;
    private final SwissLast40DaysSampleFetchService swissLast40DaysSampleFetchService;
    private final StationDao stationDao;
    private final SpotDao spotDao;
    private final SampleDao sampleDao;
    private final ForecastDao forecastDao;
    private final SpotDbService spotDbService;
    private final LastFewDaysDao lastFewDaysDao;
    private final NotificationService notificationService;
    private final FrenchLast30DaysSampleFetchService frenchLast30DaysSampleFetchService;
    private final BwSampleFetchService bwSampleFetchService;

    private boolean fetchedDataSinceRestart = false;

    private final AtomicBoolean isFetchingSwissData = new AtomicBoolean(false);
    private final AtomicBoolean isFetchingFrenchData = new AtomicBoolean(false);
    private final AtomicBoolean isFetchingBwData = new AtomicBoolean(false);

    public InputDataFetcherService(SwissSampleFetchService swissSampleFetchService, SwissForecastFetchService swissForecastFetchService, StationDao stationDao, SpotDao spotDao, SampleDao sampleDao, ForecastDao forecastDao, SpotDbService spotDbService, SwissLast40DaysSampleFetchService swissLast40DaysSampleFetchService, LastFewDaysDao lastFewDaysDao, NotificationService notificationService, FrenchLast30DaysSampleFetchService frenchLast30DaysSampleFetchService, BwSampleFetchService bwSampleFetchService) {
        this.swissSampleFetchService = swissSampleFetchService;
        this.swissForecastFetchService = swissForecastFetchService;
        this.stationDao = stationDao;
        this.spotDao = spotDao;
        this.sampleDao = sampleDao;
        this.forecastDao = forecastDao;
        this.spotDbService = spotDbService;
        this.swissLast40DaysSampleFetchService = swissLast40DaysSampleFetchService;
        this.lastFewDaysDao = lastFewDaysDao;
        this.notificationService = notificationService;
        this.frenchLast30DaysSampleFetchService = frenchLast30DaysSampleFetchService;
    }

    @Scheduled(cron = "0 1/10 * * * *")
        // 01, 11, 21, ...
    void fetchSwissDataAndWriteToDb() {
        // fetch all known CH stations.
        Set<ApiStationId> swissStationIds = filterByCountry(getAllStationIds(), CountryEnum.CH);
        fetchAndWriteToDb(swissStationIds, isFetchingSwissData, CountryEnum.CH, this::fetchAndWriteSwissData);
    }

    // 03, 08, 13, 18, 23, 28, ... in theory...
    @Scheduled(cron = "0 3/5 * * * *")
    void fetchFrenchDataAndWriteToDb() {
        // only fetch stations actually used by a spot, to avoid hammering the rate-limited Vigicrues API.
        Set<ApiStationId> frenchStationIds = spotDao.getReferencedStationIds(CountryEnum.FR);
        fetchAndWriteToDb(frenchStationIds, isFetchingFrenchData, CountryEnum.FR, this::fetchAndWriteFrenchLast30DaysAndSample);
    }

    @Scheduled(cron = "0 5/10 * * * *") // 05, 15, 25, ...
    private void fetchBwDataAndWriteToDb() {
        fetchAndWriteToDb(isFetchingBwData, CountryEnum.DE_BW, this::fetchAndWriteBwSamples);
    }

    private void fetchAndWriteToDb(Set<ApiStationId> stationIds, AtomicBoolean isFetchingForCountry, CountryEnum country, Consumer<Set<ApiStationId>> fetchForCountryFunction) {
        if (isFetchingForCountry.compareAndSet(false, true)) {
            LOG.info("Fetching {} data for {} stations...", country.name(), stationIds.size());
            try {
                try {
                    fetchForCountryFunction.accept(stationIds);
                } catch (Exception e) {
                    LOG.error("Error while fetching data for country {}. We will ignore this so that the other countries data can be fetched.", country, e);
                }

                updateCurrentInfoForAllSpotsOfStationsAndSendNotifications(stationIds);

                fetchedDataSinceRestart = true;

                LOG.info("Finished fetching {} data.", country.name());
            } finally {
                isFetchingForCountry.set(false);
            }
        } else {
            LOG.warn("Fetch already in progress for {}, skipping this trigger.", country.name());
        }
    }

    @Async
    public void triggerFrenchFetchForStationAsync(ApiStationId stationId) {
        if (stationId == null || stationId.getCountry() != CountryEnum.FR) {
            return;
        }
        LOG.info("Triggering immediate French fetch for station {}", stationId.getExternalId());
        try {
            Set<ApiStationId> ids = Set.of(stationId);
            fetchAndWriteFrenchLast30DaysAndSample(ids);
            updateCurrentInfoForAllSpotsOfStationsAndSendNotifications(ids);
        } catch (Exception e) {
            LOG.error("Error while triggering immediate French fetch for station {}", stationId.getExternalId(), e);
        }
    }

    private void updateCurrentInfoForAllSpotsOfStationsAndSendNotifications(Set<ApiStationId> stationIds) {
        Set<NotificationSpotInfo> spotsThatImproved = spotDbService.updateCurrentInfoForAllSpotsOfStations(stationIds);
        notificationService.sendNotificationsForSpots(spotsThatImproved);
    }

    private static Set<ApiStationId> filterByCountry(Set<ApiStationId> stationIds, CountryEnum country) {
        return stationIds.stream()
                .filter(stationId -> stationId.getCountry().equals(country))
                .collect(Collectors.toSet());
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

    private void fetchAndWriteBwSamples(Set<ApiStationId> stationIds) {
        List<Sample> samples = bwSampleFetchService.fetchSamples(stationIds);
        sampleDao.persistSamplesIfNotExist(samples);
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

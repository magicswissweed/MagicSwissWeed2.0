package com.aa.msw.database;

import com.aa.msw.database.repository.dao.ForecastDao;
import com.aa.msw.database.repository.dao.SampleDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CronJobDeleteOldEntries {
    public static final int DEFAULT_RETENTION_TIME_IN_DAYS = 30;
    private static final Logger LOG = LoggerFactory.getLogger(CronJobDeleteOldEntries.class);
    private final SampleDao sampleDao;
    private final ForecastDao forecastDao;

    public CronJobDeleteOldEntries(SampleDao sampleDao, ForecastDao forecastDao) {
        this.sampleDao = sampleDao;
        this.forecastDao = forecastDao;
    }

    @Scheduled(cron = "0 0 0 * * *") // every day at midnight
    public void deleteOldEntries() {
        LOG.info("Start deleting samples older than " + DEFAULT_RETENTION_TIME_IN_DAYS + " days.");
        sampleDao.deleteEventsOlderThanXDaysForEventType(DEFAULT_RETENTION_TIME_IN_DAYS);

        LOG.info("Start deleting forecasts older than " + DEFAULT_RETENTION_TIME_IN_DAYS + " days.");
        forecastDao.deleteEventsOlderThanXDaysForEventType(DEFAULT_RETENTION_TIME_IN_DAYS);

        LOG.info("Deleted samples and forecasts older than " + DEFAULT_RETENTION_TIME_IN_DAYS + " days.");
    }
}

package com.aa.msw.service;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Sample;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@Profile({"dev", "prd"})
public class CurrentSampleWatchdogService {
    private final SampleDao sampleDao;

    public CurrentSampleWatchdogService(SampleDao sampleDao) {
        this.sampleDao = sampleDao;
    }

    // This method shuts down the java process. Docker will automatically restart it on prod.
    // We chose 10 min because we fetch samples all 5 minutes, and we could get strange results on the first execution
    // of this method if the sample was not yet fetched. Another option would be to listen to the application startup
    // event and save the time and don't execute the method in the first 10 minutes or so...
    @Scheduled(fixedRate = 10 * 60 * 1000) // 10 minutes in milliseconds
    public void shutdownIfCurrentSampleIsOlderThan30Minutes() {
        ApiStationId anyStation = new ApiStationId(CountryEnum.CH, "2018");
        try {
            Sample currentSample = sampleDao.getCurrentSample(anyStation);
            if (currentSample.getTimestamp().isBefore(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(30))) {
                Runtime.getRuntime().halt(1);
            }
        } catch (NoDataAvailableException e) {
            Runtime.getRuntime().halt(1);
        }
    }
}

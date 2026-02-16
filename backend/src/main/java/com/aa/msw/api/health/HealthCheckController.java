package com.aa.msw.api.health;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Sample;
import com.aa.msw.source.InputDataFetcherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.OffsetDateTime;

import static java.time.ZoneOffset.UTC;

@RestController
public class HealthCheckController {

    private static final Duration MAX_SAMPLE_AGE = Duration.ofMinutes(35);

    private final SampleDao sampleDao;
    private final InputDataFetcherService inputDataFetcherService;

    private static final ApiStationId STATION = new ApiStationId(CountryEnum.CH, "2018");

    public HealthCheckController(SampleDao sampleDao, InputDataFetcherService inputDataFetcherService) {
        this.sampleDao = sampleDao;
        this.inputDataFetcherService = inputDataFetcherService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        OffsetDateTime now = OffsetDateTime.now(UTC);

        // Grace period after startup
        if (!inputDataFetcherService.hasFetchedDataSinceRestart()) {
            return ResponseEntity.ok("OK (waiting for first data fetch)");
        }

        // Sample freshness check
        try {
            Sample sample = sampleDao.getCurrentSample(STATION);

            if (sample.getTimestamp().isBefore(now.minus(MAX_SAMPLE_AGE))) {
                System.err.println("Healthcheck failed. Exiting.");
                System.err.println("Now: " + now);
                System.err.println("Sample timestamp: " + sample.getTimestamp());
                System.err.println("Difference minutes: " +
                        Duration.between(sample.getTimestamp(), now).toMinutes());
                System.exit(1);
                return ResponseEntity
                        .status(503)
                        .body("Unhealthy: sample older than 30 minutes");
            }
        } catch (NoDataAvailableException e) {
            return ResponseEntity
                    .status(503)
                    .body("Unhealthy: no sample available");
        }

        return ResponseEntity.ok("OK");
    }
}

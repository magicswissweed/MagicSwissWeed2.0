package com.aa.msw.api.health;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Sample;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.OffsetDateTime;

import static java.time.ZoneOffset.UTC;

@RestController
public class HealthCheckController {

    private static final Duration MAX_SAMPLE_AGE = Duration.ofMinutes(30);
    private static final Duration STARTUP_GRACE_PERIOD = Duration.ofMinutes(30);

    private final SampleDao sampleDao;
    private OffsetDateTime startedAt;

    private static final ApiStationId STATION = new ApiStationId(CountryEnum.CH, "2018");

    public HealthCheckController(SampleDao sampleDao) {
        this.sampleDao = sampleDao;
    }

    @PostConstruct
    public void init() {
        this.startedAt = OffsetDateTime.now(UTC);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        OffsetDateTime now = OffsetDateTime.now(UTC);

        // Grace period after startup
        if (now.isBefore(startedAt.plus(STARTUP_GRACE_PERIOD))) {
            return ResponseEntity.ok("OK (startup grace period)");
        }

        // Sample freshness check
        try {
            Sample sample = sampleDao.getCurrentSample(STATION);

            if (sample.getTimestamp().isBefore(now.minus(MAX_SAMPLE_AGE))) {
                System.err.println("Healthcheck failed. Exiting.");
                System.exit(1);
                return ResponseEntity
                        .status(503)
                        .body("Unhealthy: sample older than 30 minutes");
            }
        } catch (NoDataAvailableException e) {
            System.err.println("Healthcheck failed. Exiting.");
            System.exit(1);
            return ResponseEntity
                    .status(503)
                    .body("Unhealthy: no sample available");
        }

        return ResponseEntity.ok("OK");
    }
}

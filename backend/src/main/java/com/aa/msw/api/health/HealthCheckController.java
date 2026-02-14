package com.aa.msw.api.health;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Sample;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.OffsetDateTime;

import static java.time.ZoneOffset.UTC;

@RestController
public class HealthCheckController {

    private static final Duration MAX_SAMPLE_AGE = Duration.ofMinutes(30);

    private final SampleDao sampleDao;

    private static final ApiStationId STATION = new ApiStationId(CountryEnum.CH, "2018");

    public HealthCheckController(SampleDao sampleDao) {
        this.sampleDao = sampleDao;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {

        try {
            Sample sample = sampleDao.getCurrentSample(STATION);

            if (sample.getTimestamp().isBefore(OffsetDateTime.now(UTC).minus(MAX_SAMPLE_AGE))) {
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

package com.aa.msw.api.current;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.SampleApi;
import com.aa.msw.gen.api.StationToLastFewDays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
public class SampleApiController implements SampleApi {
    private static final Logger LOG = LoggerFactory.getLogger(SampleApiController.class);

    private final SampleApiService sampleApiService;

    public SampleApiController(final SampleApiService sampleApiService) {
        this.sampleApiService = sampleApiService;
    }

    @Override
    public ResponseEntity<List<StationToLastFewDays>> getLastFewDaysSamples(List<ApiStationId> stationIds) {
        return ResponseEntity.ok(
                stationIds.stream()
                        .map(stationId -> {
                            try {
                                return new StationToLastFewDays(
                                        stationId,
                                        sampleApiService.getLastFewDaysSamples(stationId)
                                );
                            } catch (Exception e) {
                                LOG.error("Error getting last few days samples for station {}", stationId, e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList()
        );
    }
}

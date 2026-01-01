package com.aa.msw.api.current;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.SampleApi;
import com.aa.msw.gen.api.StationToLast40Days;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
public class SampleApiController implements SampleApi {

    private final SampleApiService sampleApiService;

    public SampleApiController(final SampleApiService sampleApiService) {
        this.sampleApiService = sampleApiService;
    }

    @Override
    public ResponseEntity<List<StationToLast40Days>> getLast40DaysSamples(List<ApiStationId> stationIds) {
        return ResponseEntity.ok(
                stationIds.stream()
                        .map(stationId -> {
                            try {
                                return new StationToLast40Days(
                                        stationId,
                                        sampleApiService.getLast40DaysSamples(stationId)
                                );
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList()
        );
    }
}

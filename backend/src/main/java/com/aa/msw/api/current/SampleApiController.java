package com.aa.msw.api.current;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.gen.api.ApiSample;
import com.aa.msw.gen.api.SampleApi;
import com.aa.msw.gen.api.StationToLast40Days;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiSample> getCurrentSample(Integer stationId) {
        try {
            ApiSample sample = sampleApiService.getCurrentSample(stationId);
            return ResponseEntity.ok(sample);
        } catch (NoDataAvailableException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<List<StationToLast40Days>> getLast40DaysSamples(List<Integer> stationIds) {
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

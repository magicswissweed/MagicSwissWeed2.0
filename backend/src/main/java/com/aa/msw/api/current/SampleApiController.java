package com.aa.msw.api.current;

import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.gen.api.ApiSample;
import com.aa.msw.gen.api.SampleApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class SampleApiController implements SampleApi {

    private final SampleApiService sampleApiService;

    public SampleApiController(final SampleApiService sampleApiService) {
        this.sampleApiService = sampleApiService;
    }

    @Override
    public ResponseEntity<List<ApiSample>> getLastFewDaysSamples(UUID spotId) {
        return ResponseEntity.ok(sampleApiService.getLastFewDaysSamples(new SpotId(spotId)));
    }
}

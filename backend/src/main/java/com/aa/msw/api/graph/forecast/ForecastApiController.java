package com.aa.msw.api.graph.forecast;

import com.aa.msw.gen.api.ForecastApi;
import com.aa.msw.gen.api.StationToApiForecasts;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ForecastApiController implements ForecastApi {
    private final ForecastApiService forecastApiService;

    public ForecastApiController(ForecastApiService forecastApiService) {
        this.forecastApiService = forecastApiService;
    }

    @Override
    public ResponseEntity<List<StationToApiForecasts>> getForecasts() {
        try {
            return ResponseEntity.ok(forecastApiService.getAllForecasts());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

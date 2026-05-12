package com.aa.msw.api.graph.forecast;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.gen.api.ApiForecast;
import com.aa.msw.gen.api.ForecastApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ForecastApiController implements ForecastApi {
    private static final Logger LOG = LoggerFactory.getLogger(ForecastApiController.class);

    private final ForecastApiService forecastApiService;

    public ForecastApiController(ForecastApiService forecastApiService) {
        this.forecastApiService = forecastApiService;
    }

    @Override
    public ResponseEntity<ApiForecast> getForecast(UUID spotId) {
        try {
            return ResponseEntity.ok(forecastApiService.getCurrentForecast(new SpotId(spotId)));
        } catch (NoDataAvailableException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            LOG.error("Error getting forecast for spot {}", spotId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

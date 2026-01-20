package com.aa.msw.api.graph.historical;

import com.aa.msw.gen.api.HistoricalApi;
import com.aa.msw.gen.api.StationToApiHistoricalYears;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HistoricalApiController implements HistoricalApi {
    private static final Logger LOG = LoggerFactory.getLogger(HistoricalApiController.class);

    private final HistoricalYearsApiService historicalYearsApiService;

    public HistoricalApiController(HistoricalYearsApiService historicalYearsApiService) {
        this.historicalYearsApiService = historicalYearsApiService;
    }

    @Override
    public ResponseEntity<List<StationToApiHistoricalYears>> getHistoricalData() {
        try {
            List<StationToApiHistoricalYears> historicalYears = historicalYearsApiService.getAllApiHistoricalYearsData();
            return ResponseEntity.ok(historicalYears);
        } catch (Exception e) {
            LOG.error("Error getting historical data", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

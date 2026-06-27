package com.aa.msw.api.admin;

import com.aa.msw.api.graph.historical.HistoricalYearsAccessorService;
import com.aa.msw.api.station.StationApiService;
import com.aa.msw.source.InputDataFetcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    private final String adminToken;
    private final HistoricalYearsAccessorService historicalYearsAccessorService;
    private final StationApiService stationApiService;
    private final InputDataFetcherService inputDataFetcherService;

    public AdminController(
            @Value("${admin.token:}") String adminToken,
            HistoricalYearsAccessorService historicalYearsAccessorService,
            StationApiService stationApiService,
            InputDataFetcherService inputDataFetcherService) {
        this.adminToken = adminToken;
        this.historicalYearsAccessorService = historicalYearsAccessorService;
        this.stationApiService = stationApiService;
        this.inputDataFetcherService = inputDataFetcherService;
    }

    @PostMapping("/refresh/historical")
    public ResponseEntity<String> refreshHistorical(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthorized(authorization)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        LOG.info("Admin: triggering historical data refresh");
        historicalYearsAccessorService.fetchHistoricalYearsDataAndSaveToDb();
        return ResponseEntity.ok("Historical data refresh complete");
    }

    @PostMapping("/refresh/stations")
    public ResponseEntity<String> refreshStations(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthorized(authorization)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        LOG.info("Admin: triggering station refresh");
        stationApiService.fetchStationsAndSaveToDb();
        return ResponseEntity.ok("Station refresh complete");
    }

    @PostMapping("/refresh/samples")
    public ResponseEntity<String> refreshSamples(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthorized(authorization)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        LOG.info("Admin: triggering sample refresh");
        inputDataFetcherService.triggerAllFetches();
        return ResponseEntity.ok("Sample refresh complete");
    }

    private boolean isAuthorized(String authorization) {
        if (adminToken == null || adminToken.isBlank()) {
            LOG.warn("Admin endpoint called but ADMIN_TOKEN is not configured — rejecting");
            return false;
        }
        return ("Bearer " + adminToken).equals(authorization);
    }
}

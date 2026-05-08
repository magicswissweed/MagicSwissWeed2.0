package com.aa.msw.api.station;

import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStation;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.StationApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class StationApiController implements StationApi {

    private final StationApiService stationApiService;
    private final SampleDao sampleDao;

    public StationApiController(final StationApiService stationApiService, final SampleDao sampleDao) {
        this.stationApiService = stationApiService;
        this.sampleDao = sampleDao;
    }

    @Override
    public ResponseEntity<List<ApiStation>> getStations() {
        Map<ApiStationId, Set<ApiMeasurementType>> supported = sampleDao.getSupportedMeasurementsByStation();
        return ResponseEntity.ok(
                stationApiService.getStations().stream()
                        .map(s -> new ApiStation(
                                s.stationId(),
                                s.label(),
                                s.latitude(),
                                s.longitude(),
                                new ArrayList<>(supported.getOrDefault(s.stationId(), Set.of()))))
                        .toList()
        );
    }
}

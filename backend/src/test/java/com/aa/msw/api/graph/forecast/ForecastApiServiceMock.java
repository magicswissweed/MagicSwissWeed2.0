package com.aa.msw.api.graph.forecast;

import com.aa.msw.gen.api.ApiForecast;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.StationToApiForecasts;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Profile("test")
@Service
public class ForecastApiServiceMock implements ForecastApiService {

    @Override
    public ApiForecast getCurrentForecast(ApiStationId stationId) {
        return new ApiForecast();
    }

    @Override
    public List<StationToApiForecasts> getAllForecasts() {
        return List.of();
    }
}

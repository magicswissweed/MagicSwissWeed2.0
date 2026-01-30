package com.aa.msw.api.graph.forecast;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.gen.api.ApiForecast;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.StationToApiForecasts;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ForecastApiService {
    ApiForecast getCurrentForecast(ApiStationId stationId) throws NoDataAvailableException;

    List<StationToApiForecasts> getAllForecasts();
}

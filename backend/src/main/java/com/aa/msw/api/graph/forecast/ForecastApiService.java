package com.aa.msw.api.graph.forecast;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.gen.api.ApiForecast;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import org.springframework.stereotype.Service;

@Service
public interface ForecastApiService {
    ApiForecast getCurrentForecast(ApiStationId stationId, ApiMeasurementType measurementType) throws NoDataAvailableException;
}

package com.aa.msw.api.graph.forecast;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.gen.api.ApiForecast;
import org.springframework.stereotype.Service;

@Service
public interface ForecastApiService {
    ApiForecast getCurrentForecast(SpotId spotId) throws NoDataAvailableException;
}

package com.aa.msw.database.repository.dao;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.ForecastId;
import com.aa.msw.model.Forecast;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ForecastDao extends TimestampedDao, Dao<ForecastId, Forecast> {
    Forecast getCurrentForecast(int stationId) throws NoDataAvailableException;

    @Transactional
    void persistForecastsIfNotExist(List<Forecast> forecasts);
}

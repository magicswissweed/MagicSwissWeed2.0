package com.aa.msw.api.graph.forecast;

import com.aa.msw.api.graph.AbstractGraphLineApiService;
import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.database.repository.dao.ForecastDao;
import com.aa.msw.database.repository.dao.SpotDao;
import com.aa.msw.gen.api.ApiForecast;
import com.aa.msw.model.Forecast;
import com.aa.msw.model.Spot;
import org.springframework.stereotype.Service;

@Service
public class ForecastApiServiceImpl extends AbstractGraphLineApiService implements ForecastApiService {

    private final ForecastDao forecastDao;
    private final SpotDao spotDao;

    public ForecastApiServiceImpl(ForecastDao forecastDao, SpotDao spotDao) {
        this.forecastDao = forecastDao;
        this.spotDao = spotDao;
    }

    private static ApiForecast mapForecast(Forecast forecast) {
        ApiForecast apiForecast = new ApiForecast();
        apiForecast.setTimestamp(forecast.getTimestamp());
        apiForecast.setMeasuredData(mapApiLine(forecast.getMeasuredData()));
        apiForecast.setMedian(mapApiLine(forecast.getMedian()));
        apiForecast.setTwentyFivePercentile(mapApiLine(forecast.getTwentyFivePercentile()));
        apiForecast.setSeventyFivePercentile(mapApiLine(forecast.getSeventyFivePercentile()));
        apiForecast.setMin(mapApiLine(forecast.getMin()));
        apiForecast.setMax(mapApiLine(forecast.getMax()));
        return apiForecast;
    }

    @Override
    public ApiForecast getCurrentForecast(SpotId spotId) throws NoDataAvailableException {
        Spot spot = spotDao.get(spotId);
        return mapForecast(forecastDao.getCurrentForecast(spot.stationId(), spot.measurementType()));
    }
}

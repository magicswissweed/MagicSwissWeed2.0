package com.aa.msw.api.graph.forecast;

import com.aa.msw.api.graph.AbstractGraphLineApiService;
import com.aa.msw.api.spots.SpotsApiService;
import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.repository.dao.ForecastDao;
import com.aa.msw.gen.api.ApiForecast;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.StationToApiForecasts;
import com.aa.msw.model.Forecast;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Profile("!test")
@Service
public class ForecastApiServiceImpl extends AbstractGraphLineApiService implements ForecastApiService {

    private final ForecastDao forecastDao;
    private final SpotsApiService spotsApiService;

    public ForecastApiServiceImpl(ForecastDao forecastDao, SpotsApiService spotsApiService) {
        this.forecastDao = forecastDao;
        this.spotsApiService = spotsApiService;
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

    public ApiForecast getCurrentForecast(ApiStationId stationId) throws NoDataAvailableException {
        return mapForecast(forecastDao.getCurrentForecast(stationId));
    }

    @Override
    public List<StationToApiForecasts> getAllForecasts() {
        List<StationToApiForecasts> apiForecastsList = new ArrayList<>();
        for (ApiStationId station : spotsApiService.getStations()) {

            try {
                apiForecastsList.add(new StationToApiForecasts(station, getCurrentForecast(station)));
            } catch (NoDataAvailableException e) {
                // No forecast available -> don't add it to list
            }
        }
        return apiForecastsList;
    }
}

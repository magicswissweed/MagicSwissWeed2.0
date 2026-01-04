package com.aa.msw.source.swiss.hydrodaten.forecast;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Forecast;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

public interface SwissForecastFetchService {
    List<Forecast> fetchForecasts(Set<ApiStationId> stationIds) throws URISyntaxException;

    Forecast fetchForecast(ApiStationId stationId) throws IOException, URISyntaxException;
}

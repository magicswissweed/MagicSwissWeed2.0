package com.aa.msw.source.swiss.hydrodaten.historical.years;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.HistoricalYearsData;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

public interface SwissHistoricalYearsDataFetchService {

    HistoricalYearsData fetchHistoricalYearsData(ApiStationId stationId) throws IOException, URISyntaxException;

    Set<HistoricalYearsData> fetchHistoricalYearsData(Set<ApiStationId> stationIds) throws URISyntaxException;
}

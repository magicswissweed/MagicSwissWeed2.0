package com.aa.msw.api.graph.lastFewDays;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.LastFewDays;

public interface LastFewDaysApiService {
    LastFewDays getLastFewDays(ApiStationId stationId) throws NoDataAvailableException;
}

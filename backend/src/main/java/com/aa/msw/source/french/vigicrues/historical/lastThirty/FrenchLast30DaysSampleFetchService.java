package com.aa.msw.source.french.vigicrues.historical.lastThirty;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.LastFewDays;

import java.util.Set;

public interface FrenchLast30DaysSampleFetchService {
    Set<LastFewDays> fetchLast30DaysSamples(Set<ApiStationId> stationIds);
}

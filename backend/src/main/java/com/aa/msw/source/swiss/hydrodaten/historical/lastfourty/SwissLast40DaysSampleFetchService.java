package com.aa.msw.source.swiss.hydrodaten.historical.lastfourty;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.LastFewDays;

import java.net.URISyntaxException;
import java.util.Set;

public interface SwissLast40DaysSampleFetchService {
    Set<LastFewDays> fetchLast40DaysSamples(Set<ApiStationId> stationIds) throws URISyntaxException;
}

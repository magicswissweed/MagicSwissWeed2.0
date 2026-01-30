package com.aa.msw.source.swiss.hydrodaten.historical.lastfourty;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.LastFewDays;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Set;

@Profile("test")
@Service
class SwissLast40DaysSampleFetchServiceMock implements SwissLast40DaysSampleFetchService {

    @Override
    public Set<LastFewDays> fetchLast40DaysSamples(Set<ApiStationId> stationIds) throws URISyntaxException {
        return Set.of();
    }
}

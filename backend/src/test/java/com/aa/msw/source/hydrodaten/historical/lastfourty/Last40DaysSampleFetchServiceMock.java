package com.aa.msw.source.hydrodaten.historical.lastfourty;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Last40Days;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Set;

@Profile("test")
@Service
class Last40DaysSampleFetchServiceMock implements Last40DaysSampleFetchService {

    @Override
    public Set<Last40Days> fetchLast40DaysSamples(Set<ApiStationId> stationIds) throws URISyntaxException {
        return Set.of();
    }
}

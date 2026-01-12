package com.aa.msw.api.graph.last40days;

import com.aa.msw.api.graph.lastFewDays.LastFewDaysApiService;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.LastFewDays;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("test")
@Service
class LastFewDaysApiServiceMock implements LastFewDaysApiService {

    @Override
    public LastFewDays getLastFewDays(ApiStationId stationId) {
        return null;
    }
}

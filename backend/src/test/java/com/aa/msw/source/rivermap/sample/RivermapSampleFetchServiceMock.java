package com.aa.msw.source.rivermap.sample;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Sample;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Profile("test")
@Service
public class RivermapSampleFetchServiceMock implements RivermapSampleFetchService {
    @Override
    public List<Sample> fetchSamples(Set<ApiStationId> stationIds, int lastMinutes) {
        return List.of();
    }
}

package com.aa.msw.source.german.bw.sample;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Sample;

import java.util.List;
import java.util.Set;

public interface BwSampleFetchService {
    List<Sample> fetchSamples(Set<ApiStationId> stationIds);
}

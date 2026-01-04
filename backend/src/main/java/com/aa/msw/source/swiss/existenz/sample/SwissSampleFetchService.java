package com.aa.msw.source.swiss.existenz.sample;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Sample;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

public interface SwissSampleFetchService {
    List<Sample> fetchSamples(Set<ApiStationId> stationIds) throws IOException, URISyntaxException;
}

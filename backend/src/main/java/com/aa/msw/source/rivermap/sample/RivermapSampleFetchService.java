package com.aa.msw.source.rivermap.sample;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Sample;

import java.util.List;
import java.util.Set;

public interface RivermapSampleFetchService {

    /**
     * Window used by the regular poll: covers a couple of missed runs, already known samples are skipped on persist.
     */
    int POLL_WINDOW_MINUTES = 30;

    /**
     * Longest window the Rivermap API allows for a single request.
     */
    int MAX_WINDOW_MINUTES = 360;

    /**
     * Fetches the readings of the last {@code lastMinutes} minutes of <b>all</b> Rivermap stations with one request
     * and returns the samples of the given stations only (readings of stations we do not know are dropped).
     *
     * @param stationIds  our Rivermap stations (externalId = Rivermap station UUID)
     * @param lastMinutes size of the window ending now, at most {@link #MAX_WINDOW_MINUTES}
     */
    List<Sample> fetchSamples(Set<ApiStationId> stationIds, int lastMinutes);
}

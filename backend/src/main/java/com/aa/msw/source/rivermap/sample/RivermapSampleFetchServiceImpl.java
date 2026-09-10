package com.aa.msw.source.rivermap.sample;

import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Sample;
import com.aa.msw.source.AbstractFetchService;
import com.aa.msw.source.rivermap.RivermapApi;
import com.aa.msw.source.rivermap.RivermapConfigProperties;
import com.aa.msw.source.rivermap.model.RivermapReading;
import com.aa.msw.source.rivermap.model.RivermapReadingsResponse;
import com.aa.msw.source.rivermap.model.RivermapStationReadings;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rate limit of the readings endpoint: 2 requests, refilled by 1 per 2 minutes - fine for a poll every 10 minutes.
 */
@Profile("!test")
@Service
public class RivermapSampleFetchServiceImpl extends AbstractFetchService implements RivermapSampleFetchService {
    private static final Logger LOG = LoggerFactory.getLogger(RivermapSampleFetchServiceImpl.class);

    public static final String READINGS_FETCH_URL = RivermapApi.BASE_URL + "/stations/readings";

    private final RivermapConfigProperties config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RivermapSampleFetchServiceImpl(RivermapConfigProperties config) {
        this.config = config;
    }

    @Override
    public List<Sample> fetchSamples(Set<ApiStationId> stationIds, int lastMinutes) {
        if (stationIds.isEmpty()) {
            return List.of();
        }
        if (!config.hasApiKey()) {
            LOG.warn("No rivermap.api-key configured - skipping the Rivermap readings.");
            return List.of();
        }
        try {
            RivermapReadingsResponse response = objectMapper.readValue(fetchReadings(lastMinutes), RivermapReadingsResponse.class);
            Map<String, ApiStationId> stationIdsByRivermapId = stationIds.stream()
                    .collect(Collectors.toMap(ApiStationId::getExternalId, id -> id, (a, b) -> a));

            List<Sample> samples = new ArrayList<>();
            for (Map.Entry<String, RivermapStationReadings> entry : response.readings().entrySet()) {
                ApiStationId stationId = stationIdsByRivermapId.get(entry.getKey());
                RivermapStationReadings readings = entry.getValue();
                if (stationId == null || readings == null) {
                    continue; // a station we do not have (filtered out or unknown)
                }
                addSamples(samples, stationId, readings.m3s(), ApiMeasurementType.FLOW);
                addSamples(samples, stationId, readings.cm(), ApiMeasurementType.HEIGHT);
            }
            return samples;
        } catch (Exception e) {
            LOG.error("Error fetching Rivermap readings: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * "from" is relative to now, "to" relative to "from" (both in minutes) -> the window [now - minutes, now].
     */
    protected String fetchReadings(int lastMinutes) throws Exception {
        int minutes = Math.min(lastMinutes, MAX_WINDOW_MINUTES);
        String url = READINGS_FETCH_URL + "?from=" + minutes + "&to=" + minutes;
        return fetchAsString(url, Map.of(RivermapApi.API_KEY_HEADER, config.getApiKey()));
    }

    private static void addSamples(List<Sample> samples, ApiStationId stationId, List<RivermapReading> readings, ApiMeasurementType type) {
        if (readings == null) {
            return;
        }
        for (RivermapReading reading : readings) {
            if (reading == null || reading.ts() == null || reading.v() == null) {
                continue;
            }
            samples.add(new Sample(
                    new SampleId(),
                    stationId,
                    Instant.ofEpochSecond(reading.ts()).atOffset(ZoneOffset.UTC),
                    reading.v(),
                    type));
        }
    }
}

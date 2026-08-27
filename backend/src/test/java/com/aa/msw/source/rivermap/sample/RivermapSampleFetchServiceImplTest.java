package com.aa.msw.source.rivermap.sample;

import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.helper.TestResourceLoader;
import com.aa.msw.model.Sample;
import com.aa.msw.source.rivermap.RivermapConfigProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RivermapSampleFetchServiceImplTest {

    private static final ApiStationId KNOWN_STATION = new ApiStationId("AT", "0068a83f-8a46-3987-adb4-ad05b65ca510");
    private static final ApiStationId UNKNOWN_STATION = new ApiStationId("IT", "not-in-the-response");

    private final RivermapSampleFetchServiceImpl service = new RivermapSampleFetchServiceImpl(config("some-api-key")) {
        @Override
        protected String fetchReadings(int lastMinutes) {
            return TestResourceLoader.load("/testdata/rivermap_readings.json");
        }
    };

    @Test
    void shouldMapReadingsOfKnownStationsToSamples() {
        List<Sample> samples = service.fetchSamples(Set.of(KNOWN_STATION, UNKNOWN_STATION), 30);

        // the second station of the response is not ours -> dropped; the known one has 3 flow + 3 level readings
        assertEquals(6, samples.size());
        assertTrue(samples.stream().allMatch(s -> s.getStationId().equals(KNOWN_STATION)));
        assertEquals(3, samples.stream().filter(s -> s.getMeasurementType() == ApiMeasurementType.FLOW).count());
        assertEquals(3, samples.stream().filter(s -> s.getMeasurementType() == ApiMeasurementType.HEIGHT).count());

        Sample flow = samples.stream()
                .filter(s -> s.getMeasurementType() == ApiMeasurementType.FLOW && s.getValue() == 3.47)
                .findFirst().orElseThrow();
        assertEquals(Instant.ofEpochSecond(1787798100).atOffset(ZoneOffset.UTC), flow.getTimestamp());
        Sample level = samples.stream()
                .filter(s -> s.getMeasurementType() == ApiMeasurementType.HEIGHT && s.getValue() == 29.0)
                .findFirst().orElseThrow();
        assertEquals(Instant.ofEpochSecond(1787798100).atOffset(ZoneOffset.UTC), level.getTimestamp());
    }

    @Test
    void shouldNotCallTheApiWithoutStationsOrApiKey() {
        RivermapSampleFetchServiceImpl failingIfCalled = new RivermapSampleFetchServiceImpl(config(null)) {
            @Override
            protected String fetchReadings(int lastMinutes) {
                throw new AssertionError("the api must not be called");
            }
        };

        assertTrue(failingIfCalled.fetchSamples(Set.of(KNOWN_STATION), 30).isEmpty());
        assertTrue(service.fetchSamples(Set.of(), 30).isEmpty());
    }

    private static RivermapConfigProperties config(String apiKey) {
        RivermapConfigProperties config = new RivermapConfigProperties();
        config.setApiKey(apiKey);
        return config;
    }
}

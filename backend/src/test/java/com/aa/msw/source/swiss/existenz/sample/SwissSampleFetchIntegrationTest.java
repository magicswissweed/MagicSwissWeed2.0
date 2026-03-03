package com.aa.msw.source.swiss.existenz.sample;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.helper.TestResourceLoader;
import com.aa.msw.model.Sample;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SwissSampleFetchIntegrationTest {

    private final SwissSampleFetchServiceImpl service = new SwissSampleFetchServiceImpl() {
        @Override
        protected String fetchAsString(String url) {
            return TestResourceLoader.load("/testdata/existenz_samples.json");
        }
    };

    @Test
    void shouldFetchAndParseSamples() {
        Set<ApiStationId> stationIds = Set.of(
                new ApiStationId(CountryEnum.CH, "2018"),
                new ApiStationId(CountryEnum.CH, "2243")
        );

        List<Sample> samples = service.fetchSamples(stationIds);

        assertEquals(2, samples.size());

        Sample station2018 = samples.stream()
                .filter(s -> s.stationId().getExternalId().equals("2018"))
                .findFirst().orElseThrow();
        assertEquals(81, station2018.flow());
        assertTrue(station2018.temperature().isPresent());
        assertEquals(8.62, station2018.temperature().get(), 0.01);
        assertEquals(CountryEnum.CH, station2018.stationId().getCountry());
        assertNotNull(station2018.timestamp());

        Sample station2243 = samples.stream()
                .filter(s -> s.stationId().getExternalId().equals("2243"))
                .findFirst().orElseThrow();
        assertEquals(62, station2243.flow());
        assertTrue(station2243.temperature().isPresent());
        assertEquals(7.46, station2243.temperature().get(), 0.01);
    }

    @Test
    void shouldReturnEmptyListForStationNotInResponse() {
        Set<ApiStationId> stationIds = Set.of(
                new ApiStationId(CountryEnum.CH, "9999999")
        );

        List<Sample> samples = service.fetchSamples(stationIds);

        assertEquals(0, samples.size());
    }
}

package com.aa.msw.source.swiss.existenz.sample;

import com.aa.msw.gen.api.ApiMeasurementType;
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

        assertEquals(4, samples.size());

        Sample flow2018 = findSample(samples, "2018", ApiMeasurementType.FLOW);
        assertEquals(81.77, flow2018.value(), 0.01);
        assertEquals(CountryEnum.CH, flow2018.stationId().getCountry());
        assertNotNull(flow2018.timestamp());

        Sample temp2018 = findSample(samples, "2018", ApiMeasurementType.TEMPERATURE);
        assertEquals(8.62, temp2018.value(), 0.01);

        Sample flow2243 = findSample(samples, "2243", ApiMeasurementType.FLOW);
        assertEquals(62.25, flow2243.value(), 0.01);

        Sample temp2243 = findSample(samples, "2243", ApiMeasurementType.TEMPERATURE);
        assertEquals(7.46, temp2243.value(), 0.01);
    }

    private static Sample findSample(List<Sample> samples, String externalId, ApiMeasurementType type) {
        return samples.stream()
                .filter(s -> s.stationId().getExternalId().equals(externalId) && s.getMeasurementType() == type)
                .findFirst()
                .orElseThrow();
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

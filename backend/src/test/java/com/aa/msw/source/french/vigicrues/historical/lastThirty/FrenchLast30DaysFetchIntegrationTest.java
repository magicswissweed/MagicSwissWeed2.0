package com.aa.msw.source.french.vigicrues.historical.lastThirty;

import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.helper.TestResourceLoader;
import com.aa.msw.model.Sample;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrenchLast30DaysFetchIntegrationTest {

    private final FrenchLast30DaysSampleFetchServiceImpl service = new FrenchLast30DaysSampleFetchServiceImpl() {
        @Override
        protected String fetchAsString(String url) {
            return TestResourceLoader.load("/testdata/vigicrues_observations.json");
        }
    };

    @Test
    void shouldFetchAndParseLatestSample() {
        ApiStationId stationId = new ApiStationId(CountryEnum.FR, "V271201001");

        List<Sample> result = service.fetchLatestSamples(Set.of(stationId));

        assertEquals(1, result.size());

        Sample sample = result.get(0);
        assertEquals(stationId, sample.getStationId());
        assertEquals(ApiMeasurementType.FLOW, sample.getMeasurementType());
        assertTrue(sample.value() > 0);
    }
}

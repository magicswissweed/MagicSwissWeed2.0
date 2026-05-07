package com.aa.msw.source.german.bw.sample;

import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.helper.TestResourceLoader;
import com.aa.msw.model.Sample;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BwSampleFetchIntegrationTest {

    private final BwSampleFetchServiceImpl service = new BwSampleFetchServiceImpl() {
        @Override
        protected String fetchHvzBwData() {
            return TestResourceLoader.load("/testdata/hvz_bw_stations.js");
        }
    };

    @Test
    void shouldFetchFlowAndHeightSamplesForKnownStations() {
        Set<ApiStationId> stationIds = Set.of(
                new ApiStationId(CountryEnum.DE_BW, "00435"),
                new ApiStationId(CountryEnum.DE_BW, "00007")
        );

        List<Sample> samples = service.fetchSamples(stationIds);

        assertEquals(4, samples.size());
        assertEquals(2, samples.stream().filter(s -> s.getMeasurementType() == ApiMeasurementType.FLOW).count());
        assertEquals(2, samples.stream().filter(s -> s.getMeasurementType() == ApiMeasurementType.HEIGHT).count());
        assertTrue(samples.stream().allMatch(s -> s.value() >= 0));
        assertTrue(samples.stream().allMatch(s -> s.stationId().getCountry() == CountryEnum.DE_BW));
    }

    @Test
    void shouldFetchOnlyHeightWhenFlowMissing() {
        Set<ApiStationId> stationIds = Set.of(
                new ApiStationId(CountryEnum.DE_BW, "00099")
        );

        List<Sample> samples = service.fetchSamples(stationIds);

        assertEquals(1, samples.size());
        assertEquals(ApiMeasurementType.HEIGHT, samples.get(0).getMeasurementType());
    }

    @Test
    void shouldReturnEmptyForUnknownStation() {
        Set<ApiStationId> stationIds = Set.of(
                new ApiStationId(CountryEnum.DE_BW, "99999")
        );

        List<Sample> samples = service.fetchSamples(stationIds);

        assertTrue(samples.isEmpty());
    }
}

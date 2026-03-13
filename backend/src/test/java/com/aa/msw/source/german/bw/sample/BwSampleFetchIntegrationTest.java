package com.aa.msw.source.german.bw.sample;

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
    void shouldFetchSamplesForKnownStations() {
        Set<ApiStationId> stationIds = Set.of(
                new ApiStationId(CountryEnum.DE_BW, "00435"),
                new ApiStationId(CountryEnum.DE_BW, "00007")
        );

        List<Sample> samples = service.fetchSamples(stationIds);

        assertEquals(2, samples.size());
        assertTrue(samples.stream().allMatch(s -> s.flow() >= 0));
        assertTrue(samples.stream().allMatch(s -> s.stationId().getCountry() == CountryEnum.DE_BW));
    }

    @Test
    void shouldIgnoreStationsWithoutFlowData() {
        Set<ApiStationId> stationIds = Set.of(
                new ApiStationId(CountryEnum.DE_BW, "00099")
        );

        List<Sample> samples = service.fetchSamples(stationIds);

        assertEquals(0, samples.size());
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

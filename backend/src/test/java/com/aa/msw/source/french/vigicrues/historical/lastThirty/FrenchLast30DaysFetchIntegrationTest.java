package com.aa.msw.source.french.vigicrues.historical.lastThirty;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.helper.TestResourceLoader;
import com.aa.msw.model.LastFewDays;
import org.junit.jupiter.api.Test;

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
    void shouldFetchAndParseLast30Days() {
        Set<ApiStationId> stationIds = Set.of(
                new ApiStationId(CountryEnum.FR, "V271201001")
        );

        Set<LastFewDays> result = service.fetchLast30DaysSamples(stationIds);

        assertEquals(1, result.size());

        LastFewDays lastFewDays = result.iterator().next();
        assertEquals(new ApiStationId(CountryEnum.FR, "V271201001"), lastFewDays.stationId());
        assertEquals(5, lastFewDays.lastFewDaysSamples().size());
        assertTrue(lastFewDays.lastFewDaysSamples().values().stream().allMatch(v -> v > 0));
    }
}

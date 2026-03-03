package com.aa.msw.source.swiss.hydrodaten.historical.lastfourty;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.helper.TestResourceLoader;
import com.aa.msw.model.LastFewDays;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SwissLast40DaysFetchIntegrationTest {

    private final SwissLast40DaysSampleFetchServiceImpl service = new SwissLast40DaysSampleFetchServiceImpl() {
        @Override
        protected String fetchAsString(String url) {
            return TestResourceLoader.load("/testdata/hydrodaten_last40days.json");
        }
    };

    @Test
    void shouldFetchAndParseLast40Days() {
        Set<ApiStationId> stationIds = Set.of(
                new ApiStationId(CountryEnum.CH, "2018")
        );

        Set<LastFewDays> result = service.fetchLast40DaysSamples(stationIds);

        assertEquals(1, result.size());

        LastFewDays lastFewDays = result.iterator().next();
        assertEquals(new ApiStationId(CountryEnum.CH, "2018"), lastFewDays.stationId());
        assertFalse(lastFewDays.lastFewDaysSamples().isEmpty());
        assertTrue(lastFewDays.lastFewDaysSamples().values().stream().allMatch(v -> v > 0));
    }
}

package com.aa.msw.source.swiss.hydrodaten.forecast;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.helper.TestResourceLoader;
import com.aa.msw.model.Forecast;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SwissForecastFetchIntegrationTest {

    private final SwissForecastFetchServiceImpl service = new SwissForecastFetchServiceImpl() {
        @Override
        protected String fetchAsString(String url) {
            return TestResourceLoader.load("/testdata/hydrodaten_forecast.json");
        }
    };

    @Test
    void shouldFetchAndParseForecast() throws IOException, URISyntaxException {
        ApiStationId stationId = new ApiStationId(CountryEnum.CH, "2018");

        Forecast forecast = service.fetchForecast(stationId);

        assertNotNull(forecast);
        assertEquals(stationId, forecast.stationId());
        assertNotNull(forecast.timestamp());
        assertFalse(forecast.measuredData().isEmpty(), "Should have measured data");
        assertFalse(forecast.median().isEmpty(), "Should have median");
        assertFalse(forecast.min().isEmpty(), "Should have min");
        assertFalse(forecast.max().isEmpty(), "Should have max");
        assertFalse(forecast.twentyFivePercentile().isEmpty(), "Should have 25th percentile");
        assertFalse(forecast.seventyFivePercentile().isEmpty(), "Should have 75th percentile");

        assertTrue(forecast.measuredData().values().stream().allMatch(v -> v > 0));
        assertTrue(forecast.median().values().stream().allMatch(v -> v > 0));
    }

    @Test
    void shouldFetchForecastsForMultipleStations() {
        Set<ApiStationId> stationIds = Set.of(
                new ApiStationId(CountryEnum.CH, "2018"),
                new ApiStationId(CountryEnum.CH, "2243")
        );

        List<Forecast> forecasts = service.fetchForecasts(stationIds);

        assertEquals(2, forecasts.size());
    }
}

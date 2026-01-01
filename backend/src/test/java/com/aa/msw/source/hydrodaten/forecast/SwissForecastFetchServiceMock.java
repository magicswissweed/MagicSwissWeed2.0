package com.aa.msw.source.hydrodaten.forecast;

import com.aa.msw.database.helpers.id.ForecastId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.enums.Country;
import com.aa.msw.model.Forecast;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.aa.msw.database.helpers.EnumConverterHelper.apiStationId;

@Profile("test")
@Service
class SwissForecastFetchServiceMock implements SwissForecastFetchService {

    private final List<Forecast> forecasts = List.of(
            forecast(apiStationId(Country.CH, "2018")),
            forecast(apiStationId(Country.CH, "2243")),
            forecast(apiStationId(Country.CH, "2174"))
    );

    @Override
    public List<Forecast> fetchForecasts(Set<ApiStationId> stationIds) {
        return forecasts;
    }

    private OffsetDateTime offsetDateTime(int year, int month, int dayOfMonth) {
        return OffsetDateTime.of(year, month, dayOfMonth, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    @Override
    public Forecast fetchForecast(ApiStationId stationId) {
        for (Forecast forecast : forecasts) {
            if (forecast.stationId().equals(stationId.getExternalId())) {
                return forecast;
            }
        }
        throw new NotImplementedException("Add the forecast with this stationId to the ForecastFetchServiceMock");
    }

    private Forecast forecast(ApiStationId stationId) {
        return new Forecast(
                new ForecastId(),
                stationId,
                offsetDateTime(2025, 1, 1),
                Map.of(
                        offsetDateTime(2024, 12, 31), 200.0,
                        offsetDateTime(2025, 1, 1), 180.0
                ),
                Map.of(offsetDateTime(2025, 1, 2), 250.0),
                Map.of(offsetDateTime(2025, 1, 3), 240.0),
                Map.of(offsetDateTime(2025, 1, 2), 260.0),
                Map.of(offsetDateTime(2025, 1, 2), 200.0),
                Map.of(offsetDateTime(2025, 1, 2), 300.0)
        );
    }
}

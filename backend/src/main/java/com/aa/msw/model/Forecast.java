package com.aa.msw.model;

import com.aa.msw.database.helpers.id.ForecastId;
import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.gen.api.ApiStationId;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;

public record Forecast(
        ForecastId forecastId,
        @Getter ApiStationId stationId,
        @Getter OffsetDateTime timestamp,
        @Getter Map<OffsetDateTime, Double> measuredData,
        @Getter Map<OffsetDateTime, Double> median,
        @Getter Map<OffsetDateTime, Double> twentyFivePercentile,
        @Getter Map<OffsetDateTime, Double> seventyFivePercentile,
        @Getter Map<OffsetDateTime, Double> max,
        @Getter Map<OffsetDateTime, Double> min)
        implements HasId<ForecastId> {
    @Override
    public ForecastId getId() {
        return forecastId;
    }
}

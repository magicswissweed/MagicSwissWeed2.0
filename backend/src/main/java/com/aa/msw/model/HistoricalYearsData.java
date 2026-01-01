package com.aa.msw.model;

import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.database.helpers.id.HistoricalYearsDataId;
import com.aa.msw.gen.api.ApiStationId;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;

public record HistoricalYearsData(
        @Getter HistoricalYearsDataId databaseId,
        @Getter ApiStationId stationId,
        @Getter Map<OffsetDateTime, Double> median,
        @Getter Map<OffsetDateTime, Double> twentyFivePercentile,
        @Getter Map<OffsetDateTime, Double> seventyFivePercentile,
        @Getter Map<OffsetDateTime, Double> max,
        @Getter Map<OffsetDateTime, Double> min,
        @Getter Map<OffsetDateTime, Double> currentYear) implements HasId<HistoricalYearsDataId> {
    @Override
    public HistoricalYearsDataId getId() {
        return databaseId;
    }
}

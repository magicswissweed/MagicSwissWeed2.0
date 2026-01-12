package com.aa.msw.model;

import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.database.helpers.id.LastFewDaysId;
import com.aa.msw.gen.api.ApiStationId;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;

public record LastFewDays(
        @Getter LastFewDaysId databaseId,
        @Getter ApiStationId stationId,
        @Getter Map<OffsetDateTime, Double> lastFewDaysSamples) implements HasId<LastFewDaysId> {
    @Override
    public LastFewDaysId getId() {
        return databaseId;
    }
}

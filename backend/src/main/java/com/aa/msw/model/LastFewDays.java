package com.aa.msw.model;

import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.database.helpers.id.LastFewDaysId;
import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.gen.api.ApiStationId;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

public record LastFewDays(
        @Getter LastFewDaysId databaseId,
        @Getter ApiStationId stationId,
        @Getter Map<OffsetDateTime, Double> lastFewDaysSamples) implements HasId<LastFewDaysId> {
    @Override
    public LastFewDaysId getId() {
        return databaseId;
    }

    public Sample getLatestMeasurementAsSample() {
        Optional<Map.Entry<OffsetDateTime, Double>> latestEntry = lastFewDaysSamples.entrySet().stream()
                .max(Map.Entry.comparingByKey());
        if (latestEntry.isPresent()) {
            Map.Entry<OffsetDateTime, Double> latestEntryValue = latestEntry.get();
            return new Sample(
                    new SampleId(),
                    stationId,
                    latestEntryValue.getKey(),
                    Optional.empty(),
                    latestEntryValue.getValue().intValue()
            );
        }
        return null;
    }
}

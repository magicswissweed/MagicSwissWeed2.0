package com.aa.msw.model;

import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.gen.api.ApiStationId;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Optional;

public record Sample(
        SampleId sampleId,
        @Getter ApiStationId stationId,
        @Getter OffsetDateTime timestamp,
        @Getter Optional<Double> temperature,
        @Getter int flow)
        implements HasId<SampleId> {
    @Override
    public SampleId getId() {
        return sampleId;
    }
}

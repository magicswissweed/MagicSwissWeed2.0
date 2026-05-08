package com.aa.msw.model;

import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import lombok.Getter;

import java.time.OffsetDateTime;

public record Sample(
        SampleId sampleId,
        @Getter ApiStationId stationId,
        @Getter OffsetDateTime timestamp,
        @Getter double value,
        @Getter ApiMeasurementType measurementType)
        implements HasId<SampleId> {
    @Override
    public SampleId getId() {
        return sampleId;
    }
}

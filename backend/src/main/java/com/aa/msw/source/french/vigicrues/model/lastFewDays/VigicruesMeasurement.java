package com.aa.msw.source.french.vigicrues.model.lastFewDays;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public record VigicruesMeasurement(
        long timestamp,
        double value
) {
}


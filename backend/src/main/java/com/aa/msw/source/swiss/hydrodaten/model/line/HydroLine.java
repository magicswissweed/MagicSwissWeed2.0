package com.aa.msw.source.swiss.hydrodaten.model.line;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HydroLine(
        @JsonDeserialize(using = CustomOffsetDateTimeListDeserializer.class) ArrayList<OffsetDateTime> x,
        ArrayList<Double> y, String name) {
}

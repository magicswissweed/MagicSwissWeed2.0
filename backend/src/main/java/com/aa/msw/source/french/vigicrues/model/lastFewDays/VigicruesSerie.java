package com.aa.msw.source.french.vigicrues.model.lastFewDays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VigicruesSerie(
        @JsonProperty("ObssHydro") List<VigicruesMeasurement> line
) {
}

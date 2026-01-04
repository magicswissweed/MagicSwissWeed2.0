package com.aa.msw.source.swiss.hydrodaten.model.line;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HydroResponse(HydroPlot plot) {
}

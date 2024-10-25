package com.aa.msw.source.hydrodaten.model.line;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HydroLayout(ArrayList<HydroAnnotation> annotations) {
}

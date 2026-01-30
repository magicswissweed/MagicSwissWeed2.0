package com.aa.msw.source.swiss.existenz.sample.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExistenzSample(int timestamp,
                             @JsonProperty("loc") String stationId,
                             String par,
                             @JsonProperty("val") double value) {
}

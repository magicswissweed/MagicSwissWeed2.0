package com.aa.msw.source.swiss.existenz.sample.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public record ExistenzResponseSample(String source,
                                     @JsonProperty("apiurl") String apiUrl,
                                     String opendata,
                                     String license,
                                     ArrayList<ExistenzSample> payload) {
}

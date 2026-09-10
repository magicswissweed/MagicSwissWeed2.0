package com.aa.msw.source.rivermap.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// API: https://api.rivermap.org/v2/stations - the response additionally contains "sources", "rivers", "license"
// and "elapsedMs", which we do not use.
@JsonIgnoreProperties(ignoreUnknown = true)
public record RivermapStationsResponse(List<RivermapStation> stations) {
}

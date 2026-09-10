package com.aa.msw.source.rivermap.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * API: https://api.rivermap.org/v2/stations/readings
 *
 * @param readings readings per Rivermap station id (UUID)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RivermapReadingsResponse(Map<String, RivermapStationReadings> readings) {
}

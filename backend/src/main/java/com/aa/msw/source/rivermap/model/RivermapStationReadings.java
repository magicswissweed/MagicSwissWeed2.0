package com.aa.msw.source.rivermap.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * The readings of one station, grouped by unit.
 *
 * @param cm  water level in centimeters
 * @param m3s flow in cubic meters per second
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RivermapStationReadings(List<RivermapReading> cm, List<RivermapReading> m3s) {
}

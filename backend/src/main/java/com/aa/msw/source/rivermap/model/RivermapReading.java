package com.aa.msw.source.rivermap.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @param ts unix epoch seconds (UTC)
 * @param v  measured value in the unit of the enclosing list ("cm" or "m3s")
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RivermapReading(Long ts, Double v) {
}

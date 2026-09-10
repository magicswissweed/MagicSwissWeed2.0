package com.aa.msw.source.rivermap.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * A station as listed by https://api.rivermap.org/v2/stations
 *
 * @param id          UUID of the station at Rivermap - used as our externalId
 * @param type        online | manual | synthetic
 * @param sensors     "level" and/or "flow"
 * @param isActive    only active stations collect readings
 * @param name        name of the station (usually the place)
 * @param river       name of the river per language code (e.g. {"de": "Doubs"})
 * @param countryCode ISO 3166-1 alpha-2
 * @param state       state / region within the country (may be empty)
 * @param latlng      [lat, lng] as WGS-84 decimal degrees * 1_000_000, rounded down
 * @param sourceLinks links to the page of the authority publishing the data: language -> ("level" | "flow") -> url (urls may be null)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RivermapStation(
        String id,
        String type,
        List<String> sensors,
        @JsonProperty("isActive") Boolean isActive,
        String name,
        Map<String, String> river,
        String countryCode,
        String state,
        List<Long> latlng,
        Map<String, Map<String, String>> sourceLinks) {
}

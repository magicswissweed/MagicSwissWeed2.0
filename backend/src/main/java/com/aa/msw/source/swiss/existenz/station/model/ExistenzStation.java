package com.aa.msw.source.swiss.existenz.station.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExistenzStation(@JsonProperty("id") String existenzId,
                              @JsonProperty("name") String stationId,
                              ExistenzStationDetails details) {
}

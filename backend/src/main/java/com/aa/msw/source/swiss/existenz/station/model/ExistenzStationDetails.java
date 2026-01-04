package com.aa.msw.source.swiss.existenz.station.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExistenzStationDetails(@JsonProperty("id") String stationId,
                                     String name,
                                     @JsonProperty("water-body-name") String waterBodyName,
                                     @JsonProperty("water-body-type") String waterBodyType,
                                     Integer chx,
                                     Integer chy,
                                     @JsonProperty("lat") Double latitude,
                                     @JsonProperty("lon") Double longitude) {
}

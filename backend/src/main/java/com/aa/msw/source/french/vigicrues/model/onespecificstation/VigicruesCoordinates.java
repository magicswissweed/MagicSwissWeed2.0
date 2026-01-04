package com.aa.msw.source.french.vigicrues.model.onespecificstation;

import com.fasterxml.jackson.annotation.JsonProperty;

// API: https://www.vigicrues.gouv.fr/services/station.json/index.php?CdStationHydro=U251201001
// the coordinates are in the lambert-93 system (EPSG:2154) - They need to be converted to 'normal' latitude and longitude
public record VigicruesCoordinates(
        @JsonProperty("CoordXStationHydro") long x,
        @JsonProperty("CoordYStationHydro") long y) {
}

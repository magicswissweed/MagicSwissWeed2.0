package com.aa.msw.source.french.vigicrues.model.onespecificstation;

import com.fasterxml.jackson.annotation.JsonProperty;

// API: https://www.vigicrues.gouv.fr/services/station.json/index.php?CdStationHydro=U251201001
public record VigicruesStationDetail(
        @JsonProperty("CdCommune") String communeCode,
        @JsonProperty("CoordStationHydro") VigicruesCoordinates coordinates) {
}

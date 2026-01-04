package com.aa.msw.source.french.vigicrues.model.allstations;

import com.fasterxml.jackson.annotation.JsonProperty;

// API: https://www.vigicrues.gouv.fr/services/StaEntVigiCru.json
// CdEntVigiCru - id
// LbEndVigiCre - label
public record VigicruesStation(
        @JsonProperty("CdEntVigiCru") String id,
        @JsonProperty("LbEntVigiCru") String label) {
}


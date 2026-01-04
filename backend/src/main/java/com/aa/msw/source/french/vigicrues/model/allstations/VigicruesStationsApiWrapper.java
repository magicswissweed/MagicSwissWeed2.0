package com.aa.msw.source.french.vigicrues.model.allstations;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// API: https://www.vigicrues.gouv.fr/services/StaEntVigiCru.json
public record VigicruesStationsApiWrapper(
        @JsonProperty("ListEntVigiCru") List<VigicruesStation> stations) {
}


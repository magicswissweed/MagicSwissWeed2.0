package com.aa.msw.model;

import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.database.helpers.id.StationId;
import com.aa.msw.gen.api.ApiStationId;

public record Station(
        StationId databaseId,
        ApiStationId stationId,
        String label,
        Double latitude,
        Double longitude) implements HasId<StationId> {
    @Override
    public StationId getId() {
        return databaseId;
    }
}

package com.aa.msw.model;

import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.gen.api.ApiStationId;

public record Spot(
        SpotId spotId,
        boolean isPublic,
        SpotTypeEnum type,
        String name,
        ApiStationId stationId,
        Integer minFlow,
        Integer maxFlow
) implements HasId<SpotId> {
    @Override
    public SpotId getId() {
        return spotId;
    }
}

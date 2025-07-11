package com.aa.msw.model;

import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.database.helpers.id.SpotCurrentInfoId;
import com.aa.msw.database.helpers.id.SpotId;

public record SpotCurrentInfo(
        SpotCurrentInfoId spotCurrentInfoId,
        SpotId spotId,
        FlowStatusEnum currentFlowStatusEnum
) implements HasId<SpotCurrentInfoId> {
    @Override
    public SpotCurrentInfoId getId() {
        return spotCurrentInfoId;
    }
}

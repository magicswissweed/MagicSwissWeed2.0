package com.aa.msw.database.repository.dao;

import com.aa.msw.database.helpers.id.SpotCurrentInfoId;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.model.FlowStatusEnum;
import com.aa.msw.model.SpotCurrentInfo;

public interface SpotCurrentInfoDao extends Dao<SpotCurrentInfoId, SpotCurrentInfo> {
    SpotCurrentInfo get(SpotId spotId);

    void updateCurrentInfo(SpotId spotId, FlowStatusEnum flowstatus);
}

package com.aa.msw.database.repository.dao;

import com.aa.msw.database.helpers.UserToSpot;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.database.helpers.id.UserId;
import com.aa.msw.database.helpers.id.UserToSpotId;

import java.util.List;

public interface UserToSpotDao extends Dao<UserToSpotId, UserToSpot> {

    UserToSpot get(SpotId spotId);

    UserToSpot get(UserId userId, SpotId spotId);

    void setPosition(SpotId spotId, int position);

    void setWithNotification(SpotId spotId, boolean withNotification);

    void deletePrivateSpot(SpotId spotId);

    List<UserToSpot> getUserToSpotOrdered();

    boolean userHasSpot(SpotId id);
}

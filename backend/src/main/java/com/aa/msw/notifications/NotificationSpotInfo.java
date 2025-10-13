package com.aa.msw.notifications;

import com.aa.msw.database.helpers.UserToSpot;
import com.aa.msw.model.FlowStatusEnum;
import com.aa.msw.model.Spot;

public record NotificationSpotInfo(
        Spot spot,
        FlowStatusEnum flowStatus,
        UserToSpot userToSpot
) {

}

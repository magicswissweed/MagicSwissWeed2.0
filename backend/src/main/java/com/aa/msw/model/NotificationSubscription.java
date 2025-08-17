package com.aa.msw.model;

import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.database.helpers.id.NotificationId;
import com.aa.msw.database.helpers.id.UserId;

public record NotificationSubscription(
        NotificationId notificationId,
        UserId userId,
        String subscriptionToken)
        implements HasId<NotificationId> {
    @Override
    public NotificationId getId() {
        return notificationId;
    }
}

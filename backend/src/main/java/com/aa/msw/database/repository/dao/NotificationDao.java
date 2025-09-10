package com.aa.msw.database.repository.dao;

import com.aa.msw.database.helpers.id.NotificationId;
import com.aa.msw.database.helpers.id.UserId;
import com.aa.msw.model.NotificationSubscription;

import java.util.Set;

public interface NotificationDao extends Dao<NotificationId, NotificationSubscription> {
    void persistSubscriptionIfNotExists(String subscriptionToken);

    Set<NotificationSubscription> getSubscriptionsForUser(UserId userId);

    void deleteSubscriptionToken(String subscriptionToken);
}

package com.aa.msw.database.repository;

import com.aa.msw.auth.threadlocal.UserContext;
import com.aa.msw.database.helpers.id.NotificationId;
import com.aa.msw.database.helpers.id.UserId;
import com.aa.msw.database.repository.dao.NotificationDao;
import com.aa.msw.gen.jooq.tables.NotificationSubscriptionTable;
import com.aa.msw.gen.jooq.tables.daos.NotificationSubscriptionTableDao;
import com.aa.msw.gen.jooq.tables.records.NotificationSubscriptionTableRecord;
import com.aa.msw.model.NotificationSubscription;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;


@Component
public class NotificationRepository extends AbstractRepository<NotificationId, NotificationSubscription, NotificationSubscriptionTableRecord, com.aa.msw.gen.jooq.tables.pojos.NotificationSubscriptionTable, NotificationSubscriptionTableDao>
        implements NotificationDao {

    private static final NotificationSubscriptionTable TABLE = NotificationSubscriptionTable.NOTIFICATION_SUBSCRIPTION_TABLE;

    public NotificationRepository(final DSLContext dsl) {
        super(dsl, new NotificationSubscriptionTableDao(dsl.configuration()), TABLE, TABLE.ID);
    }

    @Override
    @Transactional
    public void persistSubscriptionIfNotExists(String subscriptionToken) {
        boolean doesNotExist = getAll().stream()
                .filter(s -> s.subscriptionToken().equals(subscriptionToken))
                .collect(Collectors.toSet())
                .isEmpty();
        if (doesNotExist) {
            persist(
                    new NotificationSubscription(
                            new NotificationId(),
                            UserContext.getCurrentUser().userId(),
                            subscriptionToken
                    )
            );
        }
    }

    @Override
    public Set<NotificationSubscription> getSubscriptionsForUser(UserId userId) {
        return getAll().stream()
                .filter(s -> s.userId().equals(userId))
                .collect(Collectors.toSet());
    }

    @Override
    public void deleteSubscriptionToken(String subscriptionToken) {
        dsl.deleteFrom(TABLE)
                .where(TABLE.SUBSCRIPTION_TOKEN.eq(subscriptionToken))
                .execute();
    }

    @Override
    protected NotificationSubscription mapRecord(NotificationSubscriptionTableRecord record) {
        return new NotificationSubscription(
                new NotificationId(record.getId()),
                new UserId(record.getUserId()),
                record.getSubscriptionToken()
        );
    }

    @Override
    protected NotificationSubscriptionTableRecord mapDomain(NotificationSubscription notificationSubscription) {
        final NotificationSubscriptionTableRecord record = dsl.newRecord(table);
        record.setId(notificationSubscription.notificationId().getId());
        record.setUserId(notificationSubscription.userId().getId());
        record.setSubscriptionToken(notificationSubscription.subscriptionToken());
        return record;
    }

    @Override
    protected NotificationSubscription mapEntity(com.aa.msw.gen.jooq.tables.pojos.NotificationSubscriptionTable notificationSubscription) {
        return new NotificationSubscription(
                new NotificationId(notificationSubscription.getId()),
                new UserId(notificationSubscription.getUserId()),
                notificationSubscription.getSubscriptionToken()
        );
    }
}

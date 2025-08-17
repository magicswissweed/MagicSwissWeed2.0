package com.aa.msw.database.helpers.id;

import java.util.UUID;

public class NotificationId extends DbSyncedId {
    public NotificationId() {
        super();
    }

    public NotificationId(final UUID identifier) {
        super(identifier);
    }

    public NotificationId(final String identifier) {
        super(UUID.fromString(identifier));
    }
}

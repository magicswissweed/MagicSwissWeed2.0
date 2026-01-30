package com.aa.msw.database.helpers.id;

import java.util.UUID;

public class LastFewDaysId extends DbSyncedId {
    public LastFewDaysId() {
        super();
    }

    public LastFewDaysId(final UUID identifier) {
        super(identifier);
    }

    public LastFewDaysId(final String identifier) {
        super(UUID.fromString(identifier));
    }
}

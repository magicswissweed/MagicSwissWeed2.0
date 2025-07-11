package com.aa.msw.database.helpers.id;

import java.util.UUID;

public class SpotCurrentInfoId extends DbSyncedId {
    public SpotCurrentInfoId() {
        super();
    }

    public SpotCurrentInfoId(final UUID identifier) {
        super(identifier);
    }

    public SpotCurrentInfoId(final UUID identifier, boolean isDbSynced) {
        super(identifier, isDbSynced);
    }

    public SpotCurrentInfoId(final String identifier) {
        super(UUID.fromString(identifier));
    }
}

package com.aa.msw.database.helpers;

import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.database.helpers.id.UserId;
import com.aa.msw.database.helpers.id.UserToSpotId;

import java.util.Objects;

public final class UserToSpot implements HasId<UserToSpotId> {
    private final UserToSpotId userToSpotId;
    private final UserId userId;
    private final SpotId spotId;
    private final boolean withNotification;
    private final String notes;
    private int position;

    public UserToSpot(UserToSpotId userToSpotId,
                      UserId userId,
                      SpotId spotId,
                      int position,
                      boolean withNotification,
                      String notes) {
        this.userToSpotId = userToSpotId;
        this.userId = userId;
        this.spotId = spotId;
        this.position = position;
        this.withNotification = withNotification;
        this.notes = notes;
    }

    public UserToSpot(UserToSpotId userToSpotId,
                      UserId userId,
                      SpotId spotId,
                      int position,
                      boolean withNotification) {
        this(userToSpotId, userId, spotId, position, withNotification, null);
    }

    @Override
    public UserToSpotId getId() {
        return userToSpotId;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public UserToSpotId userToSpotId() {
        return userToSpotId;
    }

    public UserId userId() {
        return userId;
    }

    public SpotId spotId() {
        return spotId;
    }

    public int position() {
        return position;
    }

    public boolean withNotification() {
        return withNotification;
    }

    public String notes() {
        return notes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UserToSpot) obj;
        return Objects.equals(this.userToSpotId, that.userToSpotId) &&
                Objects.equals(this.userId, that.userId) &&
                Objects.equals(this.spotId, that.spotId) &&
                this.position == that.position &&
                this.withNotification == that.withNotification &&
                Objects.equals(this.notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userToSpotId, userId, spotId, position, notes);
    }

    @Override
    public String toString() {
        return "UserToSpot[" +
                "userToSpotId=" + userToSpotId + ", " +
                "userId=" + userId + ", " +
                "spotId=" + spotId + ", " +
                "position=" + position + ", " +
                "withNotification=" + withNotification + ", " +
                "notes=" + notes + ']';
    }

}

package com.aa.msw.database.repository;

import com.aa.msw.auth.threadlocal.UserContext;
import com.aa.msw.database.helpers.UserToSpot;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.database.helpers.id.UserId;
import com.aa.msw.database.helpers.id.UserToSpotId;
import com.aa.msw.database.repository.dao.UserToSpotDao;
import com.aa.msw.gen.jooq.tables.UserToSpotTable;
import com.aa.msw.gen.jooq.tables.daos.UserToSpotTableDao;
import com.aa.msw.gen.jooq.tables.records.UserToSpotTableRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;


@Component
public class UserToSpotRepository extends AbstractRepository<UserToSpotId, UserToSpot, UserToSpotTableRecord, com.aa.msw.gen.jooq.tables.pojos.UserToSpotTable, UserToSpotTableDao>
        implements UserToSpotDao {

    private static final UserToSpotTable TABLE = UserToSpotTable.USER_TO_SPOT_TABLE;

    public UserToSpotRepository(final DSLContext dsl) {
        super(dsl, new UserToSpotTableDao(dsl.configuration()), TABLE, TABLE.ID);
    }

    @Override
    public boolean userHasSpot(SpotId id) {
        return !dsl.selectFrom(TABLE)
                .where(TABLE.USER_ID.eq(UserContext.getCurrentUser().userId().getId()))
                .and(TABLE.SPOT_ID.eq(id.getId()))
                .fetch(this::mapRecord)
                .isEmpty();
    }

    @Override
    public List<UserToSpot> getUserToSpotOrdered() {
        return dsl.selectFrom(TABLE)
                .where(TABLE.USER_ID.eq(UserContext.getCurrentUser().userId().getId()))
                .orderBy(TABLE.POSITION)
                .fetch(this::mapRecord);
    }

    @Override
    public Set<UserToSpot> getUserToSpots(SpotId spotId) {
        return dsl.selectFrom(TABLE)
                .where(TABLE.SPOT_ID.eq(spotId.getId()))
                .fetchSet(this::mapRecord);
    }

    @Override
    public UserToSpot get(UserId userId, SpotId spotId) {
        return dsl.selectFrom(TABLE)
                .where(TABLE.USER_ID.eq(userId.getId()))
                .and(TABLE.SPOT_ID.eq(spotId.getId()))
                .fetchOne(this::mapRecord);
    }

    @Override
    public void setPosition(SpotId spotId, int position) {
        UserId userId = UserContext.getCurrentUser().userId();

        dsl.update(TABLE)
                .set(TABLE.POSITION, position)
                .where(TABLE.USER_ID.eq(userId.getId())
                        .and(TABLE.SPOT_ID.eq(spotId.getId())))
                .execute();
    }

    @Override
    public void setWithNotification(SpotId spotId, boolean withNotification) {
        UserId userId = UserContext.getCurrentUser().userId();

        dsl.update(TABLE)
                .set(TABLE.WITHNOTIFICATION, withNotification)
                .where(TABLE.USER_ID.eq(userId.getId())
                        .and(TABLE.SPOT_ID.eq(spotId.getId())))
                .execute();
    }

    @Override
    @Transactional
    // this only deletes the mapping from the user to the spot. The spot stays in the db.
    public void deletePrivateSpot(SpotId spotId) {
        dsl.deleteFrom(TABLE)
                .where(TABLE.SPOT_ID.eq(spotId.getId())
                        .and(TABLE.USER_ID.eq(UserContext.getCurrentUser().userId().getId())))
                .execute();
    }

    @Override
    protected UserToSpot mapRecord(UserToSpotTableRecord record) {
        return new UserToSpot(
                new UserToSpotId(record.getId()),
                new UserId(record.getUserId()),
                new SpotId(record.getSpotId()),
                record.getPosition(),
                record.getWithnotification()
        );
    }

    @Override
    protected UserToSpotTableRecord mapDomain(UserToSpot userToSpot) {
        UserToSpotTableRecord record = dsl.newRecord(TABLE);
        record.setId(userToSpot.getId().getId());
        record.setUserId(userToSpot.userId().getId());
        record.setSpotId(userToSpot.spotId().getId());
        record.setPosition(userToSpot.position());
        record.setWithnotification(userToSpot.withNotification());
        return record;
    }

    @Override
    protected UserToSpot mapEntity(com.aa.msw.gen.jooq.tables.pojos.UserToSpotTable userToSpotTable) {
        return new UserToSpot(
                new UserToSpotId(userToSpotTable.getId()),
                new UserId(userToSpotTable.getUserId()),
                new SpotId(userToSpotTable.getSpotId()),
                userToSpotTable.getPosition(),
                userToSpotTable.getWithnotification()
        );
    }
}

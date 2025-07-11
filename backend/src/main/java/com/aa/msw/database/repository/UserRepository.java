package com.aa.msw.database.repository;

import com.aa.msw.database.exceptions.NoSuchUserException;
import com.aa.msw.database.helpers.id.UserExtId;
import com.aa.msw.database.helpers.id.UserId;
import com.aa.msw.database.repository.dao.UserDao;
import com.aa.msw.gen.jooq.tables.UserTable;
import com.aa.msw.gen.jooq.tables.daos.UserTableDao;
import com.aa.msw.gen.jooq.tables.records.UserTableRecord;
import com.aa.msw.model.User;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;


@Component
public class UserRepository extends AbstractRepository<UserId, User, UserTableRecord, com.aa.msw.gen.jooq.tables.pojos.UserTable, UserTableDao>
        implements UserDao {

    private static final UserTable TABLE = UserTable.USER_TABLE;

    public UserRepository(final DSLContext dsl) {
        super(dsl, new UserTableDao(dsl.configuration()), TABLE, TABLE.ID);
    }

    @Override
    @Transactional
    public User getUser(UserExtId externalId) throws NoSuchUserException {
        try {
            return dsl.selectFrom(TABLE)
                    .where(TABLE.EXTID.eq(externalId.getId()))
                    .fetch(this::mapRecord)
                    .getFirst();
        } catch (NoSuchElementException e) {
            throw new NoSuchUserException(e);
        }
    }


    @Override
    protected User mapRecord(UserTableRecord record) {
        return new User(
                new UserId(record.getId()),
                new UserExtId(record.getExtid()),
                record.getEmail(),
                record.getUsername()
        );
    }

    @Override
    protected UserTableRecord mapDomain(User user) {
        final UserTableRecord record = dsl.newRecord(table);
        record.setId(user.getId().getId());
        record.setExtid(user.externalId().getId());
        record.setEmail(user.email());
        record.setUsername("");
        return record;
    }

    @Override
    protected User mapEntity(com.aa.msw.gen.jooq.tables.pojos.UserTable userTable) {
        return new User(
                new UserId(userTable.getId()),
                new UserExtId(userTable.getExtid()),
                userTable.getEmail(),
                userTable.getUsername()
        );
    }
}

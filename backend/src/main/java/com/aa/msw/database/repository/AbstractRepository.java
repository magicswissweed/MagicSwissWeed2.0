package com.aa.msw.database.repository;

import com.aa.msw.database.helpers.AbstractUUID;
import com.aa.msw.database.helpers.id.DbSyncedId;
import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.database.repository.dao.Dao;
import org.jooq.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toUnmodifiableSet;

public abstract class AbstractRepository<ID extends DbSyncedId,
        DOMAIN extends HasId<ID>,
        RECORD extends UpdatableRecord<RECORD>,
        ENTITY extends Serializable,
        D extends DAO<RECORD, ENTITY, UUID>> implements Dao<ID, DOMAIN> {

    protected final DSLContext dsl;

    protected final D dao;
    protected final Table<RECORD> table;
    protected final TableField<RECORD, UUID> idField;

    public AbstractRepository(DSLContext dsl, D dao, Table<RECORD> table, TableField<RECORD, UUID> idField) {
        this.dsl = dsl;
        this.dao = dao;
        this.table = table;
        this.idField = idField;
    }

    @Override
    public Set<DOMAIN> getAll() {
        return dao.findAll()
                .stream()
                .map(this::mapEntity)
                .collect(toUnmodifiableSet());
    }

    @Override
    public DOMAIN get(ID id) {
        return find(id).get();
    }

    @Override
    public Set<DOMAIN> getIn(ID... ids) {
        Set<UUID> uuids = Arrays.stream(ids)
                .map(AbstractUUID::getId)
                .collect(toUnmodifiableSet());
        return dsl.selectFrom(table)
                .where(idField.in(uuids))
                .fetch(this::mapRecord)
                .stream()
                .collect(toUnmodifiableSet());
    }

    @Override
    public Optional<DOMAIN> find(ID id) {
        ENTITY pojo = dao.findById(id.getId());
        return mapNullableEntity(pojo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(ID id) {
        dsl.deleteFrom(table)
                .where(idField.eq(id.getId()))
                .execute();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(DOMAIN entity) {
        delete(entity.getId());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DOMAIN persist(DOMAIN entity) {
        if (entity.getId().isDbSynced()) {
            return update(entity);
        } else {
            return insert(entity);
        }
    }

    @Transactional
    public DOMAIN update(DOMAIN domain) {
        RECORD record = mapDomain(domain);
        record.update();
        afterPersist(domain);
        return mapRecord(record);
    }

    @Transactional
    protected DOMAIN insert(DOMAIN domain) {
        RECORD record = mapDomain(domain);
        record.insert();
        afterPersist(domain);
        return mapRecord(record);
    }

    @Override
    public Long count() {
        return dao.count();
    }

    protected void afterPersist(DOMAIN domain) {
    }

    protected abstract DOMAIN mapRecord(RECORD record);

    protected abstract RECORD mapDomain(DOMAIN domain);

    protected abstract DOMAIN mapEntity(ENTITY entity);

    protected Optional<DOMAIN> mapNullableEntity(ENTITY entity) {
        if (entity == null) {
            return Optional.empty();
        } else {
            return Optional.of(mapEntity(entity));
        }
    }

}

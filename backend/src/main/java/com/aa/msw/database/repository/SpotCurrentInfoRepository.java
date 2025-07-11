package com.aa.msw.database.repository;

import com.aa.msw.database.helpers.id.SpotCurrentInfoId;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.database.repository.dao.SpotCurrentInfoDao;
import com.aa.msw.gen.jooq.enums.Flowstatus;
import com.aa.msw.gen.jooq.tables.SpotCurrentInfoTable;
import com.aa.msw.gen.jooq.tables.daos.SpotCurrentInfoTableDao;
import com.aa.msw.gen.jooq.tables.records.SpotCurrentInfoTableRecord;
import com.aa.msw.model.FlowStatusEnum;
import com.aa.msw.model.SpotCurrentInfo;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;


@Component
public class SpotCurrentInfoRepository extends AbstractRepository<SpotCurrentInfoId, SpotCurrentInfo, SpotCurrentInfoTableRecord, com.aa.msw.gen.jooq.tables.pojos.SpotCurrentInfoTable, SpotCurrentInfoTableDao>
        implements SpotCurrentInfoDao {

    private static final SpotCurrentInfoTable TABLE = SpotCurrentInfoTable.SPOT_CURRENT_INFO_TABLE;

    public SpotCurrentInfoRepository(final DSLContext dsl) {
        super(dsl, new SpotCurrentInfoTableDao(dsl.configuration()), TABLE, TABLE.ID);
    }

    @Override
    public SpotCurrentInfo get(SpotId spotId) {
        return dsl.selectFrom(TABLE)
                .where(TABLE.SPOT_ID.eq(spotId.getId()))
                .fetchOne(this::mapRecord);
    }

    @Override
    public void updateCurrentInfo(SpotId spotId, FlowStatusEnum flowStatus) {
        if (currentInfoExistsForSpot(spotId)) {
            update(
                    new SpotCurrentInfo(
                            get(spotId).spotCurrentInfoId(),
                            spotId,
                            flowStatus
                    )
            );
        } else {
            insert(
                    new SpotCurrentInfo(
                            new SpotCurrentInfoId(),
                            spotId,
                            flowStatus
                    )
            );
        }
    }

    private boolean currentInfoExistsForSpot(SpotId spotId) {
        return get(spotId) != null;
    }

    @Override
    protected SpotCurrentInfo mapRecord(SpotCurrentInfoTableRecord record) {
        return new SpotCurrentInfo(
                new SpotCurrentInfoId(record.getId()),
                new SpotId(record.getSpotId()),
                FlowStatusEnum.valueOf(record.getCurrentflowstatus().name())
        );
    }

    @Override
    protected SpotCurrentInfoTableRecord mapDomain(SpotCurrentInfo SpotCurrentInfo) {
        SpotCurrentInfoTableRecord record = dsl.newRecord(TABLE);
        record.setId(SpotCurrentInfo.getId().getId());
        record.setSpotId(SpotCurrentInfo.spotId().getId());
        record.setCurrentflowstatus(Flowstatus.valueOf(SpotCurrentInfo.currentFlowStatusEnum().name()));
        return record;
    }

    @Override
    protected SpotCurrentInfo mapEntity(com.aa.msw.gen.jooq.tables.pojos.SpotCurrentInfoTable SpotCurrentInfoTable) {
        return new SpotCurrentInfo(
                new SpotCurrentInfoId(SpotCurrentInfoTable.getId()),
                new SpotId(SpotCurrentInfoTable.getSpotId()),
                FlowStatusEnum.valueOf(SpotCurrentInfoTable.getCurrentflowstatus().name())
        );
    }
}

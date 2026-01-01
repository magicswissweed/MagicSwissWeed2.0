package com.aa.msw.database.repository;

import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.database.repository.dao.SpotDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.enums.Spottype;
import com.aa.msw.gen.jooq.tables.SpotTable;
import com.aa.msw.gen.jooq.tables.daos.SpotTableDao;
import com.aa.msw.gen.jooq.tables.records.SpotTableRecord;
import com.aa.msw.model.Spot;
import com.aa.msw.model.SpotTypeEnum;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.aa.msw.database.helpers.EnumConverterHelper.apiStationId;
import static com.aa.msw.database.helpers.EnumConverterHelper.country;
import static java.util.stream.Collectors.toUnmodifiableSet;


@Component
public class SpotRepository extends AbstractRepository<SpotId, Spot, SpotTableRecord, com.aa.msw.gen.jooq.tables.pojos.SpotTable, SpotTableDao>
        implements SpotDao {

    private static final SpotTable TABLE = SpotTable.SPOT_TABLE;


    public SpotRepository(final DSLContext dsl) {
        super(dsl, new SpotTableDao(dsl.configuration()), TABLE, TABLE.ID);
    }

    @Override
    protected Spot mapRecord(SpotTableRecord record) {
        return new Spot(
                new SpotId(record.getId()),
                record.getIspublic(),
                mapDbToDomainEnum(record.getType()),
                record.getName(),
                apiStationId(record.getCountry(), record.getStationid()),
                record.getMinflow(),
                record.getMaxflow()
        );
    }

    @Override
    protected SpotTableRecord mapDomain(Spot spot) {
        SpotTableRecord record = dsl.newRecord(TABLE);
        record.setId(spot.getId().getId());
        record.setIspublic(spot.isPublic());
        record.setType(mapDomainToDbEnum(spot.type()));
        record.setCountry(country(spot.stationId().getCountry()));
        record.setStationid(spot.stationId().getExternalId());
        record.setName(spot.name());
        record.setMinflow(spot.minFlow());
        record.setMaxflow(spot.maxFlow());
        return record;
    }

    @Override
    protected Spot mapEntity(com.aa.msw.gen.jooq.tables.pojos.SpotTable spotTable) {
        return new Spot(
                new SpotId(spotTable.getId()),
                spotTable.getIspublic(),
                mapDbToDomainEnum(spotTable.getType()),
                spotTable.getName(),
                apiStationId(spotTable.getCountry(), spotTable.getStationid()),
                spotTable.getMinflow(),
                spotTable.getMaxflow()
        );
    }

    private Spottype mapDomainToDbEnum(SpotTypeEnum type) {
        return switch (type) {
            case RIVER_SURF -> Spottype.RIVER_SURF;
            case BUNGEE_SURF -> Spottype.BUNGEE_SURF;
        };
    }

    private SpotTypeEnum mapDbToDomainEnum(Spottype type) {
        return switch (type) {
            case RIVER_SURF -> SpotTypeEnum.RIVER_SURF;
            case BUNGEE_SURF -> SpotTypeEnum.BUNGEE_SURF;
        };
    }

    @Override
    public boolean isPublicSpot(SpotId spotId) {
        return find(spotId).map(Spot::isPublic).orElse(false);
    }

    @Override
    public List<Spot> getPublicSpots() {
        return dsl.selectFrom(TABLE)
                .where(TABLE.ISPUBLIC)
                .fetch(this::mapRecord);
    }

    @Override
    public Set<Spot> getSpotsWithStationId(ApiStationId stationId) {
        return dsl.selectFrom(TABLE)
                .where(TABLE.COUNTRY.eq(country(stationId.getCountry()))
                        .and(TABLE.STATIONID.eq(stationId.getExternalId())))
                .fetch(this::mapRecord)
                .stream()
                .collect(toUnmodifiableSet());
    }
}

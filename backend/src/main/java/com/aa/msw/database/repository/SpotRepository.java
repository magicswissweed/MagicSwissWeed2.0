package com.aa.msw.database.repository;

import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.database.repository.dao.SpotDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
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

import static com.aa.msw.database.helpers.EnumConverterHelper.apiMeasurementType;
import static com.aa.msw.database.helpers.EnumConverterHelper.apiStationId;
import static com.aa.msw.database.helpers.EnumConverterHelper.country;
import static com.aa.msw.database.helpers.EnumConverterHelper.measurementType;
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
                apiMeasurementType(record.getMeasurementType()),
                record.getMinValue().doubleValue(),
                record.getMaxValue().doubleValue()
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
        record.setMeasurementType(measurementType(spot.measurementType()));
        record.setMinValue(spot.minValue().floatValue());
        record.setMaxValue(spot.maxValue().floatValue());
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
                apiMeasurementType(spotTable.getMeasurementType()),
                spotTable.getMinValue().doubleValue(),
                spotTable.getMaxValue().doubleValue()
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

    @Override
    public Set<ApiStationId> getReferencedStationIds(CountryEnum country) {
        return dsl.selectDistinct(TABLE.COUNTRY, TABLE.STATIONID)
                .from(TABLE)
                .where(TABLE.COUNTRY.eq(country(country)))
                .fetch(r -> apiStationId(r.value1(), r.value2()))
                .stream()
                .collect(toUnmodifiableSet());
    }
}

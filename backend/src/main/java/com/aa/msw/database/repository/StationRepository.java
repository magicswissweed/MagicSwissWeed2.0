package com.aa.msw.database.repository;

import com.aa.msw.database.helpers.id.StationId;
import com.aa.msw.database.repository.dao.StationDao;
import com.aa.msw.gen.jooq.tables.StationTable;
import com.aa.msw.gen.jooq.tables.daos.StationTableDao;
import com.aa.msw.gen.jooq.tables.records.StationTableRecord;
import com.aa.msw.model.Station;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.aa.msw.database.helpers.EnumConverterHelper.apiStationId;
import static com.aa.msw.database.helpers.EnumConverterHelper.country;

@Component
public class StationRepository extends AbstractRepository
        <StationId, Station, StationTableRecord, com.aa.msw.gen.jooq.tables.pojos.StationTable, StationTableDao>
        implements StationDao {

    private static final StationTable TABLE = StationTable.STATION_TABLE;

    public StationRepository(final DSLContext dsl) {
        super(dsl, new StationTableDao(dsl.configuration()), TABLE, TABLE.DB_ID);
    }

    @Override
    protected Station mapRecord(StationTableRecord record) {
        return new Station(
                new StationId(record.getDbId()),
                apiStationId(record.getCountry(), record.getStationid()),
                record.getLabel(),
                record.getLatitude(),
                record.getLongitude()
        );
    }

    @Override
    protected StationTableRecord mapDomain(Station station) {
        final StationTableRecord record = dsl.newRecord(table);

        record.setDbId(station.databaseId().getId());
        record.setCountry(country(station.stationId().getCountry()));
        record.setStationid(station.stationId().getExternalId());
        record.setLabel(station.label());
        record.setLatitude(station.latitude());
        record.setLongitude(station.longitude());
        return record;
    }

    @Override
    protected Station mapEntity(com.aa.msw.gen.jooq.tables.pojos.StationTable stationTable) {
        return new Station(
                new StationId(stationTable.getDbId()),
                apiStationId(stationTable.getCountry(), stationTable.getStationid()),
                stationTable.getLabel(),
                stationTable.getLatitude(),
                stationTable.getLongitude()
        );
    }

    @Override
    public Set<Station> getStations() {
        return dsl.selectFrom(TABLE)
                .fetchSet(this::mapRecord);
    }

    @Override
    public void deleteAll() {
        dsl.deleteFrom(TABLE)
                .execute();
    }
}

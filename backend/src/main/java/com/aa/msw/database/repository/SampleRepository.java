package com.aa.msw.database.repository;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.enums.MeasurementType;
import com.aa.msw.gen.jooq.tables.SampleTable;
import com.aa.msw.gen.jooq.tables.StationTable;
import com.aa.msw.gen.jooq.tables.daos.SampleTableDao;
import com.aa.msw.gen.jooq.tables.records.SampleTableRecord;
import com.aa.msw.model.Sample;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.aa.msw.database.helpers.EnumConverterHelper.*;


@Component
public class SampleRepository extends AbstractTimestampedRepository
        <SampleId, Sample, SampleTableRecord, com.aa.msw.gen.jooq.tables.pojos.SampleTable, SampleTableDao>
        implements SampleDao {

    private static final SampleTable TABLE = SampleTable.SAMPLE_TABLE;

    public SampleRepository(final DSLContext dsl) {
        super(dsl, new SampleTableDao(dsl.configuration()), TABLE, TABLE.ID, TABLE.TIMESTAMP);
    }

    @Override
    protected Sample mapRecord(SampleTableRecord record) {
        return new Sample(
                new SampleId(record.getId()),
                apiStationId(record.getCountry(), record.getStationid()),
                record.getTimestamp().withOffsetSameInstant(ZoneOffset.UTC),
                record.getValue().doubleValue(),
                apiMeasurementType(record.getMeasurementType()));
    }

    @Override
    protected SampleTableRecord mapDomain(Sample sample) {
        final SampleTableRecord record = dsl.newRecord(table);
        record.setId(sample.sampleId().getId());
        record.setCountry(sample.getStationId().getCountry());
        record.setStationid(sample.getStationId().getExternalId());
        record.setTimestamp(sample.getTimestamp());
        record.setValue((float) sample.getValue());
        record.setMeasurementType(measurementType(sample.getMeasurementType()));
        return record;
    }

    @Override
    protected Sample mapEntity(com.aa.msw.gen.jooq.tables.pojos.SampleTable sampleTable) {
        return new Sample(
                new SampleId(sampleTable.getId()),
                apiStationId(sampleTable.getCountry(), sampleTable.getStationid()),
                sampleTable.getTimestamp(),
                sampleTable.getValue().doubleValue(),
                apiMeasurementType(sampleTable.getMeasurementType())
        );
    }

    @Override
    public Sample getCurrentSample(ApiStationId stationId, ApiMeasurementType type) throws NoDataAvailableException {
        return dsl.selectFrom(TABLE)
                .where(TABLE.COUNTRY.eq(stationId.getCountry())
                        .and(TABLE.STATIONID.eq(stationId.getExternalId()))
                        .and(TABLE.MEASUREMENT_TYPE.eq(measurementType(type))))
                .orderBy(TABLE.TIMESTAMP.desc())
                .limit(1)
                .fetchOptional(this::mapRecord)
                .orElseThrow(() -> new NoDataAvailableException("No current " + type.getValue() + " sample found for station " + stationId.getExternalId() + " in " + stationId.getCountry()));
    }

    @Override
    public List<Sample> getSamplesOfLastNDays(ApiStationId stationId, ApiMeasurementType type, int days) {
        return dsl.selectFrom(TABLE)
                .where(TABLE.COUNTRY.eq(stationId.getCountry())
                        .and(TABLE.STATIONID.eq(stationId.getExternalId()))
                        .and(TABLE.MEASUREMENT_TYPE.eq(measurementType(type)))
                        .and(TABLE.TIMESTAMP.greaterOrEqual(OffsetDateTime.now().minusDays(days))))
                .orderBy(TABLE.TIMESTAMP.asc())
                .fetch(this::mapRecord);
    }

    @Override
    public Map<ApiStationId, Set<ApiMeasurementType>> getSupportedMeasurementsByStation() {
        StationTable station = StationTable.STATION_TABLE;
        SampleTable sample = TABLE.as("t");

        Field<MeasurementType> type = DSL.field(DSL.name("m", "mt"), MeasurementType.class);
        @SuppressWarnings("unchecked") // generic array creation for the varargs of DSL.values
        Row1<MeasurementType>[] typeRows = Arrays.stream(MeasurementType.values())
                .map(DSL::row)
                .toArray(Row1[]::new);
        Table<?> types = DSL.values(typeRows).as("m", "mt");
        Table<?> probe = DSL.lateral(
                        DSL.selectOne()
                                .from(sample)
                                .where(sample.COUNTRY.eq(station.COUNTRY))
                                .and(sample.STATIONID.eq(station.STATIONID))
                                .and(sample.MEASUREMENT_TYPE.eq(type))
                                .limit(1))
                .as("x");

        return dsl.select(station.COUNTRY, station.STATIONID, type)
                .from(station)
                .crossJoin(types)
                .crossJoin(probe)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        r -> apiStationId(r.value1(), r.value2()),
                        Collectors.mapping(r -> apiMeasurementType(r.value3()), Collectors.toSet())
                ));
    }

    @Override
    public Map<ApiStationId, Map<ApiMeasurementType, Sample>> getLatestSamplePerStationAndType(Set<ApiStationId> stationIds) {
        if (stationIds.isEmpty()) {
            return Map.of();
        }

        return dsl.select(TABLE.fields())
                .distinctOn(TABLE.COUNTRY, TABLE.STATIONID, TABLE.MEASUREMENT_TYPE)
                .from(TABLE)
                .where(buildStationFilter(stationIds))
                .orderBy(TABLE.COUNTRY, TABLE.STATIONID, TABLE.MEASUREMENT_TYPE, TABLE.TIMESTAMP.desc())
                .fetch()
                .into(TABLE)
                .stream()
                .map(this::mapRecord)
                .collect(Collectors.groupingBy(
                        Sample::getStationId,
                        Collectors.toMap(Sample::getMeasurementType, sample -> sample)));
    }

    private static Condition buildStationFilter(Set<ApiStationId> stationIds) {
        // Group external IDs by country so we can emit `(country = X AND stationid IN (...)) OR ...`,
        // which is index-friendly and avoids vendor-specific row-value-IN syntax.
        Map<String, Set<String>> externalIdsByCountry = stationIds.stream()
                .collect(Collectors.groupingBy(
                        ApiStationId::getCountry,
                        Collectors.mapping(ApiStationId::getExternalId, Collectors.toSet())));

        return externalIdsByCountry.entrySet().stream()
                .map(entry -> TABLE.COUNTRY.eq(entry.getKey()).and(TABLE.STATIONID.in(entry.getValue())))
                .reduce(Condition::or)
                .orElse(DSL.falseCondition());
    }

    @Override
    @Transactional
    public void persistSamplesIfNotExist(List<Sample> samples) {
        for (Sample sample : samples) {
            SampleTableRecord record = mapDomain(sample);
            dsl.insertInto(TABLE)
                    .set(record)
                    .onConflict(TABLE.TIMESTAMP, TABLE.STATIONID, TABLE.MEASUREMENT_TYPE)
                    .doNothing()
                    .execute();
        }
    }
}

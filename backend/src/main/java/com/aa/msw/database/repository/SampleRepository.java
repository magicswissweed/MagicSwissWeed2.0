package com.aa.msw.database.repository;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.tables.SampleTable;
import com.aa.msw.gen.jooq.tables.daos.SampleTableDao;
import com.aa.msw.gen.jooq.tables.records.SampleTableRecord;
import com.aa.msw.model.Sample;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.aa.msw.database.helpers.EnumConverterHelper.apiStationId;
import static com.aa.msw.database.helpers.EnumConverterHelper.country;


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
        Optional<Double> optionalTemp = Optional.empty();
        if (record.getTemperature() != null) {
            DecimalFormat roundToOneDigit = new DecimalFormat("##.#");
            optionalTemp = Optional.of(
                    Double.parseDouble(roundToOneDigit.format(record.getTemperature())));
        }

        return new Sample(
                new SampleId(record.getId()),
                apiStationId(record.getCountry(), record.getStationid()),
                record.getTimestamp().withOffsetSameInstant(ZoneOffset.UTC),
                optionalTemp,
                record.getFlow());
    }

    @Override
    protected SampleTableRecord mapDomain(Sample sample) {
        final SampleTableRecord record = dsl.newRecord(table);
        record.setId(sample.sampleId().getId());
        record.setCountry(country(sample.getStationId().getCountry()));
        record.setStationid(sample.getStationId().getExternalId());
        record.setTimestamp(sample.getTimestamp());
        record.setTemperature(sample.getTemperature().map(Double::floatValue).orElse(null));
        record.setFlow(sample.getFlow());
        return record;
    }

    @Override
    protected Sample mapEntity(com.aa.msw.gen.jooq.tables.pojos.SampleTable sampleTable) {
        Float temperature = sampleTable.getTemperature();
        return new Sample(
                new SampleId(sampleTable.getId()),
                apiStationId(sampleTable.getCountry(), sampleTable.getStationid()),
                sampleTable.getTimestamp(),
                temperature == null ? Optional.empty() : Optional.of(temperature.doubleValue()),
                sampleTable.getFlow()
        );
    }

    @Override
    public Sample getCurrentSample(ApiStationId stationId) throws NoDataAvailableException {
        return dsl.selectFrom(TABLE)
                .where(TABLE.COUNTRY.eq(country(stationId.getCountry()))
                        .and(TABLE.STATIONID.eq(stationId.getExternalId())))
                .orderBy(TABLE.TIMESTAMP.desc())
                .limit(1)
                .fetchOptional(this::mapRecord)
                .orElseThrow(() -> new NoDataAvailableException("No current sample found for station " + stationId.getExternalId() + " in " + stationId.getCountry().getValue()));
    }

    @Override
    @Transactional
    public void persistSamplesIfNotExist(List<Sample> samples) {
        for (Sample sample : samples) {
            SampleTableRecord record = mapDomain(sample);
            dsl.insertInto(TABLE)
                    .set(record)
                    .onConflict(TABLE.TIMESTAMP, TABLE.STATIONID)
                    .doNothing()
                    .execute();
        }
    }
}

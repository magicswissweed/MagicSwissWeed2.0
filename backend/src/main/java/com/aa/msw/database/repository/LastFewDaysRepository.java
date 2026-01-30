package com.aa.msw.database.repository;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.LastFewDaysId;
import com.aa.msw.database.repository.dao.LastFewDaysDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.tables.LastFewDaysSamplesTable;
import com.aa.msw.gen.jooq.tables.daos.LastFewDaysSamplesTableDao;
import com.aa.msw.gen.jooq.tables.records.LastFewDaysSamplesTableRecord;
import com.aa.msw.model.LastFewDays;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.aa.msw.database.helpers.EnumConverterHelper.apiStationId;
import static com.aa.msw.database.helpers.EnumConverterHelper.country;

@Component
public class LastFewDaysRepository extends AbstractRepository
        <LastFewDaysId, LastFewDays, LastFewDaysSamplesTableRecord, com.aa.msw.gen.jooq.tables.pojos.LastFewDaysSamplesTable, LastFewDaysSamplesTableDao>
        implements LastFewDaysDao {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final LastFewDaysSamplesTable TABLE = LastFewDaysSamplesTable.LAST_FEW_DAYS_SAMPLES_TABLE;

    public LastFewDaysRepository(final DSLContext dsl) {
        super(dsl, new LastFewDaysSamplesTableDao(dsl.configuration()), TABLE, TABLE.DB_ID);
    }

    private static Map<OffsetDateTime, Double> jsonbToOrderedMap(JSONB jsonb) {
        if (jsonb == null) {
            return Collections.emptyMap();
        }
        try {
            LinkedHashMap<String, Double> tempMap = objectMapper.readValue(jsonb.data(), new TypeReference<>() {
            });

            return tempMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> OffsetDateTime.parse(e.getKey()), // Convert key
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSONB to Map<OffsetDateTime, Double>", e);
        }
    }

    private static JSONB orderedMapToJsonb(Map<OffsetDateTime, Double> map) {
        if (map == null || map.isEmpty()) {
            return JSONB.valueOf("{}");
        }
        try {
            LinkedHashMap<String, Double> tempMap = map.entrySet().stream()
                    .filter(e -> e.getKey() != null)
                    .filter(e -> e.getValue() != null)
                    .collect(Collectors.toMap(
                            e -> e.getKey().toString(),
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
            return JSONB.valueOf(objectMapper.writeValueAsString(tempMap));
        } catch (Exception e) {
            throw new RuntimeException("Error converting Map<OffsetDateTime, Double> to JSONB", e);
        }
    }


    @Override
    protected LastFewDays mapRecord(LastFewDaysSamplesTableRecord record) {
        return new LastFewDays(
                new LastFewDaysId(record.getDbId()),
                apiStationId(record.getCountry(), record.getStationId()),
                jsonbToOrderedMap(record.getLastfewdayssamples())
        );
    }

    @Override
    protected LastFewDaysSamplesTableRecord mapDomain(LastFewDays lastFewDays) {
        final LastFewDaysSamplesTableRecord record = dsl.newRecord(table);

        record.setDbId(lastFewDays.getDatabaseId().getId());
        record.setCountry(country(lastFewDays.getStationId().getCountry()));
        record.setStationId(lastFewDays.getStationId().getExternalId());
        record.setLastfewdayssamples(orderedMapToJsonb(lastFewDays.getLastFewDaysSamples()));

        return record;
    }

    @Override
    protected LastFewDays mapEntity(com.aa.msw.gen.jooq.tables.pojos.LastFewDaysSamplesTable lastFewDaysSamplesTable
    ) {
        return new LastFewDays(
                new LastFewDaysId(lastFewDaysSamplesTable.getDbId()),
                apiStationId(lastFewDaysSamplesTable.getCountry(), lastFewDaysSamplesTable.getStationId()),
                jsonbToOrderedMap(lastFewDaysSamplesTable.getLastfewdayssamples())
        );
    }

    @Override
    @Transactional
    public void persistLastFewDaysSamples(Set<LastFewDays> fetchedLastFewDaysSamples) {
        fetchedLastFewDaysSamples.stream()
                // sorting the collection to ensure consistent lock acquisition order and prevent deadlocks
                .sorted(Comparator.comparing((LastFewDays l) -> l.stationId().getCountry().toString())
                        .thenComparing(l -> l.stationId().getExternalId()))
                .forEach(lastFewDays -> {
                    deleteIfExists(lastFewDays.stationId());
                    insert(lastFewDays);
                });
    }

    @Override
    public LastFewDays getForStation(ApiStationId stationId) throws NoDataAvailableException {
        return dsl.selectFrom(TABLE)
                .where(TABLE.COUNTRY.eq(country(stationId.getCountry()))
                        .and(TABLE.STATION_ID.eq(stationId.getExternalId())))
                .limit(1)
                .fetchOptional(this::mapRecord)
                .orElseThrow(() -> new NoDataAvailableException("No current LastFewDaysSamples found for station " + stationId.getExternalId() + " in " + stationId.getCountry().getValue()));
    }

    private void deleteIfExists(ApiStationId stationId) {
        dsl.deleteFrom(TABLE)
                .where(TABLE.COUNTRY.eq(country(stationId.getCountry()))
                        .and(TABLE.STATION_ID.eq(stationId.getExternalId())))
                .execute();
    }
}

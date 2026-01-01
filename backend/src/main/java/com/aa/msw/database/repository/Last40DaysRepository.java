package com.aa.msw.database.repository;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.Last40DaysId;
import com.aa.msw.database.repository.dao.Last40DaysDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.tables.Last_40DaysSamplesTable;
import com.aa.msw.gen.jooq.tables.daos.Last_40DaysSamplesTableDao;
import com.aa.msw.gen.jooq.tables.records.Last_40DaysSamplesTableRecord;
import com.aa.msw.model.Last40Days;
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
public class Last40DaysRepository extends AbstractRepository
        <Last40DaysId, Last40Days, Last_40DaysSamplesTableRecord, com.aa.msw.gen.jooq.tables.pojos.Last_40DaysSamplesTable, Last_40DaysSamplesTableDao>
        implements Last40DaysDao {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Last_40DaysSamplesTable TABLE = Last_40DaysSamplesTable.LAST_40_DAYS_SAMPLES_TABLE;

    public Last40DaysRepository(final DSLContext dsl) {
        super(dsl, new Last_40DaysSamplesTableDao(dsl.configuration()), TABLE, TABLE.DB_ID);
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
    protected Last40Days mapRecord(Last_40DaysSamplesTableRecord record) {
        return new Last40Days(
                new Last40DaysId(record.getDbId()),
                apiStationId(record.getCountry(), record.getStationId()),
                jsonbToOrderedMap(record.getLast40dayssamples())
        );
    }

    @Override
    protected Last_40DaysSamplesTableRecord mapDomain(Last40Days last40Days) {
        final Last_40DaysSamplesTableRecord record = dsl.newRecord(table);

        record.setDbId(last40Days.getDatabaseId().getId());
        record.setCountry(country(last40Days.getStationId().getCountry()));
        record.setStationId(last40Days.getStationId().getExternalId());
        record.setLast40dayssamples(orderedMapToJsonb(last40Days.getLast40DaysSamples()));

        return record;
    }

    @Override
    protected Last40Days mapEntity(com.aa.msw.gen.jooq.tables.pojos.Last_40DaysSamplesTable last40DaysSamplesTable
    ) {
        return new Last40Days(
                new Last40DaysId(last40DaysSamplesTable.getDbId()),
                apiStationId(last40DaysSamplesTable.getCountry(), last40DaysSamplesTable.getStationId()),
                jsonbToOrderedMap(last40DaysSamplesTable.getLast40dayssamples())
        );
    }

    @Override
    @Transactional
    public void persistLast40DaysSamples(Set<Last40Days> fetchedLast40DaysSamples) {
        fetchedLast40DaysSamples.stream()
                // sorting the collection to ensure consistent lock acquisition order and prevent deadlocks
                .sorted(Comparator.comparing((Last40Days l) -> l.stationId().getCountry().toString())
                        .thenComparing(l -> l.stationId().getExternalId()))
                .forEach(last40Days -> {
                    deleteIfExists(last40Days.stationId());
                    insert(last40Days);
                });
    }

    @Override
    public Last40Days getForStation(ApiStationId stationId) throws NoDataAvailableException {
        return dsl.selectFrom(TABLE)
                .where(TABLE.COUNTRY.eq(country(stationId.getCountry()))
                        .and(TABLE.STATION_ID.eq(stationId.getExternalId())))
                .limit(1)
                .fetchOptional(this::mapRecord)
                .orElseThrow(() -> new NoDataAvailableException("No current Last40DaysSamples found for station " + stationId.getExternalId() + " in " + stationId.getCountry().getValue()));
    }

    private void deleteIfExists(ApiStationId stationId) {
        dsl.deleteFrom(TABLE)
                .where(TABLE.COUNTRY.eq(country(stationId.getCountry()))
                        .and(TABLE.STATION_ID.eq(stationId.getExternalId())))
                .execute();
    }
}

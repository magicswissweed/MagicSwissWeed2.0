package com.aa.msw.database.repository;

import com.aa.msw.database.helpers.id.HistoricalYearsDataId;
import com.aa.msw.database.repository.dao.HistoricalYearsDataDao;
import com.aa.msw.gen.jooq.tables.HistoricalYearsDataTable;
import com.aa.msw.gen.jooq.tables.daos.HistoricalYearsDataTableDao;
import com.aa.msw.gen.jooq.tables.records.HistoricalYearsDataTableRecord;
import com.aa.msw.model.HistoricalYearsData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.aa.msw.database.helpers.EnumConverterHelper.apiStationId;
import static com.aa.msw.database.helpers.EnumConverterHelper.country;

@Component
public class HistoricalYearsDataRepository extends AbstractRepository
        <HistoricalYearsDataId, HistoricalYearsData, HistoricalYearsDataTableRecord, com.aa.msw.gen.jooq.tables.pojos.HistoricalYearsDataTable, HistoricalYearsDataTableDao>
        implements HistoricalYearsDataDao {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final HistoricalYearsDataTable TABLE = HistoricalYearsDataTable.HISTORICAL_YEARS_DATA_TABLE;

    public HistoricalYearsDataRepository(final DSLContext dsl) {
        super(dsl, new HistoricalYearsDataTableDao(dsl.configuration()), TABLE, TABLE.DB_ID);
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
    protected HistoricalYearsData mapRecord(HistoricalYearsDataTableRecord record) {
        return new HistoricalYearsData(
                new HistoricalYearsDataId(record.getDbId()),
                apiStationId(record.getCountry(), record.getStationId()),
                jsonbToOrderedMap(record.getMedian()),
                jsonbToOrderedMap(record.getTwentyFivePercentile()),
                jsonbToOrderedMap(record.getSeventyFivePercentile()),
                jsonbToOrderedMap(record.getMin()),
                jsonbToOrderedMap(record.getMax()),
                jsonbToOrderedMap(record.getCurrentYear())
        );
    }

    @Override
    protected HistoricalYearsDataTableRecord mapDomain(HistoricalYearsData historicalYearsData) {
        final HistoricalYearsDataTableRecord record = dsl.newRecord(table);

        record.setDbId(historicalYearsData.getDatabaseId().getId());
        record.setCountry(country(historicalYearsData.getStationId().getCountry()));
        record.setStationId(historicalYearsData.getStationId().getExternalId());
        record.setMedian(orderedMapToJsonb(historicalYearsData.getMedian()));
        record.setTwentyFivePercentile(orderedMapToJsonb(historicalYearsData.getTwentyFivePercentile()));
        record.setSeventyFivePercentile(orderedMapToJsonb(historicalYearsData.getSeventyFivePercentile()));
        record.setMin(orderedMapToJsonb(historicalYearsData.getMin()));
        record.setMax(orderedMapToJsonb(historicalYearsData.getMax()));
        record.setCurrentYear(orderedMapToJsonb(historicalYearsData.getCurrentYear()));
        return record;
    }

    @Override
    protected HistoricalYearsData mapEntity(com.aa.msw.gen.jooq.tables.pojos.HistoricalYearsDataTable historicalYearsDataTable) {
        return new HistoricalYearsData(
                new HistoricalYearsDataId(historicalYearsDataTable.getDbId()),
                apiStationId(historicalYearsDataTable.getCountry(), historicalYearsDataTable.getStationId()),
                jsonbToOrderedMap(historicalYearsDataTable.getMedian()),
                jsonbToOrderedMap(historicalYearsDataTable.getTwentyFivePercentile()),
                jsonbToOrderedMap(historicalYearsDataTable.getSeventyFivePercentile()),
                jsonbToOrderedMap(historicalYearsDataTable.getMin()),
                jsonbToOrderedMap(historicalYearsDataTable.getMax()),
                jsonbToOrderedMap(historicalYearsDataTable.getCurrentYear())
        );
    }

    @Override
    public Set<HistoricalYearsData> getAllHistoricalYearsData() {
        return dsl.selectFrom(TABLE)
                .fetchSet(this::mapRecord);
    }

    @Override
    public void deleteAll() {
        dsl.deleteFrom(TABLE)
                .execute();
    }
}

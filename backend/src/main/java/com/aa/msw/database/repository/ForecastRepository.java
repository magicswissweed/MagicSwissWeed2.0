package com.aa.msw.database.repository;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.ForecastId;
import com.aa.msw.database.repository.dao.ForecastDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.tables.ForecastTable;
import com.aa.msw.gen.jooq.tables.daos.ForecastTableDao;
import com.aa.msw.gen.jooq.tables.records.ForecastTableRecord;
import com.aa.msw.model.Forecast;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.aa.msw.database.helpers.EnumConverterHelper.apiStationId;
import static com.aa.msw.database.helpers.EnumConverterHelper.country;


@Component
public class ForecastRepository extends AbstractTimestampedRepository
        <ForecastId, Forecast, ForecastTableRecord, com.aa.msw.gen.jooq.tables.pojos.ForecastTable, ForecastTableDao>
        implements ForecastDao {
    private static final Logger LOG = LoggerFactory.getLogger(ForecastRepository.class);

    private static final ForecastTable TABLE = ForecastTable.FORECAST_TABLE;

    public ForecastRepository(final DSLContext dsl) {
        super(dsl, new ForecastTableDao(dsl.configuration()), TABLE, TABLE.ID, TABLE.TIMESTAMP);
    }

    private static JSONB toJsonB(Map<OffsetDateTime, Double> data) throws JsonProcessingException {
        return JSONB.valueOf(new ObjectMapper().writeValueAsString(data));
    }

    private static Map<OffsetDateTime, Double> jsonbToMap(JSONB data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(data.data(), new TypeReference<>() {
        });
    }

    @Override
    protected Forecast mapRecord(ForecastTableRecord record) {
        return getForecast(
                record.getId(),
                apiStationId(record.getCountry(), record.getStationid()),
                record.getTimestamp().withOffsetSameInstant(ZoneOffset.UTC),
                record.getMeasureddata(),
                record.getMedian(),
                record.getTwentyfivepercentile(),
                record.getSeventyfivepercentile(),
                record.getMin(),
                record.getMax());
    }

    @Override
    protected ForecastTableRecord mapDomain(Forecast forecast) {
        final ForecastTableRecord record = dsl.newRecord(table);

        JSONB measuredData;
        JSONB median;
        JSONB twentyFivePercentile;
        JSONB seventyFivePercentile;
        JSONB min;
        JSONB max;
        try {
            measuredData = toJsonB(forecast.getMeasuredData());
            median = toJsonB(forecast.getMedian());
            twentyFivePercentile = toJsonB(forecast.getTwentyFivePercentile());
            seventyFivePercentile = toJsonB(forecast.getSeventyFivePercentile());
            min = toJsonB(forecast.getMin());
            max = toJsonB(forecast.getMax());
        } catch (JsonProcessingException e) {
            LOG.error("Error mapping forecast", e);
            return null;
        }

        record.setId(forecast.forecastId().getId());
        record.setCountry(country(forecast.getStationId().getCountry()));
        record.setStationid(forecast.getStationId().getExternalId());
        record.setTimestamp(forecast.getTimestamp());
        record.setMeasureddata(measuredData);
        record.setMedian(median);
        record.setTwentyfivepercentile(twentyFivePercentile);
        record.setSeventyfivepercentile(seventyFivePercentile);
        record.setMin(min);
        record.setMax(max);
        return record;
    }

    @Override
    protected Forecast mapEntity(com.aa.msw.gen.jooq.tables.pojos.ForecastTable forecastTable) {
        return getForecast(
                forecastTable.getId(),
                apiStationId(forecastTable.getCountry(), forecastTable.getStationid()),
                forecastTable.getTimestamp(),
                forecastTable.getMeasureddata(),
                forecastTable.getMedian(),
                forecastTable.getTwentyfivepercentile(),
                forecastTable.getSeventyfivepercentile(),
                forecastTable.getMin(),
                forecastTable.getMax());
    }

    private Forecast getForecast(UUID forecastId, ApiStationId stationid, OffsetDateTime timestamp, JSONB jsonMeasured, JSONB jsonMedian, JSONB jsonTwentyFivePercentile, JSONB jsonSeventyFivePercentile, JSONB jsonMin, JSONB jsonMax) {
        Map<OffsetDateTime, Double> measuredData;
        Map<OffsetDateTime, Double> median;
        Map<OffsetDateTime, Double> twentyFivePercentile;
        Map<OffsetDateTime, Double> seventyFivePercentile;
        Map<OffsetDateTime, Double> min;
        Map<OffsetDateTime, Double> max;
        try {
            measuredData = jsonbToMap(jsonMeasured);
            median = jsonbToMap(jsonMedian);
            twentyFivePercentile = jsonbToMap(jsonTwentyFivePercentile);
            seventyFivePercentile = jsonbToMap(jsonSeventyFivePercentile);
            min = jsonbToMap(jsonMin);
            max = jsonbToMap(jsonMax);
        } catch (JsonProcessingException e) {
            LOG.error("Error getting Forecast", e);
            return null;
        }

        return new Forecast(
                new ForecastId(forecastId),
                stationid,
                timestamp,
                measuredData,
                median,
                twentyFivePercentile,
                seventyFivePercentile,
                min,
                max);
    }

    @Override
    public Forecast getCurrentForecast(ApiStationId stationId) throws NoDataAvailableException {
        return dsl.selectFrom(TABLE)
                .where(TABLE.COUNTRY.eq(country(stationId.getCountry()))
                        .and(TABLE.STATIONID.eq(stationId.getExternalId())))
                .orderBy(TABLE.TIMESTAMP.desc())
                .limit(1)
                .fetchOptional(this::mapRecord)
                .orElseThrow(() -> new NoDataAvailableException("No current Forecast found for station " + stationId));
    }

    @Override
    @Transactional
    public void persistForecastsIfNotExist(List<Forecast> forecasts) {
        for (Forecast forecast : forecasts) {
            ForecastTableRecord record = mapDomain(forecast);
            dsl.insertInto(TABLE)
                    .set(record)
                    .onConflict(TABLE.TIMESTAMP, TABLE.STATIONID)
                    .doNothing()
                    .execute();
        }
    }
}

package com.aa.msw.integrationtest.sample;

import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.integrationtest.IntegrationTest;
import com.aa.msw.model.Country;
import com.aa.msw.model.Sample;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SampleRepositoryLatestSampleTest extends IntegrationTest {

    private static final ApiStationId FLOW_AND_TEMPERATURE = new ApiStationId(Country.CH, "latest-1");
    private static final ApiStationId HEIGHT_ONLY = new ApiStationId(Country.FR, "latest-2");
    private static final ApiStationId WITHOUT_SAMPLES = new ApiStationId(Country.DE, "latest-3");
    /**
     * Same external id as {@link #FLOW_AND_TEMPERATURE} but a different country - its samples must stay separate.
     */
    private static final ApiStationId SAME_EXTERNAL_ID_OTHER_COUNTRY = new ApiStationId(Country.DE, "latest-1");
    /**
     * Has samples but is not asked for - must not show up in the result.
     */
    private static final ApiStationId NOT_REQUESTED = new ApiStationId(Country.CH, "latest-4");

    @Autowired
    private SampleDao sampleDao;

    @Test
    public void shouldReturnNewestSamplePerRequestedStationAndMeasurementType() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
        sampleDao.persistSamplesIfNotExist(List.of(
                // inserted out of order on purpose: the newest sample is neither first nor last
                new Sample(new SampleId(), FLOW_AND_TEMPERATURE, now.minusHours(3), 30, ApiMeasurementType.FLOW),
                new Sample(new SampleId(), FLOW_AND_TEMPERATURE, now.minusHours(1), 10, ApiMeasurementType.FLOW),
                new Sample(new SampleId(), FLOW_AND_TEMPERATURE, now.minusHours(2), 20, ApiMeasurementType.FLOW),
                new Sample(new SampleId(), FLOW_AND_TEMPERATURE, now.minusHours(1), 17.5, ApiMeasurementType.TEMPERATURE),
                new Sample(new SampleId(), HEIGHT_ONLY, now.minusDays(2), 1.25, ApiMeasurementType.HEIGHT),
                new Sample(new SampleId(), HEIGHT_ONLY, now.minusDays(1), 1.5, ApiMeasurementType.HEIGHT),
                new Sample(new SampleId(), SAME_EXTERNAL_ID_OTHER_COUNTRY, now.minusMinutes(5), 99, ApiMeasurementType.FLOW),
                new Sample(new SampleId(), NOT_REQUESTED, now.minusMinutes(5), 42, ApiMeasurementType.FLOW)
        ));

        Map<ApiStationId, Map<ApiMeasurementType, Sample>> latest = sampleDao.getLatestSamplePerStationAndType(
                Set.of(FLOW_AND_TEMPERATURE, HEIGHT_ONLY, WITHOUT_SAMPLES, SAME_EXTERNAL_ID_OTHER_COUNTRY));

        Map<ApiMeasurementType, Sample> chSamples = latest.get(FLOW_AND_TEMPERATURE);
        assertEquals(Set.of(ApiMeasurementType.FLOW, ApiMeasurementType.TEMPERATURE), chSamples.keySet());
        assertEquals(10, chSamples.get(ApiMeasurementType.FLOW).getValue());
        assertEquals(now.minusHours(1), chSamples.get(ApiMeasurementType.FLOW).getTimestamp());
        assertEquals(17.5, chSamples.get(ApiMeasurementType.TEMPERATURE).getValue());

        Map<ApiMeasurementType, Sample> frSamples = latest.get(HEIGHT_ONLY);
        assertEquals(Set.of(ApiMeasurementType.HEIGHT), frSamples.keySet());
        assertEquals(1.5, frSamples.get(ApiMeasurementType.HEIGHT).getValue());

        Map<ApiMeasurementType, Sample> deSamples = latest.get(SAME_EXTERNAL_ID_OTHER_COUNTRY);
        assertEquals(Set.of(ApiMeasurementType.FLOW), deSamples.keySet());
        assertEquals(99, deSamples.get(ApiMeasurementType.FLOW).getValue());

        assertFalse(latest.containsKey(WITHOUT_SAMPLES));
        assertFalse(latest.containsKey(NOT_REQUESTED));
    }

    @Test
    public void shouldReturnEmptyMapForNoStations() {
        assertTrue(sampleDao.getLatestSamplePerStationAndType(Set.of()).isEmpty());
    }
}

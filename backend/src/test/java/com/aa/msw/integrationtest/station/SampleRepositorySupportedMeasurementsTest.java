package com.aa.msw.integrationtest.station;

import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.database.helpers.id.StationId;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.database.repository.dao.StationDao;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.enums.Provider;
import com.aa.msw.integrationtest.IntegrationTest;
import com.aa.msw.model.Country;
import com.aa.msw.model.Sample;
import com.aa.msw.model.Station;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SampleRepositorySupportedMeasurementsTest extends IntegrationTest {

    private static final ApiStationId FLOW_AND_HEIGHT = new ApiStationId(Country.CH, "supported-1");
    private static final ApiStationId HEIGHT_ONLY = new ApiStationId(Country.FR, "supported-2");
    private static final ApiStationId WITHOUT_SAMPLES = new ApiStationId(Country.DE, "supported-3");
    /**
     * Same external id as {@link #FLOW_AND_HEIGHT} but a different country - must not inherit its measurement types.
     */
    private static final ApiStationId SAME_EXTERNAL_ID_OTHER_COUNTRY = new ApiStationId(Country.DE, "supported-1");

    @Autowired
    private StationDao stationDao;
    @Autowired
    private SampleDao sampleDao;

    @Test
    public void shouldListMeasurementTypesThatHaveSamplesPerStation() {
        stationDao.persist(station(FLOW_AND_HEIGHT, Provider.HYDRODATEN));
        stationDao.persist(station(HEIGHT_ONLY, Provider.VIGICRUES));
        stationDao.persist(station(WITHOUT_SAMPLES, Provider.HVZ_BW));
        stationDao.persist(station(SAME_EXTERNAL_ID_OTHER_COUNTRY, Provider.HVZ_BW));

        OffsetDateTime now = OffsetDateTime.now();
        sampleDao.persistSamplesIfNotExist(List.of(
                new Sample(new SampleId(), FLOW_AND_HEIGHT, now.minusDays(200), 10, ApiMeasurementType.FLOW),
                new Sample(new SampleId(), FLOW_AND_HEIGHT, now.minusHours(1), 11, ApiMeasurementType.FLOW),
                new Sample(new SampleId(), FLOW_AND_HEIGHT, now.minusHours(1), 1.5, ApiMeasurementType.HEIGHT),
                new Sample(new SampleId(), HEIGHT_ONLY, now.minusHours(2), 2.5, ApiMeasurementType.HEIGHT)
        ));

        Map<ApiStationId, Set<ApiMeasurementType>> supported = sampleDao.getSupportedMeasurementsByStation();

        assertEquals(Set.of(ApiMeasurementType.FLOW, ApiMeasurementType.HEIGHT), supported.get(FLOW_AND_HEIGHT));
        assertEquals(Set.of(ApiMeasurementType.HEIGHT), supported.get(HEIGHT_ONLY));
        assertFalse(supported.containsKey(WITHOUT_SAMPLES));
        assertFalse(supported.containsKey(SAME_EXTERNAL_ID_OTHER_COUNTRY));
    }

    private static Station station(ApiStationId id, Provider provider) {
        return new Station(new StationId(), id, "Station " + id.getExternalId(), 47.0, 8.0, provider, null, null);
    }
}

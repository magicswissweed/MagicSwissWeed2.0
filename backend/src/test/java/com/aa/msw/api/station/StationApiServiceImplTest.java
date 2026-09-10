package com.aa.msw.api.station;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.database.helpers.id.StationId;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.database.repository.dao.StationDao;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.enums.Provider;
import com.aa.msw.model.Sample;
import com.aa.msw.model.Station;
import com.aa.msw.source.french.vigicrues.historical.lastThirty.FrenchLast30DaysSampleFetchService;
import com.aa.msw.source.french.vigicrues.stations.FrenchStationFetchService;
import com.aa.msw.source.german.bw.sample.BwSampleFetchService;
import com.aa.msw.source.german.bw.stations.DeBwStationFetchService;
import com.aa.msw.source.rivermap.sample.RivermapSampleFetchService;
import com.aa.msw.source.rivermap.stations.RivermapStationFetchService;
import com.aa.msw.source.swiss.existenz.sample.SwissSampleFetchService;
import com.aa.msw.source.swiss.hydrodaten.stations.SwissStationFetchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StationApiServiceImplTest {

    private static final Station FRESH = rivermapStation("fresh");
    private static final Station STALE = rivermapStation("stale");
    private static final Station SILENT = rivermapStation("silent");

    private final StationDao stationDao = mock(StationDao.class);
    private final SampleDao sampleDao = mock(SampleDao.class);
    private final RivermapStationFetchService rivermapStationFetchService = mock(RivermapStationFetchService.class);
    private final RivermapSampleFetchService rivermapSampleFetchService = mock(RivermapSampleFetchService.class);

    private final StationApiServiceImpl service = new StationApiServiceImpl(
            mock(SwissStationFetchService.class), stationDao, mock(FrenchStationFetchService.class),
            mock(DeBwStationFetchService.class), rivermapStationFetchService, sampleDao,
            mock(SwissSampleFetchService.class), mock(FrenchLast30DaysSampleFetchService.class),
            mock(BwSampleFetchService.class), rivermapSampleFetchService);

    @BeforeEach
    void setUp() throws NoDataAvailableException {
        // no samples in the db -> validity is decided by the readings answer only
        when(sampleDao.getCurrentSample(any(), any())).thenThrow(new NoDataAvailableException("no sample"));
        when(stationDao.getStations()).thenReturn(Set.of());
        when(rivermapStationFetchService.fetchStations()).thenReturn(new HashSet<>(Set.of(FRESH, STALE, SILENT)));
    }

    @Test
    void shouldOnlyKeepRivermapStationsWithARecentReading() {
        OffsetDateTime now = OffsetDateTime.now();
        when(rivermapSampleFetchService.fetchSamples(anySet(), eq(RivermapSampleFetchService.MAX_WINDOW_MINUTES)))
                .thenReturn(List.of(
                        sample(FRESH, now.minusHours(1)),
                        // the api answers with the latest reading of a station even if it lies outside the window
                        sample(STALE, now.minus(StationApiServiceImpl.RIVERMAP_MAX_READING_AGE).minusDays(1))));

        service.fetchStationsAndSaveToDb();

        verify(stationDao).persist(FRESH);
        verify(stationDao, never()).persist(STALE);
        verify(stationDao, never()).persist(SILENT);
        verify(stationDao).deleteByStationId(STALE.stationId());
        verify(stationDao).deleteByStationId(SILENT.stationId());
        verify(stationDao, never()).deleteByStationId(FRESH.stationId());
    }

    @Test
    void shouldKeepAllRivermapStationsWhenTheReadingsRequestFailed() {
        when(rivermapSampleFetchService.fetchSamples(anySet(), anyInt())).thenReturn(List.of());

        service.fetchStationsAndSaveToDb();

        verify(stationDao).persist(FRESH);
        verify(stationDao).persist(STALE);
        verify(stationDao).persist(SILENT);
        verify(stationDao, never()).deleteByStationId(any());
    }

    private static Station rivermapStation(String externalId) {
        return new Station(new StationId(), new ApiStationId("AT", externalId), externalId, 47.0, 13.0, Provider.RIVERMAP, null, null);
    }

    private static Sample sample(Station station, OffsetDateTime timestamp) {
        return new Sample(new SampleId(), station.stationId(), timestamp, 1.0, ApiMeasurementType.FLOW);
    }
}

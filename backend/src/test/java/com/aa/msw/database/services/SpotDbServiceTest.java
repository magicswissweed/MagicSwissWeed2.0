package com.aa.msw.database.services;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.UserToSpot;
import com.aa.msw.database.helpers.id.*;
import com.aa.msw.database.repository.dao.*;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.*;
import com.aa.msw.notifications.NotificationSpotInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotDbServiceTest {

    @Mock
    private SpotDao spotDao;
    @Mock
    private SpotCurrentInfoDao spotCurrentInfoDao;
    @Mock
    private UserToSpotDao userToSpotDao;
    @Mock
    private SampleDao sampleDao;
    @Mock
    private ForecastDao forecastDao;

    @InjectMocks
    private SpotDbService spotDbService;

    private static final ApiStationId STATION_CH_2018 = new ApiStationId(CountryEnum.CH, "2018");
    private static final ApiStationId STATION_CH_2243 = new ApiStationId(CountryEnum.CH, "2243");

    private static Spot createSpot(ApiStationId stationId, int minFlow, int maxFlow) {
        return new Spot(new SpotId(), true, SpotTypeEnum.RIVER_SURF, "Test Spot", stationId, minFlow, maxFlow);
    }

    private static Sample createSample(ApiStationId stationId, int flow) {
        return new Sample(new SampleId(), stationId, OffsetDateTime.now(), Optional.of(15.0), flow);
    }

    private static Forecast createForecast(ApiStationId stationId, double minValue, double maxValue) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Forecast(
                new ForecastId(),
                stationId,
                now,
                Map.of(now, 100.0),
                Map.of(now, 100.0),
                Map.of(now, 100.0),
                Map.of(now, 100.0),
                Map.of(now, maxValue),
                Map.of(now, minValue)
        );
    }

    private void stubCurrentInfo(Spot spot, FlowStatusEnum status) {
        lenient().when(spotCurrentInfoDao.get(spot.spotId()))
                .thenReturn(new SpotCurrentInfo(new SpotCurrentInfoId(), spot.spotId(), status));
    }

    private void stubNoCurrentInfo(Spot spot) {
        lenient().when(spotCurrentInfoDao.get(spot.spotId())).thenReturn(null);
    }

    @Nested
    class FlowStatusDetermination {

        @Test
        void shouldReturnGoodWhenFlowIsInSurfableRange() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 100));
            stubCurrentInfo(spot, FlowStatusEnum.GOOD);

            spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            verify(spotCurrentInfoDao).updateCurrentInfo(spot.spotId(), FlowStatusEnum.GOOD);
        }

        @Test
        void shouldReturnBadWhenFlowIsBelowSurfableRange() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 50));
            when(forecastDao.getCurrentForecast(STATION_CH_2018)).thenReturn(createForecast(STATION_CH_2018, 30, 60));
            stubCurrentInfo(spot, FlowStatusEnum.BAD);

            spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            verify(spotCurrentInfoDao).updateCurrentInfo(spot.spotId(), FlowStatusEnum.BAD);
        }

        @Test
        void shouldReturnBadWhenFlowIsAboveSurfableRange() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 300));
            when(forecastDao.getCurrentForecast(STATION_CH_2018)).thenReturn(createForecast(STATION_CH_2018, 250, 400));
            stubCurrentInfo(spot, FlowStatusEnum.BAD);

            spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            verify(spotCurrentInfoDao).updateCurrentInfo(spot.spotId(), FlowStatusEnum.BAD);
        }

        @Test
        void shouldReturnTendencyWhenForecastMinIsInRange() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 50));
            when(forecastDao.getCurrentForecast(STATION_CH_2018)).thenReturn(createForecast(STATION_CH_2018, 90, 250));
            stubCurrentInfo(spot, FlowStatusEnum.BAD);

            spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            verify(spotCurrentInfoDao).updateCurrentInfo(spot.spotId(), FlowStatusEnum.TENDENCY_TO_BECOME_GOOD);
        }

        @Test
        void shouldReturnTendencyWhenForecastMaxIsInRange() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 50));
            when(forecastDao.getCurrentForecast(STATION_CH_2018)).thenReturn(createForecast(STATION_CH_2018, 50, 100));
            stubCurrentInfo(spot, FlowStatusEnum.BAD);

            spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            verify(spotCurrentInfoDao).updateCurrentInfo(spot.spotId(), FlowStatusEnum.TENDENCY_TO_BECOME_GOOD);
        }

        @Test
        void shouldReturnTendencyWhenForecastSpansEntireSurfableRange() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 50));
            when(forecastDao.getCurrentForecast(STATION_CH_2018)).thenReturn(createForecast(STATION_CH_2018, 50, 250));
            stubCurrentInfo(spot, FlowStatusEnum.BAD);

            spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            verify(spotCurrentInfoDao).updateCurrentInfo(spot.spotId(), FlowStatusEnum.TENDENCY_TO_BECOME_GOOD);
        }

        @Test
        void shouldReturnBadWhenNoSampleDataAvailable() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenThrow(new NoDataAvailableException("No data"));
            stubCurrentInfo(spot, FlowStatusEnum.BAD);

            spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            verify(spotCurrentInfoDao).updateCurrentInfo(spot.spotId(), FlowStatusEnum.BAD);
        }

        @Test
        void shouldReturnBadWhenFlowNotInRangeAndNoForecastAvailable() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 50));
            when(forecastDao.getCurrentForecast(STATION_CH_2018)).thenThrow(new NoDataAvailableException("No forecast"));
            stubCurrentInfo(spot, FlowStatusEnum.BAD);

            spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            verify(spotCurrentInfoDao).updateCurrentInfo(spot.spotId(), FlowStatusEnum.BAD);
        }

        @Test
        void shouldReturnBadWhenFlowIsExactlyAtMinBoundary() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 80));
            when(forecastDao.getCurrentForecast(STATION_CH_2018)).thenReturn(createForecast(STATION_CH_2018, 30, 60));
            stubCurrentInfo(spot, FlowStatusEnum.BAD);

            spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            verify(spotCurrentInfoDao).updateCurrentInfo(spot.spotId(), FlowStatusEnum.BAD);
        }

        @Test
        void shouldReturnBadWhenFlowIsExactlyAtMaxBoundary() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 200));
            when(forecastDao.getCurrentForecast(STATION_CH_2018)).thenReturn(createForecast(STATION_CH_2018, 30, 60));
            stubCurrentInfo(spot, FlowStatusEnum.BAD);

            spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            verify(spotCurrentInfoDao).updateCurrentInfo(spot.spotId(), FlowStatusEnum.BAD);
        }
    }

    @Nested
    class NotificationDetection {

        @Test
        void shouldDetectImprovementFromBadToGood() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            UserToSpot userToSpot = new UserToSpot(new UserToSpotId(), new UserId(), spot.spotId(), 0, true);

            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 100));
            stubCurrentInfo(spot, FlowStatusEnum.BAD);
            when(userToSpotDao.getUserToSpots(spot.spotId())).thenReturn(Set.of(userToSpot));

            Set<NotificationSpotInfo> improved = spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            assertEquals(1, improved.size());
            NotificationSpotInfo info = improved.iterator().next();
            assertEquals(spot, info.spot());
            assertEquals(FlowStatusEnum.GOOD, info.flowStatus());
        }

        @Test
        void shouldDetectImprovementFromBadToTendency() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            UserToSpot userToSpot = new UserToSpot(new UserToSpotId(), new UserId(), spot.spotId(), 0, true);

            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 50));
            when(forecastDao.getCurrentForecast(STATION_CH_2018)).thenReturn(createForecast(STATION_CH_2018, 90, 250));
            stubCurrentInfo(spot, FlowStatusEnum.BAD);
            when(userToSpotDao.getUserToSpots(spot.spotId())).thenReturn(Set.of(userToSpot));

            Set<NotificationSpotInfo> improved = spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            assertEquals(1, improved.size());
            assertEquals(FlowStatusEnum.TENDENCY_TO_BECOME_GOOD, improved.iterator().next().flowStatus());
        }

        @Test
        void shouldDetectImprovementFromTendencyToGood() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            UserToSpot userToSpot = new UserToSpot(new UserToSpotId(), new UserId(), spot.spotId(), 0, true);

            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 100));
            stubCurrentInfo(spot, FlowStatusEnum.TENDENCY_TO_BECOME_GOOD);
            when(userToSpotDao.getUserToSpots(spot.spotId())).thenReturn(Set.of(userToSpot));

            Set<NotificationSpotInfo> improved = spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            assertEquals(1, improved.size());
            assertEquals(FlowStatusEnum.GOOD, improved.iterator().next().flowStatus());
        }

        @Test
        void shouldNotDetectImprovementWhenGoodStaysGood() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 100));
            stubCurrentInfo(spot, FlowStatusEnum.GOOD);

            Set<NotificationSpotInfo> improved = spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            assertTrue(improved.isEmpty());
        }

        @Test
        void shouldNotDetectImprovementWhenBadStaysBad() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 50));
            when(forecastDao.getCurrentForecast(STATION_CH_2018)).thenReturn(createForecast(STATION_CH_2018, 30, 60));
            stubCurrentInfo(spot, FlowStatusEnum.BAD);

            Set<NotificationSpotInfo> improved = spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            assertTrue(improved.isEmpty());
        }

        @Test
        void shouldNotDetectImprovementWhenGoodBecomesBad() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 50));
            when(forecastDao.getCurrentForecast(STATION_CH_2018)).thenReturn(createForecast(STATION_CH_2018, 30, 60));
            stubCurrentInfo(spot, FlowStatusEnum.GOOD);

            Set<NotificationSpotInfo> improved = spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            assertTrue(improved.isEmpty());
        }

        @Test
        void shouldDetectImprovementWhenNoCurrentInfoExists() throws Exception {
            Spot spot = createSpot(STATION_CH_2018, 80, 200);
            UserToSpot userToSpot = new UserToSpot(new UserToSpotId(), new UserId(), spot.spotId(), 0, true);

            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 100));
            stubNoCurrentInfo(spot);
            when(userToSpotDao.getUserToSpots(spot.spotId())).thenReturn(Set.of(userToSpot));

            Set<NotificationSpotInfo> improved = spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            assertEquals(1, improved.size());
        }
    }

    @Nested
    class MultipleSpots {

        @Test
        void shouldUpdateAllSpotsForStation() throws Exception {
            Spot spot1 = createSpot(STATION_CH_2018, 80, 200);
            Spot spot2 = createSpot(STATION_CH_2018, 100, 300);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot1, spot2));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 150));
            stubCurrentInfo(spot1, FlowStatusEnum.GOOD);
            stubCurrentInfo(spot2, FlowStatusEnum.GOOD);

            spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            verify(spotCurrentInfoDao).updateCurrentInfo(spot1.spotId(), FlowStatusEnum.GOOD);
            verify(spotCurrentInfoDao).updateCurrentInfo(spot2.spotId(), FlowStatusEnum.GOOD);
        }

        @Test
        void shouldHandleDifferentStatusesForSpotsOnSameStation() throws Exception {
            Spot spotInRange = createSpot(STATION_CH_2018, 80, 200);
            Spot spotOutOfRange = createSpot(STATION_CH_2018, 300, 500);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spotInRange, spotOutOfRange));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 100));
            when(forecastDao.getCurrentForecast(STATION_CH_2018)).thenReturn(createForecast(STATION_CH_2018, 80, 150));
            stubCurrentInfo(spotInRange, FlowStatusEnum.GOOD);
            stubCurrentInfo(spotOutOfRange, FlowStatusEnum.BAD);

            spotDbService.updateCurrentInfoForAllSpotsOfStation(STATION_CH_2018);

            verify(spotCurrentInfoDao).updateCurrentInfo(spotInRange.spotId(), FlowStatusEnum.GOOD);
            verify(spotCurrentInfoDao).updateCurrentInfo(spotOutOfRange.spotId(), FlowStatusEnum.BAD);
        }
    }

    @Nested
    class MultipleStations {

        @Test
        void shouldUpdateSpotsAcrossMultipleStations() throws Exception {
            Spot spot1 = createSpot(STATION_CH_2018, 80, 200);
            Spot spot2 = createSpot(STATION_CH_2243, 50, 150);
            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot1));
            when(spotDao.getSpotsWithStationId(STATION_CH_2243)).thenReturn(Set.of(spot2));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 100));
            when(sampleDao.getCurrentSample(STATION_CH_2243)).thenReturn(createSample(STATION_CH_2243, 100));
            stubCurrentInfo(spot1, FlowStatusEnum.GOOD);
            stubCurrentInfo(spot2, FlowStatusEnum.GOOD);

            Set<NotificationSpotInfo> improved = spotDbService.updateCurrentInfoForAllSpotsOfStations(
                    Set.of(STATION_CH_2018, STATION_CH_2243));

            verify(spotCurrentInfoDao).updateCurrentInfo(spot1.spotId(), FlowStatusEnum.GOOD);
            verify(spotCurrentInfoDao).updateCurrentInfo(spot2.spotId(), FlowStatusEnum.GOOD);
        }

        @Test
        void shouldCollectImprovedSpotsFromAllStations() throws Exception {
            Spot spot1 = createSpot(STATION_CH_2018, 80, 200);
            Spot spot2 = createSpot(STATION_CH_2243, 50, 150);
            UserToSpot uts1 = new UserToSpot(new UserToSpotId(), new UserId(), spot1.spotId(), 0, true);
            UserToSpot uts2 = new UserToSpot(new UserToSpotId(), new UserId(), spot2.spotId(), 0, true);

            when(spotDao.getSpotsWithStationId(STATION_CH_2018)).thenReturn(Set.of(spot1));
            when(spotDao.getSpotsWithStationId(STATION_CH_2243)).thenReturn(Set.of(spot2));
            when(sampleDao.getCurrentSample(STATION_CH_2018)).thenReturn(createSample(STATION_CH_2018, 100));
            when(sampleDao.getCurrentSample(STATION_CH_2243)).thenReturn(createSample(STATION_CH_2243, 100));
            stubCurrentInfo(spot1, FlowStatusEnum.BAD);
            stubCurrentInfo(spot2, FlowStatusEnum.BAD);
            when(userToSpotDao.getUserToSpots(spot1.spotId())).thenReturn(Set.of(uts1));
            when(userToSpotDao.getUserToSpots(spot2.spotId())).thenReturn(Set.of(uts2));

            Set<NotificationSpotInfo> improved = spotDbService.updateCurrentInfoForAllSpotsOfStations(
                    Set.of(STATION_CH_2018, STATION_CH_2243));

            assertEquals(2, improved.size());
        }

        @Test
        void shouldReturnEmptyWhenNoStationsProvided() {
            Set<NotificationSpotInfo> improved = spotDbService.updateCurrentInfoForAllSpotsOfStations(Set.of());

            assertTrue(improved.isEmpty());
            verifyNoInteractions(spotDao, sampleDao, forecastDao);
        }
    }
}

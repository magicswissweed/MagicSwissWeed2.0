package com.aa.msw.database.services;

import com.aa.msw.auth.threadlocal.UserContext;
import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.UserToSpot;
import com.aa.msw.database.helpers.id.UserToSpotId;
import com.aa.msw.database.repository.dao.*;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.*;
import com.aa.msw.notifications.NotificationSpotInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpotDbService {
    private final SpotDao spotDao;
    private final SpotCurrentInfoDao spotCurrentInfoDao;
    private final UserToSpotDao userToSpotDao;
    private final SampleDao sampleDao;
    private final ForecastDao forecastDao;

    public SpotDbService(SpotDao spotDao, SpotCurrentInfoDao spotCurrentInfoDao, UserToSpotDao userToSpotDao, SampleDao sampleDao, ForecastDao forecastDao) {
        this.spotDao = spotDao;
        this.spotCurrentInfoDao = spotCurrentInfoDao;
        this.userToSpotDao = userToSpotDao;
        this.sampleDao = sampleDao;
        this.forecastDao = forecastDao;
    }

    @Transactional
    public List<UserSpot> getUserSpotsOrdered() {
        return userToSpotDao.getUserToSpotOrdered()
                .stream()
                .map(userToSpot -> new UserSpot(userToSpot.position(), spotDao.get(userToSpot.spotId())))
                .sorted(Comparator.comparingInt(UserSpot::position))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addPrivateSpot(Spot spot, int position, boolean withNotification) {
        spotDao.persist(spot);
        persistUserToSpot(spot, position, withNotification);
        updateSpotCurrentInfo(spot, getCurrentFlowStatusEnum(spot));
    }

    @Transactional
    public void updatePrivateSpot(Spot updatedSpot) {
        Spot existingSpot = spotDao.get(updatedSpot.getId());
        if (!existingSpot.isPublic() && userToSpotDao.userHasSpot(existingSpot.getId())) {
            spotDao.update(updatedSpot);
        }
        updateSpotCurrentInfo(updatedSpot, getCurrentFlowStatusEnum(updatedSpot));
    }

    @Transactional
    public void updateSpotCurrentInfo(Spot spot, FlowStatusEnum currentFlowStatusEnum) {
        spotCurrentInfoDao.updateCurrentInfo(spot.spotId(), currentFlowStatusEnum);
    }

    @Transactional
    public void addAllPublicSpotsToUser() {
        List<Spot> publicSpots = new ArrayList<>(spotDao.getPublicSpots());

        List<Spot> mappedSpotIds = getUserSpotsOrdered().stream().map(UserSpot::spot).toList();

        for (Spot publicSpot : publicSpots) {
            if (!mappedSpotIds.contains(publicSpot)) {
                persistUserToSpot(publicSpot, 0, false);
            }
        }
    }

    @Transactional
    public Set<NotificationSpotInfo> updateCurrentInfoForAllSpotsOfStations(Set<ApiStationId> stationIds) {
        Set<NotificationSpotInfo> spotsThatImproved = new HashSet<>();
        for (ApiStationId stationId : stationIds) {
            spotsThatImproved.addAll(updateCurrentInfoForAllSpotsOfStation(stationId));
        }
        return spotsThatImproved;
    }

    @Transactional
    public Set<NotificationSpotInfo> updateCurrentInfoForAllSpotsOfStation(ApiStationId stationId) {
        Set<Spot> spots = spotDao.getSpotsWithStationId(stationId);
        Set<NotificationSpotInfo> spotsThatImproved = new HashSet<>();
        for (Spot spot : spots) {
            FlowStatusEnum updatedFlowStatusEnum = getCurrentFlowStatusEnum(spot);
            spotsThatImproved = getSpotsThatImproved(spot, updatedFlowStatusEnum);
            updateSpotCurrentInfo(spot, updatedFlowStatusEnum);
        }
        return spotsThatImproved;
    }

    private Set<NotificationSpotInfo> getSpotsThatImproved(Spot spot, FlowStatusEnum updatedFlowStatusEnum) {
        Set<NotificationSpotInfo> spotsThatImproved = new HashSet<>();
        try {
            if (hasCurrentInfoImprovedForSpot(spot, updatedFlowStatusEnum)) {
                Set<UserToSpot> userToSpots = userToSpotDao.getUserToSpots(spot.getId());
                for (UserToSpot userToSpot : userToSpots) {
                    spotsThatImproved.add(
                            new NotificationSpotInfo(
                                    spot,
                                    updatedFlowStatusEnum,
                                    userToSpot)
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while trying to get the spots that improved " + e.getMessage());
        }

        return spotsThatImproved;
    }

    private boolean hasCurrentInfoImprovedForSpot(Spot spot, FlowStatusEnum updatedFlowStatusEnum) {
        SpotCurrentInfo spotCurrentInfo = spotCurrentInfoDao.get(spot.getId());
        if (spotCurrentInfo == null || spotCurrentInfo.currentFlowStatusEnum() == null) {
            return true;
        }
        FlowStatusEnum oldFlowStatus = spotCurrentInfo.currentFlowStatusEnum();
        boolean changedFromBadToOrange = oldFlowStatus == FlowStatusEnum.BAD && updatedFlowStatusEnum == FlowStatusEnum.TENDENCY_TO_BECOME_GOOD;
        boolean changedToGreen = oldFlowStatus != FlowStatusEnum.GOOD && updatedFlowStatusEnum == FlowStatusEnum.GOOD;
        return changedFromBadToOrange || changedToGreen;
    }

    @Transactional
    protected void persistUserToSpot(Spot spot, int position, boolean withNotification) {
        increasePositionOfAllSpotsOfTypeByOne(spot.type());
        userToSpotDao.persist(
                new UserToSpot(
                        new UserToSpotId(),
                        UserContext.getCurrentUser().userId(),
                        spot.spotId(),
                        position,
                        withNotification
                )
        );
    }

    @Transactional
    protected void increasePositionOfAllSpotsOfTypeByOne(SpotTypeEnum type) {
        List<UserToSpot> userToSpots = getUserToSpotOrderedOfType(type);

        for (int i = 0; i < userToSpots.size(); i++) {
            UserToSpot userToSpot = userToSpots.get(i);
            userToSpot.setPosition(i + 1);
            userToSpotDao.update(userToSpot);
        }
    }

    @Transactional
    protected List<UserToSpot> getUserToSpotOrderedOfType(SpotTypeEnum type) {
        return userToSpotDao.getUserToSpotOrdered().stream()
                .filter(userToSpot -> spotDao.get(userToSpot.spotId()).type() == type)
                .toList();
    }

    private FlowStatusEnum getCurrentFlowStatusEnum(Spot spot) {
        try {
            Sample currentSample = sampleDao.getCurrentSample(spot.stationId());
            if (isInSurfableRange(spot, (double) currentSample.flow())) {
                return FlowStatusEnum.GOOD;
            }
            Forecast forecast = forecastDao.getCurrentForecast(spot.stationId());
            Optional<Double> minOfForecast = forecast.min().values().stream().min(Double::compareTo);
            Optional<Double> maxOfForecast = forecast.max().values().stream().max(Double::compareTo);

            if (minOfForecast.isPresent() && maxOfForecast.isPresent()) {
                Double min = minOfForecast.get();
                Double max = maxOfForecast.get();

                if (isInSurfableRange(spot, min) ||
                        isInSurfableRange(spot, max) ||
                        (min < spot.minFlow() && max > spot.maxFlow())) {
                    return FlowStatusEnum.TENDENCY_TO_BECOME_GOOD;
                }
            }
        } catch (NoDataAvailableException e) {
            return FlowStatusEnum.BAD;
        }
        return FlowStatusEnum.BAD;
    }

    private boolean isInSurfableRange(Spot spot, Double flow) {
        return flow > spot.minFlow() && flow < spot.maxFlow();
    }
}

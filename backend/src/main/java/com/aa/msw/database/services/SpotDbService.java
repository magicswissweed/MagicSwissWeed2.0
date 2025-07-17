package com.aa.msw.database.services;

import com.aa.msw.auth.threadlocal.UserContext;
import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.UserToSpot;
import com.aa.msw.database.helpers.id.UserToSpotId;
import com.aa.msw.database.repository.dao.*;
import com.aa.msw.model.*;
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
    public void addPrivateSpot(Spot spot, int position) {
        spotDao.persist(spot);
        persistUserToSpot(spot, position);
        updateSpotCurrentInfo(spot);
    }

    @Transactional
    public void updatePrivateSpot(Spot updatedSpot) {
        Spot existingSpot = spotDao.get(updatedSpot.getId());
        if (!existingSpot.isPublic() && userToSpotDao.userHasSpot(existingSpot.getId())) {
            spotDao.update(updatedSpot);
        }
        updateSpotCurrentInfo(updatedSpot);
    }

    @Transactional
    public void updateSpotCurrentInfo(Spot spot) {
        spotCurrentInfoDao.updateCurrentInfo(spot.spotId(), getCurrentFlowStatusEnum(spot));
    }

    @Transactional
    public void addAllPublicSpotsToUser() {
        List<Spot> publicSpots = new ArrayList<>(spotDao.getPublicSpots());

        List<Spot> mappedSpotIds = getUserSpotsOrdered().stream().map(UserSpot::spot).toList();

        for (Spot publicSpot : publicSpots) {
            if (!mappedSpotIds.contains(publicSpot)) {
                persistUserToSpot(publicSpot, 0);
            }
        }
    }

    @Transactional
    public void updateCurrentInfoForAllSpotsOfStations(Set<Integer> stationIds) {
        for (Integer stationId : stationIds) {
            updateCurrentInfoForAllSpotsOfStation(stationId);
        }
    }

    @Transactional
    public void updateCurrentInfoForAllSpotsOfStation(Integer stationId) {
        Set<Spot> spots = spotDao.getSpotsWithStationId(stationId);
        for (Spot spot : spots) {
            updateSpotCurrentInfo(spot);
        }
    }

    @Transactional
    protected void persistUserToSpot(Spot spot, int position) {
        increasePositionOfAllSpotsOfTypeByOne(spot.type());
        userToSpotDao.persist(
                new UserToSpot(
                        new UserToSpotId(),
                        UserContext.getCurrentUser().userId(),
                        spot.spotId(),
                        position
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

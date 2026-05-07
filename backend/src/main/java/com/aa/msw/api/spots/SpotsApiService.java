package com.aa.msw.api.spots;

import com.aa.msw.api.current.SampleApiService;
import com.aa.msw.api.station.StationApiService;
import com.aa.msw.auth.threadlocal.UserContext;
import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.exceptions.NoSampleAvailableException;
import com.aa.msw.database.helpers.UserToSpot;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.database.repository.dao.SpotCurrentInfoDao;
import com.aa.msw.database.repository.dao.SpotDao;
import com.aa.msw.database.repository.dao.UserToSpotDao;
import com.aa.msw.database.services.SpotDbService;
import com.aa.msw.gen.api.*;
import com.aa.msw.model.Spot;
import com.aa.msw.model.SpotCurrentInfo;
import com.aa.msw.model.Station;
import com.aa.msw.model.UserSpot;
import com.aa.msw.source.InputDataFetcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpotsApiService {
    private static final Logger LOG = LoggerFactory.getLogger(SpotsApiService.class);

    private final SampleApiService sampleApiService;
    private final SampleDao sampleDao;
    private final SpotDao spotDao;
    private final UserToSpotDao userToSpotDao;
    private final InputDataFetcherService inputDataFetcherService;
    private final StationApiService stationApiService;
    private final SpotCurrentInfoDao spotCurrentInfoDao;
    private final SpotDbService spotDbService;

    public SpotsApiService(SampleApiService sampleApiService, SampleDao sampleDao, SpotDao spotDao, UserToSpotDao userToSpotDao, InputDataFetcherService inputDataFetcherService, StationApiService stationApiService, SpotCurrentInfoDao spotCurrentInfoDao, SpotDbService spotDbService) {
        this.sampleApiService = sampleApiService;
        this.sampleDao = sampleDao;
        this.spotDao = spotDao;
        this.userToSpotDao = userToSpotDao;
        this.inputDataFetcherService = inputDataFetcherService;
        this.stationApiService = stationApiService;
        this.spotCurrentInfoDao = spotCurrentInfoDao;
        this.spotDbService = spotDbService;
    }

    public Set<ApiStationId> getStations() {
        return getSpots()
                .stream()
                .map(i -> i.getStation().getId())
                .collect(Collectors.toSet());
    }

    public List<ApiSpotInformation> getSpots() {
        try {
            if (UserContext.getCurrentUser() == null) {
                return getPublicSpots();
            } else {
                return getAllSpots();
            }
        } catch (Exception e) {
            LOG.error("Error while fetching spots.", e);
            return List.of();
        }
    }

    public void addPrivateSpot(Spot spot, int position, boolean withNotification) throws NoSampleAvailableException {
        spotDbService.addPrivateSpot(spot, position, withNotification);
        triggerImmediateFrenchFetchIfNeeded(spot.stationId());
    }

    public void editSpot(Spot updatedSpot, boolean withNotification) throws NoSampleAvailableException {
        if (spotDao.isPublicSpot(updatedSpot.spotId())) {
            Spot newPrivateSpot = new Spot(
                    new SpotId(), // needs new ID - not the same as the old spot
                    updatedSpot.isPublic(),
                    updatedSpot.type(),
                    updatedSpot.name(),
                    updatedSpot.stationId(),
                    updatedSpot.measurementType(),
                    updatedSpot.minValue(),
                    updatedSpot.maxValue()
            );
            UserToSpot oldUserToPublicSpotMapping = userToSpotDao.get(UserContext.getCurrentUser().userId(), updatedSpot.spotId());
            deleteMapping(oldUserToPublicSpotMapping);
            addPrivateSpot(newPrivateSpot, oldUserToPublicSpotMapping.position(), withNotification);
        } else {
            editPrivateSpot(updatedSpot);
            userToSpotDao.setWithNotification(updatedSpot.spotId(), withNotification);
            triggerImmediateFrenchFetchIfNeeded(updatedSpot.stationId());
        }
    }

    /**
     * For French spots we only fetch stations referenced by a spot, so a brand-new station won't be in
     * any sample table yet. Kick off an immediate fetch so the user doesn't have to wait for the next
     * scheduled tick (~20 minutes).
     */
    private void triggerImmediateFrenchFetchIfNeeded(ApiStationId stationId) {
        if (stationId == null || stationId.getCountry() != CountryEnum.FR) {
            return;
        }
        try {
            sampleDao.getCurrentSample(stationId);
        } catch (NoDataAvailableException e) {
            inputDataFetcherService.triggerFrenchFetchForStationAsync(stationId);
        }
    }


    public void deletePrivateSpot(SpotId spotId) {
        userToSpotDao.deletePrivateSpot(spotId);
    }

    public void orderSpots(List<SpotId> orderedSpotIds) {
        int position = 0;
        for (SpotId spotId : orderedSpotIds) {
            userToSpotDao.setPosition(spotId, position);
            position++;
        }
    }

    private List<ApiSpotInformation> getPublicSpots() {
        return getApiSpotInformationList(spotDao.getPublicSpots());
    }

    private List<ApiSpotInformation> getAllSpots() {
        List<Spot> userSpots = spotDbService.getUserSpotsOrdered().stream()
                .map(UserSpot::spot)
                .collect(Collectors.toList());
        return getApiSpotInformationList(userSpots);
    }

    private List<ApiSpotInformation> getApiSpotInformationList(List<Spot> spots) {
        List<ApiSpotInformation> spotInformationList = new ArrayList<>();
        var supportedByStation = sampleDao.getSupportedMeasurementsByStation();
        for (Spot spot : spots) {
            try {
                Station station = stationApiService.getStation(spot.stationId());
                ApiStation apiStation = new ApiStation(
                        station.stationId(),
                        station.label(),
                        station.latitude(),
                        station.longitude(),
                        new ArrayList<>(supportedByStation.getOrDefault(spot.stationId(), java.util.Set.of())));

                boolean withNotification = !spot.isPublic() && userToSpotDao.get(UserContext.getCurrentUser().userId(), spot.spotId()).withNotification();

                ApiSpotInformation info = new ApiSpotInformation()
                        .id(spot.spotId().getId())
                        .name(spot.name())
                        .isPublic(spot.isPublic())
                        .measurementType(spot.measurementType())
                                    .minValue(spot.minValue())
                                    .maxValue(spot.maxValue())
                                    .stationId(spot.stationId())
                                    .spotType(com.aa.msw.gen.api.ApiSpotInformation.SpotTypeEnum.valueOf(spot.type().name()))
                                    .currentSample(sampleApiService.getCurrentSample(spot.stationId(), spot.measurementType()))
                                    .station(apiStation)
                                    .flowStatusEnum(getFlowStatusEnum(spot.spotId()))
                                    .withNotification(withNotification)
                                    .dataPending(false);

                try {
                    info.currentSample(sampleApiService.getCurrentSample(spot.stationId()));
                } catch (NoDataAvailableException e) {
                    // No sample yet (e.g. a freshly added spot whose station has not been fetched).
                    // Return the spot anyway so the frontend can show a "fetching data" placeholder.
                    info.dataPending(true);
                }

                spotInformationList.add(info);
            } catch (NoSuchElementException e) {
                LOG.error("Station with ID {} does not exist. Skipping this spot from the list of spots.", spot.stationId());
            }
        }
        return spotInformationList;
    }

    private ApiFlowStatusEnum getFlowStatusEnum(SpotId spotId) {
        SpotCurrentInfo currentFlowStatus = spotCurrentInfoDao.get(spotId);
        if (currentFlowStatus == null) {
            return ApiFlowStatusEnum.BAD;
        } else {
            return ApiFlowStatusEnum.valueOf(currentFlowStatus.currentFlowStatusEnum().name());
        }
    }

    private void deleteMapping(UserToSpot oldUserToPublicSpotMapping) {
        userToSpotDao.delete(oldUserToPublicSpotMapping);
    }

    private void editPrivateSpot(Spot updatedSpot) {
        spotDbService.updatePrivateSpot(updatedSpot);
    }
}

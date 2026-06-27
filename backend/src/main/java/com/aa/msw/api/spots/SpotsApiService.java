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

import java.util.*;
import java.util.stream.Collectors;

import static com.aa.msw.gen.api.ApiMeasurementType.TEMPERATURE;

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
        triggerImmediateFrenchFetchIfNeeded(spot.stationId(), spot.measurementType());
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
            triggerImmediateFrenchFetchIfNeeded(updatedSpot.stationId(), updatedSpot.measurementType());
        }
    }

    /**
     * For French spots we only fetch stations referenced by a spot, so a brand-new station won't be in
     * any sample table yet. Kick off an immediate fetch so the user doesn't have to wait for the next
     * scheduled tick (~20 minutes).
     */
    private void triggerImmediateFrenchFetchIfNeeded(ApiStationId stationId, ApiMeasurementType measurementType) {
        if (stationId == null || stationId.getCountry() != CountryEnum.FR) {
            return;
        }
        try {
            sampleDao.getCurrentSample(stationId, measurementType);
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
        Set<ApiStationId> stationIds = spots.stream()
                .map(Spot::stationId)
                .collect(Collectors.toSet());
        Map<ApiStationId, Map<ApiMeasurementType, ApiSample>> latestSamplesByStation =
                sampleApiService.getLatestSamplePerStationAndType(stationIds);
        return spots.stream()
                .map(spot -> toApiSpotInformation(spot, latestSamplesByStation.getOrDefault(spot.stationId(), Map.of())))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ApiSpotInformation> toApiSpotInformation(Spot spot,
                                                              Map<ApiMeasurementType, ApiSample> latestSamplesForStation) {
        Station station;
        try {
            station = stationApiService.getStation(spot.stationId());
        } catch (NoSuchElementException e) {
            LOG.error("Station with ID {} does not exist. Skipping this spot from the list of spots.", spot.stationId());
            return Optional.empty();
        }

        ApiSample currentSample = latestSamplesForStation.get(spot.measurementType());
        return Optional.of(new ApiSpotInformation()
                .id(spot.spotId().getId())
                .name(spot.name())
                .isPublic(spot.isPublic())
                .spotType(toApiSpotType(spot.type()))
                .stationId(spot.stationId())
                .measurementType(spot.measurementType())
                .minValue(spot.minValue())
                .maxValue(spot.maxValue())
                .station(toApiStation(station, latestSamplesForStation.keySet()))
                .currentSample(currentSample)
                .currentTemperature(latestSamplesForStation.get(TEMPERATURE))
                .dataPending(currentSample == null)
                .flowStatusEnum(getFlowStatusEnum(spot.spotId()))
                .withNotification(isWithNotification(spot)));
    }

    private static ApiStation toApiStation(Station station, Set<ApiMeasurementType> supportedMeasurementTypes) {
        return new ApiStation(
                station.stationId(),
                station.label(),
                station.latitude(),
                station.longitude(),
                new ArrayList<>(supportedMeasurementTypes));
    }

    private static ApiSpotInformation.SpotTypeEnum toApiSpotType(com.aa.msw.model.SpotTypeEnum type) {
        return ApiSpotInformation.SpotTypeEnum.valueOf(type.name());
    }

    private boolean isWithNotification(Spot spot) {
        if (spot.isPublic()) {
            return false;
        }
        return userToSpotDao.get(UserContext.getCurrentUser().userId(), spot.spotId()).withNotification();
    }

    private ApiFlowStatusEnum getFlowStatusEnum(SpotId spotId) {
        SpotCurrentInfo currentFlowStatus = spotCurrentInfoDao.get(spotId);
        return currentFlowStatus == null
                ? ApiFlowStatusEnum.BAD
                : ApiFlowStatusEnum.valueOf(currentFlowStatus.currentFlowStatusEnum().name());
    }

    private void deleteMapping(UserToSpot oldUserToPublicSpotMapping) {
        userToSpotDao.delete(oldUserToPublicSpotMapping);
    }

    private void editPrivateSpot(Spot updatedSpot) {
        spotDbService.updatePrivateSpot(updatedSpot);
    }
}

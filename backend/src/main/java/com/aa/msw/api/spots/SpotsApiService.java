package com.aa.msw.api.spots;

import com.aa.msw.api.current.SampleApiService;
import com.aa.msw.api.station.StationApiService;
import com.aa.msw.auth.threadlocal.UserContext;
import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.exceptions.NoSampleAvailableException;
import com.aa.msw.database.helpers.UserToSpot;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.database.repository.dao.ForecastDao;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.database.repository.dao.SpotDao;
import com.aa.msw.database.repository.dao.UserToSpotDao;
import com.aa.msw.gen.api.ApiFlowStatusEnum;
import com.aa.msw.gen.api.ApiSpotInformation;
import com.aa.msw.gen.api.ApiStation;
import com.aa.msw.model.*;
import com.aa.msw.source.InputDataFetcherService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpotsApiService {
    private final SampleApiService sampleApiService;
    private final SampleDao sampleDao;
    private final SpotDao spotDao;
    private final ForecastDao forecastDao;
    private final UserToSpotDao userToSpotDao;
    private final InputDataFetcherService inputDataFetcherService;
    private final StationApiService stationApiService;

    public SpotsApiService(SampleApiService sampleApiService, SampleDao sampleDao, SpotDao spotDao, ForecastDao forecastDao, UserToSpotDao userToSpotDao, InputDataFetcherService inputDataFetcherService, StationApiService stationApiService) {
        this.sampleApiService = sampleApiService;
        this.sampleDao = sampleDao;
        this.spotDao = spotDao;
        this.forecastDao = forecastDao;
        this.userToSpotDao = userToSpotDao;
        this.inputDataFetcherService = inputDataFetcherService;
        this.stationApiService = stationApiService;
    }

    public Set<Integer> getStations() {
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
            return List.of();
        }
    }

    public void addPrivateSpot(Spot spot, int position) throws NoSampleAvailableException {
        fetchSamplesAndPersistIfExists(spot.stationId());
        userToSpotDao.addPrivateSpot(spot, position);
    }

    public void editSpot(Spot updatedSpot) throws NoSampleAvailableException {
        if (spotDao.isPublicSpot(updatedSpot.spotId())) {
            Spot newPrivateSpot = new Spot(
                    new SpotId(), // needs new ID - not the same as the old spot
                    updatedSpot.isPublic(),
                    updatedSpot.type(),
                    updatedSpot.name(),
                    updatedSpot.stationId(),
                    updatedSpot.minFlow(),
                    updatedSpot.maxFlow()
            );
            UserToSpot oldUserToPublicSpotMapping = userToSpotDao.get(UserContext.getCurrentUser().userId(), updatedSpot.spotId());
            deleteMapping(oldUserToPublicSpotMapping);
            addPrivateSpot(newPrivateSpot, oldUserToPublicSpotMapping.position());
        } else {
            editPrivateSpot(updatedSpot);
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
        List<Spot> userSpots = userToSpotDao.getUserSpotsOrdered().stream()
                .map(UserSpot::spot)
                .collect(Collectors.toList());
        return getApiSpotInformationList(userSpots);
    }

    private List<ApiSpotInformation> getApiSpotInformationList(List<Spot> spots) {
        List<ApiSpotInformation> spotInformationList = new ArrayList<>();
        for (Spot spot : spots) {
            try {
                Station station = stationApiService.getStation(spot.stationId());
                ApiStation apiStation = new ApiStation(station.stationId(), station.label(), station.latitude(), station.longitude());

                try {
                    spotInformationList.add(
                            new ApiSpotInformation()
                                    .id(spot.spotId().getId())
                                    .name(spot.name())
                                    .isPublic(spot.isPublic())
                                    .minFlow(spot.minFlow())
                                    .maxFlow(spot.maxFlow())
                                    .stationId(spot.stationId())
                                    .spotType(com.aa.msw.gen.api.ApiSpotInformation.SpotTypeEnum.valueOf(spot.type().name()))
                                    .currentSample(sampleApiService.getCurrentSample(spot.stationId()))
                                    .station(apiStation)
                                    .flowStatusEnum(getFlowStatusEnum(spot))
                    );
                } catch (NoDataAvailableException e) {
                    // should never happen, but if it does, we just don't return this one spot
                }

            } catch (NoSuchElementException e) {
                // ignore for the moment and do not add this ApiSpotInformation to the list
                break;
            }
        }
        return spotInformationList;
    }

    private ApiFlowStatusEnum getFlowStatusEnum(Spot spot) {
        // FIXME: If this makes the call to get the samples slow:
        //  - Bei Forecasts zusätzlich min und max (number) speichern
        //  - Dann hier nur noch min und max selektieren von DB statt Allem und dann noch über die Linie zu loopen.
        try {
            Sample currentSample = this.sampleDao.getCurrentSample(spot.stationId());
            if (isInSurfableRange(spot, (double) currentSample.flow())) {
                return ApiFlowStatusEnum.GOOD;
            }
            Forecast forecast = this.forecastDao.getCurrentForecast(spot.stationId());
            Optional<Double> minOfForecast = forecast.min().values().stream().min(Double::compareTo);
            Optional<Double> maxOfForecast = forecast.max().values().stream().max(Double::compareTo);


            if (minOfForecast.isPresent() && maxOfForecast.isPresent()) {
                Double min = minOfForecast.get();
                Double max = maxOfForecast.get();

                if (isInSurfableRange(spot, min) ||
                        isInSurfableRange(spot, max) ||
                        (min < spot.minFlow() && max > spot.maxFlow())) {
                    return ApiFlowStatusEnum.TENDENCY_TO_BECOME_GOOD;
                }
            }
        } catch (NoDataAvailableException e) {
            return ApiFlowStatusEnum.BAD;
        }
        return ApiFlowStatusEnum.BAD;
    }

    private boolean isInSurfableRange(Spot spot, Double flow) {
        return flow > spot.minFlow() && flow < spot.maxFlow();
    }

    private void deleteMapping(UserToSpot oldUserToPublicSpotMapping) {
        userToSpotDao.delete(oldUserToPublicSpotMapping);
    }

    private void editPrivateSpot(Spot updatedSpot) throws NoSampleAvailableException {
        fetchSamplesAndPersistIfExists(updatedSpot.stationId());
        userToSpotDao.updatePrivateSpot(updatedSpot);
    }

    private void fetchSamplesAndPersistIfExists(Integer stationId) throws NoSampleAvailableException {
        List<Sample> samples = inputDataFetcherService.fetchForStationId(stationId);
        sampleDao.persistSamplesIfNotExist(samples);
    }
}

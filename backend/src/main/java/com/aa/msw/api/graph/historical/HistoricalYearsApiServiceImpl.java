package com.aa.msw.api.graph.historical;

import com.aa.msw.api.graph.AbstractGraphLineApiService;
import com.aa.msw.api.spots.SpotsApiService;
import com.aa.msw.gen.api.ApiHistoricalYears;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.StationToApiHistoricalYears;
import com.aa.msw.model.HistoricalYearsData;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Profile("!test")
@Service
public class HistoricalYearsApiServiceImpl extends AbstractGraphLineApiService implements HistoricalYearsApiService {
    private final HistoricalYearsAccessorService historicalYearsAccessorService;
    private final SpotsApiService spotsApiService;

    public HistoricalYearsApiServiceImpl(HistoricalYearsAccessorService historicalYearsAccessorService, SpotsApiService spotsApiService) {
        super();
        this.historicalYearsAccessorService = historicalYearsAccessorService;
        this.spotsApiService = spotsApiService;
    }

    public ApiHistoricalYears getApiHistoricalYearsData(ApiStationId stationId) {
        HistoricalYearsData historicData = historicalYearsAccessorService.getHistoricalYearsData().get(stationId);
        if (historicData == null) {
            return null;
        }
        ApiHistoricalYears apiHistoricalYears = new ApiHistoricalYears();
        apiHistoricalYears.setMedian(mapApiLine(historicData.getMedian()));
        apiHistoricalYears.setTwentyFivePercentile(mapApiLine(historicData.getTwentyFivePercentile()));
        apiHistoricalYears.setSeventyFivePercentile(mapApiLine(historicData.getSeventyFivePercentile()));
        apiHistoricalYears.setMin(mapApiLine(historicData.getMin()));
        apiHistoricalYears.setMax(mapApiLine(historicData.getMax()));
        apiHistoricalYears.setCurrentYear(mapApiLine(historicData.getCurrentYear()));
        return apiHistoricalYears;
    }

    @Override
    public List<StationToApiHistoricalYears> getAllApiHistoricalYearsData() {
        List<StationToApiHistoricalYears> apiHistoricalYearsList = new ArrayList<>();
        for (ApiStationId station : spotsApiService.getStations()) {
            apiHistoricalYearsList.add(new StationToApiHistoricalYears(station, getApiHistoricalYearsData(station)));
        }
        return apiHistoricalYearsList;
    }
}

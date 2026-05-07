package com.aa.msw.api.current;

import com.aa.msw.api.graph.lastFewDays.LastFewDaysApiService;
import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.gen.api.ApiFlowSample;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiSample;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Sample;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SampleApiService {

    private final SampleDao sampleDao;
    private final LastFewDaysApiService lastFewDaysApiService;

    SampleApiService(final SampleDao sampleDao, LastFewDaysApiService lastFewDaysApiService) {
        this.sampleDao = sampleDao;
        this.lastFewDaysApiService = lastFewDaysApiService;
    }

    private static ApiSample mapSample(Sample sample) {
        return new ApiSample()
                .timestamp(sample.getTimestamp())
                .temperature(sample.getTemperature().orElse(null))
                .value(sample.value())
                .measurementType(sample.getMeasurementType());
    }

    public ApiSample getCurrentSample(ApiStationId apiStationId, ApiMeasurementType measurementType) throws NoDataAvailableException {
        return mapSample(sampleDao.getCurrentSample(apiStationId, measurementType));
    }

    public List<ApiFlowSample> getLastFewDaysSamples(ApiStationId stationId) throws NoDataAvailableException {
        return lastFewDaysApiService
                .getLastFewDays(stationId)
                .lastFewDaysSamples()
                .entrySet()
                .stream()
                .map(sample -> new ApiFlowSample()
                        .timestamp(sample.getKey())
                        .value(sample.getValue()))
                .toList();
    }
}

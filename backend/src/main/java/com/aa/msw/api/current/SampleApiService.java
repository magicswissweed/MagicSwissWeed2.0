package com.aa.msw.api.current;

import com.aa.msw.api.graph.last40days.Last40DaysApiService;
import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.gen.api.ApiFlowSample;
import com.aa.msw.gen.api.ApiSample;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Sample;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SampleApiService {

    private final SampleDao sampleDao;
    private final Last40DaysApiService last40daysApiService;

    SampleApiService(final SampleDao sampleDao, Last40DaysApiService last40daysApiService) {
        this.sampleDao = sampleDao;
        this.last40daysApiService = last40daysApiService;
    }

    private static ApiSample mapSample(Sample sample) {
        return new ApiSample()
                .timestamp(sample.getTimestamp())
                .temperature(sample.getTemperature().orElse(null))
                .flow(sample.flow());
    }

    public ApiSample getCurrentSample(ApiStationId apiStationId) throws NoDataAvailableException {
        return mapSample(sampleDao.getCurrentSample(apiStationId));
    }

    public List<ApiFlowSample> getLast40DaysSamples(ApiStationId stationId) throws NoDataAvailableException {
        return last40daysApiService
                .getLast40Days(stationId)
                .last40DaysSamples()
                .entrySet()
                .stream()
                .map(sample -> new ApiFlowSample()
                        .timestamp(sample.getKey())
                        .flow(sample.getValue()))
                .toList();
    }
}

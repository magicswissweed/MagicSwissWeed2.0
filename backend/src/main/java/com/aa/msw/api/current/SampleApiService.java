package com.aa.msw.api.current;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.database.repository.dao.SpotDao;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiSample;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Sample;
import com.aa.msw.model.Spot;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SampleApiService {

    private static final int LAST_FEW_DAYS_WINDOW = 8;

    private final SampleDao sampleDao;
    private final SpotDao spotDao;

    SampleApiService(final SampleDao sampleDao, final SpotDao spotDao) {
        this.sampleDao = sampleDao;
        this.spotDao = spotDao;
    }

    private static ApiSample mapSample(Sample sample) {
        return new ApiSample()
                .timestamp(sample.getTimestamp())
                .value(sample.value())
                .measurementType(sample.getMeasurementType());
    }

    public ApiSample getCurrentSample(ApiStationId apiStationId, ApiMeasurementType measurementType) throws NoDataAvailableException {
        return mapSample(sampleDao.getCurrentSample(apiStationId, measurementType));
    }

    public List<ApiSample> getLastFewDaysSamples(SpotId spotId) {
        Spot spot = spotDao.get(spotId);
        return sampleDao.getSamplesOfLastNDays(spot.stationId(), spot.measurementType(), LAST_FEW_DAYS_WINDOW)
                .stream()
                .map(SampleApiService::mapSample)
                .toList();
    }
}

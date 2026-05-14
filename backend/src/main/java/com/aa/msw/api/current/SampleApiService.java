package com.aa.msw.api.current;

import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.database.repository.dao.SampleDao;
import com.aa.msw.database.repository.dao.SpotDao;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiSample;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Sample;
import com.aa.msw.model.Spot;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public Map<ApiStationId, Map<ApiMeasurementType, ApiSample>> getLatestSamplePerStationAndType(Set<ApiStationId> stationIds) {
        Map<ApiStationId, Map<ApiMeasurementType, Sample>> raw = sampleDao.getLatestSamplePerStationAndType(stationIds);
        Map<ApiStationId, Map<ApiMeasurementType, ApiSample>> mapped = new HashMap<>(raw.size());
        raw.forEach((stationId, byType) -> {
            Map<ApiMeasurementType, ApiSample> inner = new HashMap<>(byType.size());
            byType.forEach((type, sample) -> inner.put(type, mapSample(sample)));
            mapped.put(stationId, inner);
        });
        return mapped;
    }

    public List<ApiSample> getLastFewDaysSamples(SpotId spotId) {
        Spot spot = spotDao.get(spotId);
        return sampleDao.getSamplesOfLastNDays(spot.stationId(), spot.measurementType(), LAST_FEW_DAYS_WINDOW)
                .stream()
                .map(SampleApiService::mapSample)
                .toList();
    }
}

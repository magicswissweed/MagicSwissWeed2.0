package com.aa.msw.database.repository.dao;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Sample;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SampleDao extends TimestampedDao, Dao<SampleId, Sample> {
    Sample getCurrentSample(ApiStationId apiStationId, ApiMeasurementType measurementType) throws NoDataAvailableException;

    @Transactional
    void persistSamplesIfNotExist(List<Sample> samples);

    Map<ApiStationId, Set<ApiMeasurementType>> getSupportedMeasurementsByStation();
}

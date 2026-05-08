package com.aa.msw.source.swiss.existenz.sample;

import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Sample;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Profile("test")
@Service
class SwissSampleFetchServiceMock implements SwissSampleFetchService {

    private static final OffsetDateTime FIXED_TIMESTAMP = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Override
    public List<Sample> fetchSamples(Set<ApiStationId> stationIds) {
        if (stationIds.size() == 1) {
            return samples(stationIds.iterator().next(), Optional.of(3.0), 20);
        }
        List<Sample> result = new ArrayList<>();
        result.addAll(samples(apiStationId(CountryEnum.CH, "2018"), Optional.of(15.0), 200));
        result.addAll(samples(apiStationId(CountryEnum.CH, "2243"), Optional.of(14.0), 100));
        result.addAll(samples(apiStationId(CountryEnum.CH, "2105"), Optional.empty(), 20));
        result.addAll(samples(apiStationId(CountryEnum.CH, "2152"), Optional.of(13.0), 70));
        result.addAll(samples(apiStationId(CountryEnum.CH, "2091"), Optional.of(12.0), 850));
        result.addAll(samples(apiStationId(CountryEnum.CH, "2135"), Optional.of(11.0), 90));
        result.addAll(samples(apiStationId(CountryEnum.CH, "2473"), Optional.of(10.0), 120));
        result.addAll(samples(apiStationId(CountryEnum.CH, "2152"), Optional.of(9.0), 310));
        return result;
    }

    private List<Sample> samples(ApiStationId stationId, Optional<Double> temperature, double flow) {
        List<Sample> samples = new ArrayList<>();
        samples.add(new Sample(new SampleId(), stationId, FIXED_TIMESTAMP, flow, ApiMeasurementType.FLOW));
        temperature.ifPresent(temp ->
                samples.add(new Sample(new SampleId(), stationId, FIXED_TIMESTAMP, temp, ApiMeasurementType.TEMPERATURE)));
        return samples;
    }

    private ApiStationId apiStationId(CountryEnum country, String stationId) {
        return new ApiStationId(country, stationId);
    }

}

package com.aa.msw.source.swiss.existenz.sample;

import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Sample;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Profile("test")
@Service
class SwissSampleFetchServiceMock implements SwissSampleFetchService {

    @Override
    public List<Sample> fetchSamples(Set<ApiStationId> stationIds) {
        if (stationIds.size() == 1) {
            return List.of(sample(stationIds.iterator().next(), Optional.of(3.0), 20));
        } else {
            return List.of(
                    sample(apiStationId(CountryEnum.CH, "2018"), Optional.of(15.0), 200),
                    sample(apiStationId(CountryEnum.CH, "2243"), Optional.of(14.0), 100),
                    sample(apiStationId(CountryEnum.CH, "2105"), Optional.empty(), 20),
                    sample(apiStationId(CountryEnum.CH, "2152"), Optional.of(13.0), 70),
                    sample(apiStationId(CountryEnum.CH, "2091"), Optional.of(12.0), 850),
                    sample(apiStationId(CountryEnum.CH, "2135"), Optional.of(11.0), 90),
                    sample(apiStationId(CountryEnum.CH, "2473"), Optional.of(10.0), 120),
                    sample(apiStationId(CountryEnum.CH, "2152"), Optional.of(9.0), 310)
            );
        }
    }

    private Sample sample(ApiStationId stationId, Optional<Double> temperature, int flow) {
        return new Sample(
                new SampleId(),
                stationId,
                OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                temperature,
                flow);
    }

    private ApiStationId apiStationId(CountryEnum country, String stationId) {
        return new ApiStationId(country, stationId);
    }

}

package com.aa.msw.source.existenz.sample;

import com.aa.msw.database.helpers.id.SampleId;
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
class SampleFetchServiceMock implements SampleFetchService {

    @Override
    public List<Sample> fetchSamples(Set<Integer> stationIds) {
        if (stationIds.size() == 1) {
            return List.of(sample(stationIds.iterator().next(), Optional.of(3.0), 20));
        } else {
            return List.of(
                    sample(2018, Optional.of(15.0), 200),
                    sample(2243, Optional.of(14.0), 100),
                    sample(2105, Optional.empty(), 20),
                    sample(2152, Optional.of(13.0), 70),
                    sample(2091, Optional.of(12.0), 850),
                    sample(2135, Optional.of(11.0), 90),
                    sample(2473, Optional.of(10.0), 120),
                    sample(2152, Optional.of(9.0), 310)
            );
        }
    }

    private Sample sample(int stationId, Optional<Double> temperature, int flow) {
        return new Sample(
                new SampleId(),
                stationId,
                OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                temperature,
                flow);
    }

}

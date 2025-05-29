package com.aa.msw.source.hydrodaten.historical.years;

import com.aa.msw.model.HistoricalYearsData;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

@Profile("test")
@Service
class HistoricalYearsDataFetchServiceMock implements HistoricalYearsDataFetchService {

    @Override
    public HistoricalYearsData fetchHistoricalYearsData(int stationId) throws IOException, URISyntaxException {
        return null;
    }

    @Override
    public Set<HistoricalYearsData> fetchHistoricalYearsData(Set<Integer> stationIds) throws URISyntaxException {
        return Set.of();
    }
}

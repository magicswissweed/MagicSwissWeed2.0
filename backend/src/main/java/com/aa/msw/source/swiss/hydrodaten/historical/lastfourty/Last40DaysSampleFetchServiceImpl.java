package com.aa.msw.source.swiss.hydrodaten.historical.lastfourty;

import com.aa.msw.database.helpers.id.Last40DaysId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Last40Days;
import com.aa.msw.source.swiss.hydrodaten.AbstractLineFetchService;
import com.aa.msw.source.swiss.hydrodaten.model.line.HydroLine;
import com.aa.msw.source.swiss.hydrodaten.model.line.HydroResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Profile("!test")
@Service
public class Last40DaysSampleFetchServiceImpl extends AbstractLineFetchService implements Last40DaysSampleFetchService {

    Last40DaysSampleFetchServiceImpl() {
        super("https://www.hydrodaten.admin.ch/plots/p_q_40days/", "_p_q_40days_de.json");
    }

    private Last40Days fetchLast40DaysSamples(ApiStationId stationId) throws IOException, URISyntaxException {
        HydroResponse hydroResponse = fetchFromHydro(stationId);
        Map<OffsetDateTime, Double> line;
        // Check for flow measurement
        ArrayList<HydroLine> data = hydroResponse.plot().data();
        if (data.isEmpty()) {
            throw new IOException("No data available for flow measurement");
        }
        if (data.size() < 2) {
            if (!data.getFirst().name().equals("Abfluss")) {
                throw new IOException("Flow measurement not available");
            }
            line = mapLine(data.getFirst());

        } else {
            line = mapLine(data.get(1));
        }

        return new Last40Days(
                new Last40DaysId(),
                stationId,
                line
        );
    }

    @Override
    public Set<Last40Days> fetchLast40DaysSamples(Set<ApiStationId> stationIds) throws URISyntaxException {
        Set<Last40Days> result = new HashSet<>();
        for (ApiStationId stationId : stationIds) {
            try {
                result.add(fetchLast40DaysSamples(stationId));
            } catch (IOException e) {
                // ignore, there might not be data from last 40 days
            }
        }
        return result;
    }
}

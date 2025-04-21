package com.aa.msw.source.hydrodaten.historical.lastfourty;

import com.aa.msw.database.helpers.id.Last40DaysId;
import com.aa.msw.model.Last40Days;
import com.aa.msw.source.hydrodaten.AbstractLineFetchService;
import com.aa.msw.source.hydrodaten.model.line.HydroLine;
import com.aa.msw.source.hydrodaten.model.line.HydroResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class Last40DaysSampleFetchService extends AbstractLineFetchService {

    Last40DaysSampleFetchService() {
        super("https://www.hydrodaten.admin.ch/plots/p_q_40days/", "_p_q_40days_de.json");
    }

    private Last40Days fetchLast40DaysSamples(int stationId) throws IOException, URISyntaxException {
        HydroResponse hydroResponse = fetchFromHydro(stationId);
        Map<OffsetDateTime, Double> line;
        // Check for flow measurement
        ArrayList<HydroLine> data = hydroResponse.plot().data();
        if (data.isEmpty()) {
            throw new IOException("No data available for flow measurement");
        }
        if (data.size() < 2) {
            if (!data.get(0).name().equals("Abfluss")) {
                throw new IOException("Flow measurement not available");
            }
            line = mapLine(data.get(0));

        } else {
            line = mapLine(data.get(1));
        }

        return new Last40Days(
                new Last40DaysId(),
                stationId,
                line
        );
    }

    public Set<Last40Days> fetchLast40DaysSamples(Set<Integer> stationIds) throws URISyntaxException {
        Set<Last40Days> result = new HashSet<>();
        for (Integer stationId : stationIds) {
            try {
                result.add(fetchLast40DaysSamples(stationId));
            } catch (IOException e) {
                // ignore, there might not be data from last 40 days
            }
        }
        return result;
    }
}

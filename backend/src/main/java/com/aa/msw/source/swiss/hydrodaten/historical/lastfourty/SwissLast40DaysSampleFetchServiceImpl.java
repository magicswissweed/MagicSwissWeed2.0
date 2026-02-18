package com.aa.msw.source.swiss.hydrodaten.historical.lastfourty;

import com.aa.msw.database.helpers.id.LastFewDaysId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.LastFewDays;
import com.aa.msw.source.swiss.hydrodaten.AbstractSwissHydroLineFetchService;
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
public class SwissLast40DaysSampleFetchServiceImpl extends AbstractSwissHydroLineFetchService implements SwissLast40DaysSampleFetchService {

    SwissLast40DaysSampleFetchServiceImpl() {
        super("https://www.hydrodaten.admin.ch/plots/p_q_40days/", "_p_q_40days_de.json");
    }

    private LastFewDays fetchLast40DaysSamples(ApiStationId stationId) throws IOException, URISyntaxException {
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

        return new LastFewDays(
                new LastFewDaysId(),
                stationId,
                line
        );
    }

    @Override
    public Set<LastFewDays> fetchLast40DaysSamples(Set<ApiStationId> stationIds) {
        Set<LastFewDays> result = new HashSet<>();
        for (ApiStationId stationId : stationIds) {
            try {
                result.add(fetchLast40DaysSamples(stationId));
            } catch (Exception e) {
                // ignore, there might not be data from last 40 days
            }
        }
        return result;
    }
}

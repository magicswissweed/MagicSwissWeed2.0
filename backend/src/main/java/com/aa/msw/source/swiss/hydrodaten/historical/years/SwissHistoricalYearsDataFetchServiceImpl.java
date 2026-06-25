package com.aa.msw.source.swiss.hydrodaten.historical.years;

import com.aa.msw.database.helpers.id.HistoricalYearsDataId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.HistoricalYearsData;
import com.aa.msw.source.swiss.hydrodaten.AbstractSwissHydroLineFetchService;
import com.aa.msw.source.swiss.hydrodaten.model.line.HydroResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

@Profile("!test")
@Service
public class SwissHistoricalYearsDataFetchServiceImpl
        extends AbstractSwissHydroLineFetchService
        implements SwissHistoricalYearsDataFetchService {

    private static final Logger LOG = LoggerFactory.getLogger(SwissHistoricalYearsDataFetchServiceImpl.class);

    SwissHistoricalYearsDataFetchServiceImpl() {
        super("https://www.hydrodaten.admin.ch/web/hydro/de/q_annual/", "/2023/plot.json");
    }

    public HistoricalYearsData fetchHistoricalYearsData(ApiStationId stationId) throws IOException, URISyntaxException {
        HydroResponse hydroResponse = fetchFromHydro(stationId);

        TwentyFiveToSeventyFivePercentile twentyFiveToSeventyFivePercentile = getTwentyFiveToSeventyFivePercentile(hydroResponse);

        return new HistoricalYearsData(
                new HistoricalYearsDataId(),
                stationId,
                mapLine(hydroResponse.plot().data().get(5)),
                mapLine(twentyFiveToSeventyFivePercentile.twentyFivePercentile()),
                mapLine(twentyFiveToSeventyFivePercentile.seventyFivePercentile()),
                mapLine(hydroResponse.plot().data().get(3)),
                mapLine(hydroResponse.plot().data().get(4)),
                mapLine(hydroResponse.plot().data().get(7))
        );
    }

    public Set<HistoricalYearsData> fetchHistoricalYearsData(Set<ApiStationId> stationIds) throws URISyntaxException {
        Set<HistoricalYearsData> historicalYearsData = new HashSet<>();
        for (ApiStationId stationId : stationIds) {
            try {
                historicalYearsData.add(fetchHistoricalYearsData(stationId));
            } catch (Exception e) {
                // Skip stations whose historical data is missing or malformed (e.g. an unexpected or
                // empty plot.json yielding null series). A single bad station must not fail the whole
                // fetch — otherwise it crashes the ApplicationReadyEvent listener (and thus app
                // startup) on a fresh database. Same resilience approach as InputDataFetcherService.
                LOG.warn("Skipping historical years data for station {}: {}", stationId.getExternalId(), e.toString());
            }
        }
        return historicalYearsData;
    }
}

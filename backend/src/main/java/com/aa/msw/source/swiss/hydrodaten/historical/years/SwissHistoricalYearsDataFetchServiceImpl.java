package com.aa.msw.source.swiss.hydrodaten.historical.years;

import com.aa.msw.database.helpers.id.HistoricalYearsDataId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.HistoricalYearsData;
import com.aa.msw.source.swiss.hydrodaten.AbstractSwissHydroLineFetchService;
import com.aa.msw.source.swiss.hydrodaten.model.line.HydroResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

@Profile("!test")
@Service
public class SwissHistoricalYearsDataFetchServiceImpl
        extends AbstractSwissHydroLineFetchService
        implements SwissHistoricalYearsDataFetchService {

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
            } catch (IOException e) {
                // ignore: could be that this station just does not have historical data
            }
        }
        return historicalYearsData;
    }
}

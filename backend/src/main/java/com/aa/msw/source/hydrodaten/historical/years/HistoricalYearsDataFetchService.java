package com.aa.msw.source.hydrodaten.historical.years;

import com.aa.msw.database.helpers.id.HistoricalYearsDataId;
import com.aa.msw.model.HistoricalYearsData;
import com.aa.msw.source.hydrodaten.AbstractLineFetchService;
import com.aa.msw.source.hydrodaten.model.line.HydroResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

@Service
public class HistoricalYearsDataFetchService extends AbstractLineFetchService {

    HistoricalYearsDataFetchService() {
        super("https://www.hydrodaten.admin.ch/web/hydro/de/q_annual/", "/2023/plot.json");
    }

    public Set<HistoricalYearsData> fetchHistoricalYearsData(Set<Integer> stationIds) throws URISyntaxException {
        Set<HistoricalYearsData> historicalYearsData = new HashSet<>();
        for (int stationId : stationIds) {
            try {
                historicalYearsData.add(fetchHistoricalYearsData(stationId));
            } catch (IOException e) {
                // ignore: could be that this station just does not have historical data
            }
        }
        return historicalYearsData;
    }

    public HistoricalYearsData fetchHistoricalYearsData(int stationId) throws IOException, URISyntaxException {
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
}

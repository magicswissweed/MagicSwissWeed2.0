package com.aa.msw.source.swiss.hydrodaten.forecast;

import com.aa.msw.database.helpers.id.ForecastId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Forecast;
import com.aa.msw.source.swiss.hydrodaten.AbstractLineFetchService;
import com.aa.msw.source.swiss.hydrodaten.model.line.HydroResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Profile("!test")
@Service
public class SwissForecastFetchServiceImpl extends AbstractLineFetchService implements SwissForecastFetchService {
    SwissForecastFetchServiceImpl() {
        super("https://www.hydrodaten.admin.ch/plots/q_forecast/", "_q_forecast_de.json");
    }

    private static OffsetDateTime extractTimestamp(HydroResponse hydroResponse) {
        String datetimeString = hydroResponse.plot().layout().annotations().stream()
                .filter((a) -> a.xref().equals("x"))
                .toList().get(0).x();

        return OffsetDateTime.parse(datetimeString);
    }

    public List<Forecast> fetchForecasts(Set<ApiStationId> stationIds) throws URISyntaxException {
        List<Forecast> forecasts = new ArrayList<>();
        for (ApiStationId stationId : stationIds) {
            try {
                forecasts.add(fetchForecast(stationId));
            } catch (IOException e) {
                // ignore: could be that this station just does not have a forecast
            }
        }
        return forecasts;
    }

    public Forecast fetchForecast(ApiStationId stationId) throws IOException, URISyntaxException {
        HydroResponse hydroResponse = fetchFromHydro(stationId);

        TwentyFiveToSeventyFivePercentile twentyFiveToSeventyFivePercentile = getTwentyFiveToSeventyFivePercentile(hydroResponse);

        return new Forecast(
                new ForecastId(),
                stationId,
                extractTimestamp(hydroResponse),
                mapLine(hydroResponse.plot().data().get(4)),
                mapLine(hydroResponse.plot().data().get(3)),
                mapLine(twentyFiveToSeventyFivePercentile.twentyFivePercentile()),
                mapLine(twentyFiveToSeventyFivePercentile.seventyFivePercentile()),
                mapLine(hydroResponse.plot().data().get(1)),
                mapLine(hydroResponse.plot().data().get(0))
        );
    }
}

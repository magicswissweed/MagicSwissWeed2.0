package com.aa.msw.source.french.vigicrues.historical.lastThirty;

import com.aa.msw.database.helpers.id.LastFewDaysId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.LastFewDays;
import com.aa.msw.source.french.vigicrues.AbstractFrenchLineFetchService;
import com.aa.msw.source.french.vigicrues.model.lastFewDays.VigicruesMeasurement;
import com.aa.msw.source.french.vigicrues.model.lastFewDays.VigicruesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Profile("!test")
@Service
public class FrenchLast30DaysSampleFetchServiceImpl extends AbstractFrenchLineFetchService implements FrenchLast30DaysSampleFetchService {
    private static final Logger LOG = LoggerFactory.getLogger(FrenchLast30DaysSampleFetchServiceImpl.class);

    FrenchLast30DaysSampleFetchServiceImpl() {
        super("https://www.vigicrues.gouv.fr/services/observations.json/index.php?CdStationHydro=", "&GrdSerie=Q&FormatSortie=simple");
    }

    private LastFewDays fetchLast30DaysSamples(ApiStationId stationId) throws IOException, URISyntaxException {
        VigicruesResponse vigicruesResponse = fetchFromVigicrues(stationId);
        Map<OffsetDateTime, Double> line;
        // Check for flow measurement
        List<VigicruesMeasurement> data = vigicruesResponse.serie().line();
        if (data.isEmpty()) {
            throw new IOException("No data available for flow measurement");
        }
        line = data.stream()
                .collect(Collectors.toMap(
                        m -> OffsetDateTime.ofInstant(
                                Instant.ofEpochMilli(m.timestamp()),
                                ZoneOffset.UTC
                        ),
                        VigicruesMeasurement::value
                ));

        return new LastFewDays(
                new LastFewDaysId(),
                stationId,
                line
        );
    }

    @Override
    public Set<LastFewDays> fetchLast30DaysSamples(Set<ApiStationId> stationIds) throws URISyntaxException {
        Set<LastFewDays> result = new HashSet<>();
        for (ApiStationId stationId : stationIds) {
            try {
                result.add(fetchLast30DaysSamples(stationId));

                // Random jittered delay between to avoid pattern detection
                Thread.sleep(100 + (long) (Math.random() * 50));

            } catch (InterruptedException e) {
                LOG.error("wait was interrupted", e);
            } catch (Exception e) {
                // it's also possible that for a certain station the api does not provide the flow for example, then this would also log
                // use the logger to see which stations don't provide the flow (only water height) and to see how many times we get a 503.
                // LOG.error("Error fetching station " + stationId.getExternalId() + ": " + e.getMessage());

                // If we get a 503 even with retries, the server is likely blocking us.
                // Wait 10 seconds before trying the next station in the set.
                if (e.getMessage().contains("503")) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException i) {
                        LOG.error("wait was interrupted", i);
                    }
                }
            }
        }
        return result;
    }
}

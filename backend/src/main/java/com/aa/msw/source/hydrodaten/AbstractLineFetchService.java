package com.aa.msw.source.hydrodaten;

import com.aa.msw.source.AbstractFetchService;
import com.aa.msw.source.hydrodaten.model.line.HydroLine;
import com.aa.msw.source.hydrodaten.model.line.HydroResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractLineFetchService extends AbstractFetchService {

    private final String fetchUrlPrefix;
    private final String fetchUrlSuffix;

    protected AbstractLineFetchService(String fetchUrlPrefix, String fetchUrlSuffix) {
        super();
        this.fetchUrlPrefix = fetchUrlPrefix;
        this.fetchUrlSuffix = fetchUrlSuffix;
    }

    protected static String fetchAsString(String url) throws IOException, URISyntaxException {
        HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
//		conn.setRequestProperty("Authorization", authHeaderValue);
        // ignore.
        // FIXME: There seems to be a bigger problem.
        //  We can fix that later with manual restarts,
        //  but for the moment we don't want the tests to fail because hydrodaten has issues...
//        if ((conn.getResponseCode() != 200) && (conn.getResponseCode() != 404)) {
//            throw new RuntimeException("Failed : HTTP Error code : "
//                    + conn.getResponseCode());
//        }
        InputStreamReader in = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(in);
        return br.readLine();
    }

    protected static Map<OffsetDateTime, Double> mapLine(HydroLine hydroLine) throws IOException {
        if (hydroLine == null || hydroLine.x() == null || hydroLine.y() == null || hydroLine.x().size() != hydroLine.y().size()) {
            throw new IOException("Should be the same number of dates as values.");
        }
        Map<OffsetDateTime, Double> line = new LinkedHashMap<>();
        for (int i = 0; i < hydroLine.x().size(); i++) {
            line.put(hydroLine.x().get(i), hydroLine.y().get(i));
        }
        return line;
    }

    protected HydroResponse fetchFromHydro(int stationId) throws IOException, URISyntaxException {
        String response = fetchAsString(fetchUrlPrefix + stationId + fetchUrlSuffix);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }

    protected HydroLine getFirstHalfOfHydroLine(
            List<OffsetDateTime> inputTimestamps,
            List<Double> inputFlows,
            String name) {
        ArrayList<OffsetDateTime> timestamps = new ArrayList<>();
        ArrayList<Double> flows = new ArrayList<>();

        OffsetDateTime lastTimeStamp = OffsetDateTime.MIN;
        for (int index = 0; index < inputTimestamps.size(); index++) {
            OffsetDateTime timestamp = inputTimestamps.get(index);
            if (timestamp.isAfter(lastTimeStamp)) {
                timestamps.add(timestamp);
                flows.add(inputFlows.get(index));
            }
        }

        return new HydroLine(timestamps, flows, name);
    }


    protected TwentyFiveToSeventyFivePercentile getTwentyFiveToSeventyFivePercentile(HydroResponse hydroResponse) {
        // Hydrodaten does something very strange here. In this line, there are actually two lines.
        // The second line (25 percentile) is ordered backwards (timestamps descending)
        HydroLine twentyFiveToSeventyFivePercentile = hydroResponse.plot().data().get(2);
        ArrayList<OffsetDateTime> twenty5ToSeventy5PercentileTimestamps = twentyFiveToSeventyFivePercentile.x();
        ArrayList<Double> twenty5ToSeventy5PercentileFlows = twentyFiveToSeventyFivePercentile.y();
        HydroLine seventyFivePercentile = getFirstHalfOfHydroLine(
                twenty5ToSeventy5PercentileTimestamps,
                twenty5ToSeventy5PercentileFlows,
                "seventyFivePercentile");
        HydroLine twentyFivePercentile = getFirstHalfOfHydroLine(
                twenty5ToSeventy5PercentileTimestamps.reversed(),
                twenty5ToSeventy5PercentileFlows.reversed(),
                "twentyFivePercentile");
        return new TwentyFiveToSeventyFivePercentile(seventyFivePercentile, twentyFivePercentile);
    }

    protected record TwentyFiveToSeventyFivePercentile(HydroLine seventyFivePercentile,
                                                       HydroLine twentyFivePercentile) {
    }
}

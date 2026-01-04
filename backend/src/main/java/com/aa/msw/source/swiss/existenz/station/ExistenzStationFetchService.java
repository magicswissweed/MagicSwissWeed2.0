package com.aa.msw.source.swiss.existenz.station;
// https://api.existenz.ch/apiv1/hydro/locations?app=magicswissweed.ch&version=0.2.0

import com.aa.msw.source.AbstractFetchService;
import com.aa.msw.source.swiss.existenz.station.model.ExistenzResponseStation;
import com.aa.msw.source.swiss.existenz.station.model.ExistenzStation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExistenzStationFetchService extends AbstractFetchService {
    static String EXISTENZ_FETCH_STATIONS_URL = "https://api.existenz.ch/apiv1/hydro/locations?app=magicswissweed.ch&version=0.2.0";

    private static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<StationInformation> fetchStations() throws IOException, URISyntaxException {
        HashMap<String, ExistenzStation> existenzStations = fetchData(EXISTENZ_FETCH_STATIONS_URL).payload();

        return existenzStations.entrySet().stream()
                .filter((entry) -> isNumber(entry.getKey())) // there are some (few) stations with letters in their ID, not sure how to handle atm
                .map((entry) -> new StationInformation(
                        Integer.parseInt(entry.getKey()),
                        entry.getValue().details().latitude(),
                        entry.getValue().details().longitude()))
                .collect(Collectors.toList());
    }

    private ExistenzResponseStation fetchData(String url) throws IOException, URISyntaxException {
        String response = fetchAsString(url);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }
}

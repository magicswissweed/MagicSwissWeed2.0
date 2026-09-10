package com.aa.msw.source.swiss.hydrodaten.stations;


import com.aa.msw.database.helpers.id.StationId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.enums.Provider;
import com.aa.msw.model.Country;
import com.aa.msw.model.Station;
import com.aa.msw.source.AbstractFetchService;
import com.aa.msw.source.swiss.existenz.station.ExistenzStationFetchService;
import com.aa.msw.source.swiss.existenz.station.StationInformation;
import com.aa.msw.source.swiss.hydrodaten.model.station.HydroStation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SwissStationFetchService extends AbstractFetchService {

    public static final String STATIONS_FETCH_URL = "https://www.hydrodaten.admin.ch/de/seen-und-fluesse/stationen-und-daten.json";
    public static final String STATION_LINK_PREFIX = "https://www.hydrodaten.admin.ch/de/seen-und-fluesse/stationen-und-daten/";

    private final ExistenzStationFetchService existenzStationFetchService;

    public SwissStationFetchService(ExistenzStationFetchService existenzStationFetchService) {
        this.existenzStationFetchService = existenzStationFetchService;
    }

    public Set<Station> fetchStations() {
        try {
            // The stations from existenz api including latitude and longitude
            Map<Integer, StationInformation> existenzStations = existenzStationFetchService.fetchStations().stream()
                    .collect(Collectors.toMap(StationInformation::stationId, s -> s));

            String stationsString = fetchAsString(STATIONS_FETCH_URL);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            List<HydroStation> hydroStations = objectMapper.readValue(stationsString, new TypeReference<>() {
            });
            return hydroStations.stream()
                    .map(hydroStation -> {
                        Double latitude = null;
                        Double longitude = null;
                        try {
                            latitude = existenzStations.get(Integer.parseInt(hydroStation.key())).latitude();
                            longitude = existenzStations.get(Integer.parseInt(hydroStation.key())).longitude();
                        } catch (Exception e) {
                            // somehow the stations returned from hydrodaten are different to the ones from existenz.
                            // that's why this case exists
                        }
                        return new Station(
                                new StationId(),
                                new ApiStationId(Country.CH, hydroStation.key()),
                                hydroStation.label(),
                                latitude,
                                longitude,
                                Provider.HYDRODATEN,
                                null,
                                STATION_LINK_PREFIX + hydroStation.key());
                    })
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }
}

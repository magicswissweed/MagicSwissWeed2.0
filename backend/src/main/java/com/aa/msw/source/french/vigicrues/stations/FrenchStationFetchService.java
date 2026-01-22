package com.aa.msw.source.french.vigicrues.stations;

import com.aa.msw.database.helpers.id.StationId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Station;
import com.aa.msw.source.AbstractFetchService;
import com.aa.msw.source.french.vigicrues.model.allstations.VigicruesStationsApiWrapper;
import com.aa.msw.source.french.vigicrues.model.onespecificstation.VigicruesCoordinates;
import com.aa.msw.source.french.vigicrues.model.onespecificstation.VigicruesStationDetail;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.locationtech.proj4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FrenchStationFetchService extends AbstractFetchService {
    private static final Logger LOG = LoggerFactory.getLogger(FrenchStationFetchService.class);

    public static final String STATIONS_FETCH_URL = "https://www.vigicrues.gouv.fr/services/StaEntVigiCru.json";
    public static final String CERTAIN_STATION_FETCH_URL_PREFIX = "https://www.vigicrues.gouv.fr/services/station.json/index.php?CdStationHydro=";

    public Set<Station> fetchStations() {
        try {
            String stationsString = fetchAsString(STATIONS_FETCH_URL);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            VigicruesStationsApiWrapper vigicruesStationsResponse = objectMapper
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(stationsString, new TypeReference<>() {
                    });
            return vigicruesStationsResponse.stations().stream()
                    .map(vigicruesStation -> {
                        try {
                            // Random jittered delay between to avoid pattern detection
                            Thread.sleep(150 + (long) (Math.random() * 150));

                            VigicruesStationDetail stationDetail = fetchStationDetails(vigicruesStation.id());
                            if (stationDetail.communeCode().startsWith("97")) {
                                // overseas station - unable to transform using french coordinates - each uses different system
                                return null;
                            }

                            ProjCoordinate projCoordinates = transformCoordinates(stationDetail.coordinates());

                            return new Station(
                                    new StationId(),
                                    new ApiStationId(CountryEnum.FR, vigicruesStation.id()),
                                    vigicruesStation.label(),
                                    projCoordinates.y,
                                    projCoordinates.x);
                        } catch (Exception e) {
                            LOG.error("Error fetching station details for station {} - skipping station.", vigicruesStation.id(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            LOG.error("Error fetching stations from vigicrues - returning emptySet", e);
            return Collections.emptySet();
        }
    }

    private static ProjCoordinate transformCoordinates(VigicruesCoordinates frenchCoordinates) {
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem lambert93 = crsFactory.createFromName("EPSG:2154");
        CoordinateReferenceSystem wgs84 = crsFactory.createFromName("EPSG:4326");

        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CoordinateTransform transform = ctFactory.createTransform(lambert93, wgs84);

        ProjCoordinate src = new ProjCoordinate((double) frenchCoordinates.x(), (double) frenchCoordinates.y());
        ProjCoordinate dst = new ProjCoordinate();

        transform.transform(src, dst);

        return dst;
    }

    private VigicruesStationDetail fetchStationDetails(String stationId) throws IOException, URISyntaxException {
        String stationDetailResponse = fetchAsString(CERTAIN_STATION_FETCH_URL_PREFIX + stationId);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(stationDetailResponse, new TypeReference<>() {
                });
    }
}

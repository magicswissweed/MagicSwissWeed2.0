package com.aa.msw.source.german.bw.stations;

import com.aa.msw.database.helpers.id.StationId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Station;
import com.aa.msw.source.AbstractFetchService;
import com.aa.msw.source.german.bw.HvzBwParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DeBwStationFetchService extends AbstractFetchService {
    private static final Logger LOG = LoggerFactory.getLogger(DeBwStationFetchService.class);

    public static final String HVZ_BW_JS_URL = "https://www.hvz.baden-wuerttemberg.de/js/hvz_peg_stmn.js";

    public Set<Station> fetchStations() {
        try {
            String jsContent = fetchAsString(HVZ_BW_JS_URL);
            return HvzBwParser.parse(jsContent).stream()
                    .filter(station -> station.latitude() != null && station.longitude() != null)
                    .map(station -> {
                        try {
                            return new Station(
                                    new StationId(),
                                    new ApiStationId(CountryEnum.DE_BW, station.stationId()),
                                    station.stationName() + "/" + station.riverName(),
                                    station.latitude(),
                                    station.longitude()
                            );
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            LOG.error("Error fetching BW stations: {}", e.getMessage(), e);
            return Collections.emptySet();
        }
    }
}

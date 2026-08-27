package com.aa.msw.source.rivermap.stations;

import com.aa.msw.database.helpers.id.StationId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.enums.Provider;
import com.aa.msw.model.Station;
import com.aa.msw.source.AbstractFetchService;
import com.aa.msw.source.rivermap.RivermapApi;
import com.aa.msw.source.rivermap.RivermapConfigProperties;
import com.aa.msw.source.rivermap.model.RivermapStation;
import com.aa.msw.source.rivermap.model.RivermapStationsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Profile("!test")
@Service
public class RivermapStationFetchService extends AbstractFetchService {
    private static final Logger LOG = LoggerFactory.getLogger(RivermapStationFetchService.class);

    public static final String STATIONS_FETCH_URL = RivermapApi.BASE_URL + "/stations?type=online";

    /**
     * Order in which the translations of river names and source links are preferred.
     */
    private static final List<String> PREFERRED_LANGUAGES = List.of("de", "en", "fr", "it", "es");
    private static final double LATLNG_SCALE = 1_000_000d;

    private final RivermapConfigProperties config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RivermapStationFetchService(RivermapConfigProperties config) {
        this.config = config;
    }

    public Set<Station> fetchStations() {
        if (!config.hasApiKey()) {
            LOG.warn("No rivermap.api-key configured - skipping the Rivermap stations.");
            return Collections.emptySet();
        }
        try {
            RivermapStationsResponse response = objectMapper.readValue(fetchRivermapStations(), RivermapStationsResponse.class);
            Set<Station> stations = response.stations().stream()
                    .filter(RivermapStationFilter::isRelevant)
                    .map(this::toStation)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            LOG.info("Fetched {} stations from Rivermap, {} of them are relevant for us.", response.stations().size(), stations.size());
            return stations;
        } catch (Exception e) {
            LOG.error("Error fetching Rivermap stations: {}", e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    protected String fetchRivermapStations() throws Exception {
        return fetchAsString(STATIONS_FETCH_URL, Map.of(RivermapApi.API_KEY_HEADER, config.getApiKey()));
    }

    private Station toStation(RivermapStation station) {
        try {
            return new Station(
                    new StationId(),
                    new ApiStationId(station.countryCode(), station.id()),
                    label(station),
                    station.latlng().get(0) / LATLNG_SCALE,
                    station.latlng().get(1) / LATLNG_SCALE,
                    Provider.RIVERMAP,
                    blankToNull(station.state()),
                    sourceLink(station).orElse(null));
        } catch (Exception e) {
            LOG.warn("Skipping Rivermap station {} ({}): {}", station.id(), station.name(), e.getMessage());
            return null;
        }
    }

    /**
     * "&lt;station&gt;/&lt;river&gt;" like the BW stations, e.g. "Etzgen/Etzgerbach".
     */
    private static String label(RivermapStation station) {
        return preferredTranslation(station.river())
                .map(river -> station.name() + "/" + river)
                .orElse(station.name());
    }

    /**
     * The page of the authority publishing the data: the flow page is preferred over the level page,
     * languages in the order of {@link #PREFERRED_LANGUAGES}.
     */
    private static Optional<String> sourceLink(RivermapStation station) {
        if (station.sourceLinks() == null) {
            return Optional.empty();
        }
        return Stream.of("flow", "level")
                .map(sensor -> preferredTranslation(linksOfSensor(station.sourceLinks(), sensor)))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Map<String, String> linksOfSensor(Map<String, Map<String, String>> sourceLinks, String sensor) {
        Map<String, String> byLanguage = new LinkedHashMap<>();
        sourceLinks.forEach((language, links) -> {
            if (links != null) {
                byLanguage.put(language, links.get(sensor));
            }
        });
        return byLanguage;
    }

    private static Optional<String> preferredTranslation(Map<String, String> byLanguage) {
        if (byLanguage == null) {
            return Optional.empty();
        }
        Stream<String> languages = Stream.concat(PREFERRED_LANGUAGES.stream(), byLanguage.keySet().stream().sorted());
        return languages
                .map(byLanguage::get)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .findFirst();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}

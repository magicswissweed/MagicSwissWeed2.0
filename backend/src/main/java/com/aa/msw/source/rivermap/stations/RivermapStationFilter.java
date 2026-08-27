package com.aa.msw.source.rivermap.stations;

import com.aa.msw.source.rivermap.model.RivermapStation;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Decides which of Rivermap's stations we want in our database.
 * <p>
 * Rivermap aggregates the gauges of many authorities - including the ones we already fetch ourselves (BAFU via
 * hydrodaten, Vigicrues, HVZ Baden-Württemberg). Their ids at Rivermap have nothing to do with the ids we know, so
 * such stations are recognised by the authority's domain in their source links and skipped to avoid duplicates.
 * Gauges of other authorities in the same regions (e.g. cantonal stations in CH) are kept.
 * <p>
 * Exception: France is skipped completely. Rivermap also lists French gauges via Hydroportail (hydro.eaufrance.fr),
 * and about half of those are the very same gauges we already have from Vigicrues, just under another link.
 */
public final class RivermapStationFilter {

    /**
     * Hosts of the authorities whose stations we fetch directly with our own providers.
     * A new provider fetched directly = one more entry here.
     */
    static final Set<String> HOSTS_OF_OWN_PROVIDERS = Set.of(
            "hydrodaten.admin.ch",          // Provider.HYDRODATEN
            "vigicrues.gouv.fr",            // Provider.VIGICRUES
            "hvz.baden-wuerttemberg.de"     // Provider.HVZ_BW
    );

    /**
     * Countries we do not take any station from, because our own provider covers them and the stations listed by
     * Rivermap under other authorities are mostly duplicates of ours (see class comment).
     */
    static final Set<String> COUNTRIES_COVERED_BY_OWN_PROVIDERS = Set.of(
            "FR"                            // Provider.VIGICRUES
    );

    private static final String TYPE_ONLINE = "online";
    private static final Set<String> RELEVANT_SENSORS = Set.of("level", "flow");

    private RivermapStationFilter() {
    }

    public static boolean isRelevant(RivermapStation station) {
        return TYPE_ONLINE.equals(station.type())
                && Boolean.TRUE.equals(station.isActive())
                && hasRelevantSensor(station)
                && hasCoordinates(station)
                && !isInCountryCoveredByOwnProvider(station)
                && !isFetchedByOwnProvider(station);
    }

    static boolean isInCountryCoveredByOwnProvider(RivermapStation station) {
        return station.countryCode() != null && COUNTRIES_COVERED_BY_OWN_PROVIDERS.contains(station.countryCode());
    }

    static boolean isFetchedByOwnProvider(RivermapStation station) {
        return sourceLinks(station)
                .map(RivermapStationFilter::host)
                .anyMatch(host -> HOSTS_OF_OWN_PROVIDERS.stream()
                        .anyMatch(own -> host.equals(own) || host.endsWith("." + own)));
    }

    private static boolean hasRelevantSensor(RivermapStation station) {
        return station.sensors() != null && station.sensors().stream().anyMatch(RELEVANT_SENSORS::contains);
    }

    private static boolean hasCoordinates(RivermapStation station) {
        return station.latlng() != null && station.latlng().size() == 2
                && station.latlng().get(0) != null && station.latlng().get(1) != null;
    }

    static Stream<String> sourceLinks(RivermapStation station) {
        if (station.sourceLinks() == null) {
            return Stream.empty();
        }
        return station.sourceLinks().values().stream()
                .filter(Objects::nonNull)
                .map(Map::values)
                .flatMap(java.util.Collection::stream)
                .filter(Objects::nonNull)
                .filter(url -> !url.isBlank());
    }

    private static String host(String url) {
        try {
            String host = new URI(url).getHost();
            return host == null ? "" : host.toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }
}

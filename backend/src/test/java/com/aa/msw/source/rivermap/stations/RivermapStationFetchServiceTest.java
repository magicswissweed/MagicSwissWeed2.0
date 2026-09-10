package com.aa.msw.source.rivermap.stations;

import com.aa.msw.gen.jooq.enums.Provider;
import com.aa.msw.helper.TestResourceLoader;
import com.aa.msw.model.Station;
import com.aa.msw.source.rivermap.RivermapConfigProperties;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RivermapStationFetchServiceTest {

    private final RivermapStationFetchService service = service("some-api-key");

    @Test
    void shouldSkipStationsOfAuthoritiesWeFetchOurselves() {
        Set<String> labels = service.fetchStations().stream().map(Station::label).collect(Collectors.toSet());

        // Ocourt is a BAFU station (hydrodaten link) -> already fetched by our HYDRODATEN provider.
        // Alleyras is French (Hydroportail link) -> France is skipped completely, Vigicrues has these gauges.
        // Albbruck lies in Baden-Württemberg but is a Swiss Canoe gauge (datacake), not HVZ -> we want it.
        assertEquals(Set.of("Etzgen/Etzgerbach", "Ramales/Asón", "Peißenberg/Ammer", "Albbruck by Swiss Canoe/Alb"), labels);
    }

    @Test
    void shouldMapRivermapStationToOurStationModel() {
        Station ramales = station("e5b917e0-b25d-45a4-a73a-3ee22adf037c");

        assertEquals("ES", ramales.stationId().getCountry());
        assertEquals("Ramales/Asón", ramales.label());
        assertEquals(43.263805, ramales.latitude(), 1e-9);
        assertEquals(-3.461539, ramales.longitude(), 1e-9);
        assertEquals(Provider.RIVERMAP, ramales.provider());
        assertEquals("Asón", ramales.state());
        assertEquals("https://www.chcantabrico.es/sistema-automatico-de-informacion-detalle-estacion?cod_estacion=A141", ramales.sourceLink());
    }

    @Test
    void shouldPreferFlowLinkAndFallBackToLevelLink() {
        // Peißenberg has both links -> the flow page
        assertEquals("https://www.hnd.bayern.de/pegel/isar/peissenberg-16612001/abfluss?",
                station("ad56d635-f178-5c32-a6af-c3a86971ca9c").sourceLink());
        // Albbruck has no flow link -> the level page
        assertEquals("https://app.datacake.de/pd/5cdcf449-6246-44e3-9574-4d4d42b3cbc8",
                station("2304ffae-4083-4589-904e-0146b1323a5e").sourceLink());
    }

    @Test
    void shouldReturnEmptySetWithoutApiKey() {
        assertTrue(service(null).fetchStations().isEmpty());
        assertTrue(service(" ").fetchStations().isEmpty());
    }

    private Station station(String rivermapId) {
        return service.fetchStations().stream()
                .filter(s -> s.stationId().getExternalId().equals(rivermapId))
                .findFirst()
                .orElseThrow();
    }

    private static RivermapStationFetchService service(String apiKey) {
        RivermapConfigProperties config = new RivermapConfigProperties();
        config.setApiKey(apiKey);
        return new RivermapStationFetchService(config) {
            @Override
            protected String fetchRivermapStations() {
                return TestResourceLoader.load("/testdata/rivermap_stations.json");
            }
        };
    }
}

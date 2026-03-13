package com.aa.msw.source.german.bw;

import com.aa.msw.source.german.bw.model.HvzBwStation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HvzBwParsingTest {

    @Test
    void shouldParseStationsFromJsFile() throws IOException {
        String jsContent = loadTestFile();
        List<HvzBwStation> stations = HvzBwParser.parse(jsContent);

        assertEquals(4, stations.size());
    }

    @Test
    void shouldParseStationDetails() throws IOException {
        String jsContent = loadTestFile();
        List<HvzBwStation> stations = HvzBwParser.parse(jsContent);

        HvzBwStation first = stations.getFirst();
        assertEquals("00435", first.stationId());
        assertEquals("Wiesloch-Hilfspegel", first.stationName());
        assertEquals("Leimbach", first.riverName());
        assertEquals(49.292906, first.latitude(), 0.0001);
        assertEquals(8.688106, first.longitude(), 0.0001);
    }

    @Test
    void shouldParseFlowValues() throws IOException {
        String jsContent = loadTestFile();
        List<HvzBwStation> stations = HvzBwParser.parse(jsContent);

        HvzBwStation first = stations.getFirst();
        assertTrue(first.flowValue().isPresent());
        assertEquals(0.28, first.flowValue().get(), 0.001);
        assertTrue(first.flowTimestamp().isPresent());
    }

    @Test
    void shouldParseMezTimestamp() throws IOException {
        String jsContent = loadTestFile();
        List<HvzBwStation> stations = HvzBwParser.parse(jsContent);

        HvzBwStation first = stations.getFirst();
        OffsetDateTime expected = OffsetDateTime.of(2026, 3, 13, 14, 0, 0, 0, ZoneOffset.ofHours(1));
        assertEquals(expected, first.flowTimestamp().get());
    }

    @Test
    void shouldParseMeszTimestamp() {
        // MESZ = UTC+2 (summer time)
        var result = HvzBwParser.parseOptionalTimestamp("13.03.2026 14:30 MESZ");
        assertTrue(result.isPresent());
        assertEquals(ZoneOffset.ofHours(2), result.get().getOffset());
    }

    @Test
    void shouldParseWhenContentIsOnSingleLine() throws IOException {
        // AbstractFetchService.fetchAsString() strips newlines, so real data is one long line
        String jsContent = loadTestFile().replace("\n", "").replace("\r", "");
        List<HvzBwStation> stations = HvzBwParser.parse(jsContent);

        assertEquals(4, stations.size());
        assertEquals("00435", stations.getFirst().stationId());
        assertTrue(stations.getFirst().flowValue().isPresent());
    }

    @Test
    void shouldHandleStationWithoutFlowData() throws IOException {
        String jsContent = loadTestFile();
        List<HvzBwStation> stations = HvzBwParser.parse(jsContent);

        // Station 00099 has no flow unit (empty string) so should have no flow value
        HvzBwStation noFlowStation = stations.stream()
                .filter(s -> s.stationId().equals("00099"))
                .findFirst().orElseThrow();
        assertTrue(noFlowStation.flowValue().isEmpty());
    }

    private String loadTestFile() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/testdata/hvz_bw_stations.js")) {
            assertNotNull(is);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

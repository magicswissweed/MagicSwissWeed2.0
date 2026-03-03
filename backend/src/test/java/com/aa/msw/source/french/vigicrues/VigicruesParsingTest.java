package com.aa.msw.source.french.vigicrues;

import com.aa.msw.source.french.vigicrues.model.lastFewDays.VigicruesMeasurement;
import com.aa.msw.source.french.vigicrues.model.lastFewDays.VigicruesResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class VigicruesParsingTest {

    @Test
    void shouldParseVigicruesResponse() throws IOException {
        VigicruesResponse response = parseTestFile();

        assertNotNull(response.serie());
        assertNotNull(response.serie().line());
        assertEquals(5, response.serie().line().size());
    }

    @Test
    void shouldParseMeasurementsAsTimestampValuePairs() throws IOException {
        VigicruesResponse response = parseTestFile();
        List<VigicruesMeasurement> measurements = response.serie().line();

        // First measurement
        VigicruesMeasurement first = measurements.getFirst();
        assertEquals(1768691100000L, first.timestamp());
        assertEquals(59.2, first.value(), 0.01);

        // Last measurement
        VigicruesMeasurement last = measurements.getLast();
        assertEquals(1768692300000L, last.timestamp());
        assertEquals(60.3, last.value(), 0.01);
    }

    @Test
    void shouldConvertTimestampsToOffsetDateTime() throws IOException {
        VigicruesResponse response = parseTestFile();

        // Vigicrues timestamps are milliseconds since epoch
        Map<OffsetDateTime, Double> line = response.serie().line().stream()
                .collect(Collectors.toMap(
                        m -> OffsetDateTime.ofInstant(
                                Instant.ofEpochMilli(m.timestamp()),
                                ZoneOffset.UTC
                        ),
                        VigicruesMeasurement::value
                ));

        assertEquals(5, line.size());
        assertTrue(line.values().stream().allMatch(v -> v > 0));
    }

    @Test
    void shouldHaveChronologicallyOrderedMeasurements() throws IOException {
        VigicruesResponse response = parseTestFile();
        List<VigicruesMeasurement> measurements = response.serie().line();

        for (int i = 1; i < measurements.size(); i++) {
            assertTrue(measurements.get(i).timestamp() > measurements.get(i - 1).timestamp(),
                    "Measurements should be chronologically ordered");
        }
    }

    private VigicruesResponse parseTestFile() throws IOException {
        InputStream json = getClass().getResourceAsStream("/testdata/vigicrues_observations.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(json, VigicruesResponse.class);
    }
}

package com.aa.msw.source.swiss.hydrodaten.forecast;

import com.aa.msw.source.swiss.hydrodaten.model.line.HydroLine;
import com.aa.msw.source.swiss.hydrodaten.model.line.HydroResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SwissForecastParsingTest {

    @Test
    void shouldParseHydrodatenForecastResponse() throws IOException {
        HydroResponse response = parseTestFile();

        assertNotNull(response.plot());
        assertEquals(5, response.plot().data().size(), "Forecast should have 5 data lines");
        assertEquals(2, response.plot().layout().annotations().size());
    }

    @Test
    void shouldParseForecastDataLineNames() throws IOException {
        HydroResponse response = parseTestFile();

        assertEquals("Min. / Max.", response.plot().data().get(0).name());
        assertEquals("Min. / Max.", response.plot().data().get(1).name());
        assertEquals("25.-75. Perzentil", response.plot().data().get(2).name());
        assertEquals("Median", response.plot().data().get(3).name());
        assertEquals("Gemessen", response.plot().data().get(4).name());
    }

    @Test
    void shouldParseTimestampsAndValues() throws IOException {
        HydroResponse response = parseTestFile();

        // Measured data (data[4] = "Gemessen")
        HydroLine measured = response.plot().data().get(4);
        assertFalse(measured.x().isEmpty());
        assertFalse(measured.y().isEmpty());
        assertEquals(measured.x().size(), measured.y().size());

        // All flow values should be positive
        assertTrue(measured.y().stream().allMatch(v -> v > 0));

        // Timestamps should be parseable OffsetDateTime
        for (OffsetDateTime dt : measured.x()) {
            assertNotNull(dt);
        }
    }

    @Test
    void shouldParsePercentileLineWithAscendingThenDescendingTimestamps() throws IOException {
        HydroResponse response = parseTestFile();

        // The 25-75 percentile line (data[2]) contains two concatenated lines:
        // first half ascending, second half descending
        HydroLine percentile = response.plot().data().get(2);
        assertEquals(6, percentile.x().size());

        // First 3 should be ascending
        assertTrue(percentile.x().get(0).isBefore(percentile.x().get(1)) ||
                percentile.x().get(0).isEqual(percentile.x().get(1)));
        assertTrue(percentile.x().get(1).isBefore(percentile.x().get(2)) ||
                percentile.x().get(1).isEqual(percentile.x().get(2)));

        // Last 3 should be descending (or equal) — they're the reversed second line
        assertTrue(percentile.x().get(3).isAfter(percentile.x().get(4)) ||
                percentile.x().get(3).isEqual(percentile.x().get(4)));
    }

    @Test
    void shouldExtractForecastTimestampFromAnnotation() throws IOException {
        HydroResponse response = parseTestFile();

        String timestampStr = response.plot().layout().annotations().stream()
                .filter(a -> a.xref().equals("x"))
                .findFirst()
                .orElseThrow()
                .x();

        OffsetDateTime timestamp = OffsetDateTime.parse(timestampStr);
        assertNotNull(timestamp);
        assertEquals("2026-03-03T10:00:00.000+01:00", timestampStr);
    }

    private HydroResponse parseTestFile() throws IOException {
        InputStream json = getClass().getResourceAsStream("/testdata/hydrodaten_forecast.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(json, new TypeReference<>() {});
    }
}

package com.aa.msw.source.swiss.hydrodaten.historical.lastfourty;

import com.aa.msw.source.swiss.hydrodaten.model.line.HydroLine;
import com.aa.msw.source.swiss.hydrodaten.model.line.HydroResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class SwissLast40DaysParsingTest {

    @Test
    void shouldParseHydrodatenLast40DaysResponse() throws IOException {
        HydroResponse response = parseTestFile();

        assertNotNull(response.plot());
        assertEquals(2, response.plot().data().size(), "Last 40 days should have 2 lines: water level + flow");
    }

    @Test
    void shouldParseWaterLevelAndFlowLines() throws IOException {
        HydroResponse response = parseTestFile();

        HydroLine waterLevel = response.plot().data().get(0);
        assertEquals("Wasserstand", waterLevel.name());
        assertFalse(waterLevel.x().isEmpty());
        assertEquals(waterLevel.x().size(), waterLevel.y().size());

        HydroLine flow = response.plot().data().get(1);
        assertEquals("Abfluss", flow.name());
        assertFalse(flow.x().isEmpty());
        assertEquals(flow.x().size(), flow.y().size());
    }

    @Test
    void shouldHavePositiveFlowValues() throws IOException {
        HydroResponse response = parseTestFile();

        HydroLine flow = response.plot().data().get(1);
        assertTrue(flow.y().stream().allMatch(v -> v > 0));
    }

    private HydroResponse parseTestFile() throws IOException {
        InputStream json = getClass().getResourceAsStream("/testdata/hydrodaten_last40days.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(json, new TypeReference<>() {});
    }
}

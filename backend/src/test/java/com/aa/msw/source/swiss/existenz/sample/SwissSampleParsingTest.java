package com.aa.msw.source.swiss.existenz.sample;

import com.aa.msw.source.swiss.existenz.sample.model.ExistenzResponseSample;
import com.aa.msw.source.swiss.existenz.sample.model.ExistenzSample;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SwissSampleParsingTest {

    @Test
    void shouldParseExistenzResponse() throws IOException {
        ExistenzResponseSample response = parseTestFile();

        assertNotNull(response.source());
        assertNotNull(response.apiUrl());
        assertEquals(4, response.payload().size());
    }

    @Test
    void shouldParseFlowAndTemperatureEntries() throws IOException {
        ExistenzResponseSample response = parseTestFile();
        List<ExistenzSample> payload = response.payload();

        // Station 2018 has flow and temperature
        ExistenzSample flow2018 = payload.stream()
                .filter(s -> s.stationId().equals("2018") && s.par().equals("flow"))
                .findFirst().orElseThrow();
        assertEquals(81.77, flow2018.value(), 0.01);
        assertTrue(flow2018.timestamp() > 0);

        ExistenzSample temp2018 = payload.stream()
                .filter(s -> s.stationId().equals("2018") && s.par().equals("temperature"))
                .findFirst().orElseThrow();
        assertEquals(8.62, temp2018.value(), 0.01);

        // Station 2243 has flow and temperature
        ExistenzSample flow2243 = payload.stream()
                .filter(s -> s.stationId().equals("2243") && s.par().equals("flow"))
                .findFirst().orElseThrow();
        assertEquals(62.25, flow2243.value(), 0.01);
    }

    @Test
    void shouldGroupSamplesByStation() throws IOException {
        ExistenzResponseSample response = parseTestFile();

        long station2018Count = response.payload().stream()
                .filter(s -> s.stationId().equals("2018"))
                .count();
        long station2243Count = response.payload().stream()
                .filter(s -> s.stationId().equals("2243"))
                .count();

        assertEquals(2, station2018Count, "Station 2018 should have flow + temperature");
        assertEquals(2, station2243Count, "Station 2243 should have flow + temperature");
    }

    private ExistenzResponseSample parseTestFile() throws IOException {
        InputStream json = getClass().getResourceAsStream("/testdata/existenz_samples.json");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<>() {});
    }
}

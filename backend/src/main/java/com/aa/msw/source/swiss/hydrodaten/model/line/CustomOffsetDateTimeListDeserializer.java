package com.aa.msw.source.swiss.hydrodaten.model.line;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

// helps deserializing the dates returned from hydrodaten
public class CustomOffsetDateTimeListDeserializer extends JsonDeserializer<List<OffsetDateTime>> {
    @Override
    public List<OffsetDateTime> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<String> dates = p.readValueAs(new TypeReference<>() {
        });
        return dates.stream()
                .map(date -> {
                    if (date.contains("T")) {
                        // for example we get something like this for a forecast: "2024-12-29T00:00:00.000+01:00"
                        return OffsetDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    } else {
                        // and we get something like this for example in the historical data: "2025-01-01"
                        return OffsetDateTime.parse(date + "T00:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    }
                })
                .collect(Collectors.toList());
    }
}

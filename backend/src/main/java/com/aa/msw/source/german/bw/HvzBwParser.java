package com.aa.msw.source.german.bw;

import com.aa.msw.source.german.bw.model.HvzBwStation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class HvzBwParser {
    private static final Logger LOG = LoggerFactory.getLogger(HvzBwParser.class);

    // MEZ = UTC+1 (Central European Time), MESZ = UTC+2 (Central European Summer Time)
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
            "dd.MM.yyyy HH:mm", Locale.GERMAN
    );

    private HvzBwParser() {
    }

    public static List<HvzBwStation> parse(String jsContent) {
        List<HvzBwStation> stations = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        // Split into individual station entries by splitting on "],["
        // This works whether the content has newlines or is a single line
        for (String entry : extractStationEntries(jsContent)) {
            try {
                // Replace single quotes with double quotes for JSON parsing
                String jsonLine = entry.replace("'", "\"");
                List<Object> fields = objectMapper.readValue(jsonLine, new TypeReference<>() {
                });

                if (fields.size() < 22) {
                    continue;
                }

                String stationId = asString(fields.get(0));
                String stationName = asString(fields.get(1));
                String riverName = asString(fields.get(2));

                Double latitude = asDouble(fields.get(21));
                Double longitude = asDouble(fields.get(20));

                // Flow value at index 7, unit at index 8, timestamp at index 9
                Optional<Double> flowValue = Optional.empty();
                Optional<OffsetDateTime> flowTimestamp = Optional.empty();

                String flowUnit = asString(fields.get(8));
                if ("m³/s".equals(flowUnit)) {
                    flowValue = parseOptionalDouble(fields.get(7));
                    flowTimestamp = parseOptionalTimestamp(fields.get(9));
                }

                stations.add(new HvzBwStation(
                        stationId, stationName, riverName,
                        latitude, longitude,
                        flowValue, flowTimestamp
                ));
            } catch (Exception e) {
                LOG.warn("Failed to parse HVZ BW station line: {}", e.getMessage());
            }
        }

        return stations;
    }

    static List<String> extractStationEntries(String jsContent) {
        // Find the start of the data array: first occurrence of ['
        int dataStart = jsContent.indexOf("['");
        if (dataStart < 0) {
            return List.of();
        }

        // Find the last ] that closes the outer array
        int dataEnd = jsContent.lastIndexOf(']');
        if (dataEnd < 0) {
            return List.of();
        }

        // Extract just the data portion and split on "],["
        // Each station entry is [...],[...] so splitting on "],[" gives us the inner parts
        String dataPortion = jsContent.substring(dataStart, dataEnd + 1);
        String[] rawEntries = dataPortion.split("],\\s*\\[");

        List<String> entries = new ArrayList<>();
        for (String rawEntry : rawEntries) {
            String entry = rawEntry;
            // Re-add the brackets that were consumed by the split
            if (!entry.startsWith("[")) {
                entry = "[" + entry;
            }
            if (!entry.endsWith("]")) {
                entry = entry + "]";
            }
            entries.add(entry);
        }
        return entries;
    }

    private static String asString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    private static Double asDouble(Object obj) {
        if (obj instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Optional<Double> parseOptionalDouble(Object obj) {
        if (obj == null) {
            return Optional.empty();
        }
        String str = obj.toString().trim();
        if (str.isEmpty() || "0".equals(str)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Double.parseDouble(str));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    static Optional<OffsetDateTime> parseOptionalTimestamp(Object obj) {
        if (obj == null) {
            return Optional.empty();
        }
        String str = obj.toString().trim();
        if (str.isEmpty()) {
            return Optional.empty();
        }
        try {
            // Determine timezone offset from suffix
            ZoneOffset offset;
            String dateTimePart;
            if (str.endsWith(" MESZ")) {
                offset = ZoneOffset.ofHours(2);
                dateTimePart = str.substring(0, str.length() - 5);
            } else if (str.endsWith(" MEZ")) {
                offset = ZoneOffset.ofHours(1);
                dateTimePart = str.substring(0, str.length() - 4);
            } else {
                offset = ZoneOffset.ofHours(1); // default to MEZ
                dateTimePart = str;
            }

            return Optional.of(
                    java.time.LocalDateTime.parse(dateTimePart.trim(), TIMESTAMP_FORMATTER)
                            .atOffset(offset)
            );
        } catch (Exception e) {
            LOG.warn("Failed to parse BW timestamp '{}': {}", str, e.getMessage());
            return Optional.empty();
        }
    }
}

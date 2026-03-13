package com.aa.msw.source.german.bw.model;

import java.time.OffsetDateTime;
import java.util.Optional;

public record HvzBwStation(
        String stationId,
        String stationName,
        String riverName,
        Double latitude,
        Double longitude,
        Optional<Double> flowValue,
        Optional<OffsetDateTime> flowTimestamp
) {
}

package com.aa.msw.api.graph;

import com.aa.msw.gen.api.ApiLineEntry;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AbstractGraphLineApiService {
    protected static List<ApiLineEntry> mapApiLine(Map<OffsetDateTime, Double> input) {
        return input.entrySet().stream()
                .map(entry -> {
                    ApiLineEntry output = new ApiLineEntry();
                    output.setTimestamp(entry.getKey());
                    output.setValue(entry.getValue());
                    return output;
                })
                .collect(Collectors.toList());
    }
}

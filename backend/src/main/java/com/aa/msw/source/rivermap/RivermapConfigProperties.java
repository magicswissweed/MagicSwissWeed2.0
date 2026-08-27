package com.aa.msw.source.rivermap;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration of the Rivermap API (https://api.rivermap.org).
 * Bound from {@code rivermap.api-key} (properties)
 */
@Component
@ConfigurationProperties(prefix = "rivermap")
@Data
public class RivermapConfigProperties {
    private String apiKey;

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}

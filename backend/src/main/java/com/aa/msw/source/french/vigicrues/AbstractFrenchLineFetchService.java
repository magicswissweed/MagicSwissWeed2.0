package com.aa.msw.source.french.vigicrues;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.source.AbstractFetchService;
import com.aa.msw.source.french.vigicrues.model.lastFewDays.VigicruesResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class AbstractFrenchLineFetchService extends AbstractFetchService {

    private final String fetchUrlPrefix;
    private final String fetchUrlSuffix;

    protected AbstractFrenchLineFetchService(String fetchUrlPrefix, String fetchUrlSuffix) {
        super();
        this.fetchUrlPrefix = fetchUrlPrefix;
        this.fetchUrlSuffix = fetchUrlSuffix;
    }

    protected VigicruesResponse fetchFromVigicrues(ApiStationId stationId) throws IOException, URISyntaxException {
        String response = fetchAsString(fetchUrlPrefix + stationId.getExternalId() + fetchUrlSuffix);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(response, VigicruesResponse.class);
    }
}

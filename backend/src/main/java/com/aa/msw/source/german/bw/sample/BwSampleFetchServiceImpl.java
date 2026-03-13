package com.aa.msw.source.german.bw.sample;

import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Sample;
import com.aa.msw.source.AbstractFetchService;
import com.aa.msw.source.german.bw.HvzBwParser;
import com.aa.msw.source.german.bw.model.HvzBwStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Profile("!test")
@Service
public class BwSampleFetchServiceImpl extends AbstractFetchService implements BwSampleFetchService {
    private static final Logger LOG = LoggerFactory.getLogger(BwSampleFetchServiceImpl.class);

    public static final String HVZ_BW_JS_URL = "https://www.hvz.baden-wuerttemberg.de/js/hvz_peg_stmn.js";

    @Override
    public List<Sample> fetchSamples(Set<ApiStationId> stationIds) {
        try {
            Set<String> externalIds = stationIds.stream()
                    .map(ApiStationId::getExternalId)
                    .collect(Collectors.toSet());

            String jsContent = fetchHvzBwData();
            List<HvzBwStation> allStations = HvzBwParser.parse(jsContent);

            return allStations.stream()
                    .filter(station -> externalIds.contains(station.stationId()))
                    .filter(station -> station.flowValue().isPresent() && station.flowTimestamp().isPresent())
                    .map(station -> new Sample(
                            new SampleId(),
                            new ApiStationId(CountryEnum.DE_BW, station.stationId()),
                            station.flowTimestamp().get(),
                            Optional.empty(),
                            station.flowValue().get().intValue()
                    ))
                    .toList();
        } catch (Exception e) {
            LOG.error("Error fetching BW samples: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    protected String fetchHvzBwData() throws Exception {
        return fetchAsString(HVZ_BW_JS_URL);
    }
}

package com.aa.msw.source.german.bw.sample;

import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
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
            Map<String, ApiStationId> stationIdsByExternalId = stationIds.stream()
                    .collect(Collectors.toMap(ApiStationId::getExternalId, id -> id, (a, b) -> a));

            String jsContent = fetchHvzBwData();
            List<HvzBwStation> allStations = HvzBwParser.parse(jsContent);

            List<Sample> samples = new ArrayList<>();
            for (HvzBwStation station : allStations) {
                ApiStationId stationId = stationIdsByExternalId.get(station.stationId());
                if (stationId == null) {
                    continue;
                }
                if (station.flowValue().isPresent() && station.flowTimestamp().isPresent()) {
                    samples.add(new Sample(
                            new SampleId(),
                            stationId,
                            station.flowTimestamp().get(),
                            station.flowValue().get(),
                            ApiMeasurementType.FLOW
                    ));
                }
                if (station.heightValue().isPresent() && station.heightTimestamp().isPresent()) {
                    samples.add(new Sample(
                            new SampleId(),
                            stationId,
                            station.heightTimestamp().get(),
                            station.heightValue().get(),
                            ApiMeasurementType.HEIGHT
                    ));
                }
            }
            return samples;
        } catch (Exception e) {
            LOG.error("Error fetching BW samples: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    protected String fetchHvzBwData() throws Exception {
        return fetchAsString(HVZ_BW_JS_URL);
    }
}

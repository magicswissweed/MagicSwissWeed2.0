package com.aa.msw.source.swiss.existenz.sample;

import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Sample;
import com.aa.msw.source.AbstractFetchService;
import com.aa.msw.source.swiss.existenz.sample.model.ExistenzResponseSample;
import com.aa.msw.source.swiss.existenz.sample.model.ExistenzSample;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Profile("!test")
@Service
public class SwissSampleFetchServiceImpl extends AbstractFetchService implements SwissSampleFetchService {
    private static final Logger LOG = LoggerFactory.getLogger(SwissSampleFetchServiceImpl.class);

    private static String getExistenzUrl(Set<ApiStationId> stationIds) {
        String locationsString = stationIds.stream()
                .map(ApiStationId::getExternalId)
                .map(Object::toString)
                .collect(Collectors.joining("%2C"));
        return "https://api.existenz.ch/apiv1/hydro/latest?locations=" + locationsString + "&parameters=flow%2C%20temperature&app=MagicSwissWeed&version=0.2.0";
    }

    private static List<Sample> extractSamplesForStationId(List<ExistenzSample> samples, String stationId) {
        ApiStationId apiStationId = new ApiStationId(CountryEnum.CH, stationId);
        List<Sample> result = new ArrayList<>();
        for (ExistenzSample sample : samples) {
            if (!sample.stationId().equals(stationId)) {
                continue;
            }
            ApiMeasurementType type = switch (sample.par()) {
                case "flow" -> ApiMeasurementType.FLOW;
                case "temperature" -> ApiMeasurementType.TEMPERATURE;
                default -> null;
            };
            if (type == null) {
                continue;
            }
            OffsetDateTime timestamp = Instant.ofEpochSecond(sample.timestamp()).atOffset(ZoneOffset.UTC);
            result.add(new Sample(new SampleId(), apiStationId, timestamp, sample.value(), type));
        }
        return result;
    }

    public List<Sample> fetchSamples(Set<ApiStationId> stationIds) {
        List<ExistenzSample> existenzSamples;
        try {
            String fetchUrl = getExistenzUrl(stationIds);
            existenzSamples = fetchData(fetchUrl).payload();
        } catch (Exception e) {
            LOG.error("Error while fetching samples from existenz for stationIds {}. Ignore the current fetch.", stationIds, e);
            return List.of();
        }
        return stationIds.stream()
                .flatMap(id -> extractSamplesForStationId(existenzSamples, id.getExternalId()).stream())
                .collect(Collectors.toList());
    }

    private ExistenzResponseSample fetchData(String url) throws IOException, URISyntaxException {
        String response = fetchAsString(url);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }
}

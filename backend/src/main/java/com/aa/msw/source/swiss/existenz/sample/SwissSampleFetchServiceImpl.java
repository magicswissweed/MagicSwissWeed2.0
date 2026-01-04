package com.aa.msw.source.swiss.existenz.sample;

import com.aa.msw.database.helpers.id.SampleId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.model.Sample;
import com.aa.msw.source.AbstractFetchService;
import com.aa.msw.source.swiss.existenz.exception.IncorrectDataReceivedException;
import com.aa.msw.source.swiss.existenz.sample.model.ExistenzResponseSample;
import com.aa.msw.source.swiss.existenz.sample.model.ExistenzSample;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Profile("!test")
@Service
// TODO: this is only for CH. Handle this for france
public class SwissSampleFetchServiceImpl extends AbstractFetchService implements SwissSampleFetchService {
    private static String getExistenzUrl(Set<ApiStationId> stationIds) {
        String locationsString = stationIds.stream()
                .map(ApiStationId::getExternalId)
                .map(Object::toString)
                .collect(Collectors.joining("%2C"));
        return "https://api.existenz.ch/apiv1/hydro/latest?locations=" + locationsString + "&parameters=flow%2C%20temperature&app=MagicSwissWeed&version=0.2.0";
    }

    private static Sample extractSampleForStationId(List<ExistenzSample> samples, String stationId) throws IncorrectDataReceivedException {
        List<ExistenzSample> stationSamples = samples.stream()
                .filter(sample -> sample.stationId().equals(stationId))
                .toList();

        Integer flow = null;
        Optional<Double> temp = Optional.empty();
        OffsetDateTime timestamp = null;
        for (ExistenzSample sample : stationSamples) {
            if (sample.par().equals("flow")) {
                flow = (int) sample.value();
                timestamp = Instant.ofEpochSecond(sample.timestamp()).atOffset(ZoneOffset.UTC);
            } else if (sample.par().equals("temperature")) {
                temp = Optional.of(sample.value());
            }
        }

        if (flow == null) {
            throw new IncorrectDataReceivedException("Unable to extract flow and temp for the station " + stationId + " in " + CountryEnum.CH.getValue());
        }

        return new Sample(
                new SampleId(),
                new ApiStationId(CountryEnum.CH, stationId),
                timestamp,
                temp,
                flow);
    }

    public List<Sample> fetchSamples(Set<ApiStationId> stationIds) throws IOException, URISyntaxException {
        String fetchUrl = getExistenzUrl(stationIds);
        List<ExistenzSample> existenzSamples = fetchData(fetchUrl).payload();
        List<Sample> samples = new ArrayList<>();
        for (ApiStationId stationId : stationIds) {
            try {
                samples.add(extractSampleForStationId(existenzSamples, stationId.getExternalId()));
            } catch (IncorrectDataReceivedException e) {
                // ignore -> dont fetch sample
            }
        }
        return samples;
    }

    private ExistenzResponseSample fetchData(String url) throws IOException, URISyntaxException {
        String response = fetchAsString(url);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }
}

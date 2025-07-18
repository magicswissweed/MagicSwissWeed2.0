/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (7.5.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.aa.msw.gen.api;

import com.aa.msw.gen.api.ApiForecast;
import com.aa.msw.gen.api.StationToApiForecasts;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-06-09T13:29:56.518144+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
@Validated
@Tag(name = "forecast", description = "the forecast API")
public interface ForecastApi {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * GET /api/v1/forecast/{stationId} : Get Forecast for specific Station
     *
     * @param stationId The id of the station to get the sample from. (required)
     * @return Returns the forecast for the station. (status code 200)
     */
    @Operation(
        operationId = "getForecast",
        summary = "Get Forecast for specific Station",
        tags = { "forecast" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Returns the forecast for the station.", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiForecast.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/api/v1/forecast/{stationId}",
        produces = { "application/json" }
    )
    
    default ResponseEntity<ApiForecast> getForecast(
        @Parameter(name = "stationId", description = "The id of the station to get the sample from.", required = true, in = ParameterIn.PATH) @PathVariable("stationId") Integer stationId
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"seventyFivePercentile\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"twentyFivePercentile\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"min\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"median\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"max\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"measuredData\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * GET /api/v1/forecasts : Get All Forecasts for user
     *
     * @return Returns forecasts for all spots of user (status code 200)
     */
    @Operation(
        operationId = "getForecasts",
        summary = "Get All Forecasts for user",
        tags = { "forecast" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Returns forecasts for all spots of user", content = {
                @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = StationToApiForecasts.class)))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/api/v1/forecasts",
        produces = { "application/json" }
    )
    
    default ResponseEntity<List<StationToApiForecasts>> getForecasts(
        
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "[ { \"station\" : 0, \"forecast\" : { \"seventyFivePercentile\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"twentyFivePercentile\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"min\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"median\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"max\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"measuredData\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } }, { \"station\" : 0, \"forecast\" : { \"seventyFivePercentile\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"twentyFivePercentile\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"min\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"median\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"max\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"measuredData\" : [ { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"flow\" : 0.8008281904610115, \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"timestamp\" : \"2000-01-23T04:56:07.000+00:00\" } } ]";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}

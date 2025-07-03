package com.aa.msw.gen.api;

import java.net.URI;
import java.util.Objects;
import com.aa.msw.gen.api.ApiForecast;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * StationToApiForecasts
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-06-09T13:29:56.518144+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class StationToApiForecasts {

  private Integer station;

  private ApiForecast forecast;

  public StationToApiForecasts() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public StationToApiForecasts(Integer station, ApiForecast forecast) {
    this.station = station;
    this.forecast = forecast;
  }

  public StationToApiForecasts station(Integer station) {
    this.station = station;
    return this;
  }

  /**
   * Get station
   * @return station
  */
  @NotNull 
  @Schema(name = "station", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("station")
  public Integer getStation() {
    return station;
  }

  public void setStation(Integer station) {
    this.station = station;
  }

  public StationToApiForecasts forecast(ApiForecast forecast) {
    this.forecast = forecast;
    return this;
  }

  /**
   * Get forecast
   * @return forecast
  */
  @NotNull @Valid 
  @Schema(name = "forecast", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("forecast")
  public ApiForecast getForecast() {
    return forecast;
  }

  public void setForecast(ApiForecast forecast) {
    this.forecast = forecast;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StationToApiForecasts stationToApiForecasts = (StationToApiForecasts) o;
    return Objects.equals(this.station, stationToApiForecasts.station) &&
        Objects.equals(this.forecast, stationToApiForecasts.forecast);
  }

  @Override
  public int hashCode() {
    return Objects.hash(station, forecast);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StationToApiForecasts {\n");
    sb.append("    station: ").append(toIndentedString(station)).append("\n");
    sb.append("    forecast: ").append(toIndentedString(forecast)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}


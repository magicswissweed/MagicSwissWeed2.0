package com.aa.msw.gen.api;

import java.net.URI;
import java.util.Objects;
import com.aa.msw.gen.api.ApiFlowSample;
import com.aa.msw.gen.api.ApiStationId;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * StationToLastFewDays
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-12T17:43:06.448035+01:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class StationToLastFewDays {

  private ApiStationId station;

  @Valid
  private List<@Valid ApiFlowSample> lastFewDays = new ArrayList<>();

  public StationToLastFewDays() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public StationToLastFewDays(ApiStationId station, List<@Valid ApiFlowSample> lastFewDays) {
    this.station = station;
    this.lastFewDays = lastFewDays;
  }

  public StationToLastFewDays station(ApiStationId station) {
    this.station = station;
    return this;
  }

  /**
   * Get station
   * @return station
  */
  @NotNull @Valid 
  @Schema(name = "station", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("station")
  public ApiStationId getStation() {
    return station;
  }

  public void setStation(ApiStationId station) {
    this.station = station;
  }

  public StationToLastFewDays lastFewDays(List<@Valid ApiFlowSample> lastFewDays) {
    this.lastFewDays = lastFewDays;
    return this;
  }

  public StationToLastFewDays addLastFewDaysItem(ApiFlowSample lastFewDaysItem) {
    if (this.lastFewDays == null) {
      this.lastFewDays = new ArrayList<>();
    }
    this.lastFewDays.add(lastFewDaysItem);
    return this;
  }

  /**
   * Get lastFewDays
   * @return lastFewDays
  */
  @NotNull @Valid 
  @Schema(name = "lastFewDays", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("lastFewDays")
  public List<@Valid ApiFlowSample> getLastFewDays() {
    return lastFewDays;
  }

  public void setLastFewDays(List<@Valid ApiFlowSample> lastFewDays) {
    this.lastFewDays = lastFewDays;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StationToLastFewDays stationToLastFewDays = (StationToLastFewDays) o;
    return Objects.equals(this.station, stationToLastFewDays.station) &&
        Objects.equals(this.lastFewDays, stationToLastFewDays.lastFewDays);
  }

  @Override
  public int hashCode() {
    return Objects.hash(station, lastFewDays);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StationToLastFewDays {\n");
    sb.append("    station: ").append(toIndentedString(station)).append("\n");
    sb.append("    lastFewDays: ").append(toIndentedString(lastFewDays)).append("\n");
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


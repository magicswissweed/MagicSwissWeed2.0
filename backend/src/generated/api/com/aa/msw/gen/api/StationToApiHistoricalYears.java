package com.aa.msw.gen.api;

import java.net.URI;
import java.util.Objects;
import com.aa.msw.gen.api.ApiHistoricalYears;
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
 * StationToApiHistoricalYears
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-06-09T13:29:56.518144+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class StationToApiHistoricalYears {

  private Integer station;

  private ApiHistoricalYears historical;

  public StationToApiHistoricalYears() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public StationToApiHistoricalYears(Integer station, ApiHistoricalYears historical) {
    this.station = station;
    this.historical = historical;
  }

  public StationToApiHistoricalYears station(Integer station) {
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

  public StationToApiHistoricalYears historical(ApiHistoricalYears historical) {
    this.historical = historical;
    return this;
  }

  /**
   * Get historical
   * @return historical
  */
  @NotNull @Valid 
  @Schema(name = "historical", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("historical")
  public ApiHistoricalYears getHistorical() {
    return historical;
  }

  public void setHistorical(ApiHistoricalYears historical) {
    this.historical = historical;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StationToApiHistoricalYears stationToApiHistoricalYears = (StationToApiHistoricalYears) o;
    return Objects.equals(this.station, stationToApiHistoricalYears.station) &&
        Objects.equals(this.historical, stationToApiHistoricalYears.historical);
  }

  @Override
  public int hashCode() {
    return Objects.hash(station, historical);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StationToApiHistoricalYears {\n");
    sb.append("    station: ").append(toIndentedString(station)).append("\n");
    sb.append("    historical: ").append(toIndentedString(historical)).append("\n");
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


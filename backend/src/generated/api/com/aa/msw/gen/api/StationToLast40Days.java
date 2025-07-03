package com.aa.msw.gen.api;

import java.net.URI;
import java.util.Objects;
import com.aa.msw.gen.api.ApiFlowSample;
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
 * StationToLast40Days
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-06-09T13:29:56.518144+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class StationToLast40Days {

  private Integer station;

  @Valid
  private List<@Valid ApiFlowSample> last40Days = new ArrayList<>();

  public StationToLast40Days() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public StationToLast40Days(Integer station, List<@Valid ApiFlowSample> last40Days) {
    this.station = station;
    this.last40Days = last40Days;
  }

  public StationToLast40Days station(Integer station) {
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

  public StationToLast40Days last40Days(List<@Valid ApiFlowSample> last40Days) {
    this.last40Days = last40Days;
    return this;
  }

  public StationToLast40Days addLast40DaysItem(ApiFlowSample last40DaysItem) {
    if (this.last40Days == null) {
      this.last40Days = new ArrayList<>();
    }
    this.last40Days.add(last40DaysItem);
    return this;
  }

  /**
   * Get last40Days
   * @return last40Days
  */
  @NotNull @Valid 
  @Schema(name = "last40Days", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("last40Days")
  public List<@Valid ApiFlowSample> getLast40Days() {
    return last40Days;
  }

  public void setLast40Days(List<@Valid ApiFlowSample> last40Days) {
    this.last40Days = last40Days;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StationToLast40Days stationToLast40Days = (StationToLast40Days) o;
    return Objects.equals(this.station, stationToLast40Days.station) &&
        Objects.equals(this.last40Days, stationToLast40Days.last40Days);
  }

  @Override
  public int hashCode() {
    return Objects.hash(station, last40Days);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StationToLast40Days {\n");
    sb.append("    station: ").append(toIndentedString(station)).append("\n");
    sb.append("    last40Days: ").append(toIndentedString(last40Days)).append("\n");
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


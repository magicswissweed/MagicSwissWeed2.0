package com.aa.msw.gen.api;

import java.net.URI;
import java.util.Objects;
import com.aa.msw.gen.api.ApiLineEntry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ApiForecast
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-06-09T13:29:56.518144+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class ApiForecast {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime timestamp;

  @Valid
  private List<@Valid ApiLineEntry> measuredData = new ArrayList<>();

  @Valid
  private List<@Valid ApiLineEntry> median = new ArrayList<>();

  @Valid
  private List<@Valid ApiLineEntry> twentyFivePercentile = new ArrayList<>();

  @Valid
  private List<@Valid ApiLineEntry> seventyFivePercentile = new ArrayList<>();

  @Valid
  private List<@Valid ApiLineEntry> max = new ArrayList<>();

  @Valid
  private List<@Valid ApiLineEntry> min = new ArrayList<>();

  public ApiForecast() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ApiForecast(OffsetDateTime timestamp, List<@Valid ApiLineEntry> measuredData, List<@Valid ApiLineEntry> median, List<@Valid ApiLineEntry> twentyFivePercentile, List<@Valid ApiLineEntry> seventyFivePercentile, List<@Valid ApiLineEntry> max, List<@Valid ApiLineEntry> min) {
    this.timestamp = timestamp;
    this.measuredData = measuredData;
    this.median = median;
    this.twentyFivePercentile = twentyFivePercentile;
    this.seventyFivePercentile = seventyFivePercentile;
    this.max = max;
    this.min = min;
  }

  public ApiForecast timestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Get timestamp
   * @return timestamp
  */
  @NotNull @Valid 
  @Schema(name = "timestamp", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("timestamp")
  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public ApiForecast measuredData(List<@Valid ApiLineEntry> measuredData) {
    this.measuredData = measuredData;
    return this;
  }

  public ApiForecast addMeasuredDataItem(ApiLineEntry measuredDataItem) {
    if (this.measuredData == null) {
      this.measuredData = new ArrayList<>();
    }
    this.measuredData.add(measuredDataItem);
    return this;
  }

  /**
   * Get measuredData
   * @return measuredData
  */
  @NotNull @Valid 
  @Schema(name = "measuredData", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("measuredData")
  public List<@Valid ApiLineEntry> getMeasuredData() {
    return measuredData;
  }

  public void setMeasuredData(List<@Valid ApiLineEntry> measuredData) {
    this.measuredData = measuredData;
  }

  public ApiForecast median(List<@Valid ApiLineEntry> median) {
    this.median = median;
    return this;
  }

  public ApiForecast addMedianItem(ApiLineEntry medianItem) {
    if (this.median == null) {
      this.median = new ArrayList<>();
    }
    this.median.add(medianItem);
    return this;
  }

  /**
   * Get median
   * @return median
  */
  @NotNull @Valid 
  @Schema(name = "median", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("median")
  public List<@Valid ApiLineEntry> getMedian() {
    return median;
  }

  public void setMedian(List<@Valid ApiLineEntry> median) {
    this.median = median;
  }

  public ApiForecast twentyFivePercentile(List<@Valid ApiLineEntry> twentyFivePercentile) {
    this.twentyFivePercentile = twentyFivePercentile;
    return this;
  }

  public ApiForecast addTwentyFivePercentileItem(ApiLineEntry twentyFivePercentileItem) {
    if (this.twentyFivePercentile == null) {
      this.twentyFivePercentile = new ArrayList<>();
    }
    this.twentyFivePercentile.add(twentyFivePercentileItem);
    return this;
  }

  /**
   * Get twentyFivePercentile
   * @return twentyFivePercentile
  */
  @NotNull @Valid 
  @Schema(name = "twentyFivePercentile", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("twentyFivePercentile")
  public List<@Valid ApiLineEntry> getTwentyFivePercentile() {
    return twentyFivePercentile;
  }

  public void setTwentyFivePercentile(List<@Valid ApiLineEntry> twentyFivePercentile) {
    this.twentyFivePercentile = twentyFivePercentile;
  }

  public ApiForecast seventyFivePercentile(List<@Valid ApiLineEntry> seventyFivePercentile) {
    this.seventyFivePercentile = seventyFivePercentile;
    return this;
  }

  public ApiForecast addSeventyFivePercentileItem(ApiLineEntry seventyFivePercentileItem) {
    if (this.seventyFivePercentile == null) {
      this.seventyFivePercentile = new ArrayList<>();
    }
    this.seventyFivePercentile.add(seventyFivePercentileItem);
    return this;
  }

  /**
   * Get seventyFivePercentile
   * @return seventyFivePercentile
  */
  @NotNull @Valid 
  @Schema(name = "seventyFivePercentile", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("seventyFivePercentile")
  public List<@Valid ApiLineEntry> getSeventyFivePercentile() {
    return seventyFivePercentile;
  }

  public void setSeventyFivePercentile(List<@Valid ApiLineEntry> seventyFivePercentile) {
    this.seventyFivePercentile = seventyFivePercentile;
  }

  public ApiForecast max(List<@Valid ApiLineEntry> max) {
    this.max = max;
    return this;
  }

  public ApiForecast addMaxItem(ApiLineEntry maxItem) {
    if (this.max == null) {
      this.max = new ArrayList<>();
    }
    this.max.add(maxItem);
    return this;
  }

  /**
   * Get max
   * @return max
  */
  @NotNull @Valid 
  @Schema(name = "max", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("max")
  public List<@Valid ApiLineEntry> getMax() {
    return max;
  }

  public void setMax(List<@Valid ApiLineEntry> max) {
    this.max = max;
  }

  public ApiForecast min(List<@Valid ApiLineEntry> min) {
    this.min = min;
    return this;
  }

  public ApiForecast addMinItem(ApiLineEntry minItem) {
    if (this.min == null) {
      this.min = new ArrayList<>();
    }
    this.min.add(minItem);
    return this;
  }

  /**
   * Get min
   * @return min
  */
  @NotNull @Valid 
  @Schema(name = "min", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("min")
  public List<@Valid ApiLineEntry> getMin() {
    return min;
  }

  public void setMin(List<@Valid ApiLineEntry> min) {
    this.min = min;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiForecast apiForecast = (ApiForecast) o;
    return Objects.equals(this.timestamp, apiForecast.timestamp) &&
        Objects.equals(this.measuredData, apiForecast.measuredData) &&
        Objects.equals(this.median, apiForecast.median) &&
        Objects.equals(this.twentyFivePercentile, apiForecast.twentyFivePercentile) &&
        Objects.equals(this.seventyFivePercentile, apiForecast.seventyFivePercentile) &&
        Objects.equals(this.max, apiForecast.max) &&
        Objects.equals(this.min, apiForecast.min);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, measuredData, median, twentyFivePercentile, seventyFivePercentile, max, min);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiForecast {\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    measuredData: ").append(toIndentedString(measuredData)).append("\n");
    sb.append("    median: ").append(toIndentedString(median)).append("\n");
    sb.append("    twentyFivePercentile: ").append(toIndentedString(twentyFivePercentile)).append("\n");
    sb.append("    seventyFivePercentile: ").append(toIndentedString(seventyFivePercentile)).append("\n");
    sb.append("    max: ").append(toIndentedString(max)).append("\n");
    sb.append("    min: ").append(toIndentedString(min)).append("\n");
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


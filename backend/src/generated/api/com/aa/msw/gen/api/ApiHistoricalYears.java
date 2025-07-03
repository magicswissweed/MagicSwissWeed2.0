package com.aa.msw.gen.api;

import java.net.URI;
import java.util.Objects;
import com.aa.msw.gen.api.ApiLineEntry;
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
 * ApiHistoricalYears
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-06-09T13:29:56.518144+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class ApiHistoricalYears {

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

  @Valid
  private List<@Valid ApiLineEntry> currentYear = new ArrayList<>();

  public ApiHistoricalYears() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ApiHistoricalYears(List<@Valid ApiLineEntry> median, List<@Valid ApiLineEntry> twentyFivePercentile, List<@Valid ApiLineEntry> seventyFivePercentile, List<@Valid ApiLineEntry> max, List<@Valid ApiLineEntry> min, List<@Valid ApiLineEntry> currentYear) {
    this.median = median;
    this.twentyFivePercentile = twentyFivePercentile;
    this.seventyFivePercentile = seventyFivePercentile;
    this.max = max;
    this.min = min;
    this.currentYear = currentYear;
  }

  public ApiHistoricalYears median(List<@Valid ApiLineEntry> median) {
    this.median = median;
    return this;
  }

  public ApiHistoricalYears addMedianItem(ApiLineEntry medianItem) {
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

  public ApiHistoricalYears twentyFivePercentile(List<@Valid ApiLineEntry> twentyFivePercentile) {
    this.twentyFivePercentile = twentyFivePercentile;
    return this;
  }

  public ApiHistoricalYears addTwentyFivePercentileItem(ApiLineEntry twentyFivePercentileItem) {
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

  public ApiHistoricalYears seventyFivePercentile(List<@Valid ApiLineEntry> seventyFivePercentile) {
    this.seventyFivePercentile = seventyFivePercentile;
    return this;
  }

  public ApiHistoricalYears addSeventyFivePercentileItem(ApiLineEntry seventyFivePercentileItem) {
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

  public ApiHistoricalYears max(List<@Valid ApiLineEntry> max) {
    this.max = max;
    return this;
  }

  public ApiHistoricalYears addMaxItem(ApiLineEntry maxItem) {
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

  public ApiHistoricalYears min(List<@Valid ApiLineEntry> min) {
    this.min = min;
    return this;
  }

  public ApiHistoricalYears addMinItem(ApiLineEntry minItem) {
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

  public ApiHistoricalYears currentYear(List<@Valid ApiLineEntry> currentYear) {
    this.currentYear = currentYear;
    return this;
  }

  public ApiHistoricalYears addCurrentYearItem(ApiLineEntry currentYearItem) {
    if (this.currentYear == null) {
      this.currentYear = new ArrayList<>();
    }
    this.currentYear.add(currentYearItem);
    return this;
  }

  /**
   * Get currentYear
   * @return currentYear
  */
  @NotNull @Valid 
  @Schema(name = "currentYear", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("currentYear")
  public List<@Valid ApiLineEntry> getCurrentYear() {
    return currentYear;
  }

  public void setCurrentYear(List<@Valid ApiLineEntry> currentYear) {
    this.currentYear = currentYear;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiHistoricalYears apiHistoricalYears = (ApiHistoricalYears) o;
    return Objects.equals(this.median, apiHistoricalYears.median) &&
        Objects.equals(this.twentyFivePercentile, apiHistoricalYears.twentyFivePercentile) &&
        Objects.equals(this.seventyFivePercentile, apiHistoricalYears.seventyFivePercentile) &&
        Objects.equals(this.max, apiHistoricalYears.max) &&
        Objects.equals(this.min, apiHistoricalYears.min) &&
        Objects.equals(this.currentYear, apiHistoricalYears.currentYear);
  }

  @Override
  public int hashCode() {
    return Objects.hash(median, twentyFivePercentile, seventyFivePercentile, max, min, currentYear);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiHistoricalYears {\n");
    sb.append("    median: ").append(toIndentedString(median)).append("\n");
    sb.append("    twentyFivePercentile: ").append(toIndentedString(twentyFivePercentile)).append("\n");
    sb.append("    seventyFivePercentile: ").append(toIndentedString(seventyFivePercentile)).append("\n");
    sb.append("    max: ").append(toIndentedString(max)).append("\n");
    sb.append("    min: ").append(toIndentedString(min)).append("\n");
    sb.append("    currentYear: ").append(toIndentedString(currentYear)).append("\n");
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


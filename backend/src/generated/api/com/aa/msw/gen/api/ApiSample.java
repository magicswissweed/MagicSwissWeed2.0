package com.aa.msw.gen.api;

import java.net.URI;
import java.util.Objects;
import com.aa.msw.gen.api.ApiMeasurementType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ApiSample
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-11T17:03:05.902213+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class ApiSample {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime timestamp;

  private Double value;

  private ApiMeasurementType measurementType;

  public ApiSample() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ApiSample(OffsetDateTime timestamp, Double value, ApiMeasurementType measurementType) {
    this.timestamp = timestamp;
    this.value = value;
    this.measurementType = measurementType;
  }

  public ApiSample timestamp(OffsetDateTime timestamp) {
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

  public ApiSample value(Double value) {
    this.value = value;
    return this;
  }

  /**
   * Get value
   * @return value
  */
  @NotNull 
  @Schema(name = "value", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("value")
  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value;
  }

  public ApiSample measurementType(ApiMeasurementType measurementType) {
    this.measurementType = measurementType;
    return this;
  }

  /**
   * Get measurementType
   * @return measurementType
  */
  @NotNull @Valid 
  @Schema(name = "measurementType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("measurementType")
  public ApiMeasurementType getMeasurementType() {
    return measurementType;
  }

  public void setMeasurementType(ApiMeasurementType measurementType) {
    this.measurementType = measurementType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiSample apiSample = (ApiSample) o;
    return Objects.equals(this.timestamp, apiSample.timestamp) &&
        Objects.equals(this.value, apiSample.value) &&
        Objects.equals(this.measurementType, apiSample.measurementType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, value, measurementType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiSample {\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    measurementType: ").append(toIndentedString(measurementType)).append("\n");
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


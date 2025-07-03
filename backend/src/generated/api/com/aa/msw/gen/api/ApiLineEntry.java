package com.aa.msw.gen.api;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * ApiLineEntry
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-06-09T13:29:56.518144+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class ApiLineEntry {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime timestamp;

  private Double flow;

  public ApiLineEntry() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ApiLineEntry(OffsetDateTime timestamp, Double flow) {
    this.timestamp = timestamp;
    this.flow = flow;
  }

  public ApiLineEntry timestamp(OffsetDateTime timestamp) {
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

  public ApiLineEntry flow(Double flow) {
    this.flow = flow;
    return this;
  }

  /**
   * Get flow
   * @return flow
  */
  @NotNull 
  @Schema(name = "flow", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("flow")
  public Double getFlow() {
    return flow;
  }

  public void setFlow(Double flow) {
    this.flow = flow;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiLineEntry apiLineEntry = (ApiLineEntry) o;
    return Objects.equals(this.timestamp, apiLineEntry.timestamp) &&
        Objects.equals(this.flow, apiLineEntry.flow);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, flow);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiLineEntry {\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    flow: ").append(toIndentedString(flow)).append("\n");
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


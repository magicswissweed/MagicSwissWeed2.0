package com.aa.msw.gen.api;

import java.net.URI;
import java.util.Objects;
import com.aa.msw.gen.api.CountryEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ApiStationId
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-12T17:43:06.448035+01:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class ApiStationId {

  private CountryEnum country;

  private String externalId;

  public ApiStationId() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ApiStationId(CountryEnum country, String externalId) {
    this.country = country;
    this.externalId = externalId;
  }

  public ApiStationId country(CountryEnum country) {
    this.country = country;
    return this;
  }

  /**
   * Get country
   * @return country
  */
  @NotNull @Valid 
  @Schema(name = "country", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("country")
  public CountryEnum getCountry() {
    return country;
  }

  public void setCountry(CountryEnum country) {
    this.country = country;
  }

  public ApiStationId externalId(String externalId) {
    this.externalId = externalId;
    return this;
  }

  /**
   * Get externalId
   * @return externalId
  */
  @NotNull 
  @Schema(name = "externalId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("externalId")
  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiStationId apiStationId = (ApiStationId) o;
    return Objects.equals(this.country, apiStationId.country) &&
        Objects.equals(this.externalId, apiStationId.externalId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(country, externalId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiStationId {\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
    sb.append("    externalId: ").append(toIndentedString(externalId)).append("\n");
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


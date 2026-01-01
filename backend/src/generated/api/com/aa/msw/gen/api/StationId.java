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
 * StationId
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-12-30T15:45:51.831563+01:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class StationId {

  private CountryEnum country;

  private String externalId;

  public StationId() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public StationId(CountryEnum country, String externalId) {
    this.country = country;
    this.externalId = externalId;
  }

  public StationId country(CountryEnum country) {
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

  public StationId externalId(String externalId) {
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
    StationId stationId = (StationId) o;
    return Objects.equals(this.country, stationId.country) &&
        Objects.equals(this.externalId, stationId.externalId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(country, externalId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StationId {\n");
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


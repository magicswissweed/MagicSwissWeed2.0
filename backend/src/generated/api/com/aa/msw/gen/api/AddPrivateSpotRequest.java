package com.aa.msw.gen.api;

import java.net.URI;
import java.util.Objects;
import com.aa.msw.gen.api.ApiSpot;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * AddPrivateSpotRequest
 */

@JsonTypeName("addPrivateSpot_request")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-06-09T13:29:56.518144+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class AddPrivateSpotRequest {

  private ApiSpot spot;

  private Integer position;

  public AddPrivateSpotRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AddPrivateSpotRequest(ApiSpot spot, Integer position) {
    this.spot = spot;
    this.position = position;
  }

  public AddPrivateSpotRequest spot(ApiSpot spot) {
    this.spot = spot;
    return this;
  }

  /**
   * Get spot
   * @return spot
  */
  @NotNull @Valid 
  @Schema(name = "spot", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("spot")
  public ApiSpot getSpot() {
    return spot;
  }

  public void setSpot(ApiSpot spot) {
    this.spot = spot;
  }

  public AddPrivateSpotRequest position(Integer position) {
    this.position = position;
    return this;
  }

  /**
   * Get position
   * @return position
  */
  @NotNull 
  @Schema(name = "position", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("position")
  public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AddPrivateSpotRequest addPrivateSpotRequest = (AddPrivateSpotRequest) o;
    return Objects.equals(this.spot, addPrivateSpotRequest.spot) &&
        Objects.equals(this.position, addPrivateSpotRequest.position);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spot, position);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AddPrivateSpotRequest {\n");
    sb.append("    spot: ").append(toIndentedString(spot)).append("\n");
    sb.append("    position: ").append(toIndentedString(position)).append("\n");
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


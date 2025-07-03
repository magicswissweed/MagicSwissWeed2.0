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
 * EditPrivateSpotRequest
 */

@JsonTypeName("editPrivateSpot_request")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-06-09T13:29:56.518144+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class EditPrivateSpotRequest {

  private ApiSpot spot;

  public EditPrivateSpotRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public EditPrivateSpotRequest(ApiSpot spot) {
    this.spot = spot;
  }

  public EditPrivateSpotRequest spot(ApiSpot spot) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EditPrivateSpotRequest editPrivateSpotRequest = (EditPrivateSpotRequest) o;
    return Objects.equals(this.spot, editPrivateSpotRequest.spot);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spot);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EditPrivateSpotRequest {\n");
    sb.append("    spot: ").append(toIndentedString(spot)).append("\n");
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


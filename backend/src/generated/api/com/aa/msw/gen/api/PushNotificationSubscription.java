package com.aa.msw.gen.api;

import java.net.URI;
import java.util.Objects;
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
 * PushNotificationSubscription
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-12-29T11:28:08.103650+01:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class PushNotificationSubscription {

  private String token;

  public PushNotificationSubscription() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PushNotificationSubscription(String token) {
    this.token = token;
  }

  public PushNotificationSubscription token(String token) {
    this.token = token;
    return this;
  }

  /**
   * Get token
   * @return token
  */
  @NotNull 
  @Schema(name = "token", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("token")
  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PushNotificationSubscription pushNotificationSubscription = (PushNotificationSubscription) o;
    return Objects.equals(this.token, pushNotificationSubscription.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(token);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PushNotificationSubscription {\n");
    sb.append("    token: ").append(toIndentedString(token)).append("\n");
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


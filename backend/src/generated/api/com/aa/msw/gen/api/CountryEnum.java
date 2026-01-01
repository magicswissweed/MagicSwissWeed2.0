package com.aa.msw.gen.api;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonValue;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets CountryEnum
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-12-31T15:33:14.273869+01:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public enum CountryEnum {
  
  CH("CH"),
  
  FR("FR");

  private String value;

  CountryEnum(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static CountryEnum fromValue(String value) {
    for (CountryEnum b : CountryEnum.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


package it.gov.pagopa.authorizer.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Metadata implements Serializable {

  @JsonProperty("name")
  private String name;

  @JsonProperty("short_key")
  private String shortKey;

  @JsonProperty("content")
  private List<GenericPair> content;
}

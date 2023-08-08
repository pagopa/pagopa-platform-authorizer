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
public class AuthorizedEntity implements Serializable {

  private String name;

  private String value;

  private List<String> values;
}

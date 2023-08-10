package it.gov.pagopa.authorizer.entity;

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
public class GenericPair implements Serializable {

  private String key;

  private String value;

  private List<String> values;
}

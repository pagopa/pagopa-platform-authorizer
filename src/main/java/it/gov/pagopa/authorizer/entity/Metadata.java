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
public class Metadata implements Serializable {

  private String name;

  private String shortKey;

  private List<GenericPair> content;
}

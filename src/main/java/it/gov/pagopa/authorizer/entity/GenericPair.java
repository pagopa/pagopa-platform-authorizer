package it.gov.pagopa.authorizer.entity;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenericPair implements Serializable {

  private String key;

  private String value;

  private List<String> values;
}

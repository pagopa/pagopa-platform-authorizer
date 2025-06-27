package it.gov.pagopa.authorizer.entity;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorizedEntity implements Serializable {

  private String name;

  private String value;

  private List<String> values;
}

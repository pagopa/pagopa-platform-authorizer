package it.gov.pagopa.authorizer.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthConfiguration {
    private String key;
    private String value;
}

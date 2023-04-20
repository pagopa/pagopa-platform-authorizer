package it.gov.pagopa.authorizer.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionKeyDomain implements Serializable {

    private String id;

    private String domain;

    private String subkey;

    private List<String> authorization;

    private Date insertedAt;
}

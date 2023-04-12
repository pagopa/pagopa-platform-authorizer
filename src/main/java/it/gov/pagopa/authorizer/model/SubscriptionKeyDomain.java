package it.gov.pagopa.authorizer.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SubscriptionKeyDomain implements Serializable {
    private String domain;
    private String subkey;
    private List<String> authorization;
}

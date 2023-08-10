package it.gov.pagopa.authorizer.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.List;

@Container(containerName = "skeydomains")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class SubscriptionKeyDomain implements Serializable {

  @Id
  @GeneratedValue
  @JsonProperty("id")
  private String id;

  @PartitionKey
  private String domain;

  private String subkey;

  private String description;

  private String ownerId;

  private String ownerName;

  private String ownerType;

  private List<AuthorizedEntity> authorizedEntities;

  private List<Metadata> otherMetadata;

  private String insertedAt;

  private String lastForcedRefresh;

  private String lastUpdate;
}

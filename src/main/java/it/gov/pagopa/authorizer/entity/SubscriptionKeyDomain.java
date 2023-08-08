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
  @JsonProperty("domain")
  private String domain;

  @JsonProperty("subscription_key")
  private String subscriptionKey;

  @JsonProperty("description")
  private String description;

  @JsonProperty("owner_id")
  private String ownerId;

  @JsonProperty("owner_name")
  private String ownerName;

  @JsonProperty("owner_type")
  private String ownerType;

  @JsonProperty("authorized_entities")
  private List<AuthorizedEntity> authorizedEntities;

  @JsonProperty("other_metadata")
  private List<Metadata> otherMetadata;

  @JsonProperty("inserted_at")
  private String insertedAt;

  @JsonProperty("last_forced_refresh")
  private String lastForcedRefresh;

  @JsonProperty("_ts")
  private String lastUpdate;
}

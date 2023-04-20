package it.gov.pagopa.authorizer.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Container(containerName = "skeydomains")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionKeyDomain implements Serializable {

    @Id
    @GeneratedValue
    private String id;

    @PartitionKey
    private String domain;

    private String subkey;

    private List<String> authorization;

    private LocalDate insertedAt;
}
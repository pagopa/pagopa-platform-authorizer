package it.gov.pagopa.authorizer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/** Codes associated with Creditor Institution */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CIAssociatedCodeList {

    @JsonProperty("used")
    private List<CIAssociatedCode> usedCodes;

    @JsonProperty("unused")
    private List<CIAssociatedCode> unusedCodes;
}
package uk.gov.hmcts.reform.idam.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@Jacksonized
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo implements Serializable {
    private final String sub;
    private final String uid;
    private final String name;
    @JsonProperty("given_name")
    private final String givenName;
    @JsonProperty("family_name")
    private final String familyName;
    private final List<String> roles;
}

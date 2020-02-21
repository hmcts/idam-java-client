package uk.gov.hmcts.reform.idam.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo implements Serializable {
    private String sub;
    private String uid;
    private String name;
    @JsonProperty("given_name")
    private String givenName;
    @JsonProperty("family_name")
    private String familyName;
    private List<String> roles;
}

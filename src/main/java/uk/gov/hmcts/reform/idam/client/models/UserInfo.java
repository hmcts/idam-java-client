package uk.gov.hmcts.reform.idam.client.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
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

    @JsonCreator
    public UserInfo(@JsonProperty("sub") String sub,
                    @JsonProperty("uid") String uid,
                    @JsonProperty("name") String name,
                    @JsonProperty("given_name") String givenName,
                    @JsonProperty("family_name") String familyName,
                    @JsonProperty("roles") List<String> roles) {
        this.sub = sub;
        this.uid = uid;
        this.name = name;
        this.givenName = givenName;
        this.familyName = familyName;
        this.roles = roles;
    }

}

package uk.gov.hmcts.reform.idam.client.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateUserResponse {

    private String code;

    @JsonCreator
    public AuthenticateUserResponse(String code) {
        this.code = code;
    }
}

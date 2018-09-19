package uk.gov.hmcts.reform.idam.client.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenExchangeResponse {

    @JsonProperty("access_token")
    private final String accessToken;

    @JsonCreator
    public TokenExchangeResponse(String accessToken) {
        this.accessToken = accessToken;
    }

}

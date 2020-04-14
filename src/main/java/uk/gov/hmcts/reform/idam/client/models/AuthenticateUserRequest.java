package uk.gov.hmcts.reform.idam.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticateUserRequest {
    @JsonProperty("response_type")
    private String responseType;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("redirect_uri")
    private String redirectUri;

    public AuthenticateUserRequest(String responseType, String clientId, String redirectUri) {
        this.responseType = responseType;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    public String getResponseType() {
        return responseType;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}

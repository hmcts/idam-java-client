package uk.gov.hmcts.reform.idam.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenRequest {

    @JsonProperty("client_id")
    public final String clientId;
    @JsonProperty("client_secret")
    public final String clientSecret;
    @JsonProperty("grant_type")
    public final String grantType;
    @JsonProperty("redirect_uri")
    public final String redirectUri;
    @JsonProperty("username")
    public final String username;
    @JsonProperty("password")
    public final String password;
    @JsonProperty("scope")
    public final String scope;
    @JsonProperty("refresh_token")
    public final String refreshToken;
    @JsonProperty("code")
    public final String code;


    public TokenRequest(
            String clientId,
            String clientSecret,
            String grantType,
            String redirectUri,
            String username,
            String password,
            String scope,
            String refreshToken,
            String code
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
        this.redirectUri = redirectUri;
        this.username = username;
        this.password = password;
        this.scope = scope;
        this.refreshToken = refreshToken;
        this.code = code;
    }
}



package uk.gov.hmcts.reform.idam.client.models;

import feign.form.FormProperty;

public class TokenRequest {

    @FormProperty("client_id")
    private String clientId;
    @FormProperty("client_secret")
    private String clientSecret;
    @FormProperty("grant_type")
    private String grantType;
    @FormProperty("redirect_uri")
    private String redirectUri;
    @FormProperty("username")
    private String username;
    @FormProperty("password")
    private String password;
    @FormProperty("scope")
    private String scope;
    @FormProperty("refresh_token")
    private String refreshToken;
    @FormProperty("code")
    private String code;


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

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getGrantType() {
        return grantType;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getScope() {
        return scope;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getCode() {
        return code;
    }
}



package uk.gov.hmcts.reform.idam.client.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Builder
public class ExchangeCodeRequest {
    private String code;
    private String grant_type;
    private String redirect_uri;
    private String client_id;
    private String client_secret;

    public ExchangeCodeRequest(
            String code, String grantType, String redirectUri, String clientId, String clientSecret
    ) {
        this.code = code;
        this.grant_type = grantType;
        this.redirect_uri = redirectUri;
        this.client_id = clientId;
        this.client_secret = clientSecret;
    }

    public String getCode() {
        return code;
    }

    public String getGrantType() {
        return grant_type;
    }

    public String getRedirectUri() {
        return redirect_uri;
    }

    public String getClientId() {
        return client_id;
    }

    public String getClientSecret() {
        return client_secret;
    }
}

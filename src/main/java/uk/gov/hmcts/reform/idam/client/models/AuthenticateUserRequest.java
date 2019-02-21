package uk.gov.hmcts.reform.idam.client.models;

public class AuthenticateUserRequest {
    private String response_type;
    private String client_id;
    private String redirect_uri;

    public AuthenticateUserRequest(String responseType, String clientId, String redirectUri) {
        this.response_type = responseType;
        this.client_id = clientId;
        this.redirect_uri = redirectUri;
    }

    public String getResponseType() {
        return response_type;
    }

    public String getClientId() {
        return client_id;
    }

    public String getRedirectUri() {
        return redirect_uri;
    }
}

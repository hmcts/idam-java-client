package uk.gov.hmcts.reform.idam.client.models;

public class AuthenticateUserRequest {
    private String response_type;
    private String client_id;
    private String redirect_uri;

    public AuthenticateUserRequest(String response_type, String client_id, String redirect_uri) {
        this.response_type = response_type;
        this.client_id = client_id;
        this.redirect_uri = redirect_uri;
    }

    public String getResponse_type() {
        return response_type;
    }

    public String getClient_id() {
        return client_id;
    }

    public String getRedirect_uri() {
        return redirect_uri;
    }
}

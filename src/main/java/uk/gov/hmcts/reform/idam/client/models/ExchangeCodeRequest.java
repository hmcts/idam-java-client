package uk.gov.hmcts.reform.idam.client.models;

public class ExchangeCodeRequest {
    private String code;
    private String grant_type;
    private String redirect_uri;
    private String client_id;
    private String client_secret;

    public ExchangeCodeRequest(
            String code, String grant_type, String redirect_uri, String client_id, String client_secret
    ) {
        this.code = code;
        this.grant_type = grant_type;
        this.redirect_uri = redirect_uri;
        this.client_id = client_id;
        this.client_secret = client_secret;
    }

    public String getCode() {
        return code;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public String getRedirect_uri() {
        return redirect_uri;
    }

    public String getClient_id() {
        return client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }
}

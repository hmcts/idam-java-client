package uk.gov.hmcts.reform.idam.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserRequest;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;
import uk.gov.hmcts.reform.idam.client.models.ExchangeCodeRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenExchangeResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Base64;

@Service
public class IdamClient {

    private static final String CODE = "code";
    private static final String AUTH_TYPE = CODE;
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String BASIC_AUTH_TYPE = "Basic";
    private static final String BEARER_AUTH_TYPE = "Bearer";
    private static final String RESPONSE_TYPE = "response_type";
    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String GRANT_TYPE = "grant_type";

    private IdamApi idamApi;
    private final OAuth2Configuration oauth2Configuration;

    @Autowired
    public IdamClient(IdamApi idamApi, OAuth2Configuration oauth2Configuration) {
        this.idamApi = idamApi;
        this.oauth2Configuration = oauth2Configuration;
    }

    public UserDetails getUserDetails(String bearerToken) {
        return idamApi.retrieveUserDetails(bearerToken);
    }

    public String authenticateUser(String username, String password) {
        String authorisation = username + ":" + password;
        String base64Authorisation = Base64.getEncoder().encodeToString(authorisation.getBytes());

        String clientId = oauth2Configuration.getClientId();

        String redirectUri = oauth2Configuration.getRedirectUri();

        AuthenticateUserResponse authenticateUserResponse = idamApi.authenticateUser(
                BASIC_AUTH_TYPE + " " + base64Authorisation,
                new AuthenticateUserRequest(CODE, clientId, redirectUri)
        );


        ExchangeCodeRequest exchangeCodeRequest = new ExchangeCodeRequest(authenticateUserResponse
                .getCode(), AUTHORIZATION_CODE, redirectUri, clientId, oauth2Configuration.getClientSecret());

        TokenExchangeResponse tokenExchangeResponse = idamApi.exchangeCode(exchangeCodeRequest);

        return BEARER_AUTH_TYPE + " " + tokenExchangeResponse.getAccessToken();
    }

}
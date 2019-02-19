package uk.gov.hmcts.reform.idam.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;
import uk.gov.hmcts.reform.idam.client.models.TokenExchangeResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Base64;

@Service
public class IdamClient {

    public static final String AUTH_TYPE = "code";
    public static final String GRANT_TYPE = "authorization_code";
    public static final String BASIC_AUTH_TYPE = "Basic";
    public static final String BEARER_AUTH_TYPE = "Bearer";

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

        AuthenticateUserResponse authenticateUserResponse = idamApi.authenticateUser(
            BASIC_AUTH_TYPE + " " + base64Authorisation,
            AUTH_TYPE,
            oauth2Configuration.getClientId(),
            oauth2Configuration.getRedirectUri(),
            " "
        );

        TokenExchangeResponse tokenExchangeResponse = idamApi.exchangeCode(
            authenticateUserResponse.getCode(),
            GRANT_TYPE,
            oauth2Configuration.getRedirectUri(),
            oauth2Configuration.getClientId(),
            oauth2Configuration.getClientSecret(),
            " "
        );

        return BEARER_AUTH_TYPE + " " + tokenExchangeResponse.getAccessToken();
    }
}
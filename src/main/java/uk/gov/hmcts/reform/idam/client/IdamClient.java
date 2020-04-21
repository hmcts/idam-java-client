package uk.gov.hmcts.reform.idam.client;

import feign.Response;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserRequest;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;
import uk.gov.hmcts.reform.idam.client.models.ExchangeCodeRequest;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinRequest;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinResponse;
import uk.gov.hmcts.reform.idam.client.models.TokenExchangeResponse;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class IdamClient {

    public static final String AUTH_TYPE = "code";
    public static final String GRANT_TYPE = "authorization_code";
    public static final String OPENID_GRANT_TYPE = "password";
    public static final String OPENID_SCOPE = "openid";

    public static final String BASIC_AUTH_TYPE = "Basic";
    public static final String BEARER_AUTH_TYPE = "Bearer";
    public static final String CODE = "code";

    private IdamApi idamApi;
    private OAuth2Configuration oauth2Configuration;

    @Autowired
    public IdamClient(IdamApi idamApi, OAuth2Configuration oauth2Configuration) {
        this.idamApi = idamApi;
        this.oauth2Configuration = oauth2Configuration;
    }

    public UserDetails getUserDetails(String bearerToken) {
        return idamApi.retrieveUserDetails(bearerToken);
    }

    public String getAccessToken(String username, String password) {
        TokenRequest tokenRequest =
            new TokenRequest(
                oauth2Configuration.getClientId(),
                oauth2Configuration.getClientSecret(),
                OPENID_GRANT_TYPE,
                oauth2Configuration.getRedirectUri(),
                username,
                password,
                OPENID_SCOPE,
                null,
                null
            );
        return idamApi.generateOpenIdToken(tokenRequest).accessToken;
    }

    /**
     * Authenticate user and get token.
     * This method is no longer acceptable as idam start using OpenID and /oauth2/authorize endpoint deprecated.
     *
     * @deprecated Use {@link IdamClient#getAccessToken(String, String)} instead.
     *
     */
    @Deprecated
    public String authenticateUser(String username, String password) {
        String authorisation = username + ":" + password;
        String base64Authorisation = Base64.getEncoder().encodeToString(authorisation.getBytes());

        String clientId = oauth2Configuration.getClientId();

        String redirectUri = oauth2Configuration.getRedirectUri();

        AuthenticateUserResponse authenticateUserResponse = idamApi.authenticateUser(
            BASIC_AUTH_TYPE + " " + base64Authorisation,
            new AuthenticateUserRequest(AUTH_TYPE, clientId, redirectUri)
        );

        ExchangeCodeRequest exchangeCodeRequest = new ExchangeCodeRequest(authenticateUserResponse
            .getCode(), GRANT_TYPE, redirectUri, clientId, oauth2Configuration.getClientSecret());

        TokenExchangeResponse tokenExchangeResponse = idamApi.exchangeCode(exchangeCodeRequest);

        return BEARER_AUTH_TYPE + " " + tokenExchangeResponse.getAccessToken();
    }

    public TokenExchangeResponse exchangeCode(ExchangeCodeRequest exchangeCodeRequest) {
        return idamApi.exchangeCode(exchangeCodeRequest);
    }

    public GeneratePinResponse generatePin(GeneratePinRequest pinRequest, String authorization) {
        return idamApi.generatePin(pinRequest, authorization);
    }

    public AuthenticateUserResponse authenticatePinUser(String pin, String state)
        throws UnsupportedEncodingException {
        AuthenticateUserResponse pinUserCode;
        final String clientId = oauth2Configuration.getClientId();
        final String redirectUri = URLEncoder.encode(
            oauth2Configuration.getRedirectUri(), StandardCharsets.UTF_8.toString());
        final Response response = idamApi.authenticatePinUser(pin, clientId, redirectUri, state);
        if (response.status() != HttpStatus.FOUND.value()) {
            return null;
        }
        final String code = getCodeFromRedirect(response);
        pinUserCode = new AuthenticateUserResponse(code);

        return pinUserCode;
    }

    private String getCodeFromRedirect(Response response) {
        String location = response.headers().get(HttpHeaders.LOCATION).stream().findFirst()
            .orElse("");

        UriComponents build = UriComponentsBuilder.fromUriString(location).build();
        return build.getQueryParams().getFirst(CODE);
    }

    public UserInfo getUserInfo(String bearerToken) {
        return idamApi.retrieveUserInfo(bearerToken);
    }
}
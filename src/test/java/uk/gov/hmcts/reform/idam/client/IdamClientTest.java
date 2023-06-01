package uk.gov.hmcts.reform.idam.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;
import uk.gov.hmcts.reform.idam.client.models.ExchangeCodeRequest;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinRequest;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinResponse;
import uk.gov.hmcts.reform.idam.client.models.TokenExchangeResponse;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToIgnoreCase;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@SpringBootTest(classes = {IdamClient.class, IdamApi.class, OAuth2Configuration.class})
@PropertySource(value = "classpath:application.yml")
@EnableAutoConfiguration
@AutoConfigureWireMock(port = 5050)
public class IdamClientTest {

    private static final String BEARER = "Bearer ";
    private static final String TOKEN = "dddIIiwiaWF0IjoxNTUwNjk1Nzc5LCJleHAiOjE1NTA3MjQ1NzksImRhdGEiOiJjYXNld29ya2"
        + "F1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvYnNwIiwiZ3JvdXAiOiJic3Atc3lzdGVtdXBkYXRlIn0.P"
        + "djD2Kjz6myH1p44CRCVztkl2lqkg0LXqiyoH7Hs2bg";
    private static final String REFRESH_TOKEN = "NlLWJzcC1zeXN0ZW0tdXBkYXRlQGhtY3RzLm5ldCIsImN0cyI6Ik9BVVRIMl9TVEFURUx"
        + "fyJdXaI5YUO6bNxmby3jkqRVUMe25nOwWMiOyqoZDg3ehMGJuSqMguQwMrg1kc8RB6ZtVugPctVgW_ffE9EYc3i8yfTqq8rUvxDJI"
        +  "dCT4jkArwuitcvSmUg2XCTy_YoqdwQcGZD5vI3Wya1polA";
    private static final String ID_TOKEN = "oiUlMyNTYifQ.eyJhdF9oYXNoIjoibFpJWlg1M3BzRnVCZWlpMllLWTFBUSIsInN1YiI6ImRpdR"
        + "Vmxt1h2dGD9dAPBYSR6G0LEP_N5MUUCahVMDQeSBawzwW54AOsm4wwd5UUV_Xn8tAvovt4g-iZQGwBsi6t_FTLLYiPzapL-12jKt"
        + "oCKtFmyLlfcBXTLaPywi8oFfinCRaVQ83BiKtIXuImGrYN8WeZVtvZwAQzmHqA4PoDJBzOJWptL-Z63wVFoQZy2AHaFLcR_Yv07w";
    private static final String EXCHANGE_CODE_RESULT = String.format(
        "{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":28800}", TOKEN);

    private static final String OPENID_TOKEN_RESULT = String.format(
            "{\"access_token\":\"%s\",\"refresh_token\":\"%s\",\"id_token\":\"%s\",\"token_type\":\"Bearer\","
                    + "\"scope\": \"openid profile roles\",\"expires_in\":28800}", TOKEN, REFRESH_TOKEN, ID_TOKEN);

    private static final String USER_LOGIN = "user@example.com";
    private static final String USER_PASSWORD = "Password12";
    private static final String EXCHANGE_CODE = "eEdhNnasWy7eNFAV";
    private static final String PIN_AUTH_CODE = "abcdefgh123456789";
    private static final String PIN = "ABCD1234";
    private String pinRedirectUrl;
    @Value("${idam.client.redirect_uri:}") private String redirectUri;
    @Value("${idam.client.id:}") private String clientId;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        pinRedirectUrl = redirectUri + "?code=" + PIN_AUTH_CODE;

        // See https://www.baeldung.com/jackson-optional for why this is needed
        objectMapper.registerModule(new Jdk8Module());
    }

    @Autowired
    private IdamClient idamClient;

    @Test
    public void authenticateUser() {
        stubForAuthenticateUser(HttpStatus.OK);
        String requestBody = "code=eEdhNnasWy7eNFAV&"
            + "grant_type=authorization_code&"
            + "redirect_uri=https%3A%2F%2Flocalhost%3A5000%2Freceiver&"
            + "client_secret=123456&client_id=bsp";
        stubForToken(requestBody);
        final String bearerToken = idamClient.authenticateUser(USER_LOGIN, USER_PASSWORD);
        assertThat(bearerToken).isEqualTo(BEARER + TOKEN);
    }

    @Test
    public void failedToAuthenticateUser() {
        stubForAuthenticateUser(HttpStatus.UNAUTHORIZED);
        FeignException exception = assertThrows(FeignException.class, () ->
            idamClient.authenticateUser(USER_LOGIN, USER_PASSWORD)
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void findsDetailsForAuthToken() throws JsonProcessingException {
        final String forename = "Hello";
        final String surname = "IDAM";
        UserDetails userDetails = UserDetails.builder()
            .email(USER_LOGIN)
            .forename(forename)
            .surname(surname)
            .build();
        stubForDetails(userDetails);
        UserDetails found = idamClient.getUserDetails(BEARER + TOKEN);
        assertThat(userDetails.getEmail()).isEqualTo(USER_LOGIN);
        found.getSurname().ifPresent(name -> assertThat(name).isEqualTo(surname));
        assertThat(found.getFullName()).isEqualTo(forename + " " + surname);
    }

    @Test
    public void exchangeCodeReturnsExpected() {
        stubForToken("code=eEdhNnasWy7eNFAV&grant_type=token&"
            + "redirect_uri=http%3A%2F%2Fredirect&client_secret=secret&client_id=clientId_12");
        ExchangeCodeRequest exchangeCodeRequest = ExchangeCodeRequest.builder()
            .code(EXCHANGE_CODE)
            .clientId("clientId_12")
            .grantType("token")
            .redirectUri("http://redirect")
            .clientSecret("secret")
            .build();
        final TokenExchangeResponse response = idamClient.exchangeCode(exchangeCodeRequest);
        assertThat(response.getAccessToken()).isEqualTo(TOKEN);
    }

    @Test
    public void generatePinReturnsExpected() throws JsonProcessingException {
        stubForGeneratePin();
        final GeneratePinRequest generatePinRequest = GeneratePinRequest.builder()
            .firstName("Name").build();
        final GeneratePinResponse response = idamClient.generatePin(generatePinRequest, TOKEN);
        assertThat(response.getPin()).isEqualTo(PIN);
    }

    @Test
    public void authenticatePinReturnsExpected() throws UnsupportedEncodingException {
        stubForAuthenticatePin();
        final AuthenticateUserResponse response = idamClient.authenticatePinUser(PIN, "");
        assertThat(response.getCode()).isEqualTo(PIN_AUTH_CODE);
    }

    @Test
    public void getAccessToken() {
        stubForOpenIdToken(HttpStatus.OK);
        final String token = idamClient.getAccessToken(USER_LOGIN, USER_PASSWORD);
        assertThat(token).isEqualTo(BEARER + TOKEN);
    }

    @Test
    public void getAccessTokenResponse() {
        stubForOpenIdToken(HttpStatus.OK);
        final TokenResponse tokenResponse = idamClient.getAccessTokenResponse(USER_LOGIN, USER_PASSWORD);
        assertThat(tokenResponse.accessToken).isEqualTo(TOKEN);
        assertThat(tokenResponse.expiresIn).isEqualTo("28800");
        assertThat(tokenResponse.idToken).isEqualTo(ID_TOKEN);
        assertThat(tokenResponse.refreshToken).isEqualTo(REFRESH_TOKEN);
        assertThat(tokenResponse.scope).isEqualTo("openid profile roles");
        assertThat(tokenResponse.tokenType).isEqualTo("Bearer");
    }

    @Test
    public void failedGetAccessToken() {
        stubForOpenIdToken(HttpStatus.UNAUTHORIZED);
        FeignException exception = assertThrows(FeignException.class, () ->
                idamClient.getAccessToken(USER_LOGIN, USER_PASSWORD)
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void findUserByUserId() throws JsonProcessingException {
        final String forename = "Hello";
        final String userId = "0a5874a4-3f38-4bbd";
        final String surname = "IDAM";
        final List<String> roles = List.of("citizen");
        UserDetails userDetails = UserDetails.builder()
                .id(userId)
                .email(USER_LOGIN)
                .forename(forename)
                .surname(surname)
                .roles(roles)
                .build();

        stubForUserByUserId(userDetails, userId);

        UserDetails found = idamClient.getUserByUserId(BEARER + TOKEN, userId);

        assertThat(found.getEmail()).isEqualTo(USER_LOGIN);
        assertThat(found.getForename()).isEqualTo(forename);
        found.getSurname().ifPresent(name -> assertThat(name).isEqualTo(surname));
        assertThat(found.getRoles()).isEqualTo(roles);
        assertThat(found.getId()).isEqualTo(userId);
    }

    private void stubForAuthenticateUser(HttpStatus responseStatus) {
        final String oauth2AuthorizeEndpoint = "/oauth2/authorize";
        final String authToken = "Basic dXNlckBleGFtcGxlLmNvbTpQYXNzd29yZDEy";
        final String successOauthBody = "{\"code\":\"eEdhNnasWy7eNFAV\"}";
        stubFor(WireMock.post(oauth2AuthorizeEndpoint)
            .withHeader(CONTENT_TYPE, containing(APPLICATION_FORM_URLENCODED.toString()))
            .withRequestBody(equalToIgnoreCase("response_type=code&"
                + "redirect_uri=https%3A%2F%2Flocalhost%3A5000%2Freceiver&client_id=bsp"))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(authToken))
            .willReturn(aResponse()
                .withStatus(responseStatus.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(successOauthBody)
            )
        );
    }

    private void stubForToken(String requestBody) {
        final String oauth2TokenEndpoint = "/oauth2/token";
        stubFor(WireMock.post(oauth2TokenEndpoint)
            .withHeader(CONTENT_TYPE, containing(APPLICATION_FORM_URLENCODED.toString()))
            .withRequestBody(equalToIgnoreCase(requestBody))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(EXCHANGE_CODE_RESULT)
            )
        );
    }

    private void stubForOpenIdToken(HttpStatus responseStatus) {
        final String openidTokenEndpoint = "/o/token";

        stubFor(WireMock.post(openidTokenEndpoint)
            .withHeader(CONTENT_TYPE, containing(APPLICATION_FORM_URLENCODED.toString()))
            .withRequestBody(equalToIgnoreCase("password=Password12&grant_type=password&"
                + "scope=openid+profile+roles&client_secret=123456&"
                + "redirect_uri=https%3A%2F%2Flocalhost%3A5000%2Freceiver&"
                + "client_id=bsp&username=user%40example.com"))
            .willReturn(aResponse().withStatus(responseStatus.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(OPENID_TOKEN_RESULT))
        );
    }

    private void stubForDetails(UserDetails userDetails) throws JsonProcessingException {
        final String detailsEndpoint = "/details";
        stubFor(WireMock.get(detailsEndpoint)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(userDetails))
            )
        );
    }

    private void stubForGeneratePin() throws JsonProcessingException {
        final String pinEndpoint = "/pin";
        GeneratePinResponse pinResponse = GeneratePinResponse.builder().pin(PIN).build();
        stubFor(WireMock.post(pinEndpoint)
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(pinResponse))
            )
        );
    }

    private void stubForAuthenticatePin() throws UnsupportedEncodingException {
        final String redirectUri = URLEncoder.encode(this.redirectUri, StandardCharsets.UTF_8.toString());
        final String pinEndpoint = String.format("/pin?client_id=%s&redirect_uri=%s&state", clientId, redirectUri);
        stubFor(WireMock.get(pinEndpoint)
            .willReturn(aResponse()
                .withStatus(HttpStatus.FOUND.value())
                .withHeader(HttpHeaders.LOCATION, pinRedirectUrl)
            )
        );
    }

    @Test
    public void findsUserInfoForAuthToken() throws JsonProcessingException {
        final String sub = "hello-idam@reform.local";
        final String uid = "hello-idam-01";
        final String name = "Hello IDAM";
        final String givenName = "Hello";
        final String familyName = "IDAM";
        final List<String> roles = List.of("citizen");

        UserInfo userDetails = UserInfo.builder()
            .sub(sub)
            .uid(uid)
            .name(name)
            .givenName(givenName)
            .familyName(familyName)
            .roles(roles)
            .build();

        stubForUserInfo(userDetails);

        UserInfo found = idamClient.getUserInfo(BEARER + TOKEN);

        assertThat(found.getSub()).isEqualTo(sub);
        assertThat(found.getUid()).isEqualTo(uid);
        assertThat(found.getName()).isEqualTo(name);
        assertThat(found.getGivenName()).isEqualTo(givenName);
        assertThat(found.getFamilyName()).isEqualTo(familyName);
        assertThat(found.getRoles()).isEqualTo(roles);
    }

    @Test
    public void searchUsers() throws JsonProcessingException {
        final String forename = "Hello";
        final String userId = "0a5874a4-3f38-4bbd";
        final String surname = "IDAM";
        final List<String> roles = List.of("citizen");
        UserDetails userDetails = UserDetails.builder()
                .id(userId)
                .email(USER_LOGIN)
                .forename(forename)
                .surname(surname)
                .roles(roles)
                .build();

        String query = "email:" + USER_LOGIN;
        stubForSearchUsers(List.of(userDetails), query);

        List<UserDetails> users = idamClient.searchUsers(BEARER + TOKEN, query);

        assertThat(users).hasSize(1);
        assertThat(users).containsExactly(userDetails);
    }

    private void stubForUserInfo(UserInfo userInfo) throws JsonProcessingException {
        stubFor(WireMock.get("/o/userinfo")
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(userInfo))
            )
        );
    }

    private void stubForUserByUserId(UserDetails userDetails, String userId) throws JsonProcessingException {
        stubFor(WireMock.get("/api/v1/users/" + userId)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(userDetails))
                )
        );
    }

    private void stubForSearchUsers(List<UserDetails> users, String query) throws JsonProcessingException {
        stubFor(WireMock.get(urlPathEqualTo("/api/v1/users"))
                .withQueryParam("query", equalTo(query))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(users))
                )
        );
    }
}

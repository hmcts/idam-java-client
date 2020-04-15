package uk.gov.hmcts.reform.idam.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.Lists;
import feign.FeignException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@SpringBootTest(classes = {IdamClient.class, IdamApi.class})
@PropertySource(value = "classpath:application.yml")
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
public class IdamClientTest {
    private final String BEARER = "Bearer ";
    private final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJ1c2FubmJyaGV2OWI0dGxzMzhy"
        + "MTI4dGdycCIsInN1YiI6IjI0IiwiaWF0IjoxNTUwNjk1Nzc5LCJleHAiOjE1NTA3MjQ1NzksImRhdGEiOiJjYXNld29ya2"
        + "VyLXNzY3MsY2FzZXdvcmtlci1zc2NzLWxvYTAiLCJ0eXBlIjoiQUNDRVNTIiwiaWQiOiIyNCIsImZvcmVuYW1lIjoiQnVs"
        + "ayBTY2FuIiwic3VybmFtZSI6IlN5c3RlbSBVcGRhdGUiLCJkZWZhdWx0LXNlcnZpY2UiOiJCU1AiLCJsb2EiOjAsImRlZm"
        + "F1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvYnNwIiwiZ3JvdXAiOiJic3Atc3lzdGVtdXBkYXRlIn0.P"
        + "djD2Kjz6myH1p44CRCVztkl2lqkg0LXqiyoH7Hs2bg";
    private final String REFRESH_TOKEN = "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwid2dJT1RBRm9qRTRyQymYmVLdTIsImFsZy"
        + "I6IlJTMjU2In0eyJzdWIiOiJkaXZvcmNlLWJzcC1zeXN0ZW0tdXBkYXRlQGhtY3RzLm5ldCIsImN0cyI6Ik9BVVRIMl9TVEFURUx"
        + "1NfR1JBTlQiLCJhdXRoX2xldmVsIjowLCJhdWRpdFRyYWNraW5nSWQiOiJmN2QwOGZmMC00MjU3LTQyNzMtOTE4Yy0wY2Y5MmYTI3"
        + "Y2ItMTA0MTM3MjIiLCJpc3MiOiJodHRwczovL2Zvcmdlcm9jay1hbS5zZXJ2aWNlLmNvcmUtY29tcHV0ZS1pZGFtLWFhdDIuaW50"
        + "ZXJuYWw6ODQ0My9vcGVuYW0vb2F1dGgyL2htY3RzIiwidG9rZW5OYW1lIjoicmVmcmVzaF90b2tlbiIsInRva2Vu3R5cGUiOiJCZW"
        + "EpVQst7LMVefQuZTkSD3KQ_YBnnKuicZc4mD-QLLOCY-InfC_P7Sf6ZloNiEX4H-WquApx9HLTHPNRsH3TNvJgWEJbRlpkE5dWcBe"
        + "fyJdXaI5YUO6bNxmby3jkqRVUMe25nOwWMiOyqoZDg3ehMGJuSqMguQwMrg1kc8RB6ZtVugPctVgW_ffE9EYc3i8yfTqq8rUvxDJI"
        +  "dCT4jkArwuitcvSmUg2XCTy_YoqdwQcGZD5vI3Wya1polA";
    private final String ID_TOKEN = "eyJ0eXAiOiJKV1QiLCJraWQiOiIxZXIwV1J3Z0lPVEFGb2pFNHJDL2ZiZUt1M0k9IiwiYWxnIj"
        + "oiUlMyNTYifQ.eyJhdF9oYXNoIjoibFpJWlg1M3BzRnVCZWlpMllLWTFBUSIsInN1YiI6ImRpdm9yY2UtYnNwLXN5c3RlbS11cGR"
        + "hdGVAaG1jdHMubmV0IiwiYXVkaXRUcmFja2luZ0lkIjoiZjdkMDhmZjAtNDI1Ny00MjczLTkxOGMtMGNmOTJkZGEyN2NiEwNDEzN"
        + "XQiOjE1ODY5NTI5NDB9.ZpCCIHT98QbKjNeJzALhwL2yjnHBbC4PLbGrrQaVi3thcZ5_TaGmSxcECTgpjGTESJbccvlDe7z1xihO"
        + "Vmxt1h2dGD9dAPBYSR6G0LEP_N5MUUCahVMDQeSBawzwW54AOsm4wwd5UUV_Xn8tAvovt4g-iZQGwBsi6t_FTLLYiPzapL-12jKt"
        + "oCKtFmyLlfcBXTLaPywi8oFfinCRaVQ83BiKtIXuImGrYN8WeZVtvZwAQzmHqA4PoDJBzOJWptL-Z63wVFoQZy2AHaFLcR_Yv07w";
    private final String EXCHANGE_CODE_RESULT = String.format(
        "{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":28800}", TOKEN);

    private final String OPENID_TOKEN_RESULT = String.format(
            "{\"access_token\":\"%s\",\"refresh_token\":\"%s\",\"id_token\":\"%s\",\"token_type\":\"Bearer\","
                    + "\"scope\": \"openid\",\"expires_in\":28800}", TOKEN, REFRESH_TOKEN, ID_TOKEN);

    private final String USER_LOGIN = "user@example.com";
    private final String USER_PASSWORD = "Password12";
    private final String EXCHANGE_CODE = "eEdhNnasWy7eNFAV";
    private final String PIN_AUTH_CODE = "abcdefgh123456789";
    private final String PIN = "ABCD1234";
    private String PIN_REDIRECT_URL;
    @Value("${idam.client.redirect_uri:}") private String REDIRECT_URI;
    @Value("${idam.client.id:}") private String CLIENT_ID;
    @Value("${idam.client.secret:}") private String CLIENT_SECRET;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        PIN_REDIRECT_URL = REDIRECT_URI + "?code=" + PIN_AUTH_CODE;

        // See https://www.baeldung.com/jackson-optional for why this is needed
        objectMapper.registerModule(new Jdk8Module());
    }

    @Rule
    public WireMockClassRule idamApiServer = new WireMockClassRule(WireMockSpring
        .options()
        .port(5050)
        .extensions(new ConnectionCloseExtension()));

    @Autowired
    private IdamClient idamClient;

    @Test
    public void authenticateUser() {
        stubForAuthenticateUser(HttpStatus.OK);
        stubForToken();
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
        final String FORENAME = "Hello";
        final String SURNAME = "IDAM";
        UserDetails userDetails = UserDetails.builder()
            .email(USER_LOGIN)
            .forename(FORENAME)
            .surname(SURNAME)
            .build();
        stubForDetails(userDetails);
        UserDetails found = idamClient.getUserDetails(BEARER + TOKEN);
        assertThat(userDetails.getEmail()).isEqualTo(USER_LOGIN);
        found.getSurname().ifPresent(name -> assertThat(name).isEqualTo(SURNAME));
        assertThat(found.getFullName()).isEqualTo(FORENAME + " " + SURNAME);
    }

    @Test
    public void exchangeCodeReturnsExpected() {
        stubForToken();
        ExchangeCodeRequest exchangeCodeRequest = ExchangeCodeRequest.builder()
            .code(EXCHANGE_CODE).build();
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
        final TokenResponse tokenResponse = idamClient.getAccessToken(USER_LOGIN, USER_PASSWORD);
        assertThat(tokenResponse.accessToken).isEqualTo(TOKEN);
    }

    @Test
    public void failedGetAccessToken() {
        stubForOpenIdToken(HttpStatus.UNAUTHORIZED);
        FeignException exception = assertThrows(FeignException.class, () ->
                idamClient.getAccessToken(USER_LOGIN, USER_PASSWORD)
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    private void stubForAuthenticateUser(HttpStatus responseStatus) {
        final String OAUTH2_AUTHORIZE_ENDPOINT = "/oauth2/authorize";
        final String AUTH_TOKEN = "Basic dXNlckBleGFtcGxlLmNvbTpQYXNzd29yZDEy";
        final String SUCCESS_OAUTH_BODY = "{\"code\":\"eEdhNnasWy7eNFAV\"}";
        idamApiServer.stubFor(WireMock.post(OAUTH2_AUTHORIZE_ENDPOINT)
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(responseStatus.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(SUCCESS_OAUTH_BODY)
            )
        );
    }

    private void stubForToken() {
        final String OAUTH2_TOKEN_ENDPOINT = "/oauth2/token";
        idamApiServer.stubFor(WireMock.post(OAUTH2_TOKEN_ENDPOINT)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(EXCHANGE_CODE_RESULT)
            )
        );
    }

    private void stubForOpenIdToken(HttpStatus responseStatus) {
        final String OPENID_TOKEN_ENDPOINT = "/o/token";

        idamApiServer.stubFor(WireMock.post(OPENID_TOKEN_ENDPOINT)
                .withHeader(CONTENT_TYPE, containing("application/x-www-form-urlencoded"))
                .willReturn(aResponse()
                        .withStatus(responseStatus.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(OPENID_TOKEN_RESULT)
                )
        );
    }

    private void stubForDetails(UserDetails userDetails) throws JsonProcessingException {
        final String DETAILS_ENDPOINT = "/details";
        idamApiServer.stubFor(WireMock.get(DETAILS_ENDPOINT)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(userDetails))
            )
        );
    }

    private void stubForGeneratePin() throws JsonProcessingException {
        final String PIN_ENDPOINT = "/pin";
        GeneratePinResponse pinResponse = GeneratePinResponse.builder().pin(PIN).build();
        idamApiServer.stubFor(WireMock.post(PIN_ENDPOINT)
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(pinResponse))
            )
        );
    }

    private void stubForAuthenticatePin() throws UnsupportedEncodingException {
        final String redirectUri = URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8.toString());
        final String PIN_ENDPOINT = String.format("/pin?client_id=%s&redirect_uri=%s&state", CLIENT_ID, redirectUri);
        idamApiServer.stubFor(WireMock.get(PIN_ENDPOINT)
            .willReturn(aResponse()
                .withStatus(HttpStatus.FOUND.value())
                .withHeader(HttpHeaders.LOCATION, PIN_REDIRECT_URL)
            )
        );
    }

    @Test
    public void findsUserInfoForAuthToken() throws JsonProcessingException {
        final String SUB = "hello-idam@reform.local";
        final String UID = "hello-idam-01";
        final String NAME = "Hello IDAM";
        final String GIVEN_NAME = "Hello";
        final String FAMILY_NAME = "IDAM";
        final List<String> ROLES = Lists.newArrayList("citizen");

        UserInfo userDetails = UserInfo.builder()
            .sub(SUB)
            .uid(UID)
            .name(NAME)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .roles(ROLES)
            .build();

        stubForUserInfo(userDetails);

        UserInfo found = idamClient.getUserInfo(BEARER + TOKEN);

        assertThat(found.getSub()).isEqualTo(SUB);
        assertThat(found.getUid()).isEqualTo(UID);
        assertThat(found.getName()).isEqualTo(NAME);
        assertThat(found.getGivenName()).isEqualTo(GIVEN_NAME);
        assertThat(found.getFamilyName()).isEqualTo(FAMILY_NAME);
        assertThat(found.getRoles()).isEqualTo(ROLES);
    }

    private void stubForUserInfo(UserInfo userInfo) throws JsonProcessingException {
        idamApiServer.stubFor(WireMock.get("/o/userinfo")
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(userInfo))
            )
        );
    }
}

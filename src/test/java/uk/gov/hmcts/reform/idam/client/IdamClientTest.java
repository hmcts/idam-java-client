package uk.gov.hmcts.reform.idam.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import feign.FeignException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.idam.client.models.ExchangeCodeRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenExchangeResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@SpringBootTest(classes = {IdamClient.class, IdamApi.class})
@PropertySource(value = "classpath:application.yml")
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
public class IdamClientTest {

    @Value("idam.client.redirect_uri")
    private final String BEARER = "Bearer ";
    private final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJ1c2FubmJyaGV2OWI0dGxzMzhy"
        + "MTI4dGdycCIsInN1YiI6IjI0IiwiaWF0IjoxNTUwNjk1Nzc5LCJleHAiOjE1NTA3MjQ1NzksImRhdGEiOiJjYXNld29ya2"
        + "VyLXNzY3MsY2FzZXdvcmtlci1zc2NzLWxvYTAiLCJ0eXBlIjoiQUNDRVNTIiwiaWQiOiIyNCIsImZvcmVuYW1lIjoiQnVs"
        + "ayBTY2FuIiwic3VybmFtZSI6IlN5c3RlbSBVcGRhdGUiLCJkZWZhdWx0LXNlcnZpY2UiOiJCU1AiLCJsb2EiOjAsImRlZm"
        + "F1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvYnNwIiwiZ3JvdXAiOiJic3Atc3lzdGVtdXBkYXRlIn0.P"
        + "djD2Kjz6myH1p44CRCVztkl2lqkg0LXqiyoH7Hs2bg";
    private final String EXCHANGE_CODE_RESULT = "{\"access_token\":\"eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJ1c2FubmJyaGV2OWI0dGxzMzhyMTI4dGdycCIsInN1YiI6IjI0IiwiaWF0IjoxNTUwNjk1Nzc5LCJleHAiOjE1NTA3MjQ1NzksImRhdGEiOiJjYXNld29ya2VyLXNzY3MsY2FzZXdvcmtlci1zc2NzLWxvYTAiLCJ0eXBlIjoiQUNDRVNTIiwiaWQiOiIyNCIsImZvcmVuYW1lIjoiQnVsayBTY2FuIiwic3VybmFtZSI6IlN5c3RlbSBVcGRhdGUiLCJkZWZhdWx0LXNlcnZpY2UiOiJCU1AiLCJsb2EiOjAsImRlZmF1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvYnNwIiwiZ3JvdXAiOiJic3Atc3lzdGVtdXBkYXRlIn0.PdjD2Kjz6myH1p44CRCVztkl2lqkg0LXqiyoH7Hs2bg\",\"token_type\":\"Bearer\",\"expires_in\":28800}";
    private final String USER_LOGIN = "user@example.com";
    private final String USER_PASSWORD = "Password12";
    private final String EXCHNAGE_CODE = "eEdhNnasWy7eNFAV";

    @Rule
    public WireMockClassRule idamApiServer = new WireMockClassRule(5050);

    @Autowired
    private IdamClient idamClient;

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
        stubForDetails();
        UserDetails userDetails = idamClient.getUserDetails(BEARER + TOKEN);
        assertThat(userDetails.getEmail()).isEqualTo(USER_LOGIN);
    }

    @Test
    public void exchangeCodeReturnsExpected() {
        stubForToken();
        ExchangeCodeRequest exchangeCodeRequest = ExchangeCodeRequest.builder()
            .code(EXCHNAGE_CODE).build();
        final TokenExchangeResponse response = idamClient.exchangeCode(exchangeCodeRequest);
        assertThat(response.getAccessToken()).isEqualTo(TOKEN);
    }

    private void stubForAuthenticateUser(HttpStatus responseStatus) {
        final String OAUTH2_AUTHORIZE_ENDPOINT = "/oauth2/authorize";
        final String AUTH_TOKEN = "Basic dXNlckBleGFtcGxlLmNvbTpQYXNzd29yZDEy";
        final String SUCCESS_OAUTH_BODY = "{\"code\":\"eEdhNnasWy7eNFAV\"}";
        idamApiServer.stubFor(WireMock.post(OAUTH2_AUTHORIZE_ENDPOINT)
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(responseStatus.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(SUCCESS_OAUTH_BODY)
            )
        );
    }

    private void stubForToken() {
        final String OAUTH2_TOKEN_ENDPOINT = "/oauth2/token";
        idamApiServer.stubFor(WireMock.post(OAUTH2_TOKEN_ENDPOINT)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(EXCHANGE_CODE_RESULT)
            )
        );
    }

    private void stubForDetails() throws JsonProcessingException {
        final String DETAILS_ENDPOINT = "/details";
        UserDetails userDetails = UserDetails.builder().email(USER_LOGIN).build();
        idamApiServer.stubFor(WireMock.get(DETAILS_ENDPOINT)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(objectMapper.writeValueAsString(userDetails))
            )
        );
    }
}

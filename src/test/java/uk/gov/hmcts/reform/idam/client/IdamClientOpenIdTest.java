package uk.gov.hmcts.reform.idam.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import feign.FeignException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToIgnoreCase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@SpringBootTest(classes = {IdamClient.class, IdamApi.class})
@TestPropertySource(value = "classpath:application.yml",
    properties = {"idam.client.enable-cache=true", "idam.client.refresh-before-expire-in-sec=5"})
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
public class IdamClientOpenIdTest {
    private static final String TOKEN = "dddIIiwiaWF0IjoxNTUwNjk1Nzc5LCJleHAiOjE1NTA3MjQ1NzksImRhdGEiOiJjYXNld29ya2"
        + "F1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvYnNwIiwiZ3JvdXAiOiJic3Atc3lzdGVtdXBkYXRlIn0.P"
        + "djD2Kjz6myH1p44CRCVztkl2lqkg0LXqiyoH7Hs2bg";
    private static final String REFRESH_TOKEN = "NlLWJzcC1zeXN0ZW0tdXBkYXRlQGhtY3RzLm5ldCIsImN0cyI6Ik9BVVRIMl9TVEFURUx"
        + "fyJdXaI5YUO6bNxmby3jkqRVUMe25nOwWMiOyqoZDg3ehMGJuSqMguQwMrg1kc8RB6ZtVugPctVgW_ffE9EYc3i8yfTqq8rUvxDJI"
        +  "dCT4jkArwuitcvSmUg2XCTy_YoqdwQcGZD5vI3Wya1polA";
    private static final String ID_TOKEN = "oiUlMyNTYifQ.eyJhdF9oYXNoIjoibFpJWlg1M3BzRnVCZWlpMllLWTFBUSIsInN1YiI6ImRpdR"
        + "Vmxt1h2dGD9dAPBYSR6G0LEP_N5MUUCahVMDQeSBawzwW54AOsm4wwd5UUV_Xn8tAvovt4g-iZQGwBsi6t_FTLLYiPzapL-12jKt"
        + "oCKtFmyLlfcBXTLaPywi8oFfinCRaVQ83BiKtIXuImGrYN8WeZVtvZwAQzmHqA4PoDJBzOJWptL-Z63wVFoQZy2AHaFLcR_Yv07w";

    private static final String RESPONSE_TEMP = "{\"access_token\":\"%s\",\"refresh_token\":\"%s\","
        + "\"id_token\":\"%s\",\"token_type\":\"Bearer\","
        + "\"scope\": \"openid\",\"expires_in\":\"10\"}";

    private final String OPENID_TOKEN_RESULT = String.format(RESPONSE_TEMP, TOKEN, REFRESH_TOKEN, ID_TOKEN);

    private static final String OPENID_TOKEN_REQUEST = "password=Password12&grant_type=password&"
        + "scope=openid&client_secret=123456&redirect_uri=https%3A%2F%2Flocalhost%3A5000%2Freceiver&"
        + "client_id=bsp&username=user%40example.com";

    private static final String USER_LOGIN = "user@example.com";
    private static final String USER_PASSWORD = "Password12";

    @ClassRule
    public static WireMockClassRule idamApiServer = new WireMockClassRule(WireMockSpring
        .options()
        .port(5050)
        .extensions(new ConnectionCloseExtension()));

    @SpyBean
    private IdamClient idamClient;

    @Test
    public void getAccessToken() {
        stubForOpenIdToken(HttpStatus.OK);
        final String token = idamClient.getAccessToken(USER_LOGIN, USER_PASSWORD);
        assertThat(token).isEqualTo(TOKEN);
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    public void getAccessTokenWithCache() {
        stubForOpenIdToken(HttpStatus.OK);
        final String token = idamClient.getAccessToken(USER_LOGIN, USER_PASSWORD);
        assertThat(token).isEqualTo(TOKEN);
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
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    public void getAccessTokenWithCache_getFromCache() {
        stubForOpenIdToken(HttpStatus.OK);
        final String token1 = idamClient.getAccessToken(USER_LOGIN, USER_PASSWORD);
        idamApiServer.resetAll();
        final String token2 = idamClient.getAccessToken(USER_LOGIN, USER_PASSWORD);
        assertThat(token1).isEqualTo(TOKEN);
        assertThat(token1).isEqualTo(token2);
        verify(idamClient, times(1)).retrieveAccessToken(any(), any());
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    public void getAccessTokenWithCache_cacheExpires()
        throws InterruptedException {
        stubForOpenIdToken(HttpStatus.OK);
        final String token1 = idamClient.getAccessToken(USER_LOGIN, USER_PASSWORD);
        Thread.sleep(6000);
        final String token2 = idamClient.getAccessToken(USER_LOGIN, USER_PASSWORD);
        assertThat(token1).isEqualTo(TOKEN);
        assertThat(token1).isEqualTo(token2);
        verify(idamClient, times(2)).retrieveAccessToken(any(), any());
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    public void getAccessTokenWithCache_differentKeys_noCache() {
        String userLogin = "user2@example.com";
        String userPassword = "user2pass";
        String request = "password=user2pass&grant_type=password&"
            + "scope=openid&client_secret=123456&redirect_uri=https%3A%2F%2Flocalhost%3A5000%2Freceiver&"
            + "client_id=bsp&username=user2%40example.com";

        stubForOpenIdToken(HttpStatus.OK);
        final String token1 = idamClient.getAccessToken(USER_LOGIN, USER_PASSWORD);
        String response =
            String.format(RESPONSE_TEMP, "TOKEN_xxxxxxx", "REFRESH_TOKEN_xddddd", "ID_token_xxxxx");
        stubForOpenIdToken(HttpStatus.OK, request, response);
        final String token2 = idamClient.getAccessToken(userLogin, userPassword);
        assertThat(token1).isEqualTo(TOKEN);
        assertThat(token2).isEqualTo("TOKEN_xxxxxxx");
        verify(idamClient, times(2)).retrieveAccessToken(any(), any());
    }

    private void stubForOpenIdToken(HttpStatus responseStatus) {
        stubForOpenIdToken(responseStatus, OPENID_TOKEN_REQUEST, OPENID_TOKEN_RESULT);
    }

    private void stubForOpenIdToken(HttpStatus responseStatus, String request, String response) {
        final String OPENID_TOKEN_ENDPOINT = "/o/token";
        idamApiServer.stubFor(WireMock.post(OPENID_TOKEN_ENDPOINT)
            .withHeader(CONTENT_TYPE, containing(APPLICATION_FORM_URLENCODED.toString()))
            .withRequestBody(equalToIgnoreCase(request))
            .willReturn(aResponse().withStatus(responseStatus.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(response))
        );
    }
}

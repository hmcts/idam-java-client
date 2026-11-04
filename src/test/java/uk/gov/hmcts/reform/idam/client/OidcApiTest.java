package uk.gov.hmcts.reform.idam.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToIgnoreCase;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@SpringBootTest(
    classes = {IdamClient.class, IdamApi.class, OidcApi.class, OAuth2Configuration.class},
    properties = { "idam.oidc.use_oidc_api=true" }
)
@EnableAutoConfiguration
@EnableWireMock(@ConfigureWireMock(name = "oidc-api", port = 5051))
public class OidcApiTest {

    private static final String BEARER = "Bearer ";
    private static final String TOKEN = "oidc-access-token";
    private static final String USER_LOGIN = "user@example.com";
    private static final String USER_PASSWORD = "Password12";
    private static final String OPENID_TOKEN_RESULT = String.format(
        "{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":28800}", TOKEN);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private IdamClient idamClient;

    @MockitoSpyBean
    private OidcApi oidcApi;

    @MockitoBean
    private IdamApi idamApi;

    @Test
    public void generatesOpenIdToken() {
        stubFor(WireMock.post("/o/token")
            .withHeader(CONTENT_TYPE, containing(APPLICATION_FORM_URLENCODED.toString()))
            .withRequestBody(equalToIgnoreCase("password=Password12&grant_type=password&"
                + "scope=openid+profile+roles&client_secret=123456&"
                + "redirect_uri=https%3A%2F%2Flocalhost%3A5000%2Freceiver&"
                + "client_id=bsp&username=user%40example.com"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(OPENID_TOKEN_RESULT))
        );

        TokenResponse tokenResponse = idamClient.getAccessTokenResponse(USER_LOGIN, USER_PASSWORD);

        assertThat(tokenResponse.accessToken).isEqualTo(TOKEN);
        verifyNoInteractions(idamApi);
        verify(oidcApi).generateOpenIdToken(any(TokenRequest.class));
    }

    @Test
    public void retrievesUserInfo() throws JsonProcessingException {
        UserInfo userInfo = UserInfo.builder()
            .sub("hello-idam@reform.local")
            .uid("hello-idam-01")
            .name("Hello IDAM")
            .roles(List.of("citizen"))
            .build();
        stubFor(WireMock.get("/o/userinfo")
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(BEARER + TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(userInfo)))
        );

        UserInfo found = idamClient.getUserInfo(BEARER + TOKEN);

        assertThat(found).isEqualTo(userInfo);
        verifyNoInteractions(idamApi);
        verify(oidcApi).retrieveUserInfo(any(String.class));
    }
}

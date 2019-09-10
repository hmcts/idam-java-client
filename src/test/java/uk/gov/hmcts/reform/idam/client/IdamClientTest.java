package uk.gov.hmcts.reform.idam.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import feign.FeignException;
import org.junit.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {IdamClientAutoConfiguration.class, IdamClient.class, IdamApi.class})
@ExtendWith({
    SpringExtension.class
})
@EnableAutoConfiguration
class IdamClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(5050);

    @Autowired
    private IdamClient idamClient;

    @Test
    @DisplayName("should return bearer token when successful")
    void authenticateUser() {
        // user is configured in wiremock json file as should return successful
        String bearerToken = idamClient.authenticateUser("user@example.com", "Password12");
        assertThat(bearerToken).isEqualTo("Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJ1c2FubmJyaGV2OWI0dGxzMzhy"
            + "MTI4dGdycCIsInN1YiI6IjI0IiwiaWF0IjoxNTUwNjk1Nzc5LCJleHAiOjE1NTA3MjQ1NzksImRhdGEiOiJjYXNld29ya2"
            + "VyLXNzY3MsY2FzZXdvcmtlci1zc2NzLWxvYTAiLCJ0eXBlIjoiQUNDRVNTIiwiaWQiOiIyNCIsImZvcmVuYW1lIjoiQnVs"
            + "ayBTY2FuIiwic3VybmFtZSI6IlN5c3RlbSBVcGRhdGUiLCJkZWZhdWx0LXNlcnZpY2UiOiJCU1AiLCJsb2EiOjAsImRlZm"
            + "F1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvYnNwIiwiZ3JvdXAiOiJic3Atc3lzdGVtdXBkYXRlIn0.P"
            + "djD2Kjz6myH1p44CRCVztkl2lqkg0LXqiyoH7Hs2bg");
    }

    @Test
    @DisplayName("should throw exception when unsuccessful authentication")
    void failedToAuthenticateUser() {
        // user is configured in wiremock json file as should return 401
        FeignException exception = assertThrows(FeignException.class, () ->
            idamClient.authenticateUser("anotheruser@example.com", "Password123")
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    void stubForAuthenticateUser() {

    }
}

package uk.gov.hmcts.reform.idam.client;

import feign.FeignException;
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
@AutoConfigureWireMock
class IdamClientTest {

    @Autowired
    private IdamClient idamClient;

    @Test
    @DisplayName("should return bearer token when successful")
    void authenticateUser() {
        // user is configured in wiremock json file as should return successful
        String bearerToken = idamClient.authenticateUser("user@example.com", "Password12");
        assertThat(bearerToken).isEqualTo("Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJ2YjRrYTlwYW"
                + "c5a2x2a3Bqczhqb241bDhrdCIsInN1YiI6IjMxIiwiaWF0IjoxNTM3MzcwMDgxLCJleHAiOjE1Mz"
                + "czOTg4ODEsImRhdGEiOiJjYXNld29ya2VyLXNzY3MsY2FzZXdvcmtlcixjYXNld29ya2VyLXNzY3"
                + "MtbG9hMSxjYXNld29ya2VyLWxvYTEiLCJ0eXBlIjoiQUNDRVNTIiwiaWQiOiIzMSIsImZvcmVuYW"
                + "1lIjoiQ2FzZSIsInN1cm5hbWUiOiJXb3JrZXIiLCJkZWZhdWx0LXNlcnZpY2UiOiJDQ0QiLCJsb2"
                + "EiOjEsImRlZmF1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvY2NkIiwiZ3JvdX"
                + "AiOiJjYXNld29ya2VyIn0.F3qGuDsFb_8hgyFHMNjEow0RMTTaBz2VIuRTZpbVa80");
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
}

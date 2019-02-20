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
        assertThat(bearerToken).isEqualTo("Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIzYTY3MmJkdm5tYW1sZmFlaDB2ODFia2V1NyIsInN1YiI6IjI0IiwiaWF0IjoxNTUwNjY2ODM0LCJleHAiOjE1NTA2OTU2MzQsImRhdGEiOiJjYXNld29ya2VyLXNzY3MsY2FzZXdvcmtlci1zc2NzLWxvYTAiLCJ0eXBlIjoiQUNDRVNTIiwiaWQiOiIyNCIsImZvcmVuYW1lIjoiQnVsayBTY2FuIiwic3VybmFtZSI6IlN5c3RlbSBVcGRhdGUiLCJkZWZhdWx0LXNlcnZpY2UiOiJCU1AiLCJsb2EiOjAsImRlZmF1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvYnNwIiwiZ3JvdXAiOiJic3Atc3lzdGVtdXBkYXRlIn0.BbVZAYAkRq5o0aPD5TIk-jmVeo20f9RPNPUFYhrbz5s");
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

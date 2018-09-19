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
        String bearerToken = idamClient.authenticateUser("user@example.com", "Password12");
        assertThat(bearerToken).isNotEmpty();
    }

    @Test
    @DisplayName("should throw exception when unsuccessful authentication")
    void failedToAuthenticateUser() {
        FeignException exception = assertThrows(FeignException.class, () ->
                idamClient.authenticateUser("anotheruser@example.com", "Password123")
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
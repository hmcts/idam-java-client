package uk.gov.hmcts.reform.idam.client;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@PropertySource(value = "classpath:application.yml")
@EnableAutoConfiguration
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "Idam_api", port = "5050")
@SpringBootTest(classes = {IdamClient.class})
public class IdamClientConsumerTest {

    @Autowired
    private IdamClient idamClient;

    @Pact(state = "provider returns an access token", provider = "Idam_api", consumer = "idamClient")
    RequestResponsePact getAccessTokenPact(PactDslWithProvider builder) {
        // @formatter:off

        Map<String, Object> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        params.put("username", "emCaseOfficer@email.net");
        params.put("password", "Password123");

        return builder
                .given("returns a new access token", params)
                .uponReceiving("a request to POST a citizen")
                .path("/o/token")
                .method("POST")
                .willRespondWith()
                .status(201)
                .matchHeader("Content-Type", "application/json")
                .body(new PactDslJsonBody()
                        .stringType("access_token", "some_access_token")
                        .stringType("expires_in", "1000")
                        .stringType("id_token", "some_id_token")
                        .stringType("refresh_token", "some_refresh_token")
                        .stringType("scope", "openid profile roles")
                        .stringType("token_type", "bearer")
                )
                .toPact();
        // @formatter:on
    }

    @Test
    @PactTestFor(pactMethod = "getAccessTokenPact")
    void verifyGetAccessTokenPact() {

        String accessToken = idamClient.getAccessToken("emCaseOfficer@email.net", "Password123");

        assertThat(accessToken).isEqualTo("Bearer some_access_token");
        assert(false);
    }
}

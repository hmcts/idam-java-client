package uk.gov.hmcts.reform.idam.client;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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

    private static final String IDAM_OPENID_TOKEN_URL = "/o/token";

    @Pact(provider = "Idam_api", consumer = "idamClient")
    public RequestResponsePact executeGetIdamAccessTokenAndGet200(PactDslWithProvider builder) throws JSONException {

        String[] rolesArray = new String[1];
        rolesArray[0] = "citizen";
        Map<String, String> requestheaders = Maps.newHashMap();
        requestheaders.put("Content-Type", "application/x-www-form-urlencoded");

        Map<String, String> responseheaders = Maps.newHashMap();
        responseheaders.put("Content-Type", "application/json");

        Map<String, Object> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        params.put("email", "emCaseOfficer@email.net");
        params.put("password", "Password123");
        params.put("forename","emCaseOfficer");
        params.put("surname", "jar123");
        params.put("roles", rolesArray);

        return builder
                .given("a user exists", params)
                .uponReceiving("Provider takes user/pwd and returns Auth code to Idam Client")
                .path(IDAM_OPENID_TOKEN_URL)
                .method(HttpMethod.POST.toString())
                .body(


                        "redirect_uri=https%3A%2F%2Flocalhost%3A5000%2Freceiver&client_id=pact&grant_type=password"
                        + "&username=emCaseOfficer%40email.net&password=Password123&client_secret=pactsecret"
                        + "&scope=openid profile roles", "application/x-www-form-urlencoded")
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(responseheaders)
                .body(createAuthResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetIdamAccessTokenAndGet200")
    void verifyGetAccessTokenPact() {

        String accessToken = idamClient.getAccessToken("emCaseOfficer@email.net", "Password123");

        assertThat(accessToken).isNotNull();
        assertThat(accessToken).contains("Bearer");
    }

    private PactDslJsonBody createAuthResponse() {

        return new PactDslJsonBody()
                .stringMatcher("access_token", "[a-zA-Z0-9._-]+", "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FI.AL_JD-")
                .stringMatcher("refresh_token", "[a-zA-Z0-9._-]+", "eyJ0eXAiOiJKV1QiLCJ6aXAiO.iJOT05FIiwia2lkIjoi_i9PN-k92V")
                .stringType("scope", "openid roles profile")
                .stringMatcher("id_token", "[a-zA-Z0-9._-]+", "eyJ0e.XAiOiJKV1QiLCJra-WQiOiJiL082_T3ZWdjEre")
                .stringType("token_type", "Bearer")
                .stringMatcher("expires_in", "[0-9]+", "28798");
    }
}

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
                .body("redirect_uri=https%3A%2F%2Flocalhost%3A5000%2Freceiver&client_id=bsp&grant_type=password"
                        + "&username=emCaseOfficer%40email.net&password=Password123&client_secret=123456"
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

        assertThat(accessToken).isEqualTo("Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FI");
    }

    private PactDslJsonBody createAuthResponse() {

        return new PactDslJsonBody()
                .stringType("access_token", "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FI")
                .stringType("refresh_token", "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92V")
                .stringType("scope", "openid roles profile")
                .stringType("id_token", "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre")
                .stringType("token_type", "Bearer")
                .stringType("expires_in","28798");
    }
}

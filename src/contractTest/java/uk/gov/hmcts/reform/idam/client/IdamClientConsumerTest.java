package uk.gov.hmcts.reform.idam.client;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@PropertySource(value = "classpath:application.yml")
@EnableAutoConfiguration
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "Idam_api", port = "5050", pactMethod = "executeGetIdamAccessTokenAndGet200")
@SpringBootTest(classes = {IdamClient.class})
public class IdamClientConsumerTest {

    @Autowired
    private IdamClient idamClient;

    private static final String IDAM_OPENID_TOKEN_URL = "/o/token";

    private static final String IDAM_DETAILS_URL = "/details";

    private static final String SEARCH_USERS_PATH = "/api/v1/users";

    private static final String ACCESS_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiRm8rQXAybThDT3ROb290ZjF4TWg0bGc3MFlBPSIsImFsZyI6IlJTMjU2In0.";

    @BeforeEach
    public void beforeEach() throws Exception {
        Thread.sleep(4000);
    }

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
                .uponReceiving("Provider takes user/pwd and returns token to Idam Client")
                .path(IDAM_OPENID_TOKEN_URL)
                .method(HttpMethod.POST.toString())
                .body("redirect_uri=https%3A%2F%2Flocalhost%3A5000%2Freceiver&client_id=pact&grant_type=password"
                        + "&username=emCaseOfficer%40email.net&password=Password123&client_secret=pactsecret"
                        + "&scope=openid profile roles search-user", "application/x-www-form-urlencoded")
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(responseheaders)
                .body(createAuthResponse())
                .toPact();
    }

    @Pact(provider = "Idam_api", consumer = "idamClient")
    public RequestResponsePact executeGetSearchUsersAndGet200(PactDslWithProvider builder) throws JSONException {

        return builder
                .given("Users exist for search")
                .uponReceiving("Provider receives a GET /api/v1/users with search query")
                .path(SEARCH_USERS_PATH)
                .matchQuery("query", ".*", "email:\"bob@hmcts.com\"")
                .method(HttpMethod.GET.toString())
                .headers(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createSearchUsersResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetIdamAccessTokenAndGet200")
    void verifyGetAccessTokenPact() {

        String returnedAccessToken = idamClient.getAccessToken("emCaseOfficer@email.net", "Password123");

        assertThat(returnedAccessToken).isNotNull();
        assertThat(returnedAccessToken).contains("Bearer");
    }

    @Test
    @PactTestFor(pactMethod = "executeGetSearchUsersAndGet200")
    void verifyGetSearchUsers() {

        List<UserDetails> result = idamClient.searchUsers(ACCESS_TOKEN, "email:\"bob@hmcts.com\"");

        assertThat(result).hasSize(1);
        UserDetails user = result.get(0);

        assertThat(user.getId()).isNotEmpty();
        assertThat(user.getEmail()).isNotEmpty();
        assertThat(user.getForename()).isNotEmpty();
        assertThat(user.getRoles()).isNotEmpty();
    }

    private PactDslJsonBody createAuthResponse() {

        return new PactDslJsonBody()
                .stringMatcher("access_token", "[a-zA-Z0-9._-]+",
                        "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FI.AL_JD-")
                .stringMatcher("refresh_token", "[a-zA-Z0-9._-]+",
                        "eyJ0eXAiOiJKV1QiLCJ6aXAiO.iJOT05FIiwia2lkIjoi_i9PN-k92V")
                .stringType("scope", "openid roles profile search-user")
                .stringMatcher("id_token", "[a-zA-Z0-9._-]+",
                        "eyJ0e.XAiOiJKV1QiLCJra-WQiOiJiL082_T3ZWdjEre")
                .stringType("token_type", "Bearer")
                .stringMatcher("expires_in", "[0-9]+", "28798");
    }

    private DslPart createSearchUsersResponse() {

        return new PactDslJsonArray()
                .arrayEachLike()
                .stringMatcher("id", "[a-zA-Z0-9-]+", "a833c2e2-2c73-4900-96ca-74b1efb37928")
                .stringMatcher("email", "^(.+)@(.+)$", "bob@hmcts.com")
                .stringType("forename", "Bob")
                .stringType("surname", "Reform")
                .minArrayLike("roles", 1, PactDslJsonRootValue.stringType("caseWorker"), 1)
                .closeObject();
    }
}

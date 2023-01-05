package uk.gov.hmcts.reform.idam.client;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@PropertySource(value = "classpath:application.yml")
@EnableAutoConfiguration
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "Idam_api", port = "5050")
@SpringBootTest(classes = {IdamClient.class, OAuth2Configuration.class})
public class IdamClientConsumerTest {

    public static final String TOKEN_REGEXP = "[a-zA-Z0-9._-]+";
    public static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";

    @Autowired
    private IdamClient idamClient;

    private static final String IDAM_OPENID_TOKEN_URL = "/o/token";
    private static final String IDAM_OPENID_USERINFO_URL = "/o/userinfo";

    @BeforeEach
    public void beforeEach() throws Exception {
        Thread.sleep(4000);
    }

    @Pact(consumer = "idamClient")
    public RequestResponsePact executeGetUserInfo(PactDslWithProvider builder) {

        Map<String, Object> params = new HashMap<>();
        params.put("redirect_uri", "http://www.dummy-pact-service.com/callback");
        params.put("client_id", "pact");
        params.put("client_secret", "pactsecret");
        params.put("scope", "openid profile roles");
        params.put("username", "damian@swansea.gov.uk");
        params.put("password", "Password12");

        return builder.given("I have obtained an access_token as a user", params)
                .uponReceiving("IDAM returns user info to the client")
                .path(IDAM_OPENID_USERINFO_URL)
                .headerFromProviderState("Authorization", "Bearer ${access_token}",
                        BEARER_TOKEN)
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createUserInfoResponse())
                .toPact();
    }


    @Pact(consumer = "idamClient")
    public RequestResponsePact executeGetIdamAccessTokenAndGet200(PactDslWithProvider builder)  {

        String[] rolesArray = new String[1];
        rolesArray[0] = "citizen";

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
                .body(createAuthResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserInfo", providerType = ProviderType.ASYNCH)
    void verifyUserInfo() {
        UserInfo actualUserInfo = idamClient.getUserInfo(BEARER_TOKEN);

        UserInfo expectedUserInfo = UserInfo.builder()
                .familyName("Smith")
                .givenName("John")
                .name("John Smith")
                .roles(Lists.newArrayList("caseworker-publiclaw-solicitor"))
                .sub("damian@swansea.gov.uk")
                .uid("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
                .build();

        assertThat(actualUserInfo)
                .isEqualTo(expectedUserInfo);
    }

    @Test
    @PactTestFor(pactMethod = "executeGetIdamAccessTokenAndGet200", providerType = ProviderType.ASYNCH)
    void verifyGetAccessTokenPact() {

        String returnedAccessToken = idamClient.getAccessToken("emCaseOfficer@email.net", "Password123");

        assertThat(returnedAccessToken).isNotNull();
        assertThat(returnedAccessToken).contains("Bearer");
    }


    private PactDslJsonBody createAuthResponse() {

        return new PactDslJsonBody()
                .stringMatcher("access_token", TOKEN_REGEXP,
                        "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FI.AL_JD-")
                .stringMatcher("refresh_token", TOKEN_REGEXP,
                        "eyJ0eXAiOiJKV1QiLCJ6aXAiO.iJOT05FIiwia2lkIjoi_i9PN-k92V")
                .stringType("scope", "openid roles profile search-user")
                .stringMatcher("id_token", TOKEN_REGEXP,
                        "eyJ0e.XAiOiJKV1QiLCJra-WQiOiJiL082_T3ZWdjEre")
                .stringType("token_type", "Bearer")
                .stringMatcher("expires_in", "[0-9]+", "28798");
    }

    private PactDslJsonBody createUserInfoResponse() {
        return new PactDslJsonBody()
                .stringType("sub", "damian@swansea.gov.uk")
                .stringType("uid", "33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
                .minArrayLike("roles", 1, PactDslJsonRootValue.stringType("caseworker-publiclaw-solicitor"), 1)
                .stringType("name", "John Smith")
                .stringType("given_name", "John")
                .stringType("family_name", "Smith");
    }
}

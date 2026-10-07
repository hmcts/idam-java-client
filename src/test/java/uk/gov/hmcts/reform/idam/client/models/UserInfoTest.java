package uk.gov.hmcts.reform.idam.client.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserInfoTest {

    private static final String USER_INFO_JSON = """
        {
          "sub": "hello-idam@reform.local",
          "uid": "hello-idam-01",
          "name": "Hello IDAM",
          "given_name": "Hello",
          "family_name": "IDAM",
          "roles": ["citizen"]
        }
        """;

    @Test
    @DisplayName("deserializes OIDC userinfo JSON with Jackson 3 defaults")
    void deserializesWithJackson3Defaults() {
        JsonMapper mapper = JsonMapper.builder().build();

        UserInfo userInfo = mapper.readValue(USER_INFO_JSON, UserInfo.class);

        assertUserInfo(userInfo);
    }

    @Test
    @DisplayName("deserializes OIDC userinfo JSON when consumer enables spring.jackson.use-jackson2-defaults")
    void deserializesWithJackson2Defaults() {
        // Boot 4 consumers often set spring.jackson.use-jackson2-defaults=true.
        // @Jacksonized + field @JsonProperty keeps OIDC snake_case deserialization working.
        JsonMapper mapper = JsonMapper.builder()
            .configureForJackson2()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, DateTimeFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
            .build();

        UserInfo userInfo = mapper.readValue(USER_INFO_JSON, UserInfo.class);

        assertUserInfo(userInfo);
    }

    private static void assertUserInfo(UserInfo userInfo) {
        assertThat(userInfo.getSub()).isEqualTo("hello-idam@reform.local");
        assertThat(userInfo.getUid()).isEqualTo("hello-idam-01");
        assertThat(userInfo.getName()).isEqualTo("Hello IDAM");
        assertThat(userInfo.getGivenName()).isEqualTo("Hello");
        assertThat(userInfo.getFamilyName()).isEqualTo("IDAM");
        assertThat(userInfo.getRoles()).isEqualTo(List.of("citizen"));
    }
}

package uk.gov.hmcts.reform.idam.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

/** 
 * Temporary OIDC API client for interacting with the OpenID Connect endpoints outside of the IDAM api service.
 * This client is used when the OIDC endpoints are hosted separately from the main IDAM API.
 * The correct approach for interacting with OIDC endpoints should be through standard OpenId/OAuth2 libraries rather than this temporary client.
 * OIDC endpoints should be replaced by standard OpenId/OAuth2 libraries, for example passport, express-openid-connect or Spring Security
 * See the README for more details.
 */
@FeignClient(name = "oidc-api", url = "${idam.oidc.url}", configuration = CoreFeignConfiguration.class)
public interface OidcApi {

    /**
     * OIDC user info endpoint.
     */
    @GetMapping("/o/userinfo")
    UserInfo retrieveUserInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    /**
     * OIDC token endpoint.
     */
    @PostMapping(
        value = "/o/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenResponse generateOpenIdToken(@RequestBody TokenRequest tokenRequest);

}

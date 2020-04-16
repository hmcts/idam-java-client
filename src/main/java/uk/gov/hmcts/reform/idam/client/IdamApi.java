package uk.gov.hmcts.reform.idam.client;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserRequest;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;
import uk.gov.hmcts.reform.idam.client.models.ExchangeCodeRequest;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinRequest;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinResponse;
import uk.gov.hmcts.reform.idam.client.models.TokenExchangeResponse;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@FeignClient(name = "idam-api", url = "${idam.api.url}", configuration = CoreFeignConfiguration.class)
public interface IdamApi {

    @GetMapping("/details")
    UserDetails retrieveUserDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @PostMapping("/pin")
    GeneratePinResponse generatePin(
        GeneratePinRequest requestBody,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @GetMapping(
        value = "/pin",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Response authenticatePinUser(
        @RequestHeader("pin") final String pin,
        @RequestParam("client_id") final String clientId,
        @RequestParam("redirect_uri") final String redirectUri,
        @RequestParam("state") final String state
    );

    @PostMapping(
        value = "/oauth2/authorize",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    AuthenticateUserResponse authenticateUser(
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorisation,
        @RequestBody AuthenticateUserRequest authenticateUserRequest
    );

    @PostMapping(
        value = "/oauth2/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenExchangeResponse exchangeCode(
        @RequestBody ExchangeCodeRequest exchangeCodeRequest
    );

    @GetMapping("/o/userinfo")
    UserInfo retrieveUserInfo(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @PostMapping(
        value = "/o/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenResponse generateOpenIdToken(@RequestBody TokenRequest tokenRequest);
}

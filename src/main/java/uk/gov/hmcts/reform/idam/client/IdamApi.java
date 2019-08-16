package uk.gov.hmcts.reform.idam.client;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserRequest;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;
import uk.gov.hmcts.reform.idam.client.models.ExchangeCodeRequest;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinRequest;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinResponse;
import uk.gov.hmcts.reform.idam.client.models.TokenExchangeResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@FeignClient(name = "idam-api", url = "${idam.api.url}", configuration = CoreFeignConfiguration.class)
public interface IdamApi {
    @RequestMapping(method = RequestMethod.GET, value = "/details")
    UserDetails retrieveUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);

    @RequestMapping(method = RequestMethod.POST, value = "/pin")
    GeneratePinResponse generatePin(
        GeneratePinRequest requestBody,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @RequestMapping(
        method = RequestMethod.GET,
        value = "/pin",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Response authenticatePinUser(
        @RequestHeader("pin") final String pin,
        @RequestParam("client_id") final String clientId,
        @RequestParam("redirect_uri") final String redirectUri,
        @RequestParam("state") final String state
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/oauth2/authorize",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    AuthenticateUserResponse authenticateUser(
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorisation,
        @RequestBody AuthenticateUserRequest authenticateUserRequest
    );


    @RequestMapping(
        method = RequestMethod.POST,
        value = "/oauth2/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenExchangeResponse exchangeCode(
        @RequestBody ExchangeCodeRequest exchangeCodeRequest
    );
}
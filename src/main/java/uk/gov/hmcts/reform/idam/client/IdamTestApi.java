package uk.gov.hmcts.reform.idam.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.idam.client.models.test.CreateUserRequest;

/**
 * Client for interacting with idam's user manipulation API.
 * This is only enabled in non production environments
 */
@FeignClient(
        name = "idam-test-api",
        url = "${idam.api.url}/testing-support",
        configuration = CoreFeignConfiguration.class
)
public interface IdamTestApi {
    @PostMapping(value = "/accounts", consumes = {MediaType.APPLICATION_JSON_VALUE})
    void createUser(CreateUserRequest createUserRequest);

    @GetMapping("/accounts/pin/{letterHolderId}")
    String getPinByLetterHolderId(@PathVariable("letterHolderId") String letterHolderId);
}

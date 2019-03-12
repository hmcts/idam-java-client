package uk.gov.hmcts.reform.idam.client.models.test;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreateUserRequest {

    public static final String DEFAULT_PASSWORD = "Password12";

    private final String email;
    private final String forename = "John";
    private final String surname = "Smith";
    private final Integer levelOfAccess = 0;
    private final UserGroup userGroup;
    private final List<UserRole> roles;
    private final String activationDate = "";
    private final String lastAccess = "";
    private final String password = DEFAULT_PASSWORD;
}

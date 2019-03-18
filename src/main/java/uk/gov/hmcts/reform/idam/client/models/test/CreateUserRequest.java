package uk.gov.hmcts.reform.idam.client.models.test;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreateUserRequest {

    public static final String DEFAULT_PASSWORD = "Password12";

    private String email;
    @Builder.Default
    private String forename = "John";
    @Builder.Default
    private String surname = "Smith";
    @Builder.Default
    private Integer levelOfAccess = 0;
    private UserGroup userGroup;
    private List<UserRole> roles;
    @Builder.Default
    private String activationDate = "";
    @Builder.Default
    private String lastAccess = "";
    @Builder.Default
    private String password = DEFAULT_PASSWORD;
}

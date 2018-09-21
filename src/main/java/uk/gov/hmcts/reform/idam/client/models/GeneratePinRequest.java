package uk.gov.hmcts.reform.idam.client.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
public class GeneratePinRequest {

    private final String firstName;
    @JsonInclude
    private final String lastName;
    private final List<String> roles;

    public GeneratePinRequest(String name) {
        this.firstName = name;
        this.lastName = "";
        this.roles = null;
    }
}


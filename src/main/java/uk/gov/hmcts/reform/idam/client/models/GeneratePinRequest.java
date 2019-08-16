package uk.gov.hmcts.reform.idam.client.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class GeneratePinRequest {
    private final String firstName;
    @JsonInclude
    private final String lastName;

    private List<String> roles = new ArrayList<>();

    public GeneratePinRequest(String name) {
        this.firstName = name;
        this.lastName = "";
        this.roles = null;
    }
}


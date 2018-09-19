package uk.gov.hmcts.reform.idam.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneratePinResponse {

    private final String pin;
    private final String userId;

    public GeneratePinResponse(String pin, String userId) {
        this.pin = pin;
        this.userId = userId;
    }

}

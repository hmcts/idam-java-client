package uk.gov.hmcts.reform.idam.client.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExchangeCodeRequest {
    private String code;
    private String grant_type;
    private String redirect_uri;
    private String client_id;
    private String client_secret;
}

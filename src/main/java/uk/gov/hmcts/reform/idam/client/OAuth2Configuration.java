package uk.gov.hmcts.reform.idam.client;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OAuth2Configuration {

    private String clientId;
    private String redirectUri;
    private String clientSecret;

    @Autowired
    public OAuth2Configuration(
            @Value("${idam.client.redirect_uri:}") String redirectUri,
            @Value("${idam.client.id:}") String clientId,
            @Value("${idam.client.secret:}") String clientSecret
    ) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.clientSecret = clientSecret;
    }
}

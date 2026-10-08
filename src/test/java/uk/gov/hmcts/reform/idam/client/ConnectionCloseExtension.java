package uk.gov.hmcts.reform.idam.client;

import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

//https://github.com/tomakehurst/wiremock/issues/485#issuecomment-382221826
public class ConnectionCloseExtension implements ResponseTransformerV2 {
    
    @Override
    public Response transform(Response response, ServeEvent serveEvent) {
        return Response.Builder
            .like(response)
            .headers(HttpHeaders.copyOf(response.getHeaders())
                .plus(new HttpHeader("Connection", "Close")))
            .build();
    }

    @Override
    public String getName() {
        return "ConnectionCloseExtension";
    }
}

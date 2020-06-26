package uk.gov.hmcts.reform.idam.client;

import feign.Client;
import feign.Logger;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

public class CoreFeignConfiguration {
    private int REQUEST_TIMEOUT = 10000;

    @Value("${idam.api.loglevel:NONE}")
    private Logger.Level logLevel;

    @Bean
    Logger.Level feignLoggerLevel() {
        return logLevel;
    }

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    @Primary
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Encoder feignFormEncoder() {
        return new FormEncoder(new SpringEncoder(this.messageConverters));
    }

    @Bean
    public Client getFeignHttpClient() {
        return new ApacheHttpClient(getHttpClient());
    }

    private CloseableHttpClient getHttpClient() {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(REQUEST_TIMEOUT)
            .setConnectionRequestTimeout(REQUEST_TIMEOUT)
            .setSocketTimeout(REQUEST_TIMEOUT)
            .build();

        return HttpClientBuilder
            .create()
            .useSystemProperties()
            .disableRedirectHandling()
            .setDefaultRequestConfig(config)
            .build();
    }
}

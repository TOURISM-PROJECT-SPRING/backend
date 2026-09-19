package com.example.spring_boot_project_api.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;

@Configuration
@Getter
public class BakongConfig {

    @Value("${bakong.api.url:https://api-bakong.nbc.gov.kh/v1}")
    private String apiUrl;

    @Value("${BAKONG_API_TOKEN:}")
    private String apiToken;

    @Value("${bakong.merchant.name:SovannDomNour}")
    private String merchantName;

    @Value("${bakong.merchant.account:sovanndomnour@aclb}")
    private String merchantAccount;

    @Value("${bakong.merchant.city:Siem Reap}")
    private String merchantCity;

    @Value("${bakong.qr.expiry-minutes:5}")
    private int qrExpiryMinutes;

    @Bean(name = "bakongRestTemplate")
    public RestTemplate bakongRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(15));
        return new RestTemplate(factory);
    }
}

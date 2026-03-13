package com.ketrams.v2.config;

import com.ketrams.v2.service.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

@Configuration
public class SmsServiceConfig {

    @Bean
    @Primary
    public SmsService smsService(
            @Value("${sms.provider:mock}") String provider,
            Map<String, SmsService> serviceMap) {

        System.out.println("📋 Available SMS service beans: " + serviceMap.keySet());

        String beanName = provider + "SmsService";
        SmsService service = serviceMap.get(beanName);
        if (service == null) {
            throw new RuntimeException("❌ No SMS service found for provider: " + provider + " (looked for bean: " + beanName + ")");
        }
        return service;
    }
}
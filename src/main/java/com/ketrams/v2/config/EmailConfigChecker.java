package com.ketrams.v2.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailConfigChecker {
    @Value("${spring.mail.username}")
    private String emailUsername;

    @PostConstruct
    public void check() {
        System.out.println(">>>> EMAIL_USERNAME resolved to: '" + emailUsername + "'");
    }
}
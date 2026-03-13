package com.ketrams.v2.service.impl;

import com.ketrams.v2.service.SmsService;
import org.springframework.stereotype.Service;

@Service("mockSmsService")
public class MockSmsService implements SmsService {

    @Override
    public void sendSms(String phoneNumber, String message) {
        String formattedNumber = formatPhoneNumber(phoneNumber);
        System.out.println("========== MOCK SMS ==========");
        System.out.println("To: " + formattedNumber);
        System.out.println("Message: " + message);
        System.out.println("==============================");
    }

    private String formatPhoneNumber(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("0")) {
            return "254" + cleaned.substring(1);
        }
        if (cleaned.startsWith("7")) {
            return "254" + cleaned;
        }
        return cleaned;
    }
}
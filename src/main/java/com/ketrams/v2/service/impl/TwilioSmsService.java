package com.ketrams.v2.service.impl;

import com.ketrams.v2.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("twilioSmsService")
public class TwilioSmsService implements SmsService {

    @Value("${sms.twilio.account-sid}")
    private String accountSid;

    @Value("${sms.twilio.auth-token}")
    private String authToken;

    @Value("${sms.twilio.phone-number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        System.out.println("✅ Twilio SDK initialized");
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        String formattedNumber = formatPhoneNumber(phoneNumber);

        try {
            Message twilioMessage = Message.creator(
                    new PhoneNumber(formattedNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    message
            ).create();

            System.out.println("✅ SMS sent via Twilio, SID: " + twilioMessage.getSid());
        } catch (Exception e) {
            System.err.println("❌ Twilio error: " + e.getMessage());
            fallbackMock(phoneNumber, message);
        }
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

    private void fallbackMock(String phoneNumber, String message) {
        System.out.println("========== FALLBACK MOCK SMS ==========");
        System.out.println("To: " + phoneNumber);
        System.out.println("Message: " + message);
        System.out.println("=======================================");
    }
}
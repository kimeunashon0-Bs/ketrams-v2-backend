package com.ketrams.v2.service;

public interface SmsService {
    /**
     * Sends an SMS message.
     * @param phoneNumber The recipient's phone number (any local format).
     * @param message The message content.
     */
    void sendSms(String phoneNumber, String message);
}
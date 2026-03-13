package com.ketrams.v2.service.auth;

import com.ketrams.v2.entity.OtpRequest;
import com.ketrams.v2.repository.OtpRequestRepository;
import com.ketrams.v2.service.EmailService;
import com.ketrams.v2.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    @Autowired
    private OtpRequestRepository otpRepository;

    @Autowired(required = false)
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;

    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public OtpRequest createOtpRequest(String phoneNumber) {
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setPhoneNumber(phoneNumber);
        otpRequest.setOtpCode(generateOtp());
        otpRequest.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otpRequest.setVerified(false);
        return otpRepository.save(otpRequest);
    }

    public boolean verifyOtp(String phoneNumber, String otpCode) {
        Optional<OtpRequest> latest = otpRepository
                .findTopByPhoneNumberAndVerifiedFalseOrderByCreatedAtDesc(phoneNumber);
        if (latest.isPresent()) {
            OtpRequest otpRequest = latest.get();
            if (otpRequest.getOtpCode().equals(otpCode)
                    && otpRequest.getExpiryTime().isAfter(LocalDateTime.now())) {
                otpRequest.setVerified(true);
                otpRepository.save(otpRequest);
                return true;
            }
        }
        return false;
    }

    public void sendOtp(String phoneNumber, String email, String deliveryMethod, String otpCode) {
        String message = "Your KETRAMS OTP is: " + otpCode + ". It expires in 5 minutes.";

        if ("EMAIL".equals(deliveryMethod) && email != null && !email.isEmpty()) {
            if (emailService != null) {
                emailService.sendOtpEmail(email, otpCode);
            } else {
                System.out.println("Email service not configured – falling back to SMS.");
                smsService.sendSms(phoneNumber, message);
            }
        } else {
            smsService.sendSms(phoneNumber, message);
        }
    }
}
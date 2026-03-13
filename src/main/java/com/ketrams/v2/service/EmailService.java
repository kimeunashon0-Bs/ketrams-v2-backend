package com.ketrams.v2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String to, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Your KETRAMS Verification Code");
        message.setText("Your OTP is: " + otpCode + "\n\nThis code will expire in 5 minutes.");
        mailSender.send(message);
        System.out.println("OTP email sent to " + to);
    }

    public void sendCredentialsEmail(String to, String phone, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("KETRAMS Institution Account Created");
        message.setText("Your institution account has been approved.\n\n" +
                "Login credentials:\n" +
                "Phone: " + phone + "\n" +
                "Temporary Password: " + password + "\n\n" +
                "Please login and change your password.");
        mailSender.send(message);
        System.out.println("Credentials email sent to " + to);
    }
}
package com.ketrams.v2.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String to, String otpCode) {
        Email from = new Email(fromEmail);
        Email toEmail = new Email(to);
        String subject = "Your KETRAMS Verification Code";
        String body = "Your OTP is: " + otpCode + "\n\nThis code will expire in 5 minutes.";
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("OTP email sent to " + to);
            } else {
                System.err.println("SendGrid error: " + response.getStatusCode() + " " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("SendGrid exception: " + ex.getMessage());
        }
    }

    public void sendCredentialsEmail(String to, String phone, String password) {
        Email from = new Email(fromEmail);
        Email toEmail = new Email(to);
        String subject = "KETRAMS Institution Account Created";
        String body = "Your institution account has been approved.\n\n" +
                "Login credentials:\n" +
                "Phone: " + phone + "\n" +
                "Temporary Password: " + password + "\n\n" +
                "Please login and change your password.";
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Credentials email sent to " + to);
            } else {
                System.err.println("SendGrid error: " + response.getStatusCode() + " " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("SendGrid exception: " + ex.getMessage());
        }
    }
}
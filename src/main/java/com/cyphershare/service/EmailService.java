package com.cyphershare.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSessionCodeEmail(String toEmail, String sessionCode, String senderEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("CypherShare: You received a secure file transfer");
            
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                + "<h2 style='color: #0f766e;'>CypherShare Secure Transfer</h2>"
                + "<p><strong>" + senderEmail + "</strong> has sent you files securely.</p>"
                + "<div style='background-color: #f1f5f9; padding: 15px; border-radius: 8px; margin: 20px 0;'>"
                + "<p style='margin: 0;'>Your unique session code is:</p>"
                + "<h1 style='color: #0f766e; letter-spacing: 5px; margin: 10px 0;'>" + sessionCode + "</h1>"
                + "</div>"
                + "<p>To receive the files, visit <a href='https://cyphershare.com' style='color: #0ea5e9;'>cyphershare.com</a> and enter the code.</p>"
                + "</div>";
                
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

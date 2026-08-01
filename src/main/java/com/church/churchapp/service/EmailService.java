package com.church.churchapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * Sends an email asynchronously.
     * If JavaMailSender is not configured or configured incorrectly, falls back to logging to console.
     */
    public CompletableFuture<Boolean> sendEmail(String to, String subject, String body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (mailSender == null || fromEmail == null || fromEmail.trim().isEmpty()) {
                    logger.info("⚠️ [SIMULATION MODE] Email not configured. Logging content:\nTo: {}\nSubject: {}\nBody: {}", to, subject, body);
                    // Simulate successful delivery in local dev environments
                    return true;
                }

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);

                mailSender.send(message);
                logger.info("✅ Email successfully sent to {}", to);
                return true;
            } catch (Exception e) {
                logger.error("❌ Failed to send email to {}: {}", to, e.getMessage());
                // Also log simulated output for debugging
                logger.info("⚠️ [SIMULATION FALLBACK] Logging email content:\nTo: {}\nSubject: {}\nBody: {}", to, subject, body);
                return false;
            }
        });
    }
}

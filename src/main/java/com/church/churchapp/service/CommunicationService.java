package com.church.churchapp.service;

import com.church.churchapp.entity.CommunicationLog;
import com.church.churchapp.entity.Member;
import com.church.churchapp.repository.CommunicationLogRepository;
import com.church.churchapp.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class CommunicationService {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationService.class);

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private CommunicationLogRepository logRepository;

    public List<CommunicationLog> getAllLogs() {
        return logRepository.findAllByOrderBySentDateDesc();
    }

    /**
     * Sends a broadcast message to multiple members across multiple channels.
     */
    public void sendBroadcast(List<Long> memberIds, List<String> channels, String subject, String message) {
        List<Member> members = memberRepository.findAllById(memberIds);
        
        for (Member member : members) {
            String fullName = (member.getFirstName() + " " + (member.getLastName() != null ? member.getLastName() : "")).trim();
            
            for (String channel : channels) {
                final String currentChannel = channel.toUpperCase();
                
                if ("EMAIL".equals(currentChannel)) {
                    String email = member.getEmail();
                    if (email != null && !email.trim().isEmpty()) {
                        emailService.sendEmail(email, subject, message)
                            .thenAccept(success -> logOutcome("EMAIL", email, subject, message, success));
                    } else {
                        logOutcome("EMAIL", fullName, subject, message + " (Skipped: No email)", false);
                    }
                } 
                else if ("WHATSAPP".equals(currentChannel)) {
                    String phone = twilioService.getMemberPhoneNumber(member);
                    if (phone != null && !phone.trim().isEmpty()) {
                        twilioService.sendCustomWhatsApp(phone, message)
                            .thenAccept(success -> logOutcome("WHATSAPP", phone, subject, message, success));
                    } else {
                        logOutcome("WHATSAPP", fullName, subject, message + " (Skipped: No phone)", false);
                    }
                } 
                else if ("SMS".equals(currentChannel)) {
                    String phone = twilioService.getMemberPhoneNumber(member);
                    if (phone != null && !phone.trim().isEmpty()) {
                        twilioService.sendSMS(phone, message)
                            .thenAccept(success -> logOutcome("SMS", phone, subject, message, success));
                    } else {
                        logOutcome("SMS", fullName, subject, message + " (Skipped: No phone)", false);
                    }
                } 
                else if ("PUSH".equals(currentChannel)) {
                    // Push notifications logged as In-App alerts for the member
                    logOutcome("PUSH", fullName, subject, message, true);
                }
            }
        }
    }

    private void logOutcome(String channel, String recipient, String subject, String message, boolean success) {
        try {
            CommunicationLog commLog = new CommunicationLog();
            commLog.setChannel(channel);
            commLog.setRecipient(recipient);
            commLog.setSubject(subject);
            commLog.setMessage(message);
            commLog.setStatus(success ? "SENT" : "FAILED");
            commLog.setSentDate(LocalDateTime.now());
            logRepository.save(commLog);
            logger.info("Logged communication broadcast: Channel={}, Recipient={}, Status={}", channel, recipient, commLog.getStatus());
        } catch (Exception e) {
            logger.error("Failed to write communication log to database: {}", e.getMessage());
        }
    }
}

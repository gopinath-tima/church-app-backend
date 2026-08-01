package com.church.churchapp.service;

import com.church.churchapp.entity.Member;
import com.church.churchapp.entity.Transaction;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TwilioService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioService.class);

    @Value("${twilio.enabled:true}")
    private boolean enabled;

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-whatsapp-number:whatsapp:+14155238886}")
    private String fromWhatsappNumber;

    @Value("${twilio.from-sms-number:+14155238886}")
    private String fromSmsNumber;

    @Value("${twilio.content-sid:HXb5b62575e6e4ff6129ad7c8efe1f983e}")
    private String contentSid;

    @PostConstruct
    public void init() {
        if (enabled) {
            try {
                Twilio.init(accountSid, authToken);
                logger.info("Twilio successfully initialized with Account SID: {}", accountSid);
            } catch (Exception e) {
                logger.error("Failed to initialize Twilio SDK: {}", e.getMessage());
            }
        } else {
            logger.info("Twilio integration is disabled in configuration.");
        }
    }

    /**
     * Sends a subscription payment WhatsApp notification asynchronously.
     */
    public CompletableFuture<Void> sendSubscriptionNotification(Member member, Transaction transaction) {
        if (!enabled) {
            logger.info("Twilio is disabled. Skipping WhatsApp notification.");
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                String rawPhone = getMemberPhoneNumber(member);
                if (rawPhone == null || rawPhone.trim().isEmpty()) {
                    logger.warn("Member ID {} has no contact or WhatsApp number. Cannot send notification.", member.getMemberId());
                    return;
                }

                String formattedToNumber = formatToWhatsAppNumber(rawPhone);
                logger.info("Sending WhatsApp notification to {} for subscription payment transaction: {}", formattedToNumber, transaction.getTransactionId());

                // Prepare Content Variables JSON
                // Placeholders: "1" = month/year, "2" = amount (e.g. Rs. 500)
                String monthYear = (transaction.getForMonth() != null ? transaction.getForMonth() : "") 
                        + "/" + (transaction.getForYear() != null ? transaction.getForYear() : "");
                String amountStr = transaction.getAmount() != null ? String.format("Rs. %.2f", transaction.getAmount()) : "Rs. 0.00";
                
                String contentVariables = String.format("{\"1\":\"%s\",\"2\":\"%s\"}", monthYear, amountStr);
                logger.debug("Content variables for Twilio template: {}", contentVariables);

                Message message = Message.creator(
                        new PhoneNumber(formattedToNumber),
                        new PhoneNumber(fromWhatsappNumber),
                        "Your subscription payment of " + amountStr + " for " + monthYear + " has been processed."
                )
                .setContentSid(contentSid)
                .setContentVariables(contentVariables)
                .create();

                logger.info("WhatsApp notification successfully sent! Twilio Message SID: {}", message.getSid());
            } catch (Exception e) {
                logger.error("Failed to send Twilio WhatsApp notification: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * Sends a custom text WhatsApp message asynchronously.
     */
    public CompletableFuture<Boolean> sendCustomWhatsApp(String rawPhone, String bodyText) {
        if (!enabled) {
            logger.info("Twilio is disabled. Skipping custom WhatsApp message.");
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String formattedToNumber = formatToWhatsAppNumber(rawPhone);
                logger.info("Sending custom WhatsApp message to {}", formattedToNumber);

                Message message = Message.creator(
                        new PhoneNumber(formattedToNumber),
                        new PhoneNumber(fromWhatsappNumber),
                        bodyText
                ).create();

                logger.info("Custom WhatsApp message sent! Message SID: {}", message.getSid());
                return message.getSid() != null;
            } catch (Exception e) {
                logger.error("Failed to send custom WhatsApp message to {}: {}", rawPhone, e.getMessage(), e);
                return false;
            }
        });
    }

    /**
     * Sends a template-based WhatsApp message asynchronously.
     */
    public CompletableFuture<Boolean> sendTemplateWhatsApp(String rawPhone, String templateSid, String jsonVariables) {
        if (!enabled) {
            logger.info("Twilio is disabled. Skipping template WhatsApp message.");
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String formattedToNumber = formatToWhatsAppNumber(rawPhone);
                logger.info("Sending template WhatsApp message ({}) to {}", templateSid, formattedToNumber);

                Message message = Message.creator(
                        new PhoneNumber(formattedToNumber),
                        new PhoneNumber(fromWhatsappNumber),
                        ""
                )
                .setContentSid(templateSid)
                .setContentVariables(jsonVariables)
                .create();

                logger.info("Template WhatsApp message sent! Message SID: {}", message.getSid());
                return message.getSid() != null;
            } catch (Exception e) {
                logger.error("Failed to send template WhatsApp message to {}: {}", rawPhone, e.getMessage(), e);
                return false;
            }
        });
    }

    /**
     * Sends a custom SMS asynchronously.
     */
    public CompletableFuture<Boolean> sendSMS(String rawPhone, String bodyText) {
        if (!enabled) {
            logger.info("Twilio is disabled. Skipping custom SMS message.");
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // For standard SMS, ensure the phone number has a + and country code
                String formattedToNumber = rawPhone.replaceAll("[^0-9+]", "");
                if (!formattedToNumber.startsWith("+")) {
                    if (formattedToNumber.length() == 10) {
                        formattedToNumber = "+91" + formattedToNumber; // India fallback
                    } else {
                        formattedToNumber = "+" + formattedToNumber;
                    }
                }
                
                // For SMS sender, strip "whatsapp:" if present in fromSmsNumber
                String sender = fromSmsNumber.replace("whatsapp:", "");

                logger.info("Sending custom SMS to {}", formattedToNumber);

                Message message = Message.creator(
                        new PhoneNumber(formattedToNumber),
                        new PhoneNumber(sender),
                        bodyText
                ).create();

                logger.info("Custom SMS sent! Message SID: {}", message.getSid());
                return message.getSid() != null;
            } catch (Exception e) {
                logger.error("Failed to send custom SMS to {}: {}", rawPhone, e.getMessage(), e);
                return false;
            }
        });
    }

    public String getMemberPhoneNumber(Member member) {
        if (member.getWhatsappNumber() != null && !member.getWhatsappNumber().trim().isEmpty()) {
            return member.getWhatsappNumber().trim();
        }
        if (member.getContactNumber() != null && !member.getContactNumber().trim().isEmpty()) {
            return member.getContactNumber().trim();
        }
        return null;
    }

    /**
     * Formats a raw phone number into a standard Twilio WhatsApp recipient URI (whatsapp:+E164)
     */
    public String formatToWhatsAppNumber(String rawPhone) {
        // Remove spaces, parentheses, dashes, and other non-numeric/non-plus characters
        String digits = rawPhone.replaceAll("[^0-9+]", "");

        if (digits.startsWith("whatsapp:")) {
            return digits;
        }

        if (digits.startsWith("+")) {
            return "whatsapp:" + digits;
        }

        // If it's a 10-digit mobile number, default to prepending +91 (India)
        if (digits.length() == 10) {
            return "whatsapp:+91" + digits;
        }

        // Fallback: prepend + if not already present
        return "whatsapp:+" + digits;
    }
}

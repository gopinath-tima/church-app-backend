package com.church.churchapp.controller;

import com.church.churchapp.entity.Member;
import com.church.churchapp.repository.MemberRepository;
import com.church.churchapp.service.TwilioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/twilio")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPER_PLUS_ADMIN')")
public class TwilioController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TwilioService twilioService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Broadcast a custom text WhatsApp message to all members.
     * POST /api/twilio/broadcast-custom
     * Request body: { "message": "Important Announcement..." }
     */
    @PostMapping("/broadcast-custom")
    public ResponseEntity<?> broadcastCustomMessage(@RequestBody Map<String, String> payload) {
        String messageText = payload.get("message");
        if (messageText == null || messageText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message text is required"));
        }

        List<Member> members = memberRepository.findAll();
        int total = members.size();
        int processed = 0;
        int skipped = 0;

        for (Member member : members) {
            String rawPhone = twilioService.getMemberPhoneNumber(member);
            if (rawPhone == null || rawPhone.trim().isEmpty()) {
                skipped++;
                continue;
            }

            twilioService.sendCustomWhatsApp(rawPhone, messageText);
            processed++;
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("status", "Broadcast initiated");
        summary.put("totalMembers", total);
        summary.put("sentCount", processed);
        summary.put("skippedCount", skipped);

        return ResponseEntity.ok(summary);
    }

    /**
     * Broadcast a pre-approved Twilio Content Template (WhatsApp Template) to all members.
     * POST /api/twilio/broadcast-template
     * Request body:
     * {
     *   "contentSid": "HXb5b62575e6e4ff6129ad7c8efe1f983e",
     *   "contentVariables": {
     *      "1": "parameter1",
     *      "2": "parameter2"
     *   }
     * }
     */
    @PostMapping("/broadcast-template")
    public ResponseEntity<?> broadcastTemplateMessage(@RequestBody Map<String, Object> payload) {
        String contentSid = (String) payload.get("contentSid");
        if (contentSid == null || contentSid.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "contentSid is required"));
        }

        Object rawVariables = payload.get("contentVariables");
        String jsonVariables = "{}";
        if (rawVariables != null) {
            try {
                jsonVariables = objectMapper.writeValueAsString(rawVariables);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to parse contentVariables: " + e.getMessage()));
            }
        }

        List<Member> members = memberRepository.findAll();
        int total = members.size();
        int processed = 0;
        int skipped = 0;

        for (Member member : members) {
            String rawPhone = twilioService.getMemberPhoneNumber(member);
            if (rawPhone == null || rawPhone.trim().isEmpty()) {
                skipped++;
                continue;
            }

            twilioService.sendTemplateWhatsApp(rawPhone, contentSid, jsonVariables);
            processed++;
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("status", "Template broadcast initiated");
        summary.put("totalMembers", total);
        summary.put("sentCount", processed);
        summary.put("skippedCount", skipped);

        return ResponseEntity.ok(summary);
    }
}

package com.church.churchapp.controller;

import com.church.churchapp.entity.CommunicationLog;
import com.church.churchapp.service.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/communication")
@PreAuthorize("hasAnyRole('MINISTRY', 'ADMIN', 'SUPER_ADMIN', 'SUPER_PLUS_ADMIN')")
public class CommunicationController {

    @Autowired
    private CommunicationService communicationService;

    @GetMapping("/logs")
    public ResponseEntity<List<CommunicationLog>> getLogs() {
        return ResponseEntity.ok(communicationService.getAllLogs());
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendBroadcast(@RequestBody Map<String, Object> payload) {
        try {
            @SuppressWarnings("unchecked")
            List<Number> memberIdNumbers = (List<Number>) payload.get("memberIds");
            @SuppressWarnings("unchecked")
            List<String> channels = (List<String>) payload.get("channels");
            String subject = (String) payload.get("subject");
            String message = (String) payload.get("message");

            if (memberIdNumbers == null || memberIdNumbers.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "recipient list memberIds is required"));
            }
            if (channels == null || channels.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "channels list is required"));
            }
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "message content is required"));
            }

            // Convert List<Number> to List<Long>
            List<Long> memberIds = new ArrayList<>();
            for (Number num : memberIdNumbers) {
                memberIds.add(num.longValue());
            }

            // Trigger broadcast sending
            communicationService.sendBroadcast(memberIds, channels, subject, message);

            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Broadcast triggered successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to dispatch broadcast: " + e.getMessage()));
        }
    }
}

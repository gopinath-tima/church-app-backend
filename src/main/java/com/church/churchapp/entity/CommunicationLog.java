package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "communication_logs")
@Getter
@Setter
public class CommunicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String channel; // "SMS", "EMAIL", "WHATSAPP", "PUSH"

    private String recipient; // Phone, Email, or Member ID/Name

    private String subject; // Optional, mainly for Email/Push

    @Column(length = 2000)
    private String message;

    private String status; // "SENT", "FAILED"

    @Column(name = "sent_date")
    private LocalDateTime sentDate;
}

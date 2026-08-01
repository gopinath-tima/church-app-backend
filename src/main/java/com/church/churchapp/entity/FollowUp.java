package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "visitor_follow_ups")
@Getter
@Setter
public class FollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "visitor_id", nullable = false)
    private Visitor visitor;

    @Column(name = "follow_up_date")
    private String followUpDate;

    @Column(name = "follow_up_type")
    private String followUpType; // Phone Call, Email, Home Visit, In-Person Chat

    private String status = "Pending"; // Pending, Completed

    @Column(length = 2000)
    private String notes;

    private String assignee;
}

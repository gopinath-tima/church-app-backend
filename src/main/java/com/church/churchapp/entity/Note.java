package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Data
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noteId;

    @Column(columnDefinition = "TEXT")
    private String noteText;

    private LocalDateTime createdAt;

    // Many-to-1 relationship back to the Member
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
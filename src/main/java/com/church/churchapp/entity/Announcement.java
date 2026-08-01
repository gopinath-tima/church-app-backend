package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
@Getter
@Setter
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String content;

    @Column(name = "target_audience")
    private String targetAudience; // e.g. "ALL", "MEMBERS", or group/ministry IDs

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "author_name")
    private String authorName;
}

package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.Filter;
import com.church.churchapp.config.TenantEntityListener;

@Entity
@Data
@Table(name = "events")
@Filter(name = "tenantFilter", condition = "branch_id = :branchId")
@EntityListeners(TenantEntityListener.class)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // We use String to easily capture HTML5 datetime-local inputs (YYYY-MM-DDTHH:mm)
    private String startDate;
    private String endDate;

    private String location;
    private String organizer;
    private String targetAudience; // e.g., "Youth", "All", "Choir"

    @Column(name = "teacher_id")
    private Long teacherId;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;
}
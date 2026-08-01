package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.Filter;
import com.church.churchapp.config.TenantEntityListener;

@Entity
@Data
@Table(name = "church_groups")
@Filter(name = "tenantFilter", condition = "branch_id = :branchId")
@EntityListeners(TenantEntityListener.class)
public class ChurchGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category; // e.g. "Cell Group", "Ministry Team", "Bible Study", "Fellowship"
    private String meetingTime; // e.g. "Saturdays 5 PM"
    private String location; // e.g. "Church Hall", "John's House"
    private String status; // "Active", "Inactive"

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;
}

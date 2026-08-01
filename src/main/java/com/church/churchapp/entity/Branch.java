package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "branches")
@Getter
@Setter
public class Branch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(unique = true)
    private String branchCode;
    
    private String churchName;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String contactNumber;
    private String email;
    private String headPastorName;
    
    private java.time.LocalDate establishedDate;
    
    @Column(nullable = false)
    private String status = "Active"; // Active, Inactive
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.Filter;
import com.church.churchapp.config.TenantEntityListener;

@Entity
@Data
@Table(name = "transactions")
@Filter(name = "tenantFilter", condition = "branch_id = :branchId")
@EntityListeners(TenantEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String transactionId; // e.g., TRX-101

    private String date; // YYYY-MM-DD
    
    @Column(columnDefinition = "TEXT")
    private String description;

    private String type; // Subscription, Offering, Donation, Sponsorship, Property Rent, Expense
    private String method; // Cash, Bank Transfer, Card, Cheque
    
    private Double amount;
    
    private String status = "Completed"; // Pending, Completed, Failed
    private String memberName; // e.g. Thangamuthu, Guest
    private String notes; // Extra references or remarks

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    // --- Subscription Specific Fields ---
    private Long memberId;
    private Integer forMonth;
    private Integer forYear;
    private String subscriptionFrequency; // Weekly, Monthly, Yearly
    private Integer forWeek;

    // --- Offering Specific Fields ---
    private String eventName;

    // --- Donation Specific Fields ---
    private String donorName;

    // --- Property Rent Specific Fields ---
    private String tenantPurpose;
    private Long leaseId;

}

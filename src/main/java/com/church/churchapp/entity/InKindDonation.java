package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "in_kind_donations")
public class InKindDonation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;
    private String donorName;
    private String itemName;
    
    private Integer quantity;
    private String unit;
    
    private Double estimatedValue; // Optional in frontend
    
    private String date; // YYYY-MM-DD
    private String purpose;
    
    @Column(columnDefinition = "TEXT")
    private String notes;

}

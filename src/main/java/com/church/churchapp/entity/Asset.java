package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.Filter;
import com.church.churchapp.config.TenantEntityListener;

@Entity
@Table(name = "assets")
@Data
@Filter(name = "tenantFilter", condition = "branch_id = :branchId")
@EntityListeners(TenantEntityListener.class)
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category; // Chairs, Speakers, Musical instruments, Cameras, Fans, Lights, Vehicles

    private String purchaseDate;
    private Double purchasePrice;
    
    private String warrantyExpirationDate;
    
    private String nextMaintenanceDate;
    private Integer maintenanceFrequencyMonths;

    private Double depreciationRate; // e.g., 10 for 10% per year
    private Double currentValue;

    private String status = "Active"; // Active, In Maintenance, Retired
    
    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;
}

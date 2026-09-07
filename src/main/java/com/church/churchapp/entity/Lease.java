package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Filter;
import com.church.churchapp.config.TenantEntityListener;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "leases")
@Filter(name = "tenantFilter", condition = "branch_id = :branchId")
@EntityListeners(TenantEntityListener.class)
public class Lease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenantName;
    private String propertyName;
    
    private LocalDate startDate;
    private Integer durationMonths;
    private Double monthlyRent;
    
    private String status = "Active"; // Active, Expired, Terminated

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;
}

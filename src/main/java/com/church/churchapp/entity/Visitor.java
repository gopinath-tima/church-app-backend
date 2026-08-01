package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.Filter;
import com.church.churchapp.config.TenantEntityListener;

@Entity
@Table(name = "visitors")
@Filter(name = "tenantFilter", condition = "branch_id = :branchId")
@EntityListeners(TenantEntityListener.class)
@Getter
@Setter
public class Visitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "visit_date")
    private String visitDate;

    private String status = "Registered"; // Registered, In Progress, Converted, Archived

    private String gender;
    private String address;

    @Column(name = "first_visit_notes", length = 2000)
    private String firstVisitNotes;

    @Column(name = "converted_member_id")
    private Long convertedMemberId;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;
}

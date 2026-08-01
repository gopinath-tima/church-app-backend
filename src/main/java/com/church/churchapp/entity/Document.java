package com.church.churchapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.Filter;
import com.church.churchapp.config.TenantEntityListener;

@Entity
@Data
@Table(name = "documents")
@Filter(name = "tenantFilter", condition = "branch_id = :branchId")
@EntityListeners(TenantEntityListener.class)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private Long fileSize; // Size in bytes
    private String category; // Member Certificate, Baptism Certificate, Marriage Certificate, Financial Document, Vendor Bill
    private String uploadDate; // YYYY-MM-DD

    private Long memberId; // Optional link to a member
    private String memberName; // Cached name for quick search fallback

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JsonIgnore
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file_data", columnDefinition = "bytea")
    private byte[] fileData;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;
}

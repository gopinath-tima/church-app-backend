package com.church.churchapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Filter;
import com.church.churchapp.config.TenantEntityListener;

@Entity
@Table(name = "families")
@Data
@Filter(name = "tenantFilter", condition = "branch_id = :branchId")
@EntityListeners(TenantEntityListener.class)
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long familyId;

    private String familyName;
    private String address;

    // ✅ FIX: Stops the infinite loop crash!
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "head_member_id")
    private Member headMember;

    // ✅ FIX: Stops the infinite loop crash and prevents cascading deletions!
    @JsonIgnore
    @OneToMany(mappedBy = "family", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<Member> members = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;
}
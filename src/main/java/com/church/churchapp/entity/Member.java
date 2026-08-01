package com.church.churchapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import com.church.churchapp.config.TenantEntityListener;

@Entity
@Table(name = "members")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "branchId", type = Long.class))
@Filter(name = "tenantFilter", condition = "branch_id = :branchId")
@EntityListeners(TenantEntityListener.class)
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(name = "custom_member_id", unique = true)
    private String customMemberId;

    // --- Basic Info ---
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;

    // ✅ UNIQUE EMAIL
    @Column(nullable = false, unique = true)
    private String email;

    // ✅ UNIQUE MOBILE NUMBER
    @Column(name = "phone_number", nullable = false, unique = true)
    private String contactNumber;

    private String alternateContact;
    private String whatsappNumber;
    private String address;

    // ✅ FIX: Ignore sending image binary data as text to prevent crashes
    @JsonIgnore
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "profile_photo", columnDefinition = "bytea")
    private byte[] profilePhoto;

    @Column(name = "photo_content_type")
    private String photoContentType;

    // --- Emergency Contact ---
    private String emergencyContactName;
    private String emergencyContactNumber;

    // --- Personal & Professional ---
    private String bloodGroup;
    private String education;
    private String occupation;
    private String languagesKnown;
    private String maritalStatus;
    private String anniversaryDate;
    private Long spouseMemberId;
    private String spouseName;
    private String numberOfChildren;

    // --- Spiritual Info ---
    private String joinDate;
    private String membershipStatus;
    private String baptismStatus;
    private String baptismDate;
    private String confirmationStatus;
    private String communionStatus;
    private String previousChurch;

    // --- Ministry & Skills ---
    private String ministryRole;
    private String volunteerStatus;
    private String skills;

    // --- Contributions ---
    private String titheMember;
    private String preferredGivingMethod;
    private String specialRemarks;

    @Column(name = "subscription_approved")
    private Boolean subscriptionApproved = true;

    private String status = "Active";

    // --- Relationships ---
    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;
    
    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    // ✅ FIX: FetchType.EAGER stops the Lazy Initialization crash
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "member_ministries",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "ministry_id")
    )
    private Set<Ministry> ministries = new HashSet<>();
}

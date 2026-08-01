package com.church.churchapp.dto;

import lombok.Data;
import java.util.List;

@Data
public class MemberDetailResponse {

    private Long memberId;
    private String customMemberId;
    private String firstName;
    private String lastName;
    private String gender;

    // ✅ FIX: Changed from LocalDate to String to match your database entity
    private String dateOfBirth;

    // Contact Info
    private String contactNumber;
    private String alternateContact;
    private String whatsappNumber;
    private String email;
    private String address;

    // Emergency Contacts
    private String emergencyContactName;
    private String emergencyContactNumber;

    // Family & Personal
    private String maritalStatus;
    private String anniversaryDate;
    private Long spouseMemberId;
    private String spouseName;
    private String numberOfChildren;
    private Long familyId;
    private String familyName;
    private Long familyHeadMemberId;

    // Spiritual
    private String membershipStatus;
    private String baptismStatus;

    // ✅ FIX: Changed from LocalDate to String to match your database entity
    private String baptismDate;

    private String confirmationStatus;
    private String communionStatus;
    private String previousChurch;

    // Ministry & Skills
    private String ministryRole;
    private String volunteerStatus;
    private String skills;
    private List<String> ministries;

    // Demographics
    private String occupation;
    private String education;
    private String bloodGroup;
    private String languagesKnown;

    // Contribution & Notes
    private String titheMember;
    private String preferredGivingMethod;
    private String specialRemarks;
    private Boolean subscriptionApproved;

    // Photo handling
    private boolean hasPhoto;
    private String photoUrl;
}

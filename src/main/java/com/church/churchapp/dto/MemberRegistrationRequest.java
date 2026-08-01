package com.church.churchapp.dto;

import lombok.Data;

@Data
public class MemberRegistrationRequest {

    // ✅ NEW: Catch the custom ID from React
    private String memberId;

    // 1. Basic Info
    private String firstName;
    private String lastName;
    private String gender;
    private String dob;

    // 2. Contact Info
    private String mobileNumber;
    private String altContactNumber;
    private String email;
    private String whatsappNumber;
    private String emergencyContactName;
    private String emergencyContactNumber;

    // 3. Address Info
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String district;
    private String state;
    private String pincode;
    private String country;

    // 4. Family Info
    private String familyId;
    private String familyHeadName;
    private String relationship;
    private String maritalStatus;
    private String anniversaryDate;
    private Long spouseMemberId;
    private String spouseName;
    private String numberOfChildren;

    // 5. Spiritual Info
    private String dateJoined;
    private String membershipStatus;
    private String baptismStatus;
    private String baptismDate;
    private String confirmationStatus;
    private String communionStatus;
    private String previousChurch;

    // 6. Ministry Info
    private String ministryGroups;
    private String ministryRole;
    private String volunteerStatus;
    private String skills;

    // 7. Contribution Info
    private String titheMember;
    private String preferredGivingMethod;

    // 8 & 9. Personal & Notes
    private String occupation;
    private String education;
    private String bloodGroup;
    private String languagesKnown;
    private String pastoralNotes;
    private String prayerRequests;
    private String specialRemarks;
}

package com.church.churchapp.controller;

import com.church.churchapp.dto.MemberRegistrationRequest;
import com.church.churchapp.dto.MemberDetailResponse;
import com.church.churchapp.entity.Member;
import com.church.churchapp.repository.MemberRepository;
import com.church.churchapp.service.MemberRegistrationService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members")
@PreAuthorize("hasAnyRole('ADD_MEMBER', 'ADMIN', 'SUPER_ADMIN', 'SUPER_PLUS_ADMIN')")
public class MemberController {

    @Autowired
    private MemberRegistrationService memberRegistrationService;

    @Autowired
    private MemberRepository memberRepository;

    // ==========================================
    // ✅ ADD MEMBER
    // ==========================================
    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addMember(
            @RequestParam("memberData") String memberDataString,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            MemberRegistrationRequest request = mapper.readValue(memberDataString, MemberRegistrationRequest.class);

            Member savedMember = memberRegistrationService.registerNewMember(request, photo);
            return ResponseEntity.ok(savedMember);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error saving member: " + e.getMessage());
        }
    }

    // ==========================================
    // ✅ GET MEMBER PHOTO
    // ==========================================
    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getMemberPhoto(@PathVariable Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (member.getProfilePhoto() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, member.getPhotoContentType())
                .body(member.getProfilePhoto());
    }

    // ==========================================
    // ✅ GET ALL MEMBERS (Needed for Alerts Dashboard!)
    // ==========================================
    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        try {
            List<Member> allMembers = memberRepository.findAll();

            // Strip large photo data from the list so the React Alerts page loads instantly
            for (Member m : allMembers) {
                stripLargeFields(m);
            }

            return ResponseEntity.ok(allMembers);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================================
    // ✅ SEARCH MEMBERS (Includes Name Search)
    // ==========================================
    @Transactional(readOnly = true)
    @GetMapping("/search")
    public ResponseEntity<?> searchMember(@RequestParam String type, @RequestParam String value) {
        try {
            List<Member> results = new ArrayList<>();
            String trimmed = value == null ? "" : value.trim();

            if ("id".equalsIgnoreCase(type)) {
                memberRepository.findByCustomMemberId(trimmed).ifPresent(m -> results.add(stripLargeFields(m)));
                try {
                    Long numericId = Long.parseLong(trimmed);
                    memberRepository.findById(numericId).ifPresent(m -> {
                        Member cleaned = stripLargeFields(m);
                        if (!results.contains(cleaned)) results.add(cleaned);
                    });
                } catch (NumberFormatException ignored) { }
            } else if ("phone".equalsIgnoreCase(type)) {
                memberRepository.findByContactNumber(trimmed).ifPresent(m -> results.add(stripLargeFields(m)));
            } else if ("email".equalsIgnoreCase(type)) {
                memberRepository.findByEmail(trimmed).ifPresent(m -> results.add(stripLargeFields(m)));
            } else if ("name".equalsIgnoreCase(type)) {
                memberRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(trimmed, trimmed)
                        .forEach(m -> {
                            Member cleaned = stripLargeFields(m);
                            if (!results.contains(cleaned)) results.add(cleaned);
                        });
            } else if ("all".equalsIgnoreCase(type)) {
                // Search by name
                memberRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(trimmed, trimmed)
                        .forEach(m -> {
                            Member cleaned = stripLargeFields(m);
                            if (!results.contains(cleaned)) results.add(cleaned);
                        });
                
                // Search by phone
                memberRepository.findByContactNumberContaining(trimmed)
                        .forEach(m -> {
                            Member cleaned = stripLargeFields(m);
                            if (!results.contains(cleaned)) results.add(cleaned);
                        });

                // Search by Custom ID
                memberRepository.findByCustomMemberId(trimmed).ifPresent(m -> {
                    Member cleaned = stripLargeFields(m);
                    if (!results.contains(cleaned)) results.add(cleaned);
                });
            } else {
                return ResponseEntity.badRequest().body("Unsupported search type: " + type);
            }

            return ResponseEntity.ok(results);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Search failed: " + ex.getMessage());
        }
    }

    // Bandwidth Optimizer
    private Member stripLargeFields(Member member) {
        member.setProfilePhoto(null);
        // We KEEP photoContentType so frontend knows a photo exists
        return member;
    }

    // ==========================================
    // ✅ GET FULL MEMBER DETAILS (For View Panel)
    // ==========================================
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<MemberDetailResponse> getMemberDetails(@PathVariable Long id) {
        return memberRepository.findById(id)
                .map(this::mapToDetailResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // DTO Mapper
    private MemberDetailResponse mapToDetailResponse(Member member) {
        MemberDetailResponse dto = new MemberDetailResponse();
        dto.setMemberId(member.getMemberId());
        dto.setCustomMemberId(member.getCustomMemberId());
        dto.setFirstName(member.getFirstName());
        dto.setLastName(member.getLastName());
        dto.setGender(member.getGender());
        dto.setDateOfBirth(member.getDateOfBirth());
        dto.setContactNumber(member.getContactNumber());
        dto.setAlternateContact(member.getAlternateContact());
        dto.setWhatsappNumber(member.getWhatsappNumber());
        dto.setEmail(member.getEmail());
        dto.setAddress(member.getAddress());

        // Emergency Contacts mapped successfully
        dto.setEmergencyContactName(member.getEmergencyContactName());
        dto.setEmergencyContactNumber(member.getEmergencyContactNumber());

        dto.setMaritalStatus(member.getMaritalStatus());
        dto.setAnniversaryDate(member.getAnniversaryDate());
        dto.setSpouseMemberId(member.getSpouseMemberId());
        dto.setSpouseName(member.getSpouseName());
        dto.setNumberOfChildren(member.getNumberOfChildren());
        dto.setMembershipStatus(member.getMembershipStatus());
        dto.setBaptismStatus(member.getBaptismStatus());
        dto.setBaptismDate(member.getBaptismDate());
        dto.setConfirmationStatus(member.getConfirmationStatus());
        dto.setCommunionStatus(member.getCommunionStatus());
        dto.setPreviousChurch(member.getPreviousChurch());
        dto.setMinistryRole(member.getMinistryRole());
        dto.setVolunteerStatus(member.getVolunteerStatus());
        dto.setSkills(member.getSkills());
        dto.setOccupation(member.getOccupation());
        dto.setEducation(member.getEducation());
        dto.setBloodGroup(member.getBloodGroup());
        dto.setLanguagesKnown(member.getLanguagesKnown());
        dto.setSpecialRemarks(member.getSpecialRemarks());
        dto.setTitheMember(member.getTitheMember());
        dto.setPreferredGivingMethod(member.getPreferredGivingMethod());
        dto.setSubscriptionApproved(member.getSubscriptionApproved());

        if (member.getFamily() != null) {
            dto.setFamilyId(member.getFamily().getFamilyId());
            dto.setFamilyName(member.getFamily().getFamilyName());
            dto.setFamilyHeadMemberId(
                    member.getFamily().getHeadMember() != null ? member.getFamily().getHeadMember().getMemberId() : null
            );
        }

        dto.setMinistries(
                member.getMinistries()
                        .stream()
                        .map(m -> m.getMinistryName())
                        .collect(Collectors.toList())
        );

        dto.setHasPhoto(member.getPhotoContentType() != null);
        dto.setPhotoUrl(member.getPhotoContentType() != null
                ? "/api/members/" + member.getMemberId() + "/photo"
                : null);
        return dto;
    }

    // ==========================================
    // ✅ UPDATE EXISTING MEMBER
    // ==========================================
    @PutMapping(value = "/update/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateMember(
            @PathVariable Long id,
            @RequestParam("memberData") String memberDataString,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MemberRegistrationRequest request = mapper.readValue(memberDataString, MemberRegistrationRequest.class);

            Member updatedMember = memberRegistrationService.updateExistingMember(id, request, photo);
            return ResponseEntity.ok(updatedMember);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==========================================
    // ✅ SAFELY DELETE MEMBER
    // ==========================================
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        try {
            memberRegistrationService.deleteMemberSafely(id);
            return ResponseEntity.ok("Member deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Delete Failed: " + e.getMessage());
        }
    }

    // ==========================================
    // ✅ UPDATE SUBSCRIPTION APPROVAL
    // ==========================================
    @PutMapping("/{id}/subscription-approval")
    @PreAuthorize("hasRole('SUPER_PLUS_ADMIN')")
    public ResponseEntity<?> updateSubscriptionApproval(
            @PathVariable Long id,
            @RequestParam Boolean approved) {
        try {
            Member member = memberRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Member not found"));
            member.setSubscriptionApproved(approved);
            Member saved = memberRepository.save(member);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to update subscription approval: " + e.getMessage());
        }
    }
}

package com.church.churchapp.service;

import com.church.churchapp.dto.MemberRegistrationRequest;
import com.church.churchapp.entity.*;
import com.church.churchapp.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MemberRegistrationService {

    @Autowired private MemberRepository memberRepository;
    @Autowired private FamilyRepository familyRepository;
    @Autowired private MinistryRepository ministryRepository;
    @Autowired private NoteRepository noteRepository;

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildFullName(Member member) {
        String firstName = trimToNull(member.getFirstName());
        String lastName = trimToNull(member.getLastName());
        if (firstName == null && lastName == null) {
            return null;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    private void applyMarriageDetails(Member member, MemberRegistrationRequest request, Long currentMemberId) {
        String maritalStatus = trimToNull(request.getMaritalStatus());
        member.setMaritalStatus(maritalStatus);

        if (!"Married".equalsIgnoreCase(maritalStatus)) {
            member.setAnniversaryDate(null);
            member.setSpouseMemberId(null);
            member.setSpouseName(null);
            return;
        }

        member.setAnniversaryDate(trimToNull(request.getAnniversaryDate()));

        if (request.getSpouseMemberId() != null) {
            if (currentMemberId != null && request.getSpouseMemberId().equals(currentMemberId)) {
                throw new RuntimeException("Partner link cannot point to the same member.");
            }

            Member linkedSpouse = memberRepository.findById(request.getSpouseMemberId())
                    .orElseThrow(() -> new RuntimeException("Linked partner was not found."));

            if (linkedSpouse.getSpouseMemberId() != null
                    && (currentMemberId == null || !linkedSpouse.getSpouseMemberId().equals(currentMemberId))) {
                throw new RuntimeException("Selected partner is already linked to another member.");
            }

            member.setSpouseMemberId(linkedSpouse.getMemberId());
            member.setSpouseName(buildFullName(linkedSpouse));
            return;
        }

        member.setSpouseMemberId(null);
        member.setSpouseName(trimToNull(request.getSpouseName()));
    }

    private void clearIncomingPartnerLinks(Long memberId, Long keepMemberId) {
        for (Member linkedMember : memberRepository.findBySpouseMemberId(memberId)) {
            if (keepMemberId != null && keepMemberId.equals(linkedMember.getMemberId())) {
                continue;
            }
            linkedMember.setSpouseMemberId(null);
            linkedMember.setSpouseName(null);
            memberRepository.save(linkedMember);
        }
    }

    @Transactional
    public Member registerNewMember(MemberRegistrationRequest request, MultipartFile photo) {

        // Check for Duplicates
        if (request.getMemberId() != null && !request.getMemberId().trim().isEmpty()) {
            if (memberRepository.existsByCustomMemberId(request.getMemberId().trim())) {
                throw new RuntimeException("Member ID '" + request.getMemberId() + "' is already used by another member.");
            }
        }
        if (request.getMobileNumber() != null && !request.getMobileNumber().trim().isEmpty()) {
            if (memberRepository.existsByContactNumber(request.getMobileNumber().trim())) {
                throw new RuntimeException("Mobile Number '" + request.getMobileNumber() + "' is already used by another member.");
            }
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (memberRepository.existsByEmail(request.getEmail().trim())) {
                throw new RuntimeException("Email '" + request.getEmail() + "' is already used by another member.");
            }
        }

        Member newMember = new Member();

        if (request.getMemberId() != null && !request.getMemberId().trim().isEmpty()) {
            newMember.setCustomMemberId(request.getMemberId().trim());
        } else {
            String randomId = "MEM-" + (System.currentTimeMillis() % 1000000);
            newMember.setCustomMemberId(randomId);
        }

        newMember.setFirstName(request.getFirstName());
        newMember.setLastName(request.getLastName());
        newMember.setDateOfBirth(request.getDob());
        newMember.setGender(request.getGender());
        newMember.setEmail(request.getEmail() != null && !request.getEmail().trim().isEmpty() ? request.getEmail().trim() : null);

        newMember.setContactNumber(request.getMobileNumber());
        newMember.setAlternateContact(request.getAltContactNumber());
        newMember.setWhatsappNumber(request.getWhatsappNumber());
        newMember.setEmergencyContactName(request.getEmergencyContactName());
        newMember.setEmergencyContactNumber(request.getEmergencyContactNumber());

        String fullAddress = request.getAddressLine1();
        if (request.getAddressLine2() != null && !request.getAddressLine2().isEmpty()) fullAddress += ", " + request.getAddressLine2();
        if (request.getCity() != null) fullAddress += ", " + request.getCity();
        if (request.getState() != null) fullAddress += ", " + request.getState();
        if (request.getPincode() != null) fullAddress += " - " + request.getPincode();
        newMember.setAddress(fullAddress);

        newMember.setBloodGroup(request.getBloodGroup());
        newMember.setEducation(request.getEducation());
        newMember.setOccupation(request.getOccupation());
        newMember.setLanguagesKnown(request.getLanguagesKnown());
        newMember.setNumberOfChildren(request.getNumberOfChildren());
        applyMarriageDetails(newMember, request, null);

        newMember.setJoinDate(request.getDateJoined());
        newMember.setMembershipStatus(request.getMembershipStatus());
        newMember.setBaptismStatus(request.getBaptismStatus());
        newMember.setBaptismDate(request.getBaptismDate());
        newMember.setConfirmationStatus(request.getConfirmationStatus());
        newMember.setCommunionStatus(request.getCommunionStatus());
        newMember.setPreviousChurch(request.getPreviousChurch());

        newMember.setMinistryRole(request.getMinistryRole());
        newMember.setVolunteerStatus(request.getVolunteerStatus());
        newMember.setSkills(request.getSkills());

        newMember.setTitheMember(request.getTitheMember());
        newMember.setPreferredGivingMethod(request.getPreferredGivingMethod());
        newMember.setSpecialRemarks(request.getSpecialRemarks());

        if (photo != null && !photo.isEmpty()) {
            try {
                newMember.setProfilePhoto(photo.getBytes());
                newMember.setPhotoContentType(photo.getContentType());
            } catch (IOException e) {
                System.out.println("Could not save photo to database: " + e.getMessage());
            }
        }

        if (request.getFamilyId() != null && !request.getFamilyId().trim().isEmpty()) {
            String familyIdValue = request.getFamilyId().trim();
            Long parsedFamilyId = null;
            try {
                parsedFamilyId = Long.parseLong(familyIdValue);
            } catch (NumberFormatException ignored) { }

            final Long familyIdAsLong = parsedFamilyId;
            final String lastNameVal = request.getLastName();
            final String fullAddressVal = fullAddress;

            Optional<Family> maybeFamily = familyIdAsLong != null
                    ? familyRepository.findById(familyIdAsLong)
                    : Optional.empty();

            Family family = maybeFamily.orElseGet(() -> {
                Family newFamily = new Family();
                newFamily.setFamilyName(lastNameVal != null && !lastNameVal.isBlank()
                        ? lastNameVal + " Family (" + familyIdValue + ")"
                        : "Family " + familyIdValue);
                newFamily.setAddress(fullAddressVal);
                if (familyIdAsLong != null) {
                    newFamily.setFamilyId(familyIdAsLong);
                }
                return familyRepository.save(newFamily);
            });
            newMember.setFamily(family);
        } else if (request.getFamilyHeadName() != null && !request.getFamilyHeadName().trim().isEmpty()) {
            Family newFamily = new Family();
            newFamily.setFamilyName(request.getLastName() + " Family");
            newFamily.setAddress(fullAddress);

            if ("Head".equalsIgnoreCase(request.getRelationship())) {
                newMember = memberRepository.save(newMember);
                newFamily.setHeadMember(newMember);
            }

            newFamily = familyRepository.save(newFamily);
            newMember.setFamily(newFamily);
        }

        if (request.getMinistryGroups() != null && !request.getMinistryGroups().trim().isEmpty()) {
            List<String> requestedMinistries = List.of(request.getMinistryGroups().split(",\\s*"));
            for (String ministryName : requestedMinistries) {
                if(ministryName.trim().isEmpty()) continue;

                Ministry ministry = ministryRepository.findByMinistryName(ministryName.trim())
                        .orElseGet(() -> {
                            Ministry newMin = new Ministry();
                            newMin.setMinistryName(ministryName.trim());
                            return ministryRepository.save(newMin);
                        });
                newMember.getMinistries().add(ministry);
            }
        }

        newMember = memberRepository.save(newMember);

        if ("Head".equalsIgnoreCase(request.getRelationship()) && newMember.getFamily() != null) {
            Family fam = newMember.getFamily();
            if (fam.getHeadMember() == null) {
                fam.setHeadMember(newMember);
                familyRepository.save(fam);
            }
        }

        if (request.getPastoralNotes() != null && !request.getPastoralNotes().trim().isEmpty()) {
            Note note = new Note();
            note.setNoteText(request.getPastoralNotes());
            note.setMember(newMember);
            note.setCreatedAt(LocalDateTime.now());
            noteRepository.save(note);
        }

        if ("Married".equalsIgnoreCase(newMember.getMaritalStatus()) && newMember.getSpouseMemberId() != null) {
            clearIncomingPartnerLinks(newMember.getMemberId(), newMember.getSpouseMemberId());
        }

        return newMember;
    }

    @Transactional
    public Member updateExistingMember(Long id, MemberRegistrationRequest request, MultipartFile photo) {
        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found in database."));

        // 1. Check Duplicates (ONLY IF THEY CHANGED IT)
        if (request.getMemberId() != null && !request.getMemberId().equals(existingMember.getCustomMemberId())) {
            if (memberRepository.existsByCustomMemberId(request.getMemberId().trim())) {
                throw new RuntimeException("Member ID is already used by someone else!");
            }
        }
        if (request.getMobileNumber() != null && !request.getMobileNumber().equals(existingMember.getContactNumber())) {
            if (memberRepository.existsByContactNumber(request.getMobileNumber().trim())) {
                throw new RuntimeException("Mobile Number is already used by someone else!");
            }
        }
        if (request.getEmail() != null && !request.getEmail().equals(existingMember.getEmail())) {
            if (memberRepository.existsByEmail(request.getEmail().trim())) {
                throw new RuntimeException("Email is already used by someone else!");
            }
        }

        // 2. Map Basic Data
        existingMember.setCustomMemberId(request.getMemberId());
        existingMember.setFirstName(request.getFirstName());
        existingMember.setLastName(request.getLastName());
        existingMember.setDateOfBirth(request.getDob());
        existingMember.setGender(request.getGender());
        existingMember.setEmail(request.getEmail() != null && !request.getEmail().trim().isEmpty() ? request.getEmail().trim() : null);

        existingMember.setContactNumber(request.getMobileNumber());
        existingMember.setAlternateContact(request.getAltContactNumber());
        existingMember.setWhatsappNumber(request.getWhatsappNumber());
        existingMember.setEmergencyContactName(request.getEmergencyContactName());
        existingMember.setEmergencyContactNumber(request.getEmergencyContactNumber());

        String fullAddress = request.getAddressLine1();
        if (request.getAddressLine2() != null && !request.getAddressLine2().isEmpty()) fullAddress += ", " + request.getAddressLine2();
        if (request.getCity() != null) fullAddress += ", " + request.getCity();
        if (request.getState() != null) fullAddress += ", " + request.getState();
        if (request.getPincode() != null) fullAddress += " - " + request.getPincode();
        existingMember.setAddress(fullAddress);

        existingMember.setBloodGroup(request.getBloodGroup());
        existingMember.setEducation(request.getEducation());
        existingMember.setOccupation(request.getOccupation());
        existingMember.setLanguagesKnown(request.getLanguagesKnown());
        existingMember.setNumberOfChildren(request.getNumberOfChildren());
        applyMarriageDetails(existingMember, request, existingMember.getMemberId());

        existingMember.setJoinDate(request.getDateJoined());
        existingMember.setMembershipStatus(request.getMembershipStatus());
        existingMember.setBaptismStatus(request.getBaptismStatus());
        existingMember.setBaptismDate(request.getBaptismDate());
        existingMember.setConfirmationStatus(request.getConfirmationStatus());
        existingMember.setCommunionStatus(request.getCommunionStatus());
        existingMember.setPreviousChurch(request.getPreviousChurch());

        existingMember.setMinistryRole(request.getMinistryRole());
        existingMember.setVolunteerStatus(request.getVolunteerStatus());
        existingMember.setSkills(request.getSkills());

        existingMember.setTitheMember(request.getTitheMember());
        existingMember.setPreferredGivingMethod(request.getPreferredGivingMethod());
        existingMember.setSpecialRemarks(request.getSpecialRemarks());

        // 3. Handle Photo
        if (photo != null && !photo.isEmpty()) {
            try {
                existingMember.setProfilePhoto(photo.getBytes());
                existingMember.setPhotoContentType(photo.getContentType());
            } catch (IOException e) {
                System.out.println("Could not update photo in database: " + e.getMessage());
            }
        }

        // 4. Handle Ministries
        existingMember.getMinistries().clear();
        if (request.getMinistryGroups() != null && !request.getMinistryGroups().trim().isEmpty()) {
            List<String> requestedMinistries = List.of(request.getMinistryGroups().split(",\\s*"));
            for (String ministryName : requestedMinistries) {
                if(ministryName.trim().isEmpty()) continue;
                Ministry ministry = ministryRepository.findByMinistryName(ministryName.trim())
                        .orElseGet(() -> {
                            Ministry newMin = new Ministry();
                            newMin.setMinistryName(ministryName.trim());
                            return ministryRepository.save(newMin);
                        });
                existingMember.getMinistries().add(ministry);
            }
        }

        existingMember = memberRepository.save(existingMember);

        if ("Married".equalsIgnoreCase(existingMember.getMaritalStatus()) && existingMember.getSpouseMemberId() != null) {
            clearIncomingPartnerLinks(existingMember.getMemberId(), existingMember.getSpouseMemberId());
        } else {
            clearIncomingPartnerLinks(existingMember.getMemberId(), null);
        }

        return existingMember;
    }

    // ==========================================
    // ✅ SAFELY DELETE MEMBER METHOD (UNTANGLED)
    // ==========================================
    @Transactional
    public void deleteMemberSafely(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found in database."));

        clearIncomingPartnerLinks(memberId, null);

        // 1. Completely Untangle from Family to prevent Cascade Crash
        if (member.getFamily() != null) {
            Family family = member.getFamily();

            // a. Remove them as Head of Family
            if (family.getHeadMember() != null && family.getHeadMember().getMemberId().equals(memberId)) {
                family.setHeadMember(null);
            }

            // b. Remove them from the Family's internal list
            family.getMembers().remove(member);

            // c. Disconnect the family from the member
            member.setFamily(null);

            familyRepository.save(family);
        }

        // 2. Clear Ministry links (Many-to-Many Join Table)
        member.getMinistries().clear();
        memberRepository.save(member); // Save to update the join table first

        // 3. Delete attached Pastoral/Prayer Notes
        List<Note> attachedNotes = noteRepository.findByMember(member);
        noteRepository.deleteAll(attachedNotes);

        // 4. Finally, delete the Member safely
        memberRepository.delete(member);
    }
}

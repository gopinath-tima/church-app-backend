package com.church.churchapp.service;

import com.church.churchapp.dto.MemberRegistrationRequest;
import com.church.churchapp.entity.FollowUp;
import com.church.churchapp.entity.Member;
import com.church.churchapp.entity.Visitor;
import com.church.churchapp.repository.FollowUpRepository;
import com.church.churchapp.repository.VisitorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisitorService {

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private FollowUpRepository followUpRepository;

    @Autowired
    private MemberRegistrationService memberRegistrationService;

    @Autowired
    private com.church.churchapp.repository.MemberRepository memberRepository;

    @Transactional
    public Visitor registerVisitor(Visitor visitor) {
        if (visitor.getStatus() == null || visitor.getStatus().isEmpty()) {
            visitor.setStatus("Registered");
        }

        // Check if there is already a member with the same email or phone number
        if (visitor.getConvertedMemberId() == null) {
            java.util.Optional<Member> memberOpt = java.util.Optional.empty();
            if (visitor.getEmail() != null && !visitor.getEmail().trim().isEmpty()) {
                memberOpt = memberRepository.findByEmail(visitor.getEmail().trim());
            }
            if (memberOpt.isEmpty() && visitor.getPhoneNumber() != null && !visitor.getPhoneNumber().trim().isEmpty()) {
                memberOpt = memberRepository.findByContactNumber(visitor.getPhoneNumber().trim());
            }

            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                visitor.setConvertedMemberId(member.getMemberId());
                visitor.setStatus("Convert");
                
                // Copy info from member to visitor if empty
                if (visitor.getFirstName() == null || visitor.getFirstName().trim().isEmpty()) {
                    visitor.setFirstName(member.getFirstName());
                }
                if (visitor.getLastName() == null || visitor.getLastName().trim().isEmpty()) {
                    visitor.setLastName(member.getLastName());
                }
                if (visitor.getEmail() == null || visitor.getEmail().trim().isEmpty()) {
                    visitor.setEmail(member.getEmail());
                }
                if (visitor.getPhoneNumber() == null || visitor.getPhoneNumber().trim().isEmpty()) {
                    visitor.setPhoneNumber(member.getContactNumber());
                }
                if (visitor.getGender() == null || visitor.getGender().trim().isEmpty()) {
                    visitor.setGender(member.getGender());
                }
                if (visitor.getAddress() == null || visitor.getAddress().trim().isEmpty()) {
                    visitor.setAddress(member.getAddress());
                }
            }
        } else {
            // If explicit convertedMemberId is provided, retrieve member data if not already populated
            visitor.setStatus("Convert");
            java.util.Optional<Member> memberOpt = memberRepository.findById(visitor.getConvertedMemberId());
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                if (visitor.getFirstName() == null || visitor.getFirstName().trim().isEmpty()) {
                    visitor.setFirstName(member.getFirstName());
                }
                if (visitor.getLastName() == null || visitor.getLastName().trim().isEmpty()) {
                    visitor.setLastName(member.getLastName());
                }
                if (visitor.getEmail() == null || visitor.getEmail().trim().isEmpty()) {
                    visitor.setEmail(member.getEmail());
                }
                if (visitor.getPhoneNumber() == null || visitor.getPhoneNumber().trim().isEmpty()) {
                    visitor.setPhoneNumber(member.getContactNumber());
                }
                if (visitor.getGender() == null || visitor.getGender().trim().isEmpty()) {
                    visitor.setGender(member.getGender());
                }
                if (visitor.getAddress() == null || visitor.getAddress().trim().isEmpty()) {
                    visitor.setAddress(member.getAddress());
                }
            }
        }

        return visitorRepository.save(visitor);
    }

    public List<Visitor> getAllVisitors() {
        return visitorRepository.findAll();
    }

    public Visitor getVisitorById(Long id) {
        return visitorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visitor not found with id: " + id));
    }

    @Transactional
    public Visitor updateVisitor(Long id, Visitor details) {
        Visitor visitor = getVisitorById(id);
        visitor.setFirstName(details.getFirstName());
        visitor.setLastName(details.getLastName());
        visitor.setEmail(details.getEmail());
        visitor.setPhoneNumber(details.getPhoneNumber());
        visitor.setVisitDate(details.getVisitDate());
        visitor.setGender(details.getGender());
        visitor.setAddress(details.getAddress());
        visitor.setFirstVisitNotes(details.getFirstVisitNotes());
        if (details.getStatus() != null) {
            visitor.setStatus(details.getStatus());
        }
        return visitorRepository.save(visitor);
    }

    @Transactional
    public FollowUp addFollowUp(Long visitorId, FollowUp followUp) {
        Visitor visitor = getVisitorById(visitorId);
        followUp.setVisitor(visitor);
        if (followUp.getStatus() == null || followUp.getStatus().isEmpty()) {
            followUp.setStatus("Pending");
        }
        
        // Update visitor status to "In Progress" if it is currently "Registered"
        if ("Registered".equalsIgnoreCase(visitor.getStatus())) {
            visitor.setStatus("In Progress");
            visitorRepository.save(visitor);
        }
        
        return followUpRepository.save(followUp);
    }

    public List<FollowUp> getFollowUpsForVisitor(Long visitorId) {
        return followUpRepository.findByVisitorId(visitorId);
    }

    @Transactional
    public FollowUp updateFollowUp(Long followUpId, FollowUp details) {
        FollowUp followUp = followUpRepository.findById(followUpId)
                .orElseThrow(() -> new RuntimeException("Follow-up not found with id: " + followUpId));
        followUp.setFollowUpDate(details.getFollowUpDate());
        followUp.setFollowUpType(details.getFollowUpType());
        followUp.setStatus(details.getStatus());
        followUp.setNotes(details.getNotes());
        followUp.setAssignee(details.getAssignee());
        return followUpRepository.save(followUp);
    }

    @Transactional
    public Member convertVisitorToMember(Long visitorId) {
        Visitor visitor = getVisitorById(visitorId);
        if ("Converted".equalsIgnoreCase(visitor.getStatus()) || "Convert".equalsIgnoreCase(visitor.getStatus())) {
            throw new RuntimeException("Visitor has already been converted to a member.");
        }

        // Map visitor to member registration request
        MemberRegistrationRequest request = new MemberRegistrationRequest();
        request.setFirstName(visitor.getFirstName());
        request.setLastName(visitor.getLastName());
        request.setEmail(visitor.getEmail());
        request.setMobileNumber(visitor.getPhoneNumber());
        request.setDateJoined(visitor.getVisitDate()); // Use visit date as join date
        request.setGender(visitor.getGender());
        request.setAddressLine1(visitor.getAddress());
        request.setPastoralNotes(visitor.getFirstVisitNotes());
        request.setMembershipStatus("Active"); // default status for converted members

        // Save new Member
        Member member = memberRegistrationService.registerNewMember(request, null);

        // Update Visitor Status and Link
        visitor.setStatus("Convert");
        visitor.setConvertedMemberId(member.getMemberId());
        visitorRepository.save(visitor);

        return member;
    }

    public Map<String, Object> getVisitorReports() {
        List<Visitor> visitors = visitorRepository.findAll();
        List<FollowUp> followUps = followUpRepository.findAll();

        long totalVisitors = visitors.size();
        long converted = visitors.stream().filter(v -> "Converted".equalsIgnoreCase(v.getStatus()) || "Convert".equalsIgnoreCase(v.getStatus())).count();
        long registered = visitors.stream().filter(v -> "Registered".equalsIgnoreCase(v.getStatus())).count();
        long inProgress = visitors.stream().filter(v -> "In Progress".equalsIgnoreCase(v.getStatus())).count();
        long archived = visitors.stream().filter(v -> "Archived".equalsIgnoreCase(v.getStatus())).count();

        long pendingFollowUps = followUps.stream().filter(f -> "Pending".equalsIgnoreCase(f.getStatus())).count();
        long completedFollowUps = followUps.stream().filter(f -> "Completed".equalsIgnoreCase(f.getStatus())).count();

        double conversionRate = totalVisitors > 0 ? ((double) converted / totalVisitors) * 100 : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVisitors", totalVisitors);
        stats.put("convertedCount", converted);
        stats.put("registeredCount", registered);
        stats.put("inProgressCount", inProgress);
        stats.put("archivedCount", archived);
        stats.put("pendingFollowUps", pendingFollowUps);
        stats.put("completedFollowUps", completedFollowUps);
        stats.put("conversionRate", Math.round(conversionRate * 100.0) / 100.0);

        return stats;
    }
}

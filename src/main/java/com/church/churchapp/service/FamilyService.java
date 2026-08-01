package com.church.churchapp.service;

import com.church.churchapp.entity.Family;
import com.church.churchapp.entity.Member;
import com.church.churchapp.repository.FamilyRepository;
import com.church.churchapp.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FamilyService {

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<Family> getAllFamilies() {
        return familyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Family> getFamilyById(Long id) {
        return familyRepository.findById(id);
    }

    @Transactional
    public Family createFamily(String familyName, String address, Long headMemberId) {
        Family family = new Family();
        family.setFamilyName(familyName);
        family.setAddress(address);

        if (headMemberId != null) {
            Member head = memberRepository.findById(headMemberId).orElse(null);
            if (head != null) {
                family.setHeadMember(head);
            }
        }

        Family savedFamily = familyRepository.save(family);

        if (headMemberId != null) {
            Member head = memberRepository.findById(headMemberId).orElse(null);
            if (head != null) {
                head.setFamily(savedFamily);
                memberRepository.save(head);
            }
        }

        return savedFamily;
    }

    @Transactional
    public Family updateFamily(Long id, String familyName, String address, Long headMemberId) {
        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Family not found"));

        if (familyName != null) family.setFamilyName(familyName);
        if (address != null) family.setAddress(address);

        if (headMemberId != null) {
            Member newHead = memberRepository.findById(headMemberId).orElse(null);
            if (newHead != null) {
                family.setHeadMember(newHead);
                newHead.setFamily(family);
                memberRepository.save(newHead);
            }
        } else if (headMemberId == null && familyName != null) {
           // Allow updating just the name without removing the head
        }

        return familyRepository.save(family);
    }

    @Transactional
    public Family addMemberToFamily(Long familyId, Long memberId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("Family not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setFamily(family);
        memberRepository.save(member);
        
        // Refresh family to include the new member
        return familyRepository.findById(familyId).orElse(family);
    }

    @Transactional
    public Family removeMemberFromFamily(Long familyId, Long memberId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("Family not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (member.getFamily() != null && member.getFamily().getFamilyId().equals(familyId)) {
            member.setFamily(null);
            memberRepository.save(member);
            
            // If the member was the head, remove them as head
            if (family.getHeadMember() != null && family.getHeadMember().getMemberId().equals(memberId)) {
                family.setHeadMember(null);
                familyRepository.save(family);
            }
        }
        
        return familyRepository.findById(familyId).orElse(family);
    }

    @Transactional
    public void deleteFamily(Long id) {
        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Family not found"));

        // Unlink all members before deleting so CascadeType.ALL doesn't delete the actual people
        for (Member m : family.getMembers()) {
            m.setFamily(null);
            memberRepository.save(m);
        }
        
        // Clear the collections to prevent Hibernate from cascading the delete
        family.getMembers().clear();
        family.setHeadMember(null);
        familyRepository.save(family); // Save the unlinked state

        // Now delete the family safely
        familyRepository.delete(family);
    }
}

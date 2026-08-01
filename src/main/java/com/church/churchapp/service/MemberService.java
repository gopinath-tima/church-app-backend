package com.church.churchapp.service;

import com.church.churchapp.entity.Member;
import com.church.churchapp.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    // Fetch all members (Useful for when we build your frontend table!)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // Fetch a single member by their ID
    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    // Delete a member
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    // NOTE: We completely removed the old "save" code from here because
    // your new MemberRegistrationService handles all the complex saving now!
}
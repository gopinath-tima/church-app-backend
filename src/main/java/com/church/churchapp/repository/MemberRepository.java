package com.church.churchapp.repository;

import com.church.churchapp.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Tools to check if data is already used (for adding new members)
    boolean existsByCustomMemberId(String customMemberId);
    boolean existsByEmail(String email);
    boolean existsByContactNumber(String contactNumber);

    // ✅ NEW: Search Tools for Edit Mode
    Optional<Member> findByCustomMemberId(String customMemberId);
    Optional<Member> findByContactNumber(String contactNumber);
    Optional<Member> findByEmail(String email);

    // Name & Phone search (partial match, case-insensitive)
    List<Member> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    List<Member> findByContactNumberContaining(String contactNumber);
    List<Member> findByCustomMemberIdContainingIgnoreCase(String customMemberId);

    List<Member> findBySpouseMemberId(Long spouseMemberId);
}

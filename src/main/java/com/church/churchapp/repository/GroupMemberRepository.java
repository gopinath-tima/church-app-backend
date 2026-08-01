package com.church.churchapp.repository;

import com.church.churchapp.entity.GroupMember;
import com.church.churchapp.entity.ChurchGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroup(ChurchGroup group);
    List<GroupMember> findByMemberId(Long memberId);
    void deleteByGroup(ChurchGroup group);
    void deleteByGroupAndMemberId(ChurchGroup group, Long memberId);
}

package com.church.churchapp.controller;

import com.church.churchapp.entity.ChurchGroup;
import com.church.churchapp.entity.GroupMember;
import com.church.churchapp.repository.ChurchGroupRepository;
import com.church.churchapp.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups")
@PreAuthorize("hasAnyRole('GROUPS', 'ADMIN', 'SUPER_ADMIN', 'SUPER_PLUS_ADMIN')")
public class GroupController {

    @Autowired
    private ChurchGroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    // ✅ Get all groups
    @GetMapping
    public List<ChurchGroup> getAllGroups() {
        return groupRepository.findAll();
    }

    // ✅ Get group by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupById(@PathVariable Long id) {
        Optional<ChurchGroup> group = groupRepository.findById(id);
        if (group.isPresent()) {
            return ResponseEntity.ok(group.get());
        }
        return ResponseEntity.notFound().build();
    }

    // ✅ Create new group
    @PostMapping
    public ResponseEntity<ChurchGroup> createGroup(@RequestBody ChurchGroup group) {
        if (group.getStatus() == null) {
            group.setStatus("Active");
        }
        ChurchGroup saved = groupRepository.save(group);
        return ResponseEntity.ok(saved);
    }

    // ✅ Update existing group
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Long id, @RequestBody ChurchGroup groupDetails) {
        Optional<ChurchGroup> optionalGroup = groupRepository.findById(id);
        if (optionalGroup.isPresent()) {
            ChurchGroup group = optionalGroup.get();
            group.setName(groupDetails.getName());
            group.setDescription(groupDetails.getDescription());
            group.setCategory(groupDetails.getCategory());
            group.setMeetingTime(groupDetails.getMeetingTime());
            group.setLocation(groupDetails.getLocation());
            group.setStatus(groupDetails.getStatus());
            return ResponseEntity.ok(groupRepository.save(group));
        }
        return ResponseEntity.badRequest().body("Group not found");
    }

    // ✅ Delete group along with its members
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        Optional<ChurchGroup> optionalGroup = groupRepository.findById(id);
        if (optionalGroup.isPresent()) {
            ChurchGroup group = optionalGroup.get();
            groupMemberRepository.deleteByGroup(group);
            groupRepository.delete(group);
            return ResponseEntity.ok("Group deleted successfully");
        }
        return ResponseEntity.badRequest().body("Group not found");
    }

    // ✅ Get all member connections for a group
    @GetMapping("/{id}/members")
    public List<GroupMember> getGroupMembers(@PathVariable Long id) {
        Optional<ChurchGroup> group = groupRepository.findById(id);
        if (group.isPresent()) {
            return groupMemberRepository.findByGroup(group.get());
        }
        return List.of();
    }

    // ✅ Get all group connections for a specific member
    @GetMapping("/member/{memberId}")
    public List<GroupMember> getGroupsForMember(@PathVariable Long memberId) {
        return groupMemberRepository.findByMemberId(memberId);
    }

    // ✅ Link/Add a member to a group
    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMemberToGroup(@PathVariable Long id, @RequestBody GroupMember groupMemberDetails) {
        Optional<ChurchGroup> optionalGroup = groupRepository.findById(id);
        if (optionalGroup.isPresent()) {
            ChurchGroup group = optionalGroup.get();
            
            // Check if member already in group
            List<GroupMember> existing = groupMemberRepository.findByGroup(group);
            boolean alreadyExists = existing.stream()
                .anyMatch(gm -> gm.getMemberId().equals(groupMemberDetails.getMemberId()));
            if (alreadyExists) {
                return ResponseEntity.badRequest().body("Member is already in this group.");
            }

            GroupMember groupMember = new GroupMember();
            groupMember.setGroup(group);
            groupMember.setMemberId(groupMemberDetails.getMemberId());
            groupMember.setMemberName(groupMemberDetails.getMemberName());
            groupMember.setRole(groupMemberDetails.getRole() != null ? groupMemberDetails.getRole() : "Member");
            groupMember.setJoinDate(groupMemberDetails.getJoinDate() != null ? groupMemberDetails.getJoinDate() : LocalDate.now().toString());

            return ResponseEntity.ok(groupMemberRepository.save(groupMember));
        }
        return ResponseEntity.badRequest().body("Group not found");
    }

    // ✅ Unlink/Remove a member from a group
    @DeleteMapping("/{id}/members/{memberId}")
    @Transactional
    public ResponseEntity<?> removeMemberFromGroup(@PathVariable Long id, @PathVariable Long memberId) {
        Optional<ChurchGroup> optionalGroup = groupRepository.findById(id);
        if (optionalGroup.isPresent()) {
            groupMemberRepository.deleteByGroupAndMemberId(optionalGroup.get(), memberId);
            return ResponseEntity.ok("Member removed from group successfully.");
        }
        return ResponseEntity.badRequest().body("Group not found");
    }
}

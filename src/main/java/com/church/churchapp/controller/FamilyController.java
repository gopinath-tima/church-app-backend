package com.church.churchapp.controller;

import com.church.churchapp.entity.Family;
import com.church.churchapp.entity.Member;
import com.church.churchapp.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/families")
@PreAuthorize("hasAnyRole('CONTACTS', 'ADMIN', 'SUPER_ADMIN', 'SUPER_PLUS_ADMIN')")
@Transactional
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    private Map<String, Object> mapFamily(Family f) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("familyId", f.getFamilyId());
        map.put("familyName", f.getFamilyName());
        map.put("address", f.getAddress() != null ? f.getAddress() : "");
        
        if (f.getHeadMember() != null) {
            Map<String, Object> headMap = new java.util.HashMap<>();
            headMap.put("memberId", f.getHeadMember().getMemberId());
            headMap.put("firstName", f.getHeadMember().getFirstName());
            headMap.put("lastName", f.getHeadMember().getLastName());
            map.put("headMember", headMap);
        } else {
            map.put("headMember", null);
        }

        if (f.getMembers() != null) {
            map.put("members", f.getMembers().stream().map(m -> {
                Map<String, Object> mMap = new java.util.HashMap<>();
                mMap.put("memberId", m.getMemberId());
                mMap.put("firstName", m.getFirstName());
                mMap.put("lastName", m.getLastName());
                mMap.put("customMemberId", m.getCustomMemberId() != null ? m.getCustomMemberId() : "");
                return mMap;
            }).collect(Collectors.toList()));
        } else {
            map.put("members", new java.util.ArrayList<>());
        }
        
        return map;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllFamilies() {
        List<Map<String, Object>> result = familyService.getAllFamilies()
            .stream()
            .map(this::mapFamily)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFamily(@PathVariable Long id) {
        return familyService.getFamilyById(id)
                .map(this::mapFamily)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createFamily(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("familyName");
        String address = (String) payload.get("address");
        
        Long headId = null;
        if (payload.get("headMemberId") != null) {
            headId = Long.valueOf(payload.get("headMemberId").toString());
        }

        Family saved = familyService.createFamily(name, address, headId);
        return ResponseEntity.ok(mapFamily(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateFamily(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("familyName");
        String address = (String) payload.get("address");
        
        Long headId = null;
        if (payload.containsKey("headMemberId") && payload.get("headMemberId") != null) {
            headId = Long.valueOf(payload.get("headMemberId").toString());
        }

        Family updated = familyService.updateFamily(id, name, address, headId);
        return ResponseEntity.ok(mapFamily(updated));
    }

    @PostMapping("/{id}/members/{memberId}")
    public ResponseEntity<Map<String, Object>> addMember(@PathVariable Long id, @PathVariable Long memberId) {
        Family updated = familyService.addMemberToFamily(id, memberId);
        return ResponseEntity.ok(mapFamily(updated));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Map<String, Object>> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        Family updated = familyService.removeMemberFromFamily(id, memberId);
        return ResponseEntity.ok(mapFamily(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFamily(@PathVariable Long id) {
        familyService.deleteFamily(id);
        return ResponseEntity.ok().build();
    }
}

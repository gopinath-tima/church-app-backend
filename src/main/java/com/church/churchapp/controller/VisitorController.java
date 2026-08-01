package com.church.churchapp.controller;

import com.church.churchapp.entity.FollowUp;
import com.church.churchapp.entity.Member;
import com.church.churchapp.entity.Visitor;
import com.church.churchapp.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visitors")
@PreAuthorize("hasAnyRole('CONTACTS', 'ADMIN', 'SUPER_ADMIN', 'SUPER_PLUS_ADMIN')")
public class VisitorController {

    @Autowired
    private VisitorService visitorService;

    @PostMapping
    public ResponseEntity<Visitor> registerVisitor(@RequestBody Visitor visitor) {
        return ResponseEntity.ok(visitorService.registerVisitor(visitor));
    }

    @GetMapping
    public ResponseEntity<List<Visitor>> getAllVisitors() {
        return ResponseEntity.ok(visitorService.getAllVisitors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Visitor> getVisitorById(@PathVariable Long id) {
        return ResponseEntity.ok(visitorService.getVisitorById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Visitor> updateVisitor(@PathVariable Long id, @RequestBody Visitor visitor) {
        return ResponseEntity.ok(visitorService.updateVisitor(id, visitor));
    }

    @PostMapping("/{id}/follow-ups")
    public ResponseEntity<FollowUp> addFollowUp(@PathVariable Long id, @RequestBody FollowUp followUp) {
        return ResponseEntity.ok(visitorService.addFollowUp(id, followUp));
    }

    @GetMapping("/{id}/follow-ups")
    public ResponseEntity<List<FollowUp>> getFollowUpsForVisitor(@PathVariable Long id) {
        return ResponseEntity.ok(visitorService.getFollowUpsForVisitor(id));
    }

    @PutMapping("/follow-ups/{followUpId}")
    public ResponseEntity<FollowUp> updateFollowUp(@PathVariable Long followUpId, @RequestBody FollowUp followUp) {
        return ResponseEntity.ok(visitorService.updateFollowUp(followUpId, followUp));
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<Member> convertVisitorToMember(@PathVariable Long id) {
        try {
            Member member = visitorService.convertVisitorToMember(id);
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getVisitorReports() {
        return ResponseEntity.ok(visitorService.getVisitorReports());
    }
}

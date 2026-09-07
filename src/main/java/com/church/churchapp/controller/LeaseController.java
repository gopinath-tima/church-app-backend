package com.church.churchapp.controller;

import com.church.churchapp.entity.Lease;
import com.church.churchapp.service.LeaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leases")
@PreAuthorize("hasAnyRole('ACCOUNTING', 'ADMIN', 'SUPER_ADMIN', 'SUPER_PLUS_ADMIN')")
public class LeaseController {

    @Autowired
    private LeaseService leaseService;

    @GetMapping
    public ResponseEntity<List<Lease>> getAllLeases() {
        return ResponseEntity.ok(leaseService.getAllLeases());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lease> getLeaseById(@PathVariable Long id) {
        return leaseService.getLeaseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Lease> createLease(@RequestBody Lease lease) {
        return ResponseEntity.ok(leaseService.createLease(lease));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lease> updateLease(@PathVariable Long id, @RequestBody Lease lease) {
        try {
            return ResponseEntity.ok(leaseService.updateLease(id, lease));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLease(@PathVariable Long id) {
        leaseService.deleteLease(id);
        return ResponseEntity.ok().build();
    }
}

package com.church.churchapp.service;

import com.church.churchapp.entity.Lease;
import com.church.churchapp.entity.Branch;
import com.church.churchapp.repository.LeaseRepository;
import com.church.churchapp.repository.BranchRepository;
import com.church.churchapp.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LeaseService {

    @Autowired
    private LeaseRepository leaseRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public List<Lease> getAllLeases() {
        return leaseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Lease> getLeaseById(Long id) {
        return leaseRepository.findById(id);
    }

    @Transactional
    public Lease createLease(Lease lease) {
        Long currentBranchId = TenantContext.getCurrentBranch();
        if (currentBranchId != null) {
            Branch branch = branchRepository.findById(currentBranchId)
                    .orElseThrow(() -> new RuntimeException("Branch not found"));
            lease.setBranch(branch);
        }
        return leaseRepository.save(lease);
    }

    @Transactional
    public Lease updateLease(Long id, Lease updatedLease) {
        return leaseRepository.findById(id).map(existing -> {
            existing.setTenantName(updatedLease.getTenantName());
            existing.setPropertyName(updatedLease.getPropertyName());
            existing.setStartDate(updatedLease.getStartDate());
            existing.setDurationMonths(updatedLease.getDurationMonths());
            existing.setMonthlyRent(updatedLease.getMonthlyRent());
            existing.setStatus(updatedLease.getStatus());
            return leaseRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Lease not found"));
    }

    @Transactional
    public void deleteLease(Long id) {
        leaseRepository.deleteById(id);
    }
}

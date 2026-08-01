package com.church.churchapp.service;

import com.church.churchapp.entity.Branch;
import com.church.churchapp.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {

    @Autowired
    private BranchRepository branchRepository;

    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }
    
    public List<Branch> getActiveBranches() {
        return branchRepository.findByStatus("Active");
    }

    public Branch getBranchById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id " + id));
    }

    public Branch createBranch(Branch branch) {
        return branchRepository.save(branch);
    }

    public Branch updateBranch(Long id, Branch branchDetails) {
        Branch branch = getBranchById(id);
        
        branch.setName(branchDetails.getName());
        branch.setBranchCode(branchDetails.getBranchCode());
        branch.setChurchName(branchDetails.getChurchName());
        branch.setAddress(branchDetails.getAddress());
        branch.setCity(branchDetails.getCity());
        branch.setState(branchDetails.getState());
        branch.setCountry(branchDetails.getCountry());
        branch.setPostalCode(branchDetails.getPostalCode());
        branch.setContactNumber(branchDetails.getContactNumber());
        branch.setEmail(branchDetails.getEmail());
        branch.setHeadPastorName(branchDetails.getHeadPastorName());
        branch.setEstablishedDate(branchDetails.getEstablishedDate());
        branch.setStatus(branchDetails.getStatus());
        
        return branchRepository.save(branch);
    }

    public void deleteBranch(Long id) {
        Branch branch = getBranchById(id);
        branchRepository.delete(branch);
    }
}

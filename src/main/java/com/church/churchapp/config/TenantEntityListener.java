package com.church.churchapp.config;

import com.church.churchapp.entity.Branch;
import jakarta.persistence.PrePersist;
import java.lang.reflect.Method;

public class TenantEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        Long branchId = TenantContext.getCurrentBranch();
        if (branchId != null) {
            try {
                // Check if the entity has a getBranch method
                Method getBranchMethod = entity.getClass().getMethod("getBranch");
                Branch currentBranch = (Branch) getBranchMethod.invoke(entity);
                
                // Only set branch if it hasn't been set explicitly
                if (currentBranch == null) {
                    Branch proxyBranch = new Branch();
                    proxyBranch.setId(branchId);
                    
                    Method setBranchMethod = entity.getClass().getMethod("setBranch", Branch.class);
                    setBranchMethod.invoke(entity, proxyBranch);
                }
            } catch (Exception e) {
                // Entity doesn't have Branch relationship, ignore
            }
        }
    }
}

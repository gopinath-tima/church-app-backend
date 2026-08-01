package com.church.churchapp.config;

public class TenantContext {
    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();

    public static void setCurrentBranch(Long branchId) {
        currentTenant.set(branchId);
    }

    public static Long getCurrentBranch() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}

package com.church.churchapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_HEADER = "X-Branch-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Read the branch ID from the JWT payload
        Object jwtBranchIdObj = request.getAttribute("jwtBranchId");
        Object jwtRolesObj = request.getAttribute("jwtRoles");
        
        Long enforcedBranchId = null;
        boolean isSuperAdmin = false;
        
        if (jwtRolesObj instanceof java.util.List) {
            java.util.List<String> roles = (java.util.List<String>) jwtRolesObj;
            if (roles.contains("SUPER_ADMIN") || roles.contains("SUPER_PLUS_ADMIN")) {
                isSuperAdmin = true;
            }
        }
        
        if (jwtBranchIdObj != null) {
            enforcedBranchId = ((Number) jwtBranchIdObj).longValue();
        }
        
        // If the user has an enforced branch ID and is NOT a super admin, force that branch ID
        if (enforcedBranchId != null && !isSuperAdmin) {
            TenantContext.setCurrentBranch(enforcedBranchId);
        } else {
            // Super admins or users without a branch can use the frontend header
            String headerBranchId = request.getHeader(TENANT_HEADER);
            if (headerBranchId != null && !headerBranchId.isEmpty()) {
                try {
                    TenantContext.setCurrentBranch(Long.valueOf(headerBranchId));
                } catch (NumberFormatException e) {
                    // Ignore invalid headers
                }
            }
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}

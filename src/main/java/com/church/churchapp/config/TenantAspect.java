package com.church.churchapp.config;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantAspect {

    @Autowired
    private EntityManager entityManager;

    @Before("execution(* com.church.churchapp.repository.*.*(..))")
    public void enableTenantFilter() {
        Long branchId = TenantContext.getCurrentBranch();
        if (branchId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("branchId", branchId);
        } else {
            Session session = entityManager.unwrap(Session.class);
            session.disableFilter("tenantFilter");
        }
    }
}

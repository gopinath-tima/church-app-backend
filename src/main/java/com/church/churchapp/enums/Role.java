package com.church.churchapp.enums;

public enum Role {
    SUPER_PLUS_ADMIN,
    SUPER_ADMIN,
    ADMIN,
    CORE,
    INVENTORY,
    FINANCE,
    WORKFORCE,
    COMMUNICATION,
    REPORTING,
    ADD_MEMBER, // ✅ Added the new role here
    DOCUMENTS,
    CONTACTS,
    ACCOUNTING,
    GROUPS,
    EVENTS,
    FORMS,
    MINISTRY,
    BRANCH_ADMIN, // Can manage a single branch
    PASTOR,       // Can view data for their branch
    STAFF         // Can view and edit data for their branch
}
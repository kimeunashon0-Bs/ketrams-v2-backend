package com.ketrams.v2.entity.enums;

public enum ApplicationStatus {
    PENDING_INSTITUTION,       // Initial state after student submits
    APPROVED_BY_INSTITUTION,   // Institution approved
    REJECTED_BY_INSTITUTION,   // Institution rejected
    PENDING_SUB_COUNTY,        // After institution approval, waiting for sub-county
    VERIFIED,                   // Sub-county verified eligibility
    PENDING_CLARIFICATION,      // Sub-county needs more info
    FACILITATED,                // Bursary/facilitation approved
    NOT_FACILITATED,            // Bursary denied
    WAITLISTED                  // On hold for funding
}
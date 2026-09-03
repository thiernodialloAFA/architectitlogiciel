package com.pvg.governance.domain;

/** Standard lifecycle: Draft → Active → Retired (retired documents are immutable). */
public enum StandardStatus {
    DRAFT, ACTIVE, RETIRED
}

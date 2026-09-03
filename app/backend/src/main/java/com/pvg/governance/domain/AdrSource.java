package com.pvg.governance.domain;

/**
 * Source-of-truth marker for an ADR (proposal §3.2): PLATFORM-authored records are
 * canonical here; REPOSITORY records are read-only imports indexed from a team's own
 * repo, which stays canonical.
 */
public enum AdrSource {
    PLATFORM, REPOSITORY
}

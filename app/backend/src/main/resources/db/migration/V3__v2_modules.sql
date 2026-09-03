-- Architecture Governance Platform — v2 schema
-- Remaining proposal modules (§3.3–§3.5, §6 items 4–6): Architecture Advice Forum
-- & governance workflow, Standards & reference patterns library, versioned C4
-- diagrams, ADR repository indexing identity, and fitness-function/contract-test
-- check references.

-- ---------------------------------------------------------------------------
-- §3.3 Architecture Advice Forum & governance workflow
-- ---------------------------------------------------------------------------
CREATE TABLE forum_session (
    id UUID PRIMARY KEY,
    session_date DATE NOT NULL,
    title VARCHAR(300) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX forum_session_date_idx ON forum_session (session_date DESC);

CREATE TABLE aaf_proposal (
    id UUID PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    summary TEXT NOT NULL,
    -- Declared scope per the three-tier model in the job ad: TEAM / DOMAIN / GROUP.
    scope VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    submitted_by VARCHAR(200) NOT NULL,
    department VARCHAR(200),
    -- A proposal links to an ADR draft, an application (whose C4 gives the context), or both.
    adr_id UUID REFERENCES adr (id),
    application_id UUID REFERENCES application_entry (id),
    session_id UUID REFERENCES forum_session (id),
    -- Outcome capture: advice given, by whom, final decision (taken by the proposer,
    -- per the advice process — the forum advises, it does not approve).
    advice_given TEXT,
    advised_by VARCHAR(300),
    final_decision TEXT,
    decided_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX aaf_proposal_status_idx ON aaf_proposal (status);
CREATE INDEX aaf_proposal_session_idx ON aaf_proposal (session_id);

-- Explicit, rarely-used arbitration record for the group-scope escalation path;
-- kept as its own table so "how often is this actually invoked" is a trivial query.
CREATE TABLE arbitration_record (
    id UUID PRIMARY KEY,
    proposal_id UUID NOT NULL UNIQUE REFERENCES aaf_proposal (id),
    requested_by VARCHAR(200) NOT NULL,
    rationale TEXT NOT NULL,
    outcome TEXT NOT NULL,
    decided_by VARCHAR(200) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- §3.4 Standards & reference patterns library
-- ---------------------------------------------------------------------------
CREATE TABLE standard (
    id UUID PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    category VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    -- Version bumps on every content change; the append-only audit trail holds the
    -- old/new content of each bump, which is the document's version history.
    version INT NOT NULL DEFAULT 1,
    content TEXT NOT NULL,
    owner VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX standard_category_idx ON standard (category);
CREATE INDEX standard_status_idx ON standard (status);

-- ADRs that established or amended the standard.
CREATE TABLE standard_adr_link (
    standard_id UUID NOT NULL REFERENCES standard (id) ON DELETE CASCADE,
    adr_id UUID NOT NULL REFERENCES adr (id) ON DELETE CASCADE,
    PRIMARY KEY (standard_id, adr_id)
);

-- Landscape entries where the standard is applied (adoption visibility).
CREATE TABLE standard_application_link (
    standard_id UUID NOT NULL REFERENCES standard (id) ON DELETE CASCADE,
    application_id UUID NOT NULL REFERENCES application_entry (id) ON DELETE CASCADE,
    PRIMARY KEY (standard_id, application_id)
);

-- ---------------------------------------------------------------------------
-- §3.5 C4 model viewer — textual C4 per landscape entry, versioned append-only
-- (a new row per revision; no update or delete), rendered client-side.
-- ---------------------------------------------------------------------------
CREATE TABLE c4_diagram (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES application_entry (id) ON DELETE CASCADE,
    version INT NOT NULL,
    label VARCHAR(200) NOT NULL,
    source TEXT NOT NULL,
    author VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (application_id, version)
);

CREATE INDEX c4_diagram_app_idx ON c4_diagram (application_id);

-- ---------------------------------------------------------------------------
-- §6 item 6 — fitness-function / contract-testing status integration. The
-- platform tracks that a check exists and its pass/fail history (status changes
-- land in the audit trail); it does not reimplement ArchUnit, dependency-cruiser
-- or Pact.
-- ---------------------------------------------------------------------------
CREATE TABLE check_reference (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES application_entry (id) ON DELETE CASCADE,
    name VARCHAR(300) NOT NULL,
    check_type VARCHAR(30) NOT NULL,
    tool VARCHAR(100) NOT NULL,
    link TEXT,
    description TEXT,
    last_status VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    last_run_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX check_reference_app_idx ON check_reference (application_id);

-- ---------------------------------------------------------------------------
-- §3.2 option (a) — ADR repository indexing: a stable per-file identity so that
-- re-imports are idempotent updates of the same read-only record, never duplicates.
-- ---------------------------------------------------------------------------
ALTER TABLE adr ADD COLUMN source_path TEXT;

CREATE UNIQUE INDEX adr_repo_source_idx ON adr (source_repo_url, source_path)
    WHERE source = 'REPOSITORY';

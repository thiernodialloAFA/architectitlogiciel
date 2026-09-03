-- Architecture Governance Platform — v1 schema
-- Modules: Application Landscape, Risk Register, ADRs, append-only audit log.

CREATE TABLE application_entry (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    owner_team VARCHAR(200) NOT NULL,
    owner_department VARCHAR(200) NOT NULL,
    lifecycle_status VARCHAR(30) NOT NULL,
    c4_model_link TEXT,
    adr_log_link TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE application_dependency (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES application_entry (id) ON DELETE CASCADE,
    depends_on_id UUID NOT NULL REFERENCES application_entry (id) ON DELETE CASCADE,
    integration_type VARCHAR(30) NOT NULL,
    description TEXT,
    UNIQUE (application_id, depends_on_id, integration_type)
);

CREATE INDEX application_dependency_app_idx ON application_dependency (application_id);
CREATE INDEX application_dependency_dep_idx ON application_dependency (depends_on_id);

CREATE TABLE risk_entry (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES application_entry (id),
    title VARCHAR(300) NOT NULL,
    category VARCHAR(40) NOT NULL,
    description TEXT NOT NULL,
    current_controls TEXT,
    inherent_impact VARCHAR(10) NOT NULL,
    inherent_likelihood VARCHAR(10) NOT NULL,
    residual_impact VARCHAR(10) NOT NULL,
    residual_likelihood VARCHAR(10) NOT NULL,
    blast_radius TEXT,
    treatment_decision VARCHAR(20) NOT NULL,
    cost_to_fix VARCHAR(100),
    target_date DATE,
    status VARCHAR(20) NOT NULL,
    risk_owner VARCHAR(200) NOT NULL,
    evidence_link TEXT,
    review_cadence_days INT NOT NULL DEFAULT 90,
    last_assessed_at DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX risk_entry_application_idx ON risk_entry (application_id);
CREATE INDEX risk_entry_category_idx ON risk_entry (category);
CREATE INDEX risk_entry_status_idx ON risk_entry (status);

CREATE SEQUENCE adr_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE adr (
    id UUID PRIMARY KEY,
    adr_number INT NOT NULL UNIQUE,
    title VARCHAR(300) NOT NULL,
    status VARCHAR(20) NOT NULL,
    context TEXT NOT NULL,
    decision TEXT NOT NULL,
    consequences TEXT,
    alternatives_considered TEXT,
    author VARCHAR(200) NOT NULL,
    department VARCHAR(200),
    tags TEXT,
    ai_related BOOLEAN NOT NULL DEFAULT FALSE,
    source VARCHAR(20) NOT NULL DEFAULT 'PLATFORM',
    source_repo_url TEXT,
    supersedes_id UUID REFERENCES adr (id),
    superseded_by_id UUID REFERENCES adr (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Native PostgreSQL full-text search (proposal §4): no separate search engine.
    search_vector TSVECTOR GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(context, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(decision, '')), 'B') ||
        setweight(to_tsvector('english',
            coalesce(consequences, '') || ' ' ||
            coalesce(alternatives_considered, '') || ' ' ||
            coalesce(tags, '')), 'C')
    ) STORED
);

CREATE INDEX adr_search_idx ON adr USING GIN (search_vector);
CREATE INDEX adr_status_idx ON adr (status);

CREATE TABLE adr_risk_link (
    adr_id UUID NOT NULL REFERENCES adr (id) ON DELETE CASCADE,
    risk_entry_id UUID NOT NULL REFERENCES risk_entry (id) ON DELETE CASCADE,
    PRIMARY KEY (adr_id, risk_entry_id)
);

-- Append-only audit trail (proposal §5): state changes are recorded as immutable
-- events in the same transaction as the change; the application exposes no update
-- or delete operation for this table.
CREATE TABLE audit_event (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(30) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(30) NOT NULL,
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    actor VARCHAR(200) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX audit_event_entity_idx ON audit_event (entity_type, entity_id);
CREATE INDEX audit_event_occurred_idx ON audit_event (occurred_at DESC);

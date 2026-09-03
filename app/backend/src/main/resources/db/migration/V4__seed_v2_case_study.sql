-- Seeded v2 demo data (proposal §5: invented, no production data) extending the
-- hospitality case study with the Advice Forum, Standards library, C4 diagrams,
-- check references, and one repository-indexed ADR.

-- ---------------------------------------------------------------------------
-- Repository-indexed ADR (§3.2 option a): the canonical record lives in the
-- owning team's repository; this read-only copy exists for cross-department
-- search and linking. Re-imports update this row via (source_repo_url, source_path).
-- ---------------------------------------------------------------------------
INSERT INTO adr (id, adr_number, title, status, context, decision, consequences, alternatives_considered,
                 author, department, tags, ai_related, source, source_repo_url, source_path)
VALUES ('44444444-4444-4444-4444-444444444406', nextval('adr_number_seq'),
        'Adopt an acquirer-agnostic tokenisation vault', 'ACCEPTED',
        'The payment orchestration service must exit acquirer-specific tokenisation to unblock the SDK upgrade flagged by PCI DSS 4.0 attestation.',
        'Introduce an acquirer-agnostic network-token vault behind our own tokenisation API; acquirer SDKs consume vault tokens only.',
        'Acquirer switch-over becomes a routing change; the vault becomes the PCI scope boundary and needs its own attestation.',
        'Per-acquirer tokenisation (rejected: lock-in, blocks SDK upgrades); outsourcing tokenisation to a single PSP (rejected: same lock-in, higher fees).',
        'Payments Platform Team', 'Finance Technology', 'payments,pci,tokenisation', FALSE,
        'REPOSITORY', 'https://github.com/pvg-demo/payment-orchestration', 'docs/adr/0007-tokenisation-vault.md');

-- ---------------------------------------------------------------------------
-- Architecture Advice Forum (§3.3): one held session with captured outcomes,
-- one planned session with an agenda, and a single (deliberately rare)
-- group-scope arbitration.
-- ---------------------------------------------------------------------------
INSERT INTO forum_session (id, session_date, title, status, notes) VALUES
    ('55555555-5555-5555-5555-555555555501', CURRENT_DATE - 21,
     'Architecture Advice Forum — last month', 'HELD',
     'Well attended; PMS replacement advice dominated. Arbitration invoked once (payment orchestration mandate) — first time this year.'),
    ('55555555-5555-5555-5555-555555555502', CURRENT_DATE + 9,
     'Architecture Advice Forum — next month', 'PLANNED',
     NULL);

INSERT INTO aaf_proposal (id, title, summary, scope, status, submitted_by, department,
                          adr_id, application_id, session_id,
                          advice_given, advised_by, final_decision, decided_at) VALUES
    ('66666666-6666-6666-6666-666666666601',
     'Replace CRS-PMS nightly batch with event-driven reservation sync',
     'Seeking advice on the reservation-events stream design (ADR-2 draft): parallel-run duration, idempotency keys, and PMS adapter ownership before committing budget.',
     'DOMAIN', 'DECIDED', 'Integration Architecture Lead', 'Distribution & E-commerce',
     '44444444-4444-4444-4444-444444444402', '11111111-1111-1111-1111-111111111101',
     '55555555-5555-5555-5555-555555555501',
     'Run the parallel phase for a full seasonal peak (6 months minimum); derive idempotency keys from CRS reservation version numbers, not timestamps; resort operations must co-own the adapter.',
     'Group IT/Software Architect; Head of Resort Operations IT; Payments Platform Owner',
     'Proceed as advised — ADR-2 updated with the 6-month parallel run and versioned idempotency keys; budget request submitted.',
     now() - interval '20 days'),
    ('66666666-6666-6666-6666-666666666602',
     'Mandate a single payment orchestration platform across all brands',
     'Two brands run their own PSP integrations outside the group payment orchestration service, fragmenting PCI scope. Proposal: mandate migration onto the shared platform.',
     'GROUP', 'DECIDED', 'Payments Platform Owner', 'Finance Technology',
     NULL, '11111111-1111-1111-1111-111111111105',
     '55555555-5555-5555-5555-555555555501',
     'Advice was split: brand CTOs accepted the principle but disputed the migration timeline; no consensus reached at the forum.',
     'Group IT/Software Architect; brand CTOs; Group CISO',
     'Escalated to group-scope arbitration (see arbitration record) — mandate upheld with an extended 24-month migration window.',
     now() - interval '18 days'),
    ('66666666-6666-6666-6666-666666666603',
     'Guardrail gate for AI Guest Concierge general availability',
     'Before the concierge pilot (ADR-4) goes GA, agree the guardrail evaluation thresholds and red-team exit criteria that gate the launch.',
     'DOMAIN', 'SCHEDULED', 'AI Architecture Advisor', 'Digital Innovation',
     '44444444-4444-4444-4444-444444444404', '11111111-1111-1111-1111-111111111107',
     '55555555-5555-5555-5555-555555555502',
     NULL, NULL, NULL, NULL),
    ('66666666-6666-6666-6666-666666666604',
     'Adaptive rate limiting for OTA channel connectors',
     'OTA rate-limit changes keep breaking channel sync (risk register). Seeking advice on an adaptive rate-limiting approach before writing the ADR.',
     'TEAM', 'SUBMITTED', 'Channel Integrations Lead', 'Distribution & E-commerce',
     NULL, '11111111-1111-1111-1111-111111111102', NULL,
     NULL, NULL, NULL, NULL);

INSERT INTO arbitration_record (id, proposal_id, requested_by, rationale, outcome, decided_by, occurred_at) VALUES
    ('77777777-7777-7777-7777-777777777701', '66666666-6666-6666-6666-666666666602',
     'Payments Platform Owner',
     'Group-scope decision with unresolved disagreement between brand CTOs on migration timeline; PCI scope fragmentation is a group-level compliance exposure that cannot stay open.',
     'Mandate upheld: all brands migrate to the shared payment orchestration platform; migration window extended from 12 to 24 months; brand-specific acquirer contracts honoured until expiry.',
     'Group CTO', now() - interval '18 days');

-- ---------------------------------------------------------------------------
-- Standards & reference patterns library (§3.4), linked to the ADRs that
-- established them and the landscape entries where they are applied.
-- ---------------------------------------------------------------------------
INSERT INTO standard (id, title, category, status, version, content, owner) VALUES
    ('88888888-8888-8888-8888-888888888801',
     'Event-driven integration standard', 'INTEGRATION', 'ACTIVE', 2,
     E'# Event-driven integration standard\n\nCross-domain data flows use domain events on the group event backbone, not point-to-point database access or file drops.\n\n- Events are versioned, schema-registered, and carry an idempotency key derived from the source aggregate version.\n- Consumers must be replay-safe; producers own schema compatibility (backward within a major version).\n- Batch files are permitted only as a reconciliation fallback during migration parallel runs.',
     'Group IT/Software Architect'),
    ('88888888-8888-8888-8888-888888888802',
     'REST API conventions', 'API_CONVENTION', 'ACTIVE', 1,
     E'# REST API conventions\n\nEvery cross-domain REST API publishes an OpenAPI 3 contract as the source of truth, versioned in the provider''s repository.\n\n- Breaking changes require a new major version and a published deprecation window.\n- Provider CI runs contract-compatibility checks on every change.\n- Errors use RFC 7807 problem+json; pagination, filtering and naming follow the group profile in this document.',
     'Group IT/Software Architect'),
    ('88888888-8888-8888-8888-888888888803',
     'AI integration pattern: RAG with guardrails', 'AI_PATTERN', 'ACTIVE', 1,
     E'# AI integration pattern: RAG with guardrails\n\nLLM features ground responses via retrieval-augmented generation over approved content and per-user context fetched at request time under that user''s authorisation.\n\n- No fine-tuning on personal data.\n- Input/output guardrails aligned to the OWASP LLM Top 10 (prompt injection, insecure output handling, sensitive data disclosure) are mandatory before GA.\n- A red-team evaluation with documented exit criteria gates general availability.',
     'AI Architecture Advisor'),
    ('88888888-8888-8888-8888-888888888804',
     'Analytics data masking standard', 'DATA', 'DRAFT', 1,
     E'# Analytics data masking standard (draft)\n\nGuest PII landing in the lakehouse bronze layer must be masked or tokenised at ingestion; column-level access policies apply from bronze upward.\n\n- Direct identifiers are tokenised; quasi-identifiers are generalised per the group data-classification matrix.\n- Re-identification keys live outside the analytics platform.\n- Access reviews run quarterly (draft: pending Data & Analytics sign-off).',
     'Data Platform Product Owner');

INSERT INTO standard_adr_link (standard_id, adr_id) VALUES
    ('88888888-8888-8888-8888-888888888801', '44444444-4444-4444-4444-444444444402'),
    ('88888888-8888-8888-8888-888888888801', '44444444-4444-4444-4444-444444444401'),
    ('88888888-8888-8888-8888-888888888802', '44444444-4444-4444-4444-444444444403'),
    ('88888888-8888-8888-8888-888888888803', '44444444-4444-4444-4444-444444444404');

INSERT INTO standard_application_link (standard_id, application_id) VALUES
    ('88888888-8888-8888-8888-888888888801', '11111111-1111-1111-1111-111111111101'),
    ('88888888-8888-8888-8888-888888888801', '11111111-1111-1111-1111-111111111104'),
    ('88888888-8888-8888-8888-888888888802', '11111111-1111-1111-1111-111111111101'),
    ('88888888-8888-8888-8888-888888888802', '11111111-1111-1111-1111-111111111102'),
    ('88888888-8888-8888-8888-888888888802', '11111111-1111-1111-1111-111111111105'),
    ('88888888-8888-8888-8888-888888888803', '11111111-1111-1111-1111-111111111107'),
    ('88888888-8888-8888-8888-888888888804', '11111111-1111-1111-1111-111111111106');

-- Version history for the event standard (v1 → v2) recorded the same way the
-- application records content changes: as an append-only audit event.
INSERT INTO audit_event (entity_type, entity_id, action, field_name, old_value, new_value, actor)
VALUES ('STANDARD', '88888888-8888-8888-8888-888888888801', 'FIELD_CHANGED', 'content',
        'v1 draft: events preferred over point-to-point integration (no idempotency or replay requirements yet).',
        'v2: adds mandatory idempotency keys, replay-safe consumers, and the batch-as-fallback rule.',
        'seed-migration');

-- ---------------------------------------------------------------------------
-- C4 diagrams (§3.5): textual Mermaid C4, versioned append-only per application.
-- ---------------------------------------------------------------------------
INSERT INTO c4_diagram (id, application_id, version, label, source, author) VALUES
    ('99999999-9999-9999-9999-999999999901', '11111111-1111-1111-1111-111111111101', 1,
     'System context — CRS (before event sync)',
     E'C4Context\n    title System context — Central Reservation System (v1)\n    Person(guest, "Guest", "Books stays across brands")\n    System(crs, "Central Reservation System", "Availability, rates and reservations")\n    System(cmg, "Channel Manager Gateway", "OTA synchronisation")\n    System_Ext(ota, "OTA channels", "Booking platforms and tour operators")\n    System(pms, "Property Management System", "Front-desk operations")\n    System(pay, "Payment Orchestration", "Deposits, guarantees, refunds")\n    Rel(guest, crs, "Books via web and app")\n    Rel(cmg, crs, "Pushes OTA bookings, pulls availability", "REST")\n    Rel(ota, cmg, "Channel updates")\n    Rel(crs, pms, "Nightly reservation file", "SFTP batch")\n    Rel(crs, pay, "Payment operations", "REST")',
     'seed-migration'),
    ('99999999-9999-9999-9999-999999999902', '11111111-1111-1111-1111-111111111101', 2,
     'System context — CRS (target: event-driven PMS sync)',
     E'C4Context\n    title System context — Central Reservation System (v2 target)\n    Person(guest, "Guest", "Books stays across brands")\n    System(crs, "Central Reservation System", "Availability, rates and reservations")\n    System(cmg, "Channel Manager Gateway", "OTA synchronisation")\n    System_Ext(ota, "OTA channels", "Booking platforms and tour operators")\n    System(events, "Reservation events stream", "Booking created/modified/cancelled")\n    System(pms, "Property Management System", "Front-desk operations (via adapter)")\n    System(pay, "Payment Orchestration", "Deposits, guarantees, refunds")\n    Rel(guest, crs, "Books via web and app")\n    Rel(cmg, crs, "Pushes OTA bookings, pulls availability", "REST")\n    Rel(ota, cmg, "Channel updates")\n    Rel(crs, events, "Publishes reservation events")\n    Rel(events, pms, "Consumed by PMS adapter")\n    Rel(crs, pms, "Nightly file — reconciliation fallback only", "SFTP batch")\n    Rel(crs, pay, "Payment operations", "REST")',
     'seed-migration'),
    ('99999999-9999-9999-9999-999999999903', '11111111-1111-1111-1111-111111111107', 1,
     'Container view — AI Guest Concierge (RAG with guardrails)',
     E'C4Container\n    title Container view — AI Guest Concierge (pilot)\n    Person(guest, "Guest", "Asks pre-arrival and on-site questions")\n    System_Boundary(concierge, "AI Guest Concierge") {\n        Container(chat, "Chat frontend", "React", "Guest conversation UI")\n        Container(orchestrator, "Concierge orchestrator", "Spring Boot", "Session handling, guardrails, prompt assembly")\n        Container(retrieval, "Retrieval service", "Python", "RAG over approved resort content")\n        ContainerDb(vector, "Vector index", "pgvector", "Approved content embeddings")\n    }\n    System_Ext(llm, "LLM provider", "Hosted model API")\n    System(crs, "Central Reservation System", "Reservation context")\n    System(loyalty, "Loyalty & CRM Platform", "Guest profile and tier")\n    Rel(guest, chat, "Chats with")\n    Rel(chat, orchestrator, "Messages", "HTTPS")\n    Rel(orchestrator, retrieval, "Grounding queries")\n    Rel(retrieval, vector, "Similarity search")\n    Rel(orchestrator, llm, "Guarded prompts / responses", "HTTPS")\n    Rel(orchestrator, crs, "Reservation context (guest-authorised)", "REST")\n    Rel(orchestrator, loyalty, "Profile and tier (guest-authorised)", "REST")',
     'seed-migration');

-- ---------------------------------------------------------------------------
-- Fitness-function / contract-test check references (§6 item 6). Statuses are
-- pushed by CI via POST /api/checks/{id}/status; history lands in the audit trail.
-- ---------------------------------------------------------------------------
INSERT INTO check_reference (id, application_id, name, check_type, tool, link, description, last_status, last_run_at) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa01', '11111111-1111-1111-1111-111111111101',
     'Booking-engine layering fitness function', 'FITNESS_FUNCTION', 'ArchUnit',
     'https://github.com/pvg-demo/crs/actions/workflows/ci.yml',
     'Fails the CRS build if web → service → repository layering is violated.',
     'PASSING', now() - interval '1 day'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa02', '11111111-1111-1111-1111-111111111101',
     'Reservation-events provider contract verification', 'CONTRACT_TEST', 'Pact',
     'https://pact-broker.pvg-demo.example/pacticipants/crs',
     'Verifies the CRS reservation-events contract against the PMS adapter consumer pact.',
     'FAILING', now() - interval '2 hours'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa03', '11111111-1111-1111-1111-111111111102',
     'Connector module-boundary check', 'FITNESS_FUNCTION', 'dependency-cruiser',
     'https://github.com/pvg-demo/channel-gateway/actions/workflows/ci.yml',
     'Blocks cross-connector imports so one OTA connector cannot couple to another.',
     'PASSING', now() - interval '3 days'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa04', '11111111-1111-1111-1111-111111111105',
     'Acquirer routing consumer contracts', 'CONTRACT_TEST', 'Pact',
     'https://pact-broker.pvg-demo.example/pacticipants/payment-orchestration',
     'Consumer-driven contracts for every acquirer adapter behind the orchestration API.',
     'PASSING', now() - interval '1 day'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa05', '11111111-1111-1111-1111-111111111107',
     'LLM guardrail evaluation suite', 'FITNESS_FUNCTION', 'promptfoo',
     NULL,
     'Prompt-injection and data-leakage evaluation set; must pass before GA (see risk register).',
     'UNKNOWN', NULL);

-- ---------------------------------------------------------------------------
-- Audit trail for the seeded v2 records (actor: seed migration)
-- ---------------------------------------------------------------------------
INSERT INTO audit_event (entity_type, entity_id, action, field_name, old_value, new_value, actor)
SELECT 'FORUM_SESSION', id, 'CREATED', NULL, NULL, title, 'seed-migration' FROM forum_session;

INSERT INTO audit_event (entity_type, entity_id, action, field_name, old_value, new_value, actor)
SELECT 'AAF_PROPOSAL', id, 'CREATED', NULL, NULL, title, 'seed-migration' FROM aaf_proposal;

INSERT INTO audit_event (entity_type, entity_id, action, field_name, old_value, new_value, actor)
SELECT 'ARBITRATION', id, 'CREATED', NULL, NULL, 'Arbitration: ' || outcome, 'seed-migration' FROM arbitration_record;

INSERT INTO audit_event (entity_type, entity_id, action, field_name, old_value, new_value, actor)
SELECT 'STANDARD', id, 'CREATED', NULL, NULL, title, 'seed-migration' FROM standard;

INSERT INTO audit_event (entity_type, entity_id, action, field_name, old_value, new_value, actor)
SELECT 'C4_DIAGRAM', id, 'CREATED', NULL, NULL, label || ' (v' || version || ')', 'seed-migration' FROM c4_diagram;

INSERT INTO audit_event (entity_type, entity_id, action, field_name, old_value, new_value, actor)
SELECT 'CHECK_REFERENCE', id, 'CREATED', NULL, NULL, name, 'seed-migration' FROM check_reference;

INSERT INTO audit_event (entity_type, entity_id, action, field_name, old_value, new_value, actor)
SELECT 'ADR', id, 'CREATED', NULL, NULL, title, 'seed-migration' FROM adr WHERE source = 'REPOSITORY';

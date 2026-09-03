-- Seeded demo case study (proposal §5: "No production data in v1" — all data below is
-- invented). A coherent hospitality booking/loyalty landscape (proposal §6.3) rather
-- than disconnected placeholder rows.

-- ---------------------------------------------------------------------------
-- Application landscape
-- ---------------------------------------------------------------------------
INSERT INTO application_entry (id, name, description, owner_team, owner_department, lifecycle_status, c4_model_link, adr_log_link) VALUES
    ('11111111-1111-1111-1111-111111111101', 'Central Reservation System (CRS)',
     'Core booking engine: availability, rates, and reservations for all resorts and residences. The hub most other systems integrate with.',
     'Core Booking', 'Distribution & E-commerce', 'ACTIVE', NULL, NULL),
    ('11111111-1111-1111-1111-111111111102', 'Channel Manager Gateway',
     'Synchronises availability/rates/inventory with OTAs and tour-operator channels.',
     'Channel Integrations', 'Distribution & E-commerce', 'ACTIVE', NULL, NULL),
    ('11111111-1111-1111-1111-111111111103', 'Property Management System (PMS)',
     'Legacy on-premise PMS used by resort front desks for check-in/out, room assignment and folio management. Vendor has announced end of support.',
     'Resort Operations IT', 'Operations', 'ACTIVE', NULL, NULL),
    ('11111111-1111-1111-1111-111111111104', 'Loyalty & CRM Platform',
     'Guest profiles, loyalty points accrual/redemption, and campaign segmentation.',
     'Guest Experience', 'Marketing & CRM', 'ACTIVE', NULL, NULL),
    ('11111111-1111-1111-1111-111111111105', 'Payment Orchestration Service',
     'Routes card and alternative payments across acquirers; tokenisation and PCI DSS scope boundary.',
     'Payments', 'Finance Technology', 'ACTIVE', NULL, NULL),
    ('11111111-1111-1111-1111-111111111106', 'Data Platform (Lakehouse)',
     'Central analytics platform ingesting bookings, stays, and loyalty events for BI and data science.',
     'Data Engineering', 'Data & Analytics', 'ACTIVE', NULL, NULL),
    ('11111111-1111-1111-1111-111111111107', 'AI Guest Concierge (LLM pilot)',
     'Pilot LLM-powered assistant answering guest questions pre-arrival and on-site, grounded on resort and reservation data (RAG).',
     'Innovation Lab', 'Digital Innovation', 'PILOT', NULL, NULL);

-- ---------------------------------------------------------------------------
-- Cross-domain dependencies (integration edges)
-- ---------------------------------------------------------------------------
INSERT INTO application_dependency (id, application_id, depends_on_id, integration_type, description) VALUES
    ('22222222-2222-2222-2222-222222222201', '11111111-1111-1111-1111-111111111102', '11111111-1111-1111-1111-111111111101', 'REST_API',
     'Channel manager pushes OTA bookings into the CRS and pulls availability/rates.'),
    ('22222222-2222-2222-2222-222222222202', '11111111-1111-1111-1111-111111111101', '11111111-1111-1111-1111-111111111103', 'BATCH_FILE',
     'Nightly flat-file export of reservations from CRS to the on-prem PMS (SFTP, fixed-width format).'),
    ('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111101', '11111111-1111-1111-1111-111111111105', 'REST_API',
     'CRS calls payment orchestration for deposits, guarantees and refunds.'),
    ('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111104', '11111111-1111-1111-1111-111111111101', 'EVENTS',
     'Loyalty platform consumes booking-confirmed / stay-completed events for points accrual.'),
    ('22222222-2222-2222-2222-222222222205', '11111111-1111-1111-1111-111111111106', '11111111-1111-1111-1111-111111111101', 'BATCH_FILE',
     'Daily ingestion of reservation data into the lakehouse.'),
    ('22222222-2222-2222-2222-222222222206', '11111111-1111-1111-1111-111111111106', '11111111-1111-1111-1111-111111111104', 'BATCH_FILE',
     'Daily ingestion of loyalty/CRM events into the lakehouse.'),
    ('22222222-2222-2222-2222-222222222207', '11111111-1111-1111-1111-111111111107', '11111111-1111-1111-1111-111111111101', 'REST_API',
     'Concierge retrieves reservation context for grounding answers.'),
    ('22222222-2222-2222-2222-222222222208', '11111111-1111-1111-1111-111111111107', '11111111-1111-1111-1111-111111111104', 'REST_API',
     'Concierge reads guest profile/loyalty tier to personalise responses.');

-- ---------------------------------------------------------------------------
-- Risk register (covers all five job-description categories; one entry is
-- deliberately stale to demonstrate staleness tracking)
-- ---------------------------------------------------------------------------
INSERT INTO risk_entry (id, application_id, title, category, description, current_controls,
                        inherent_impact, inherent_likelihood, residual_impact, residual_likelihood,
                        blast_radius, treatment_decision, cost_to_fix, target_date, status,
                        risk_owner, evidence_link, review_cadence_days, last_assessed_at) VALUES
    ('33333333-3333-3333-3333-333333333301', '11111111-1111-1111-1111-111111111103',
     'PMS vendor end-of-support in 18 months', 'END_OF_LIFE',
     'Vendor has announced end of extended support. After that date: no security patches, no regulatory updates (fiscal reporting), rising insurance and compliance exposure.',
     'Extended-support contract signed; quarterly vendor patch review.',
     'HIGH', 'HIGH', 'HIGH', 'MEDIUM',
     'All resort front-desk operations; CRS nightly sync; folio/fiscal reporting.',
     'MIGRATE', 'EUR 1.5M-2.5M (programme estimate)', '2027-12-31', 'IN_PROGRESS',
     'Head of Resort Operations IT', NULL, 90, CURRENT_DATE - 20),
    ('33333333-3333-3333-3333-333333333302', '11111111-1111-1111-1111-111111111101',
     'CRS-PMS nightly batch is a single point of failure', 'INTEGRATION_BRITTLENESS',
     'Reservations reach resorts via one nightly fixed-width file over SFTP. A failed or partial file means front desks work from stale data for up to 24h; recovery is manual re-run with no idempotency guarantees.',
     'File-arrival monitoring alert; documented manual re-run procedure.',
     'HIGH', 'MEDIUM', 'HIGH', 'MEDIUM',
     'Check-in experience at all properties; overbooking exposure on re-run errors.',
     'INVEST', 'EUR 200k-350k (event-driven replacement, see ADR-2)', '2026-09-30', 'OPEN',
     'Integration Architecture Lead', NULL, 60, CURRENT_DATE - 10),
    ('33333333-3333-3333-3333-333333333303', '11111111-1111-1111-1111-111111111105',
     'Acquirer SDK pinned two major versions behind', 'SECURITY_COMPLIANCE',
     'Payment orchestration embeds an acquirer SDK two major versions behind the supported line; upcoming PCI DSS 4.0 attestation flags it. Upgrade blocked by a breaking API change in tokenisation flow.',
     'Compensating controls documented for last attestation; WAF rules in front of the service.',
     'HIGH', 'MEDIUM', 'HIGH', 'LOW',
     'Card payments group-wide if attestation fails; fines and acquirer relationship.',
     'INVEST', 'EUR 90k (upgrade + regression pack)', '2026-06-30', 'IN_PROGRESS',
     'Payments Platform Owner', NULL, 90, CURRENT_DATE - 30),
    ('33333333-3333-3333-3333-333333333304', '11111111-1111-1111-1111-111111111104',
     'Loyalty monolith change lead time blocks campaigns', 'TECHNICAL_DEBT',
     'Loyalty & CRM is a 10-year-old monolith with entangled campaign, points and profile modules; average change lead time is 6 weeks, and marketing-critical campaign changes routinely miss seasonal windows.',
     'Change advisory board triage; feature-freeze windows before peak season.',
     'MEDIUM', 'HIGH', 'MEDIUM', 'HIGH',
     'Marketing campaign agility; guest-facing loyalty features across brands.',
     'INVEST', 'EUR 400k first tranche (strangler extraction of campaign module)', '2027-03-31', 'OPEN',
     'Guest Experience Engineering Manager', NULL, 90, CURRENT_DATE - 5),
    ('33333333-3333-3333-3333-333333333305', '11111111-1111-1111-1111-111111111107',
     'LLM concierge lacks a security baseline (prompt injection / data leakage)', 'AI_READINESS',
     'Pilot answers are grounded on reservation and profile data, but there is no evaluated guardrail set against prompt injection, insecure output handling, or PII leakage per the OWASP LLM Top 10. No red-team exercise has been run.',
     'Pilot restricted to invited guests; system prompt forbids revealing other guests'' data (untested).',
     'MEDIUM', 'MEDIUM', 'MEDIUM', 'MEDIUM',
     'Guest PII confidentiality; brand reputation; pilot go/no-go decision.',
     'INVEST', 'EUR 40k (guardrail evaluation + red-team exercise)', '2026-04-30', 'OPEN',
     'AI Architecture Advisor', NULL, 45, CURRENT_DATE - 12),
    ('33333333-3333-3333-3333-333333333306', '11111111-1111-1111-1111-111111111106',
     'Guest PII replicated to lakehouse without masking', 'SECURITY_COMPLIANCE',
     'Raw reservation and loyalty extracts land in the bronze layer with unmasked guest PII, readable by all analytics engineers. GDPR data-minimisation and access reviews are not demonstrably enforced.',
     'Access restricted to the analytics AD group; annual access review.',
     'HIGH', 'MEDIUM', 'MEDIUM', 'MEDIUM',
     'GDPR exposure across all brands; data-platform trust for future AI use cases.',
     'INVEST', 'EUR 120k (masking + column-level policies in bronze/silver)', '2026-10-31', 'OPEN',
     'Data Platform Product Owner', NULL, 90, CURRENT_DATE - 200),
    ('33333333-3333-3333-3333-333333333307', '11111111-1111-1111-1111-111111111102',
     'OTA rate-limit changes break channel sync', 'INTEGRATION_BRITTLENESS',
     'Two OTAs changed rate-limit policies this year, each time causing hours of failed availability updates because limits are hard-coded per channel.',
     'Per-channel retry with backoff; on-call runbook.',
     'MEDIUM', 'MEDIUM', 'LOW', 'MEDIUM',
     'Availability accuracy on affected OTA channels (revenue leakage, overbooking).',
     'TOLERATE', 'EUR 30k (adaptive rate-limiting) — deferred', NULL, 'OPEN',
     'Channel Integrations Lead', NULL, 120, CURRENT_DATE - 40),
    ('33333333-3333-3333-3333-333333333308', '11111111-1111-1111-1111-111111111101',
     'CRS booking engine still runs on Java 8', 'TECHNICAL_DEBT',
     'Core booking engine runs on Java 8 (public updates ended); several critical libraries are frozen on final-compatible versions with known CVEs mitigated only by network segmentation.',
     'Network segmentation; virtual-patching rules on the API gateway.',
     'HIGH', 'MEDIUM', 'MEDIUM', 'MEDIUM',
     'Booking flow for all channels; blocks adoption of newer integration libraries.',
     'MIGRATE', 'EUR 250k (JDK 21 migration + dependency uplift)', '2026-12-31', 'OPEN',
     'Core Booking Tech Lead', NULL, 90, CURRENT_DATE - 15);

-- ---------------------------------------------------------------------------
-- ADRs (case law), including a supersedes chain and an AI-related decision
-- ---------------------------------------------------------------------------
INSERT INTO adr (id, adr_number, title, status, context, decision, consequences, alternatives_considered,
                 author, department, tags, ai_related, source, source_repo_url, supersedes_id, superseded_by_id) VALUES
    ('44444444-4444-4444-4444-444444444401', nextval('adr_number_seq'),
     'Adopt the advice process and a monthly Architecture Advice Forum', 'ACCEPTED',
     'Architecture decisions were bottlenecked on a central review board with 3-4 week queues, pushing teams to bypass governance entirely.',
     'Adopt the Harmel-Law advice process: anyone may make an architectural decision after seeking advice from affected parties and experts, recorded as an ADR. A monthly Architecture Advice Forum (AAF) provides the venue; group-scope arbitration exists but is expected to be rare.',
     'Decision latency drops; accountability moves to decision-takers; the ADR log becomes case law. Requires discipline in recording advice received.',
     'Keep central review board (rejected: proven bottleneck); federated boards per department (rejected: fragments cross-domain coherence).',
     'Group IT/Software Architect', 'Group Architecture', 'governance,advice-process,aaf', FALSE, 'PLATFORM', NULL, NULL, NULL),
    ('44444444-4444-4444-4444-444444444402', nextval('adr_number_seq'),
     'Replace CRS-PMS nightly batch with event-driven reservation sync', 'PROPOSED',
     'The nightly fixed-width file transfer from CRS to the on-prem PMS is the single most fragile integration in the landscape (risk register: CRS-PMS nightly batch is a single point of failure).',
     'Introduce a reservation-events stream (booking created/modified/cancelled) consumed by a PMS adapter; the nightly file remains as reconciliation fallback during a 6-month parallel run.',
     'Near-real-time resort data; idempotent replay; requires PMS adapter development against a legacy API and a carefully-monitored parallel run.',
     'Upgrade to vendor''s two-way sync module (rejected: locks in the end-of-support PMS); more frequent batch files (rejected: shrinks but keeps the failure mode).',
     'Integration Architecture Lead', 'Distribution & E-commerce', 'integration,events,crs,pms', FALSE, 'PLATFORM', NULL, NULL, NULL),
    ('44444444-4444-4444-4444-444444444403', nextval('adr_number_seq'),
     'OpenAPI-first contracts for all cross-domain REST APIs', 'ACCEPTED',
     'Cross-domain integrations broke repeatedly because consumers discovered breaking changes at deploy time; there was no shared contract format nor compatibility rule.',
     'Every cross-domain REST API publishes an OpenAPI 3 contract as the source of truth, versioned in the provider''s repo; breaking changes require a new major version and a deprecation window; provider CI runs contract-compatibility checks.',
     'Consumers can generate clients and detect drift in CI; providers carry explicit deprecation duties.',
     'Consumer-driven contracts only (kept as complement, not replacement); shared canonical data model (rejected: historic failure mode, too rigid).',
     'Group IT/Software Architect', 'Group Architecture', 'api,openapi,contracts,standards', FALSE, 'PLATFORM', NULL,
     '44444444-4444-4444-4444-444444444405', NULL),
    ('44444444-4444-4444-4444-444444444404', nextval('adr_number_seq'),
     'AI Guest Concierge uses RAG with guardrails; no fine-tuning on guest data', 'ACCEPTED',
     'The concierge pilot must answer using reservation/profile context without training-data leakage risk, and must be governed like any other architecture decision (AI decisions flow through the same ADR process).',
     'Ground responses via retrieval-augmented generation over approved content and per-guest context fetched at request time under the guest''s own authorisation; apply input/output guardrails aligned to the OWASP LLM Top 10 (prompt injection, insecure output handling, sensitive data disclosure); prohibit fine-tuning on guest personal data.',
     'No guest data enters model weights; guardrail latency added per request; a red-team evaluation is required before general availability (tracked in the risk register).',
     'Fine-tuning a model on historic conversations (rejected: GDPR erasure and leakage risk); ungrounded base model (rejected: hallucinated answers about bookings).',
     'AI Architecture Advisor', 'Digital Innovation', 'ai,llm,rag,guardrails,owasp', TRUE, 'PLATFORM', NULL, NULL, NULL),
    ('44444444-4444-4444-4444-444444444405', nextval('adr_number_seq'),
     'Point-to-point loyalty synchronisation with CRS', 'SUPERSEDED',
     'Early loyalty integration copied booking rows directly from the CRS database on a schedule.',
     'Synchronise loyalty data via direct database reads from CRS replica (historic decision).',
     'Fast to build; created hidden schema coupling that broke on every CRS upgrade.',
     'Message-based integration (deferred at the time for cost reasons).',
     'Former Integration Lead', 'Marketing & CRM', 'loyalty,integration,legacy', FALSE, 'PLATFORM', NULL,
     NULL, '44444444-4444-4444-4444-444444444403');

INSERT INTO adr_risk_link (adr_id, risk_entry_id) VALUES
    ('44444444-4444-4444-4444-444444444402', '33333333-3333-3333-3333-333333333302'),
    ('44444444-4444-4444-4444-444444444404', '33333333-3333-3333-3333-333333333305');

-- ---------------------------------------------------------------------------
-- Audit trail for the seeded records (actor: seed migration)
-- ---------------------------------------------------------------------------
INSERT INTO audit_event (entity_type, entity_id, action, field_name, old_value, new_value, actor)
SELECT 'APPLICATION', id, 'CREATED', NULL, NULL, name, 'seed-migration' FROM application_entry;

INSERT INTO audit_event (entity_type, entity_id, action, field_name, old_value, new_value, actor)
SELECT 'RISK_ENTRY', id, 'CREATED', NULL, NULL, title, 'seed-migration' FROM risk_entry;

INSERT INTO audit_event (entity_type, entity_id, action, field_name, old_value, new_value, actor)
SELECT 'ADR', id, 'CREATED', NULL, NULL, title, 'seed-migration' FROM adr;

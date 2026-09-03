# Architecture Governance Platform (v2)

Implementation of the [Architecture Governance Platform proposal](../docs/architecture-governance-platform-proposal.md). v1 built the proposal's recommended first slice (§6) — **Application Landscape**, **Architecture Risk Register**, and **Decision Records (ADRs)** — deeply, with a shared append-only audit trail and full-text ADR search. v2 delivers the five roadmap modules: the **Architecture Advice Forum** governance workflow, the **Standards & reference patterns library**, the **C4 model viewer**, **ADR repository indexing**, and **fitness-function / contract-testing status integration**.

Ships pre-seeded with the proposal's fictional hospitality-group case study (7 applications, 8 integration dependencies, 8 risks across all five categories, 6 ADRs — including a supersedes chain, an AI-guardrails decision and a repository-imported record — plus forum sessions, proposals, an arbitration, versioned standards, C4 diagram versions and CI check references) so every screen demonstrates real governance workflows out of the box.

## Run it

```bash
cd app
docker compose up --build
```

| URL | What |
| --- | --- |
| http://localhost:8080 | The app (React SPA served by nginx, `/api` proxied same-origin) |
| http://localhost:8081/swagger-ui.html | OpenAPI / Swagger UI on the backend |
| http://localhost:8081/api/dashboard/summary | Raw API example |

## Local development

Backend (needs Java 21 and a PostgreSQL 16 on `localhost:5432` with db/user/password `governance`, e.g. `docker compose up db`):

```bash
cd app/backend
mvn spring-boot:run
```

Frontend (Vite dev server on http://localhost:5173, proxies `/api` to `localhost:8080`):

```bash
cd app/frontend
npm install
npm run dev
```

Tests and checks:

```bash
cd app/backend && mvn verify        # integration tests (Testcontainers) + ArchUnit fitness functions
cd app/frontend && npm run lint     # oxlint
cd app/frontend && npm run build    # tsc + vite production build
```

## What's inside

| Piece | Choice | Why (per proposal §3) |
| --- | --- | --- |
| Backend | Spring Boot 3.5, Java 21 | Layered monolith: `web → service → repository`, enforced by ArchUnit |
| Database | PostgreSQL 16 | Relational model + `tsvector` full-text ADR search, Flyway migrations |
| Frontend | React 19 + TypeScript + Vite | SPA with TanStack Query; no UI framework, small custom design system |
| API docs | springdoc-openapi | Swagger UI out of the box |

### Modules

- **Dashboard** — portfolio KPIs, risks-by-category, top residual risks, stale-assessment alerts, plus v2 governance signals: proposals in flight, arbitration count, active standards, failing checks.
- **Application Landscape** — system cards with owner, lifecycle status, dependency lists (typed integrations), linked open-risk counts. Each entry's detail page now renders its **C4 model** (versioned, append-only Mermaid sources rendered client-side with a pinned build treating diagram source as untrusted input) and its **fitness-function / contract-test references** with last CI-reported status.
- **Risk Register** — five categories (technical debt, end-of-life, integration brittleness, security/compliance, AI-readiness), inherent vs residual scoring, TIME treatment decisions, review-cadence staleness flags, top-10 CSV export for leadership reporting.
- **Decision Records** — ADR case law with `Proposed → Accepted → Deprecated/Superseded` lifecycle (terminal records are immutable), supersedes back-links, risk links, tags, and PostgreSQL full-text search. The **AI Architecture Register** is a filtered view over AI-related ADRs, not a separate silo.
- **Architecture Advice Forum** (v2) — anyone submits a proposal with a declared scope (`team / domain / group`); proposals are scheduled onto forum sessions, advice and the proposer's final decision are captured, and group-scope escalations get an explicit, permanently recorded **arbitration** (`POST /api/forum/proposals/{id}/arbitration`) that should stay rare.
- **Standards & reference patterns** (v2) — curated `Draft → Active → Retired` standards across integration / API convention / AI pattern / security / data categories; every content edit bumps the version with old+new content retained in the audit trail, and each standard links to the ADR case law that established it and the landscape entries applying it.
- **ADR repository indexing** (v2, source-of-truth option a) — `POST /api/adr-imports` accepts a repository URL plus Markdown documents (Nygard/MADR headings are parsed), upserts read-only ADR records keyed by `(repo URL, file path)`, and re-imports update rather than duplicate. Imported ADRs are searchable alongside platform ADRs but cannot be edited in the platform — attempts get HTTP 409.
- **Check references** (v2) — each landscape entry registers its ArchUnit / dependency-cruiser / Pact style checks; CI pushes results to `POST /api/checks/{id}/status`. The platform tracks that checks exist and pass — it does not reimplement them.
- **Audit trail** — every create/update/status change on any entity is recorded append-only (actor, field-level old/new values) and shown on detail pages; there is no delete or update API for audit events.
- **Roadmap** — the five v2 modules are now marked delivered; remaining items are production hardening (OIDC/SSO, scheduled repository pulls, live CI/Pact Broker webhooks).

### Architecture fitness functions

`LayeringArchitectureTest` (ArchUnit) fails the build if the `web → service → repository` layering is violated, if controllers touch repositories directly, or if domain code depends on Spring web classes — the same practice the proposal §6 recommends platform teams adopt.

### Security posture (demo scope)

Per proposal §4/§7 the v1 tenant model is a single internal demo: there is **no authentication**; the optional `X-Actor` request header (sanitised, capped, defaulting to `demo-user`) attributes audit entries. The compose credentials are throwaway demo values. Before any real deployment: put an identity provider in front (OIDC), replace header-based attribution with authenticated principals, and rotate real secrets in via environment/secret manager — never into git.

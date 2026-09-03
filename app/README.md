# Architecture Governance Platform (v1)

Implementation of the [Architecture Governance Platform proposal](../docs/architecture-governance-platform-proposal.md), built to the proposal's recommended v1 scope (§6): the **Application Landscape**, **Architecture Risk Register**, and **Decision Records (ADRs)** modules are built deeply — with a shared append-only audit trail and full-text ADR search — while the remaining modules are explicitly presented on the in-app **Roadmap** page instead of being shipped as shallow stubs.

Ships pre-seeded with the proposal's fictional hospitality-group case study (7 applications, 8 integration dependencies, 8 risks across all five categories, 5 ADRs including a supersedes chain and an AI-guardrails decision) so every screen demonstrates real governance workflows out of the box.

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

- **Dashboard** — portfolio KPIs, risks-by-category, top residual risks, stale-assessment alerts.
- **Application Landscape** — system cards with owner, lifecycle status, dependency lists (typed integrations), linked open-risk counts.
- **Risk Register** — five categories (technical debt, end-of-life, integration brittleness, security/compliance, AI-readiness), inherent vs residual scoring, TIME treatment decisions, review-cadence staleness flags, top-10 CSV export for leadership reporting.
- **Decision Records** — ADR case law with `Proposed → Accepted → Deprecated/Superseded` lifecycle (terminal records are immutable), supersedes back-links, risk links, tags, and PostgreSQL full-text search. The **AI Architecture Register** is a filtered view over AI-related ADRs, not a separate silo.
- **Audit trail** — every create/update/status change on any entity is recorded append-only (actor, field-level old/new values) and shown on detail pages; there is no delete or update API for audit events.
- **Roadmap** — Advice Forum workflow, standards library, C4 viewer, ADR repo indexing, and fitness-function status integration, labelled as future work per §6.

### Architecture fitness functions

`LayeringArchitectureTest` (ArchUnit) fails the build if the `web → service → repository` layering is violated, if controllers touch repositories directly, or if domain code depends on Spring web classes — the same practice the proposal §6 recommends platform teams adopt.

### Security posture (demo scope)

Per proposal §4/§7 the v1 tenant model is a single internal demo: there is **no authentication**; the optional `X-Actor` request header (sanitised, capped, defaulting to `demo-user`) attributes audit entries. The compose credentials are throwaway demo values. Before any real deployment: put an identity provider in front (OIDC), replace header-based attribution with authenticated principals, and rotate real secrets in via environment/secret manager — never into git.

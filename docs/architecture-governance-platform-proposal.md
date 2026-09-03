# Proposal — Architecture Governance Platform (for your validation before any code is written)

> **Status: PROPOSAL ONLY.** Per your request ("feel free to propose the plan and let me
> validate before implementation"), nothing in this document has been built yet. This is
> the plan to review, adjust, and approve — once you confirm scope, I'll implement in the
> incremental order described below.

## 1. Why this shape

The job description asks for a role that operates across four pillars:

1. **Architecture governance** — the advice process, the Architecture Advice Forum, ADR
   case law.
2. **Application architecture & cross-domain coherence** — curated standards, reference
   patterns, integration coherence.
3. **Application landscape stocktake & risk prioritisation** — the "foundational, first-
   year" body of work, living as a risk register feeding the roadmap/budget cycle.
4. **AI architecture (application-level)** — advisory, not a product on its own, but the
   platform should make AI-integration decisions/patterns visible and reviewable like any
   other architectural decision.

Rather than four disconnected tools, I'm proposing **one platform with four modules**,
because the job ad itself treats these as connected: an AAF proposal produces an ADR,
which may close or open a risk-register entry, which is scored against curated standards,
and AI-integration decisions are just another category of decision flowing through the
same governance process. A single system with a shared decision/entity model reflects that
connectedness better than four standalone tools.

## 2. Frameworks & standards to adopt (not reinvent)

The platform is a governance/workflow layer *around* proven, existing tools and formats —
it does not replace them:

| Concern | Standard / format adopted | Why not build custom |
|---|---|---|
| Architecture decisions | **ADR** (Nygard / MADR format), stored as structured records, exportable as Markdown so they can also live colocated in application repos if a team prefers that | Industry-standard, tool-agnostic; avoids lock-in |
| System modelling | **C4 model**, textual (Structurizr DSL-compatible or Mermaid C4 syntax) | Matches the "textual C4 modelling baseline" 6-month milestone directly |
| Governance process | **Harmel-Law Advice Process** + **Architecture Advice Forum** cadence model | Explicitly named in the job ad as the valued pattern |
| Risk taxonomy | Five categories named in the job ad (technical debt, EOL, integration brittleness, security/compliance, AI-readiness) + **TIME model** (Tolerate/Invest/Migrate/Eliminate) recommendation field | Matches stated scope exactly; avoids inventing a proprietary taxonomy |
| Fitness functions | Represented as **CI check references** (e.g., a link/webhook status from ArchUnit/Spring Boot CI, dependency-cruiser, etc.) rather than reimplementing static-analysis engines | The platform tracks *that* a fitness function exists and its pass/fail history; it does not replace ArchUnit or CI |
| Contract testing | Same pattern: reference/link **Pact Broker** or **Spring Cloud Contract** verification status per partner integration, surfaced on that integration's landscape entry | Avoids rebuilding a contract-testing engine |

This keeps the platform's job honest: **govern and make visible**, not replace
specialist tools that already do their job well.

## 3. Proposed application modules

### 3.1 Application Landscape & Risk Register
- Inventory of applications: owner (team/department), lifecycle status, dependencies
  (which other systems it integrates with — feeding the cross-domain view), and links to
  its C4 model / ADR log if present.
- Risk register entries per the Module 4 template (category, impact, likelihood, blast
  radius, recommended action, cost-to-fix, review cadence) with **staleness tracking**
  (flags entries past their review cadence — directly answers the "how do you keep it a
  living artefact" problem).
- A "top-priority risks" view exportable/shareable for budget-cycle conversations.

### 3.2 Architecture Decision Records (ADR) & Case Law
- Structured ADR creation (title, status lifecycle: Proposed → Accepted →
  Deprecated/Superseded, context, decision, consequences, alternatives considered).
- Cross-linking (`Supersedes` / `Superseded-by`), full-text search, and tagging by
  department/domain and by which risk-register entries or standards it touches.
- Optional export to Markdown for teams who want ADRs colocated in their own repo as well
  (the platform becomes the searchable index/case-law layer across all departments, not a
  replacement for repo-colocated files if a team already does that well).

### 3.3 Architecture Advice Forum (AAF) & Governance Workflow
- Proposal submission (links to an ADR draft, a C4 diagram, or both) with a declared scope
  (team/domain/group) — the three-tier model from the job ad.
- Forum session scheduling, agenda, and outcome capture (advice given, by whom, final
  decision, link to the resulting/updated ADR).
- An explicit, rarely-used **arbitration record** for the "group scope only, never a
  default" escalation path, so it's visible how often it's actually invoked (a healthy
  process should show this used sparingly).

### 3.4 Standards & Reference Patterns Library
- Curated, versioned documents (integration standards, API conventions, AI-integration
  patterns) — each standard can link to the ADRs that established or amended it, and to
  landscape entries where it's been applied, giving visibility into adoption.

### 3.5 C4 Model Viewer
- Store/render textual C4 (Mermaid C4 syntax to start — renders natively in many
  Markdown viewers and is simpler to implement than a full Structurizr-compatible engine)
  per application landscape entry, versioned alongside the entry.

### 3.6 AI Architecture Register
- A specialised view/filter within the ADR + Standards modules for AI-integration
  decisions specifically (LLM feature designs, agentic tooling choices, security-baseline
  checklist per OWASP LLM Top 10) — deliberately **not** a separate module, to reinforce
  that AI decisions are governed the same way as any other architecture decision, per the
  job ad's framing of AI architecture as application-side work integrated into normal
  practice.

## 4. Proposed technical stack

As requested: **React (frontend), Java + Spring Boot (backend), PostgreSQL (database)**.

- **Frontend**: React + TypeScript, a component library (e.g., MUI or a lightweight
  headless-UI approach — happy to take your preference), React Query/TanStack Query for
  data fetching, React Router. Mermaid.js embedded for rendering textual C4 diagrams
  in-browser.
- **Backend**: Spring Boot (Java 21 LTS), layered as Controller → Service → Repository,
  with **ArchUnit** fitness functions enforcing that layering from day one (eating our own
  dog food, per Module 5 of the learning path) and **Flyway** for versioned schema
  migrations. REST API, documented with springdoc-openapi (OpenAPI 3), so the platform's
  own API is a live example of the conformance practices in Module 6.
- **Database**: PostgreSQL, with full-text search (native `tsvector`) for ADR/standards
  search rather than introducing a separate search engine initially — simpler ops, upgrade
  path to a dedicated search engine only if/when genuinely needed.
- **Auth**: deferred to your input — do you have an existing group SSO/identity provider
  (SAML/OIDC) this should integrate with, or should the first cut use simple local
  accounts with roles (Contributor / Domain Reviewer / Group Architect)?
- **CI**: GitHub Actions running backend tests (incl. ArchUnit fitness functions) and
  frontend build/lint on every PR — again, a real, working example of the practices the
  role is meant to champion.

## 5. Suggested build order (once approved)

1. Data model + backend skeleton: Application Landscape + Risk Register (the "first-cut
   stocktake" deliverable is the highest-value, most time-boxed item in the job ad).
2. ADR module (status lifecycle, cross-linking, search).
3. Frontend for modules 1–2 (landscape table, risk register views, ADR list/detail/create).
4. AAF/governance workflow module + Standards library.
5. C4 viewer (Mermaid embed) + AI Architecture Register view.
6. Fitness-function/contract-testing status integration (webhook or manual-link based
   first pass; deeper CI integration as a later iteration).

## 6. Open questions for you before I start building

1. **Scope for v1**: build all six modules end-to-end (thinner slice each), or fully
   build modules 3.1–3.2 first (landscape + risk register + ADRs) and defer 3.3–3.6 to a
   v2? Given the job ad calls out the stocktake/risk register as the single most
   time-critical first-year deliverable, I'd lean toward the latter unless you disagree.
2. **Auth model** for v1 — simple local accounts with roles, or do you want to design for
   SSO integration from the start (even if mocked)?
3. **Deployment target** — should I include Docker Compose (Postgres + backend + frontend)
   for local/demo running, and is a specific cloud target (for a later CI/CD story) already
   assumed, or out of scope for now?
4. **Naming** — do you want a working name for the platform, or is a generic
   `architecture-governance-platform` fine for now?

Once you confirm these (or tell me to just proceed with the defaults above), I'll scaffold
the repository structure and start with item 1 of the build order.

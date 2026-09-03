# Proposal — Architecture Governance Platform (for your validation before any code is written)

> **Status: v2 IMPLEMENTED — full proposal delivered.** v1 built the recommended first
> slice from §6 — the application landscape, the architecture risk register, and ADR
> decision records built deeply with a shared append-only audit trail and the seeded
> hospitality case study — in [`app/`](../app/README.md) (React + TypeScript frontend,
> Spring Boot/Java 21 backend, PostgreSQL, Docker Compose, CI). v2 delivered the five
> remaining modules per the build order below: the Architecture Advice Forum workflow with
> scoped proposals and the rarely-used arbitration record (§3.3), the versioned standards &
> reference-patterns library (§3.5), the C4 model viewer with pinned client-side Mermaid
> rendering (§3.4), read-only ADR repository indexing per source-of-truth option (a)
> (§3.2), and fitness-function/contract-testing status integration (§6). The app's Roadmap
> page now tracks only production hardening (OIDC/SSO, scheduled repository pulls, live
> CI/Pact webhooks). The §7 open questions were resolved with the stated defaults. The
> original proposal text follows unchanged.

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

Rather than four disconnected tools, I'm proposing **one platform spanning six connected
modules** (§3), because the job ad itself treats the underlying pillars as connected: an
AAF proposal produces an ADR, which may close or open a risk-register entry, which is
scored against curated standards, and AI-integration decisions are just another category
of decision flowing through the same governance process. A single system with a shared
decision/entity model reflects that connectedness better than four standalone tools.

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
- Risk register entries per the (now expanded) Module 4 template — category, description,
  current controls, inherent vs. residual impact/likelihood, blast radius, treatment
  decision, cost-to-fix, target date/status, risk owner, and last-assessed/evidence link —
  with **staleness tracking** (flags entries past their review cadence — directly answers
  the "how do you keep it a living artefact" problem).
- A "top-priority risks" view exportable/shareable for budget-cycle conversations.

### 3.2 Architecture Decision Records (ADR) & Case Law
- Structured ADR creation (title, status lifecycle: Proposed → Accepted →
  Deprecated/Superseded, context, decision, consequences, alternatives considered).
- Cross-linking (`Supersedes` / `Superseded-by`), full-text search, and tagging by
  department/domain and by which risk-register entries or standards it touches.
- **Source-of-truth decision (needs your input, see §6):** for any team that already
  colocates ADRs as Markdown files in their own repo, the platform should not become a
  second, divergent copy that silently drifts from the real one. Two honest options, not
  "both at once": (a) the **repository is canonical** — the platform periodically indexes/
  imports ADR files from configured repos (read-only, via a CI webhook or scheduled pull)
  purely for cross-department search and linking; or (b) the **platform is canonical** for
  teams that don't already colocate ADRs, with a one-way, explicitly-versioned export to
  Markdown for teams who want a repo copy, clearly labelled as a generated artefact, not a
  second source of truth. Defaulting to (a) for teams with an existing practice and (b) for
  teams starting fresh avoids inventing an unresolved dual-write problem.

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
- Store textual C4 per application landscape entry, versioned alongside the entry, and
  render it client-side with Mermaid.js (a pinned, tested version bundled with the
  frontend — not dependent on GitHub's/GitLab's built-in Mermaid renderer, which has not
  reliably supported the C4 diagram type). Diagram *source text* submitted by users must be
  treated as untrusted input for rendering purposes (see §7 on security).

### 3.6 AI Architecture Register
- A specialised view/filter within the ADR + Standards modules for AI-integration
  decisions specifically (LLM feature designs, agentic tooling choices, security-baseline
  checklist per OWASP's LLM application security guidance) — deliberately **not** a
  separate module, to reinforce that AI decisions are governed the same way as any other
  architecture decision, per the job ad's framing of AI architecture as application-side
  work integrated into normal practice.

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
  accounts with roles (Contributor / Domain Reviewer / Group Architect)? Whichever is
  chosen for v1, treat it explicitly as a **demo-scoped** auth boundary, not a production
  security control, until reviewed — see §7.
- **CI**: GitHub Actions running backend tests (incl. ArchUnit fitness functions) and
  frontend build/lint on every PR — again, a real, working example of the practices the
  role is meant to champion.

## 5. Security & audit posture (v1)

This platform stores architecture decisions, risk posture, system dependencies, and
potentially security-relevant findings — it needs to defend its own data seriously even as
a portfolio/demo project, not just be functionally correct:

- **Audit trail, not just CRUD**: state changes to risk-register entries and ADR status
  transitions should be recorded as immutable, appended audit events (who, when, old
  value, new value), not just overwritten fields — this is what makes the register
  defensible in an actual budget/audit conversation, and it is cheap to build in from the
  start (an `audit_log` table written to in the same transaction) versus retrofitted later.
- **Roles reflect advice, not approval**: role permissions (Contributor / Domain Reviewer /
  Group Architect) should control who can *create and edit* records, not silently imply
  that a "Group Architect" role approves every change — that would contradict the
  decentralised advice-process model (Module 3) the whole platform exists to support.
- **Treat rendered content as untrusted input**: Markdown/Mermaid diagram source submitted
  by users is rendered back to other users' browsers — apply standard output
  sanitisation/CSP practice to avoid stored XSS via diagram or ADR text, the same as any
  other user-generated-content feature.
- **No production data in v1**: explicitly a demo/portfolio system using seeded, invented
  application/risk data — not a place to input real employer information without a
  separate security review and explicit sign-off.

## 6. Suggested build order (once approved)

Given the breadth of §3, building all six modules as thin, shallow slices risks a demo
that looks superficial everywhere rather than credible anywhere. I'd recommend instead
building **one vertical slice deeply**: Application Landscape + Risk Register + ADRs,
fully working end-to-end (including the audit trail and the ADR source-of-truth decision
from §3.2) against a realistic, seeded hospitality/travel case study, before adding the
AAF workflow, Standards library, C4 viewer, or AI Architecture Register as later,
explicitly-labelled roadmap items (even a well-designed "coming soon" screen for those is
more credible in an interview than a shallow, half-working version of each).

1. Data model + backend skeleton: Application Landscape + Risk Register (the "first-cut
   stocktake" deliverable is the highest-value, most time-boxed item in the job ad),
   including the audit-log table from §5 from the start.
2. ADR module (status lifecycle, cross-linking, search), with the repo-vs-platform
   source-of-truth question from §3.2 resolved for the seeded example data.
3. Frontend for modules 1–2 (landscape table, risk register views, ADR list/detail/create),
   seeded with a coherent example case study (e.g., a hospitality booking/loyalty
   integration) rather than disconnected placeholder rows.
4. AAF/governance workflow module + Standards library.
5. C4 viewer (Mermaid embed, pinned version) + AI Architecture Register view.
6. Fitness-function/contract-testing status integration (webhook or manual-link based
   first pass; deeper CI integration as a later iteration).

## 7. Open questions for you before I start building

1. **Scope for v1**: I'd recommend building steps 1–3 above fully (landscape + risk
   register + ADRs, one coherent seeded case study, working audit trail) and presenting
   steps 4–6 as a clearly-labelled roadmap rather than building all six shallowly — agree,
   or do you want a different v1 boundary?
2. **Auth model** for v1 — simple local accounts with roles, or do you want to design for
   SSO integration from the start (even if mocked)?
3. **Deployment target** — should I include Docker Compose (Postgres + backend + frontend)
   for local/demo running, and is a specific cloud target (for a later CI/CD story) already
   assumed, or out of scope for now?
4. **Naming** — do you want a working name for the platform, or is a generic
   `architecture-governance-platform` fine for now?

Once you confirm these (or tell me to just proceed with the defaults above), I'll scaffold
the repository structure and start with item 1 of the build order.

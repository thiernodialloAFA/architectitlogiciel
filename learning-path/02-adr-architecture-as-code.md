# Module 2 — Architecture-as-Code: ADRs Colocated with Application Code

## Learning objectives

- Write a well-formed **Architecture Decision Record (ADR)** using a recognised template
  (Michael Nygard's original format, or MADR).
- Explain why ADRs must be **colocated with the application repository** (not in a wiki or
  central EA tool) for the "case law of architecture decisions" model described in the job
  ad to work.
- Design a lightweight **ADR workflow** (numbering, status lifecycle, review/approval,
  supersession) that scales across five IT departments without a central bottleneck.
- Distinguish an ADR from a design doc, an RFC, and a runbook.

## Curated resources

- **Original source**: Michael Nygard, "Documenting Architecture Decisions" (2011) —
  http://cognitect.com/blog/2011/11/15/documenting-architecture-decisions — the canonical
  short-format ADR.
- **MADR template** (Markdown ADR, richer format with options/consequences):
  https://adr.github.io/madr/
- **adr-tools** (CLI for creating/managing numbered ADRs in a repo):
  https://github.com/npryce/adr-tools
- **log4brains** (renders a browsable ADR log/website from Markdown ADRs in a repo,
  supports multi-package/monorepo ADR logs): https://github.com/thomvaill/log4brains
- **ADR GitHub org / adr.github.io** — collection of templates and tooling:
  https://adr.github.io
- **Article**: ThoughtWorks, "Lightweight Architecture Decision Records" —
  https://www.thoughtworks.com/en-us/radar/techniques/lightweight-architecture-decision-records
- **Article**: Joel Parker Henderson's ADR template collection (good source of
  alternative templates, incl. Y-statements): https://github.com/joelparkerhenderson/architecture-decision-record

## Hands-on exercise

1. Set up an `docs/adr/` directory in a sample repo (or this one) using `adr-tools` or a
   plain numbered-Markdown convention (`0001-record-architecture-decisions.md`, ...).
2. Write ADR-0001 as the *meta-ADR*: "We will record architecture decisions" (this is the
   traditional first ADR — it documents the decision to use ADRs).
3. Write two more ADRs for genuine decisions in a real or example system (e.g., "choice of
   messaging broker", "synchronous vs asynchronous integration for X").
4. Add a `Superseded-by` / `Supersedes` link between two ADRs to practise the lifecycle.
5. Write a one-page "ADR policy" describing: who can propose one, what status values exist
   (Proposed / Accepted / Deprecated / Superseded), and how it plugs into the Architecture
   Advice Forum from Module 3 (an ADR is often the artefact produced *by* an advice-process
   conversation).

## Validation quiz (difficult — scenario based)

<details>
<summary><strong>Q1.</strong> A team writes an ADR titled "We will use Kafka" with no context, no alternatives considered, and no consequences section — just the decision. A reviewer accepts it because "at least they wrote something." Is this good enough, and what specifically is missing that will matter in 18 months?</summary>

Not good enough. An ADR's entire value is as *case law* — future readers (including the
same team in two years, or a different team facing an analogous decision) need to know
**why**, not just **what**. Missing: (1) Context — what forces led here (throughput needs?
existing broker already in the stack? team familiarity?); (2) Alternatives considered and
why rejected (RabbitMQ, SNS/SQS, a synchronous REST call) — without this, a future
architecture review can't tell whether the constraints have changed enough to revisit it;
(3) Consequences — including negative/accepted trade-offs (operational complexity, another
system to run, learning curve), not just the upside. Eighteen months later, when someone
proposes "let's replace Kafka with X" in an Advice Forum, an ADR with only the decision
gives zero case-law value — nobody can tell if the original reasoning still holds. This is
exactly the failure mode "case law of architecture decisions teams reference and learn
from" is meant to prevent.
</details>

<details>
<summary><strong>Q2.</strong> Two departments independently number their ADRs starting from 0001 in their own repos. A group-scope proposal needs to reference decisions from both. What's the actual problem here (or is there one), and what would you change, if anything?</summary>

There usually isn't a real problem, and "fixing" it by imposing a single global sequential
numbering scheme across departments is the wrong instinct — it recreates a central
bottleneck (someone has to hand out the next number) that undermines the whole point of
colocating ADRs with each repo for decentralised governance. The correct fix is scoping the
reference, not the numbering: cross-reference ADRs by `repo-name#0004` (or a stable URL)
rather than by bare number, and if a group-scope decision needs its own record because it
spans repos with no single natural home, put it in a dedicated group-level
architecture-decisions repo/log (e.g., aggregated via log4brains' multi-package mode) while
leaving department-local ADRs local. The goal is discoverability across departments, not
uniqueness of a bare integer.
</details>

<details>
<summary><strong>Q3.</strong> A senior engineer argues ADRs should require sign-off from the Group IT Architect before merging, "so quality stays high." Given the role's governance model (team/domain/group scope, differentiated by blast radius), is this the right control point? What would you propose instead?</summary>

No — this recreates a central-approval bottleneck that is explicitly what the
decentralised, blast-radius-scoped governance model (team/domain/group) is designed to
avoid, and it doesn't scale to "case law across all five IT departments" if one person
must review every record. The right control point is scoped by blast radius: team-scope
decisions are recorded and merged by the team itself, no external sign-off needed (this is
how ADR practice becomes something teams do "without prompting," an explicit 18-month
success marker). Domain-scope and group-scope decisions are the ones that go through an
architecture review (an Advice Forum session, per Module 3) *before* or *as* the ADR is
written, with the ADR being the artefact the review produces — so the Architect's
involvement is about which decisions warrant a forum conversation (based on blast radius),
not a merge-gate on every ADR file in every repo.
</details>

<details>
<summary><strong>Q4.</strong> A team deprecates ADR-0007 without writing a new ADR to explain what replaced it and why, just changing the status field to "Deprecated." Six months later, an audit needs to know what integration pattern the team currently uses and why. What's wrong with this deprecation, and what's the fix?</summary>

Marking status alone destroys the case-law value at exactly the moment it matters most —
the whole point of a "Superseded-by" link is to preserve an unbroken decision trail. If a
decision is deprecated without a successor ADR, an auditor or future team has a
dead-end: they know the old approach is no longer trusted, but not what replaced it, why,
or whether the replacement itself has since been reconsidered. The fix: any deprecation
of a decision still in effect operationally must be paired with a new ADR for whatever
replaced it, cross-linked (`Supersedes: 0007` / `Superseded-by: 0014`), so the chain is
walkable end-to-end. If the *system itself* was decommissioned (not replaced by a new
decision), the ADR can be marked "Deprecated — system decommissioned, see ADR/ticket X" but
never left as a silent dead end.
</details>

# Module 1 — Software Architecture Fundamentals & Textual C4 Modelling

## Learning objectives

By the end of this module you can:

- Explain the difference between architecture *documentation* and architecture *as code*,
  and why the latter matters for a portfolio spanning five IT departments.
- Produce Context, Container and Component diagrams using the **C4 model** (Simon Brown),
  written in a **textual DSL** (Structurizr DSL or Mermaid C4) instead of a drawing tool.
- Justify why a "textual C4 modelling baseline" is easier to keep alive than diagrams made
  in Visio/draw.io, and what "alive" means operationally (CI-checked, PR-reviewed, versioned
  next to the code it describes).
- Distinguish C4's four levels from other notations (UML, ArchiMate) and know when each is
  the right tool.

## Curated resources

- **C4 model — official site**: https://c4model.com (start here; read all four diagram
  levels + the "notation" and "faq" pages).
- **Book**: Simon Brown, *Software Architecture for Developers* (leanpub) — the canonical
  reference behind C4.
- **Structurizr DSL** (textual C4, versionable, renders diagrams from text):
  https://github.com/structurizr/dsl — read the language reference and the "workspace"
  examples.
- **Mermaid C4 diagrams** (lighter-weight alternative, uses Mermaid's own C4 syntax):
  https://mermaid.js.org/syntax/c4.html — note this renders in the Mermaid Live Editor and
  in any tool embedding a current Mermaid.js version, but GitHub's and GitLab's *built-in*
  Markdown Mermaid renderers have historically lagged behind Mermaid releases and have not
  reliably supported the C4 diagram type — verify support on your actual target renderer
  (or pin/vendor a known-good Mermaid.js version if embedding it yourself) before relying
  on "renders natively in GitHub" as a workflow assumption.
- **Article**: Simon Brown, "Diagrams as Code" — https://simonbrown.je/diagrams-as-code/
- **Article**: ThoughtWorks Technology Radar entries on "Diagrams as code" (search radar
  archives) — useful for how to frame this to sceptical stakeholders.
- **Comparative reading**: Gregor Hohpe, *The Software Architect Elevator* — chapters on
  communicating architecture to different audiences (why a single diagram notation is not
  enough, and why textual/versioned matters for a role spanning many teams).

## Hands-on exercise

Pick a system you know well (or a toy system). Write:

1. A **System Context** diagram (Structurizr DSL or Mermaid C4) showing the system, its
   users, and the external systems it integrates with.
2. A **Container** diagram breaking the system into its deployable units (web app, API,
   database, message broker, etc.).
3. One **Component** diagram for the single most architecturally significant container.

Commit these as text files in a `docs/architecture/c4/` folder alongside a short README
explaining how to regenerate the rendered diagrams (e.g. `structurizr-cli export` or the
Mermaid live editor). This is exactly the "textual C4 modelling baseline... prototyped on
at least one flagship system" the job ad names as a 6-month deliverable — do it for real,
not as a reading exercise.

## Validation quiz (difficult — scenario based)

<details>
<summary><strong>Q1.</strong> Your platform team asks you to add a fifth diagram type, "Deployment," to your standard flagship-system documentation set, arguing "C4 doesn't cover infrastructure." Is this a correct reading of the C4 model, and what's your response?</summary>

Incorrect premise: C4 already defines a **Deployment diagram** as a fifth, optional
diagram type — it maps container instances onto infrastructure nodes (e.g., which
container runs in which Kubernetes cluster/region/on which cloud service). It does not
replace Context/Container/Component/Code; it's an additional view for a different
audience (ops/platform). The correct response: confirm you already have this available in
the model, agree to produce a Deployment diagram for the flagship system in question, and
clarify with the platform team whether they want it hand-maintained or generated from
infra-as-code (e.g. derived from Terraform/K8s manifests) — the latter is preferable
because it can't drift from reality.
</details>

<details>
<summary><strong>Q2.</strong> A team lead argues: "We already have an ArchiMate model in a paid enterprise-architecture tool maintained by a central EA team; why introduce textual C4 on top of it?" Give the strongest, most honest counter-argument, and one legitimate reason they might be right.</summary>

Strongest counter: ArchiMate/EA-tool models in this org are typically maintained by a
central team disconnected from the delivery teams and repos; they decay because updating
them is not part of any team's workflow, and by the time a proposal reaches an Architecture
Advice Forum the "official" model is already stale. Textual C4 colocated with the
repository is updated (or at least PR-reviewed) as part of the same change that alters the
system, making staleness a visible, blockable PR issue rather than an invisible drift.
It's cheaper (no tool licence), diffable, and reviewable in the same PR as the code.

Legitimate reason they might be right: if the org's actual need is enterprise-wide
capability/business-architecture mapping (not just software architecture) across
non-technical stakeholders, ArchiMate's broader notation (business layer, motivation
layer) covers ground C4 explicitly does not — C4 is scoped to *software* architecture. The
two are not mutually exclusive: C4 for the software layer, ArchiMate (if genuinely used)
for the business/strategy layer above it. The job ad's explicit de-scoping of
"TOGAF/enterprise architecture certification as a primary credential" is a signal that
this specific role is optimised for the C4/ADR/fitness-function toolchain, not
EA-tool stewardship.
</details>

<details>
<summary><strong>Q3.</strong> You review a Container diagram where the author has drawn the PostgreSQL database as directly called by three different services, each owning different tables, with no ownership boundary shown. What's the C4-modelling problem here, distinct from the architectural problem?</summary>

Careful with the framing here: a C4 **container** is a deployable/runnable unit or a data
store, not necessarily an ownership boundary — a single shared PostgreSQL instance can be
one legitimate container even if multiple services use it, and "split it into three
containers" is not a valid modelling fix if there is genuinely one deployable database
instance. The real C4-modelling problem is narrower: the diagram is currently *silent*
about a fact that matters architecturally, namely which service touches which tables. The
fix is to make that fact visible without inventing containers that don't exist — draw the
explicit relationship arrows from each of the three services to the one database container
(C4 already expects one arrow per consumer, each optionally labelled with what it's used
for, e.g. "reads/writes `bookings` schema"), and/or add a supplementary note or a
lower-level Component diagram of the database's schemas to show the ownership split
without misrepresenting deployment topology. The architectural problem (shared-database
integration is generally an anti-pattern because it creates hidden coupling and blocks
independent schema evolution) is a separate, downstream discussion — but as reviewer, the
first fix is modelling honesty about *relationships and usage*, not fabricating container
boundaries that don't correspond to real deployable units.
</details>

<details>
<summary><strong>Q4.</strong> Someone proposes generating Component diagrams automatically from static analysis of the codebase (e.g., package/class dependency graphs) and asks if this replaces the need for a human-authored C4 model. Evaluate.</summary>

Partial win, not a full replacement. Auto-generated dependency graphs are good at
capturing *code-level structural fact* (what calls what) but not *architectural intent*
(why these components are grouped this way, what the boundary is supposed to mean, which
dependencies are allowed vs. accidental/forbidden). A Component diagram is a curated,
intent-carrying abstraction — it deliberately omits detail. Auto-generated graphs tend to
be too detailed/noisy to serve as communication artefacts and don't distinguish "this
coupling is fine" from "this coupling is architectural decay." The mature pattern (see
Module 5, fitness functions) is to use the auto-generated dependency graph as an *input to
a fitness function* that checks the actual code against the intended, human-authored C4
component boundaries — divergence is the signal, not the diagram itself.
</details>

<details>
<summary><strong>Q5.</strong> A stakeholder insists the Context diagram must show internal team names/org units instead of system names ("that's what people actually care about"). Should you comply?</summary>

No — this conflates the C4 model with an org chart, and is a classic scope violation of
what the diagram type is for. The System Context diagram's audience is "anyone, technical
or non-technical," and its job is to show the system's place among users and other
*systems*, not reporting lines. Team ownership is legitimate metadata but belongs as an
annotation/tag (many textual C4 tools support tagging elements with an owning team) rather
than replacing system identity with org identity — org structures change far more often
than system boundaries, and baking org names into the diagram's primary labels guarantees
rot (Conway's Law tells you the org chart and the system boundary are related but not
identical, and treating them as identical is exactly the trap). Offer the compromise:
system names as primary labels, owning team as a visible property/tag on each box.
</details>

# Learning Path — Group IT/Software Architect (Pierre & Vacances Group)

This learning path is built directly from the job description for the **Group IT/Software
Architect** role. It is organised so that finishing it leaves you able to speak credibly,
in an interview or in the job itself, about every responsibility in the posting:

- Architecture governance (advice process, Architecture Advice Forum, case law of decisions)
- Application architecture & cross-domain coherence
- Application landscape stocktake & risk prioritisation
- AI architecture (application-level, not ML platform work)
- Technical advisory & raising architecture maturity across teams
- The "architecture as code" toolchain: textual C4, colocated ADRs, fitness functions,
  contract/conformance testing
- Cloud architecture literacy at the Platform Engineering boundary
- Security architecture beyond AI, and how to present all of the above credibly in the
  actual hiring process (portfolio, case study, interview)

## How to use this path

1. Module numbers are **stable identifiers**, not a strict reading order — see
   "Recommended reading order" below for the sequence that builds vocabulary most
   logically (it interleaves modules rather than reading 1→12 straight through).
2. Every module has **learning objectives**, **curated resources** (free/official where
   possible), and a **hands-on exercise** you should actually do, not just read about.
3. Every module ends with a **difficult, scenario-based validation quiz**. These are
   deliberately hard — designed to catch people who have only read about the topic, not
   practised it. Answers are in a collapsible `<details>` block; don't peek before you
   commit to an answer.
4. Treat this repository as your own architecture-as-code sandbox: as you go through
   Modules 1, 2, 5, 6 and 11, actually write the C4 model, ADRs, fitness functions and
   threat model described in the exercises inside `docs/` and commit them — ideally against
   the single running example (**"StayPoints,"** introduced in Module 11) rather than a
   different toy system per module, so the artefacts accumulate into one coherent, showable
   body of work rather than disconnected fragments. That artefact becomes something you can
   show in an interview (Module 12 tells you exactly how).

## Modules

| # | Module | Maps to job requirement |
|---|--------|--------------------------|
| 1 | [Software Architecture Fundamentals & Textual C4 Modelling](01-architecture-fundamentals-c4.md) | "textual C4 modelling baseline prototyped on at least one flagship system" |
| 2 | [Architecture-as-Code: ADRs Colocated with Application Code](02-adr-architecture-as-code.md) | "ADR practice, structured, colocated with application repositories" |
| 3 | [Decentralised Governance: Harmel-Law Advice Process & the Architecture Advice Forum](03-decentralised-governance-aaf.md) | "Architecture governance", "AAF cadence established" |
| 4 | [Application Landscape Stocktake, Technical Debt & the Risk Register](04-portfolio-stocktake-risk-register.md) | "Application landscape stocktake and risk prioritisation" |
| 5 | [Fitness Functions & Evolutionary/Continuous Architecture](05-fitness-functions-evolutionary-architecture.md) | "Fitness functions running in CI...structural integrity, external API conformance" |
| 6 | [API Contract & Conformance Testing](06-api-contract-testing.md) | "...and partner integration contracts" |
| 7 | [AI Architecture: LLM Integration & Agentic Patterns at the Application Level](07-ai-architecture-llm-integration.md) | "AI architecture", "AI-readiness gaps", "agentic tooling patterns" |
| 8 | [Cloud Architecture Literacy](08-cloud-architecture-literacy.md) | "Cloud architecture literacy...collaborate credibly with Platform Engineering" |
| 9 | [Cross-Domain Integration Patterns & Application-Level Standards](09-cross-domain-integration-patterns.md) | "Application architecture and cross-domain coherence" |
| 10 | [Technical Advisory, Influence & Running Architecture Reviews](10-technical-advisory-leadership.md) | "Technical advisory", "hold a room during an architecture review, disagree well" |
| 11 | [Security Architecture Beyond AI, and a Coherent Hospitality Case Study](11-security-architecture-hospitality-case-study.md) | "security and compliance exposure" risk category; introduces the running "StayPoints" example used across Modules 9, 11, 12 |
| 12 | [Interview Preparation, Portfolio & Case Study Presentation](12-interview-preparation-portfolio.md) | Turning all of the above into a convincing case study, portfolio, and interview performance for this specific role |

## Recommended reading order

Read in this order rather than strictly 1→12 — it introduces cross-domain integration and
the running case study earlier, since later modules (fitness functions, contract testing,
AI architecture) are easier to reason about once you have one concrete system and its
integration boundaries in mind, rather than meeting that system for the first time in
Module 11:

**1 → 2 → 9 → 11 → 3 → 4 → 5 → 6 → 7 → 8 → 10 → 12**

Module numbers stay fixed (so cross-references between modules remain stable); only the
suggested traversal order changes.

## Suggested pace

- **Weeks 1–2**: Modules 1, 2 (build the C4 + ADR habit immediately, on a real repo).
- **Week 3**: Module 9 (cross-domain integration patterns) then Module 11 (security +
  the StayPoints case study) — get one coherent example system with real integration
  boundaries established before layering governance and fitness-function work on top of it.
- **Weeks 4–5**: Modules 3, 4 (governance model + stocktake/risk register — this is the
  "first-year foundational body of work" called out in the job ad), now applied to
  StayPoints rather than an abstract example.
- **Weeks 6–7**: Modules 5, 6 (fitness functions + contract testing in CI).
- **Weeks 8–9**: Module 7 (AI architecture) — the most differentiated, least-commoditised
  skill area in the posting; reuse StayPoints' Guest Support AI scenario from Module 11.
- **Week 10**: Module 8 (cloud literacy) — enough to be credible with Platform Engineering,
  not enough to replace them.
- **Week 11**: Module 10 — the soft-skill/authority dimension the role explicitly calls out
  ("influence without direct authority... disagree well").
- **Week 12**: Module 12 — turn everything above into an actual case-study presentation,
  portfolio evidence pack, and rehearsed interview answers.

## Explicitly out of scope (by design of the job ad)

The role explicitly does **not** prioritise TOGAF/enterprise-architecture certification as
a primary credential, and is not ML-platform/data-science work. This path reflects that:
you will not find a TOGAF exam-cram module here, and Module 7 stays firmly on the
application/integration side of AI, not model training or ML-ops.

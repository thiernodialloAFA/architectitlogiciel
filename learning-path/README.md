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

## How to use this path

1. Work through the modules in order — each builds on the previous one's vocabulary.
2. Every module has **learning objectives**, **curated resources** (free/official where
   possible), and a **hands-on exercise** you should actually do, not just read about.
3. Every module ends with a **difficult, scenario-based validation quiz**. These are
   deliberately hard — designed to catch people who have only read about the topic, not
   practised it. Answers are in a collapsible `<details>` block; don't peek before you
   commit to an answer.
4. Treat this repository as your own architecture-as-code sandbox: as you go through
   Modules 1–2 and 5–6, actually write the C4 model, ADRs and fitness functions described
   in the exercises inside `docs/` and commit them. That artefact becomes something you
   can show in an interview.

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

## Suggested pace

- **Weeks 1–2**: Modules 1–2 (build the C4 + ADR habit immediately, on a real repo).
- **Weeks 3–4**: Modules 3–4 (governance model + stocktake/risk register — this is the
  "first-year foundational body of work" called out in the job ad).
- **Weeks 5–6**: Modules 5–6 (fitness functions + contract testing in CI).
- **Weeks 7–8**: Module 7 (AI architecture) — the most differentiated, least-commoditised
  skill area in the posting.
- **Week 9**: Module 8 (cloud literacy) — enough to be credible with Platform Engineering,
  not enough to replace them.
- **Week 10**: Modules 9–10 — synthesis, and the soft-skill/authority dimension the role
  explicitly calls out ("influence without direct authority... disagree well").

## Explicitly out of scope (by design of the job ad)

The role explicitly does **not** prioritise TOGAF/enterprise-architecture certification as
a primary credential, and is not ML-platform/data-science work. This path reflects that:
you will not find a TOGAF exam-cram module here, and Module 7 stays firmly on the
application/integration side of AI, not model training or ML-ops.

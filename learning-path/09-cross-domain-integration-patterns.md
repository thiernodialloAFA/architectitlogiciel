# Module 9 — Cross-Domain Integration Patterns & Application-Level Standards

## Learning objectives

- Curate a coherent set of **application-level standards and reference patterns** for
  integration (the job ad's core phrase for this responsibility), spanning synchronous
  (REST/gRPC), asynchronous (event-driven/messaging), and batch integration styles.
- Apply classic **Enterprise Integration Patterns** vocabulary to keep proposals across
  a five-department portfolio coherent, without imposing a single rigid style where it
  doesn't fit.
- Distinguish orchestration vs. choreography for cross-domain workflows, and know the
  concrete trade-offs (visibility/traceability vs. coupling/single-point-of-failure).
- Recognise data-ownership and API-design anti-patterns that erode cross-domain coherence
  over time (shared databases across service boundaries, "god" integration hubs,
  inconsistent API versioning/error-handling conventions across departments).

## Curated resources

- **Book**: Gregor Hohpe & Bobby Woolf, *Enterprise Integration Patterns* — still the
  reference vocabulary (Message Router, Content-Based Router, Aggregator, Dead Letter
  Channel, etc.) two decades on; you don't need to read cover to cover, but know the core
  pattern catalogue well enough to name-check it credibly. Companion site:
  https://www.enterpriseintegrationpatterns.com
- **Book**: Sam Newman, *Building Microservices* (2nd edition) — chapters on
  "Communication Styles" and "Workflow" (orchestration vs. choreography) are the most
  relevant for cross-domain coherence work.
- **Article**: Martin Fowler, "Orchestration vs. Choreography" —
  https://martinfowler.com/articles/microservices.html (and related short posts) — read
  for the trade-off framing you'll need in an Advice Forum debate.
- **Article**: search "API design guidelines" from well-known engineering orgs (e.g.,
  publicly available API style guides from major tech companies) as *examples* of the
  kind of curated, versioned standard document this role is expected to produce and
  maintain — not to copy verbatim, but to see the level of specificity (error format,
  pagination convention, versioning policy) that makes a standard actually usable.
- **Article**: Zhamak Dehghani, "Data Mesh" principles — https://martinfowler.com/articles/data-monolith-to-mesh.html
  — relevant background for cross-domain *data* coherence specifically (domain-oriented
  data ownership vs. centralised integration hubs), even though the job is application-
  architecture-focused rather than data-platform-focused.
- **Article**: search "anti-corruption layer pattern DDD" (Eric Evans' Domain-Driven
  Design origin) — a core pattern for keeping cross-domain integration coherent when two
  domains have genuinely different models of the same concept (e.g., "Guest" meaning
  something different in a booking system vs. a loyalty system).

## Hands-on exercise

1. Draft a **one-page "Integration Standards" reference document** covering: (a) when to
   use synchronous request/response vs. asynchronous event-driven integration (a decision
   tree or a short set of guiding questions, not a rigid rule); (b) a standard error-
   response shape and versioning policy for internally-facing APIs; (c) the org's default
   stance on shared databases across service boundaries (recommend against, with the
   narrow exceptions under which it's tolerated); (d) when an anti-corruption layer is
   warranted between two domains with diverging models of the same concept.
2. Pick two hypothetical or real IT departments in the org (e.g., "Booking" and
   "Loyalty") and design a concrete cross-domain integration for a plausible workflow (e.g.
   "booking a stay earns loyalty points"), explicitly choosing orchestration or
   choreography and justifying the choice against the trade-offs from the resources above.
3. Write the ADR (Module 2) for that integration choice, and note which C4 diagram
   (Module 1, likely a Container or System Landscape-level diagram) would need updating to
   reflect it.

## Validation quiz (difficult — scenario based)

<details>
<summary><strong>Q1.</strong> Two departments building a "guest checks in" → "loyalty points awarded" workflow disagree: one wants a central orchestrator service that calls each department's API in sequence and tracks the whole workflow's state; the other wants each service to react to a "GuestCheckedIn" event independently, with no central coordinator. As the reviewing architect, what questions determine which is right here, not "which pattern do you personally prefer"?</summary>

The right pattern follows from the concrete needs of this workflow, not a general
preference for either style. Key questions: (1) **Visibility/traceability need** — does
the business need a single place to see "where is this specific check-in's downstream
processing right now, and did it fully complete"? If yes (e.g., customer support needs to
answer "why didn't I get my points"), orchestration's centralised state tracking is a real
advantage; pure choreography makes this materially harder to answer without extra
distributed-tracing tooling. (2) **Coupling tolerance** — does introducing a coordinator
service create a new single point of failure and a new deployment dependency every
downstream department must integrate against, when some steps (e.g., a marketing team's
optional "send promo" reaction) don't strictly need to be sequenced or tracked centrally?
If most steps are genuinely independent reactions with no ordering requirement,
choreography avoids unnecessary central coupling. (3) **Failure/retry semantics** — who is
responsible for retrying a failed step and for how long, and does that responsibility
belong naturally to one component (favouring orchestration) or is it cleanly owned by each
reactor (favouring choreography)? In practice, many real workflows are hybrid: a light
orchestrator for the parts genuinely needing sequencing/visibility, choreographed events
for genuinely independent side-reactions — the wrong move is picking one pattern
dogmatically for the whole workflow before separating these steps.
</details>

<details>
<summary><strong>Q2.</strong> A department wants an exception to the "no shared databases across service boundaries" standard, arguing their two services are "basically the same team, it's fine." Six months later a third team, in a different department, wants to build a new feature reading directly from that same database because "it's already shared, what's one more reader." How do you evaluate the original exception, and how do you handle the new request?</summary>

The original exception should have been scoped explicitly and narrowly from the start —
"two services owned by literally the same team, reviewed and time-boxed, revisit if
either service gets a different owning team or a new consumer appears" — precisely because
an ungoverned exception has exactly the failure mode now appearing: shared-database
coupling has a strong gravitational pull, since "it's already shared, what's one more
reader" always sounds locally reasonable while each addition makes the eventual
untangling cost higher and further erodes the standard's credibility org-wide. Handle the
new request by declining it and treating it as the trigger to revisit the original
exception, not by extending the exception further: the moment a *third*, differently-owned
team wants in, the "basically the same team" justification has already expired, and
you now likely have three teams each independently and quietly evolving schema
expectations against one database with no coordinated ownership — a textbook integration-
brittleness entry for the risk register (Module 4). The corrective action is to require
the new team to integrate via an API (even a thin one exposing just what they need) and,
separately, schedule a review of whether the original two-service exception itself should
now be unwound given it's already attracting more consumers than intended.
</details>

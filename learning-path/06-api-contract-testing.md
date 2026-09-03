# Module 6 — API Contract & Conformance Testing

## Learning objectives

- Distinguish **schema/spec conformance** (does the implementation match a declared
  contract like OpenAPI/AsyncAPI) from **consumer-driven contract testing** (does the
  implementation satisfy what its actual consumers need, verified via recorded
  interactions).
- Design and run a **consumer-driven contract testing** pipeline using Pact (or Spring
  Cloud Contract for an all-Java/Spring estate), including a contract broker and
  can-i-deploy safety gates.
- Apply this specifically to **partner-facing APIs** — the job ad's explicit phrase — where
  you often can't co-deploy with the consumer and need stronger guarantees than "it works
  in our staging environment."
- Know when contract testing is the wrong tool (e.g., for integrations where the partner
  will never participate in a shared contract-testing workflow) and what to use instead
  (schema validation gateways, recorded-traffic replay/shadow testing).

## Curated resources

- **Pact** (consumer-driven contract testing, polyglot, broker-based):
  https://docs.pact.io — read "Getting Started," "Pact Broker," and "can-i-deploy."
- **Spring Cloud Contract** (JVM-native alternative, integrates naturally with a Spring
  Boot backend, producer-driven variant of contract testing):
  https://spring.io/projects/spring-cloud-contract
- **Article**: Martin Fowler, "Contract Test" — https://martinfowler.com/bliki/ContractTest.html
  and "Consumer-Driven Contracts" — https://martinfowler.com/articles/consumerDrivenContracts.html
  — foundational vocabulary; read both, they describe related but distinct ideas.
- **OpenAPI Specification** — https://www.openapis.org — for the static-conformance side
  (validating implementation against a published spec); pair with tools like
  `openapi-diff` or Spectral (https://github.com/stoplightio/spectral) for spec-linting in
  CI.
- **Article**: search "Pact vs Spring Cloud Contract" for current (2023+) practitioner
  comparisons — useful for justifying a tool choice for a specific partner-API scenario in
  an interview.
- **Book chapter**: *Building Microservices* (Sam Newman), chapters on "Testing" and
  "Integration" — good grounding in why contract tests exist between the fast-unit-test and
  slow-end-to-end-test layers of the test pyramid.

## Hands-on exercise

1. Pick (or invent) a partner-facing API — e.g., a payments or loyalty-points partner
   integration relevant to a hospitality/travel group.
2. Write a **Pact consumer test** simulating your service as the consumer of the partner's
   API (or vice versa, if you own the provider side), generating a pact file.
3. Set up (locally, or described in a README if you can't run infrastructure) a **Pact
   Broker** workflow: publish the pact, verify it against a provider stub, and demonstrate
   a `can-i-deploy` check that would block a deploy if the contract is unsatisfied.
4. Separately, add an **OpenAPI spec-lint** step (Spectral) to CI that fails the build on
   breaking changes to a partner-facing endpoint (e.g., removing a field consumers depend
   on) — this is the static-conformance fitness function from Module 5, applied
   specifically to partner contracts.
5. Write two paragraphs: when would you use Pact vs. when would you rely on
   OpenAPI-spec-lint alone (hint: the answer depends on whether the partner is willing to
   participate in a shared contract-testing workflow at all).

## Validation quiz (difficult — scenario based)

<details>
<summary><strong>Q1.</strong> An external partner refuses to run a Pact verification step against your published contract, saying "we don't use that tool and won't integrate it into our CI." Your team wants to give up on contract testing this integration and rely on end-to-end tests in a shared staging environment instead. Is that the right fallback, and is there a better one?</summary>

Giving up entirely on contract testing is not the best available fallback, and shared
staging end-to-end tests bring their own well-known problems (flaky, slow, and typically
staging environments drift from production or aren't kept in sync by *both* parties
reliably). A better middle ground: you don't need the partner's active participation to
get most of the value. You can (a) maintain the OpenAPI/AsyncAPI contract for your side of
the integration and run static spec-lint/diff checks in your own CI to catch *your*
breaking changes before they reach the partner (Module 5/6 conformance fitness function);
(b) if you're the consumer, record real interactions with the partner's actual API (via a
recording proxy) and replay them as regression tests against your integration code,
detecting when *their* behaviour changes even without their cooperation; (c) treat the
integration as a named entry in the risk register (Module 4) under "integration
brittleness," since a non-participating partner is a durable architectural risk worth
tracking, not a one-time inconvenience to route around silently.
</details>

<details>
<summary><strong>Q2.</strong> A provider team's `can-i-deploy` check keeps failing because a consumer team's pact expects a field that the provider considers a "deprecated, about to be removed" field. The provider team wants to force the deploy through despite the failing gate, arguing "the consumer needs to update, not us." Evaluate this, given the point of contract testing.</summary>

Forcing the deploy through defeats the entire purpose of consumer-driven contract testing,
which exists precisely to prevent a provider from unilaterally breaking a consumer it may
not even have full visibility into — a red `can-i-deploy` gate is the system doing its job
correctly, not a false positive to override. The right sequence is: the provider
communicates the deprecation to the consumer team (via the advice process / a shared
channel, not just a code change), agrees a timeline, and the consumer updates their pact
(and their code) to stop depending on the field, at which point the gate naturally turns
green because the actual, current expectation changed. Overriding the gate "because we're
right that it should be deprecated" reintroduces exactly the silent-breakage risk contract
testing is meant to eliminate — being right about the deprecation doesn't change the fact
that the consumer's running code, right now, still needs that field.
</details>

<details>
<summary><strong>Q3.</strong> Someone argues that once you have a comprehensive OpenAPI spec-lint check (Module 5/6, static conformance) in CI, consumer-driven contract testing (Pact) becomes redundant "because the spec already defines the contract." Is this correct?</summary>

Not correct — the two check different things and neither subsumes the other. A spec-lint
check verifies that the *implementation matches the declared spec document* (e.g., it
didn't silently remove a field the spec still lists, or it returns response shapes
matching the schema) — but it says nothing about whether the spec itself, even if
perfectly honoured, actually reflects what a *specific real consumer* needs. A provider
can honour its own OpenAPI spec to the letter while still breaking a consumer that depended
on an undocumented behaviour, a field marked optional-in-spec but treated as required in
practice, or a specific error-code semantic the spec under-specifies. Consumer-driven
contract testing captures exactly those real, consumer-observed expectations — including
ones the spec author never thought to document — and verifies them independently of
whether the spec itself changed. Mature practice runs both: spec-lint as a fast,
cheap static gate; Pact/contract tests as the dynamic, consumer-reality gate.
</details>

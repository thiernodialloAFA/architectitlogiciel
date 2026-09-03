# Module 6 — API Contract & Conformance Testing

## Learning objectives

- Distinguish **schema/spec conformance** (does the implementation match a declared
  contract like OpenAPI/AsyncAPI) from **consumer-driven contract testing** (does the
  implementation satisfy what its actual consumers need, verified via recorded
  interactions).
- Design and run a **consumer-driven contract testing** pipeline using Pact, including a
  contract broker and `can-i-deploy` safety gates — and know precisely which side (consumer
  or provider) does what, and in which order.
- Understand **Spring Cloud Contract** as a distinct, *producer-driven* alternative
  (contracts are authored and owned by the provider, who generates stubs for consumers and
  server-side tests for itself) — and be able to say clearly why it is not a drop-in
  substitute for Pact's broker/`can-i-deploy` workflow, even on an all-Java/Spring estate.
- Apply this specifically to **partner-facing APIs** — the job ad's explicit phrase — where
  you often can't co-deploy with the consumer and need stronger guarantees than "it works
  in our staging environment."
- Know when contract testing is the wrong tool (e.g., for integrations where the partner
  will never participate in a shared contract-testing workflow) and what to use instead
  (schema validation gateways, recorded-traffic replay/shadow testing).

## Curated resources

- **Pact** (consumer-driven contract testing, polyglot, broker-based):
  https://docs.pact.io — read "Getting Started," "Pact Broker," and "can-i-deploy." Note the
  workflow direction precisely: the **consumer** writes a test against a mock of the
  provider, which generates a pact file; the pact is published to the broker; the
  **provider** then replays the pact's requests against its *real, running implementation*
  and publishes a pass/fail verification result back to the broker — it is the provider
  that gets verified against the consumer's expectations, not the other way around.
- **Spring Cloud Contract** (JVM-native, **producer-driven** contract testing — a distinct
  model from Pact, not an interchangeable "Java version" of it): the provider team writes
  contracts (Groovy/YAML) describing its own API, from which the tooling generates both a
  WireMock stub (for consumers to test against locally) and provider-side tests that fail
  the provider's build if its implementation drifts from the contract. There is no
  consumer-authored pact file and no Pact-Broker-style `can-i-deploy` gate — a team
  choosing this tool needs its own convention for sharing stubs with consumers (e.g., a
  stub-artifact repository), and should not describe it as "using the Pact Broker."
  https://spring.io/projects/spring-cloud-contract
- **Article**: Martin Fowler, "Contract Test" — https://martinfowler.com/bliki/ContractTest.html
  and "Consumer-Driven Contracts" — https://martinfowler.com/articles/consumerDrivenContracts.html
  — foundational vocabulary; read both, they describe related but distinct ideas.
- **OpenAPI Specification** — https://www.openapis.org — for the static-conformance side.
  Be precise about what each companion tool actually checks, since they are commonly
  conflated: **Spectral** (https://github.com/stoplightio/spectral) lints a *single* OpenAPI
  document against style/design rules (e.g., "every path must have an operationId") — it
  has no notion of "before" and "after" and cannot by itself detect a breaking change.
  **`oasdiff`** (https://github.com/Tufin/oasdiff) or **`openapi-diff`** compare two spec
  versions and flag breaking changes (a removed field, a narrowed enum). Neither tool
  verifies that the *running implementation* actually matches the spec document at
  all — that requires a runtime check (e.g., request/response schema validation
  middleware, or provider-side contract tests as in Pact/Spring Cloud Contract above).
  These are three distinct controls, not interchangeable synonyms for "spec conformance."
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
   Broker** workflow: publish the consumer pact, run the **provider verification** step
   (the provider replays the pact's requests against its own real implementation and
   publishes the pass/fail result — not "a provider stub," which would test nothing real),
   and demonstrate a `can-i-deploy` check that would block a deploy if the contract is
   unsatisfied.
4. Separately, add an **OpenAPI breaking-change check** to CI using `oasdiff` (comparing the
   current spec against the last-released baseline) *and* a Spectral lint pass (style/design
   rules on the spec itself) for a partner-facing endpoint — treat these as two distinct
   static checks, neither of which verifies the running implementation actually matches the
   spec (that verification is what the Pact/Spring Cloud Contract step above provides).
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
<summary><strong>Q3.</strong> Someone argues that once you have a comprehensive OpenAPI breaking-change check (`oasdiff`/Spectral, Module 5/6, static conformance) in CI, consumer-driven contract testing (Pact) becomes redundant "because the spec already defines the contract." Is this correct?</summary>

Not correct — the two check different things and neither subsumes the other. A static spec
check verifies facts *about the spec document itself* (did this change remove a field the
previous version declared? does the document follow our style rules?) — it says nothing
about whether the *running implementation* actually honours that document, and nothing
about whether the spec, even if perfectly honoured, reflects what a *specific real
consumer* needs. A provider can pass every spec-level check while still breaking a
consumer that depended on an undocumented behaviour, a field marked optional-in-spec but
treated as required in practice, or a specific error-code semantic the spec
under-specifies. Consumer-driven contract testing captures exactly those real,
consumer-observed expectations — including ones the spec author never thought to
document — by replaying real recorded requests against the real provider implementation.
Mature practice runs all of these together: spec-lint and breaking-change diff as fast,
cheap static gates on the document; Pact provider verification as the dynamic,
implementation-reality gate.
</details>

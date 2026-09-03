# Module 5 — Fitness Functions & Evolutionary/Continuous Architecture

## Learning objectives

- Define **architectural fitness functions**: objective, automatable checks that verify a
  system still meets an intended architectural characteristic (not just functional
  correctness).
- Categorise fitness functions (atomic vs. holistic, triggered vs. continuous, static vs.
  dynamic) and pick the right kind for structural-integrity, API-conformance, and
  partner-integration-contract checks — the three the job ad names explicitly for the
  12-month milestone.
- Implement at least one structural fitness function using **ArchUnit** (Java/JVM) and know
  the equivalent tools in other stacks (e.g., `dep-cruiser`/`ts-arch` for TS/JS,
  `NetArchTest` for .NET).
- Wire a fitness function into **CI** so a build fails on architectural regression, not just
  on functional test failure.

## Curated resources

- **Book**: Neal Ford, Rebecca Parsons, Patrick Kua, *Building Evolutionary Architectures*
  (O'Reilly) — the canonical source for the term "fitness function" in this context. Read
  at minimum the chapters on fitness function categories and incremental change.
- **ArchUnit** (Java, most directly relevant given the stated Java/Spring Boot stack):
  https://www.archunit.org — read the "layered architecture," "cyclic dependencies," and
  "naming conventions" rule examples.
- **Article**: Neal Ford & Rebecca Parsons, "Fitness Function-Driven Development" —
  search martinfowler.com / thoughtworks.com for the original articles that predate the
  book.
- **dependency-cruiser** (JS/TS structural fitness functions, useful if any frontend/BFF
  layers need architectural checks too): https://github.com/sverweij/dependency-cruiser
- **Article**: ThoughtWorks Technology Radar entries on "Fitness functions" and
  "Architectural fitness function as a service" across multiple radar editions — read how
  the framing has evolved.
- **Spring Boot + ArchUnit integration guides**: search "archunit spring boot maven/gradle
  setup" for current-version setup instructions (favour the official ArchUnit docs over
  blog posts, which go stale fast).

## Hands-on exercise

In a sample Spring Boot project (or this repository once the backend proposal in
`docs/architecture-governance-platform-proposal.md` is approved and scaffolded):

1. Add ArchUnit and write a **layered-architecture fitness function**: controllers may
   depend on services; services may depend on repositories; repositories must never depend
   back on controllers or services (classic layering-violation check).
2. Write a **naming-convention fitness function**: e.g., all `@RestController` classes must
   live in a `..web..` package and be suffixed `Controller`.
3. Write a **cyclic-dependency fitness function** across your top-level packages
   (`freeze` the current cycle set if you can't fix it all at once — ArchUnit supports
   "freeze" so new cycles fail CI while pre-existing ones are grandfathered, an important
   incremental-adoption pattern for a real portfolio with legacy debt).
4. Wire the ArchUnit test into the existing CI pipeline (Maven/Gradle `test` phase) so a
   pull request fails if it violates the rule.
5. Separately, sketch (doesn't need to be code) what an **external API-conformance**
   fitness function would check for a partner-facing API — e.g., validating the deployed
   OpenAPI spec against a contract baseline (ties directly into Module 6).

## Validation quiz (difficult — scenario based)

<details>
<summary><strong>Q1.</strong> A team adds an ArchUnit rule forbidding cyclic package dependencies. The very first CI run on their existing, years-old codebase fails with 47 pre-existing cycles. They conclude "fitness functions don't work for legacy code" and remove the rule entirely. What's the actual mistake, and what should they have done?</summary>

The mistake is treating "the rule finds pre-existing violations" as a failure of the tool,
when it's actually the tool working correctly — the codebase genuinely has 47 cycles; the
fitness function just made an invisible problem visible. Removing the rule entirely throws
away all future protection to avoid dealing with the past. The correct incremental-adoption
pattern (explicitly supported by ArchUnit's `freeze()` API, and a general fitness-function
best practice from *Building Evolutionary Architectures*) is to **freeze** the current
violation set as a known baseline: the test passes today because it only fails on *new*
violations beyond the frozen baseline, while existing violations are recorded (often in a
checked-in file) as visible, trackable technical debt — ideally cross-referenced into the
risk register from Module 4. This gets you CI protection against regression starting
immediately, without requiring a big-bang cleanup as a precondition.
</details>

<details>
<summary><strong>Q2.</strong> A fitness function that checks "external API conformance" against an OpenAPI contract baseline passes in CI, but a partner integration breaks in production the same week because the *runtime behaviour* (e.g., an error code returned in an edge case) diverged from the spec even though the code's route/schema definitions matched. What category of fitness function was missing, and how would you add it?</summary>

The static/structural check (does the code's declared API surface match the contract
document) is one category of fitness function, but it does not verify *dynamic* behaviour —
what the service actually does at runtime for real request/response pairs, especially edge
cases (error paths, boundary values) that a schema-only check can't catch. What's missing
is a **dynamic, executable contract test** — this is precisely the distinction between
schema/structural conformance (Module 5) and consumer-driven contract testing (Module 6):
tools like Pact or Spring Cloud Contract record/replay real request-response interactions
and run them against the live (or a deployed) service in CI, catching behavioural drift
that a spec-diff can't. The fix is to add a contract-testing fitness function (dynamic,
triggered on deploy or nightly) alongside the existing static schema-conformance check,
not to replace one with the other — they catch different failure classes.
</details>

<details>
<summary><strong>Q3.</strong> A junior architect proposes a fitness function that fails the build if any service's average response time in a load test exceeds 200ms, framing it as an "architectural" check. A colleague objects that performance testing isn't architecture. Who's right?</summary>

Both have a partial point, and the resolution matters for how you scope fitness functions
in this role. Performance/latency thresholds absolutely can be legitimate fitness
functions — *Building Evolutionary Architectures* explicitly includes performance,
scalability, and other "-ilities" as valid architectural characteristics worth protecting
with automated checks, not just structural/dependency rules. So framing it as
"architectural" is defensible if latency is a stated, deliberate architectural
characteristic of the system (e.g., an SLA commitment baked into a design decision/ADR).
The colleague's objection has force only if this is being introduced as a one-off,
un-connected-to-any-decision performance gate rather than tied to an actual documented
architectural characteristic — in which case it risks becoming test-suite noise
(flaky load-test thresholds unrelated to any agreed intent) rather than a meaningful
fitness function. The fix: trace the 200ms threshold back to an ADR or explicit
non-functional requirement before treating a build failure on it as an "architecture"
signal — otherwise it's just a performance test wearing an architecture badge.
</details>

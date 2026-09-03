# Module 8 — Cloud Architecture Literacy

## Learning objectives

- Understand enough cloud infrastructure vocabulary and trade-offs (compute, networking,
  managed data services, IAM, observability) to **collaborate credibly with Platform
  Engineering at the application/infrastructure boundary** — the job ad's explicit scope
  for this dimension, deliberately not full infra ownership.
- Recognise the architectural implications of common cloud patterns: managed
  Kubernetes vs. serverless/FaaS, managed database services vs. self-hosted, service mesh,
  event-bus/managed-messaging services, and multi-region/availability-zone design.
- Know where the application/infrastructure boundary typically sits in a Team Topologies-
  style platform-engineering setup, and what "credible collaboration" looks like in
  practice (asking the right questions, understanding constraints, not re-deciding infra
  choices unilaterally).
- Map basic cost-awareness (why architectural decisions have direct cloud cost
  consequences — e.g., chatty cross-AZ traffic, over-provisioned managed services) into
  the risk-register and technical-advisory work from Modules 4 and 10.

## Curated resources

- **Book**: Gregor Hohpe, *The Software Architect Elevator* — several chapters directly
  address bridging development and infrastructure/operations perspectives; particularly
  relevant given this module's framing.
- **Team Topologies** (Skelton & Pais) — re-read the **Platform Team** chapter
  specifically through the lens of "where does application architecture responsibility end
  and platform responsibility begin."
- **Official cloud provider "Well-Architected Framework" documents** — AWS Well-Architected
  Framework (https://aws.amazon.com/architecture/well-architected/), Azure Well-Architected
  Framework, Google Cloud Architecture Framework — read at least one in full; these are
  vendor-authored but genuinely useful as structured checklists across reliability,
  security, cost, performance, and operational excellence — cross-check vendor bias by
  reading more than one if time allows.
- **The Twelve-Factor App**: https://12factor.net — foundational, still relevant
  vocabulary for what makes an application "cloud-native" and easy for a platform team to
  operate.
- **Article**: search "service mesh vs API gateway architecture" and "serverless vs
  containers trade-offs 2024" for current, vendor-neutral comparative reading — cloud
  tooling changes fast enough that older sources (pre-2021) are frequently stale on
  specifics, though the underlying trade-offs (cold start vs. operational overhead,
  vendor lock-in vs. velocity) remain durable.
- **CNCF Cloud Native Landscape**: https://landscape.cncf.io — useful as a map of the
  ecosystem breadth, not something to memorise; skim to recognise category names
  (service mesh, observability, GitOps) when Platform Engineering references them.

## Hands-on exercise

Write a one-page **"application/infrastructure boundary contract"** for a hypothetical
flagship application, structured as:

- What the **application team** owns and decides (container image, application-level
  config, autoscaling *policy* inputs like target CPU%, application-level observability
  instrumentation/traces).
- What **Platform Engineering** owns and provides (the underlying cluster/runtime, network
  policy defaults, the managed database service tier, base observability infrastructure).
- The **negotiated middle** where both parties need to agree (data residency/region
  choices with compliance implications, choice between a managed message broker service
  vs. self-hosted, disaster-recovery RTO/RPO targets that constrain both application design
  and infrastructure provisioning).
- Where you, as the Group IT Architect, sit in this conversation: not deciding
  infrastructure unilaterally, but ensuring application-level architectural decisions
  (e.g., a new system with a hard low-latency requirement) are surfaced to Platform
  Engineering early enough to shape feasible infra choices, and vice versa.

## Validation quiz (difficult — scenario based)

<details>
<summary><strong>Q1.</strong> A team proposes a new flagship system with a hard requirement for sub-50ms p99 latency between two of its components, and plans to deploy both components as independent services communicating over the network, "because that's our microservices standard." Platform Engineering hasn't been consulted yet. What's your concern as the architect operating at the application/infrastructure boundary?</summary>

The concern is that a supposedly application-level standard ("we always split into
independent network-communicating services") is about to collide with a hard latency
constraint that infrastructure topology (same node/AZ placement, network path, service
mesh sidecar overhead, etc.) directly determines — and this is exactly the kind of
decision that needs Platform Engineering's input *before* the design is finalised, not
after, because retrofitting placement/topology guarantees onto an already-built two-service
architecture is far more expensive than considering a different decomposition (or a
co-located deployment unit) up front. As the architect, the job isn't to independently
decide the infra topology (that's Platform Engineering's domain) nor to let a generic
"microservices standard" override a genuine latency requirement without appropriate
scrutiny — it's to flag the tension early, bring both the team and Platform Engineering
into the same conversation (naturally, this is exactly the kind of proposal that belongs
at the Architecture Advice Forum, Module 3, given its cross-domain implications), and make
sure the eventual design decision (network call vs. same-process call vs.
co-scheduled-with-affinity-guarantees) is made with both perspectives present.
</details>

<details>
<summary><strong>Q2.</strong> Platform Engineering announces a move from self-hosted Kafka to a managed cloud messaging service across the whole IT estate, presented as "purely an infrastructure change, no application impact." Should you, as the application architect, simply accept that framing?</summary>

No — verify the framing rather than accepting it, because messaging-technology migrations
are a classic case where "purely infrastructure" undersells real application-level
implications. Concretely check: delivery-guarantee semantics (does the managed service
offer the same at-least-once/exactly-once/ordering guarantees the application code
currently assumes?), API/client-library compatibility (does application code need any
changes at all, or just config?), message size/retention limits that might differ from
self-hosted defaults, and cost-model shifts that could affect the risk register (Module 4)
if the new service has different failure/backpressure behaviour under load. This is exactly
the "application/infrastructure boundary" collaboration the job ad calls out — your role
isn't to block or re-decide Platform Engineering's infrastructure choice, but to make sure
the application-level consequences are actually assessed (possibly surfacing a need for
integration/contract tests, Module 5/6, to verify behaviour didn't silently change) rather
than assumed away by the word "purely."
</details>

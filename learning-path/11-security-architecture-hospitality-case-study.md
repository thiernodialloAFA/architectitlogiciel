# Module 11 — Security Architecture Beyond AI, and a Coherent Hospitality Case Study

## Why this module exists

The earlier modules cover AI-specific security (Module 7) and cite OWASP ASVS in passing
(Module 4), but the role's "security and compliance exposure" risk category and its
cross-domain scope demand broader security-architecture literacy than that — and every
module so far has used a different, disconnected example integration. This module closes
both gaps at once: it adds the missing security-architecture breadth, and gives you **one
coherent, realistic hospitality/travel case study** (a guest booking and loyalty platform)
to carry through interview answers instead of a different toy example each time.

## Learning objectives

- Run a lightweight **threat model** for an application-level design (STRIDE or an
  equivalent structured walkthrough), distinct from — and a precondition for — the
  AI-specific security baseline in Module 7.
- Reason about **identity, authentication and service-to-service authorization** patterns
  (OAuth2/OIDC token flows, service-to-service mTLS or signed tokens, least-privilege API
  scopes) at the level needed to review a design, not to implement an identity provider.
- Apply **data classification** thinking to a real workflow (which fields are PII, which
  are payment data, which trigger GDPR/CNIL obligations) and connect it to the risk
  register's "security and compliance exposure" category from Module 4.
- Know the architectural shape of **incident/disaster-recovery design** (RTO/RPO,
  active-active vs. active-passive, backup/restore testing) well enough to ask Platform
  Engineering the right questions (ties to Module 8's boundary-contract exercise).
- Carry a single, plausible **hospitality case study** — a guest booking + loyalty-points
  platform — through a security lens, ready to reuse in Modules 9 and 12 and in an actual
  interview.

## Curated resources

- **OWASP Application Security Verification Standard (ASVS)**:
  https://owasp.org/www-project-application-security-verification-standard/ — already
  referenced in Module 4; here, actually work through the "Authentication," "Session
  Management," and "Access Control" chapters as your checklist vocabulary.
- **STRIDE threat modelling**: search "Microsoft STRIDE threat modeling" for the original
  framing (Spoofing, Tampering, Repudiation, Information disclosure, Denial of service,
  Elevation of privilege) — a fast, structured way to walk a design for security gaps
  without needing a dedicated security engineer in the room.
- **OAuth 2.0 / OIDC**: read the OAuth 2.0 Simplified guide (Aaron Parecki,
  https://www.oauth.com) for the token-flow vocabulary you need to review, not implement,
  an auth design — specifically the client-credentials flow (service-to-service) and
  authorization-code-with-PKCE flow (user-facing).
- **GDPR** (relevant given Pierre & Vacances operates in France/EU): read the official
  EU GDPR text summary at https://gdpr-info.eu for the articles on data minimisation,
  purpose limitation, and the right to erasure — enough to recognise when a design decision
  (e.g., "we log full guest profile data indefinitely for analytics") creates a compliance
  risk-register entry, not to give legal advice.
- **Google SRE Book**, chapter on "Disaster Recovery" (free online:
  https://sre.google/sre-book/table-of-contents/) — grounding for RTO/RPO vocabulary and
  why untested backups are not a real recovery capability.
- **Article**: search "PCI DSS scope reduction tokenization architecture" for how payment
  data is typically kept out of an application's compliance scope via tokenization —
  directly relevant to a hospitality booking platform that takes payments.

## The case study (use this across Modules 9, 11, 12)

**"StayPoints"** — a fictional but realistic guest booking and loyalty platform for a
hospitality group:

- A **Booking** domain (search availability, reserve a stay, modify/cancel), integrating
  with a **Payments** partner (tokenized card payments, PCI scope kept out of the
  application via a hosted payment page) and a **Property Management System (PMS)** used
  on-site at each property (often a legacy, vendor-controlled system — a realistic
  end-of-life/integration-brittleness risk-register candidate).
- A **Loyalty** domain (points awarded on completed stays, redeemable for discounts),
  owned by a different team, with its own concept of "Guest" that only partially overlaps
  with Booking's concept (a genuine anti-corruption-layer scenario, per Module 9).
- A **Guest Support** feature considering an LLM-assisted triage/response tool (the AI
  Architecture scenario from Module 7 — reuse it here rather than inventing a new one).

## Hands-on exercise

1. Run a lightweight STRIDE pass on the Booking → Payments integration: for each STRIDE
   category, name at least one concrete threat and its mitigation (e.g., Tampering: a
   modified booking-amount request between client and API — mitigated by server-side price
   recalculation, never trusting a client-supplied amount).
2. Classify the data fields StayPoints holds (guest name/email = PII; card number = never
   stored, tokenized at the payment partner; booking history = PII with a legitimate
   retention/erasure question) and write two risk-register entries (Module 4 template) this
   classification surfaces — e.g., "guest profile data retained indefinitely with no
   erasure workflow" as a security/compliance-exposure entry.
3. Write the application/infrastructure boundary contract (Module 8's exercise) for
   StayPoints' disaster-recovery requirements: what RTO/RPO does the business actually need
   for the Booking domain (probably tight — bookings are revenue-critical) versus the
   Loyalty domain (probably looser), and why that split matters for cost conversations with
   Platform Engineering.

## Validation quiz (difficult — scenario based)

<details>
<summary><strong>Q1.</strong> A junior engineer on the Booking team proposes that the mobile app calculate the booking total (nights × rate + fees) and send it to the API, which will store it as-is "since we already validate the rate exists." Using STRIDE, what's wrong, and what's the fix?</summary>

This is a Tampering risk: any value computed client-side and merely checked for
existence (not recomputed) can be manipulated by an attacker controlling the client
request — validating that "the rate exists" says nothing about whether the *total* sent
matches what that rate should produce for that number of nights and fee schedule. An
attacker (or a buggy client) could send a real, valid rate ID alongside a manipulated
total. The fix is architectural, not a client-side patch: the server must independently
recompute the authoritative total from trusted server-side data (the rate, nights, fee
rules) and never trust a client-supplied price field for anything that affects what is
actually charged — the client-sent total, if sent at all, should only ever be used as a
display-consistency check that triggers an error if it disagrees with the server's
calculation, never as the value that gets charged.
</details>

<details>
<summary><strong>Q2.</strong> The Loyalty team wants to store a copy of each guest's full booking history (dates, property, amount paid) "so we can calculate tier status without calling the Booking API every time." What data-classification and compliance concern does this raise, and what's the architectural alternative?</summary>

Copying full booking history into the Loyalty domain duplicates PII (and arguably
payment-adjacent data — amount paid) outside its domain of origin, creating a second place
where a GDPR erasure request ("delete my data") must now be honoured, a second attack
surface for that data, and a data-consistency problem (the copy silently drifting from the
Booking domain's version over time) — this is exactly the kind of decision that should
surface a security/compliance-exposure risk-register entry (Module 4) and, per Module 9's
integration standards, is a candidate for an anti-corruption-layer/event-driven pattern
rather than direct data duplication. The architectural alternative: Booking publishes a
minimal, purpose-specific event when a stay completes (e.g., "stay-completed: guest ID,
tier-relevant point value, date" — not the full booking record), and Loyalty consumes only
that minimal projection, satisfying its actual need (tier calculation) without duplicating
data it doesn't need and without becoming a second erasure-obligation holder for full
booking history. If Loyalty later needs to *display* booking details, it should call
Booking's API for that specific, audited purpose rather than holding a stale local copy.
</details>

<details>
<summary><strong>Q3.</strong> Platform Engineering proposes active-active multi-region deployment for the entire StayPoints platform "for resilience," including the Loyalty domain. As the architect, applying the boundary-contract thinking from Module 8, how do you respond?</summary>

Don't accept "the whole platform" as the unit of this decision without first asking
whether every domain actually needs the same resilience posture — active-active
multi-region is expensive (data replication/consistency complexity, cost, operational
overhead) and that cost should be justified per-domain against actual business impact, not
applied uniformly because it sounds like good practice. Booking is plausibly a strong
candidate: an outage directly blocks revenue and guest-facing availability checks in
real time. Loyalty is a weaker candidate: if point calculation and redemption are briefly
delayed during a regional failover, the business impact is materially lower than a guest
being unable to book a stay — a simpler active-passive setup with a longer acceptable RTO
might be entirely sufficient and considerably cheaper. The right response is to bring
Platform Engineering the actual business RTO/RPO requirements per domain (from the
boundary-contract exercise) and let the infrastructure design follow the business need,
rather than adopting a single resilience tier for the whole platform by default — this is
also a concrete, realistic example of the "collaborate credibly with Platform Engineering"
skill the role calls out, showing up as a cost-aware pushback rather than a blank approval.
</details>

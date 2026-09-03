# Module 3 — Decentralised Governance: Harmel-Law Advice Process & the Architecture Advice Forum

## Learning objectives

- Explain Andrew Harmel-Law's **Architecture Advice Process**: any team/individual can make
  any architecture decision, provided they seek advice from those affected and those with
  relevant expertise — decision authority stays with the proposer, not a central board.
- Describe how the **Architecture Advice Forum (AAF)** operationalises this at scale (as
  used at Xapo Bank and other decentralised orgs): a recurring forum where significant
  proposals are presented for advice, not approval.
- Map the job ad's **three-tier governance model** (team / domain / group scope,
  differentiated by blast radius) onto this process, and articulate when the Group IT
  Architect arbitrates vs. merely advises.
- Identify the failure modes of both over-centralised governance (bottleneck, slow,
  disempowering) and ungoverned "advice in name only" (no real accountability, decisions
  made without seeking advice at all).

## Curated resources

- **Primary source**: Andrew Harmel-Law, *"Scaling the Practice of Architecture,
  Conversationally"* (martinfowler.com) —
  https://martinfowler.com/articles/scaling-architecture-conversationally.html — read this
  in full; it is the direct origin of the advice-process language used in the job ad.
- **Book**: Andrew Harmel-Law, *Facilitating Software Architecture* (O'Reilly, 2024) —
  the fuller treatment, including running AAF-style sessions.
- **Xapo Bank engineering blog**: search for their public posts/talks on their Architecture
  Advice Forum implementation — a named example the job ad references directly ("the
  Architecture Advice Forum model published by Xapo Bank and other European cases").
- **Article**: Martin Fowler, "Who Needs an Architect?" —
  https://martinfowler.com/ieeeSoftware/whoNeedsArchitect.pdf — background on why
  architecture-as-decision-facilitation beats architecture-as-approval-gate.
- **Talk**: search for conference talks (GOTO, QCon) titled "Decentralised Architecture
  Decision Making" or "Advice Process" for practitioner walk-throughs of running a forum.
- **Team Topologies** (Skelton & Pais) — chapters on platform/enabling teams — useful
  background for the "Works alongside Platform Engineering at the application/
  infrastructure boundary" line in the job ad.

## Hands-on exercise

Design (on paper/Markdown, not just in your head) a concrete AAF operating model for a
5-department IT org:

1. Cadence (e.g., biweekly, 90 minutes) and how proposals get on the agenda.
2. The **entry criteria** for "significant enough to bring to the forum" (blast radius
   heuristics: does it cross team boundaries? does it set a precedent other teams will
   copy? does it touch a shared/critical system?).
3. The **artefact** each proposal must bring (an ADR draft, a one-page context/options doc).
4. What happens when advice conflicts across attendees and no consensus emerges — write the
   explicit escalation/arbitration rule, and who holds it (only at group scope, per the job
   ad, "never as a default").
5. How outcomes get captured back into the ADR log (Module 2) so the forum builds "case
   law" rather than just having good meetings.

## Validation quiz (difficult — scenario based)

<details>
<summary><strong>Q1.</strong> A junior engineer proposes a significant cross-team integration change at the AAF. Two senior architects from other domains give strongly conflicting advice. Under Harmel-Law's model, who decides, and does the Group IT Architect need to step in?</summary>

Under the advice process, decision authority stays with the proposer (the junior
engineer) *within the scope they're actually empowered to commit to*, not with whoever is
most senior in the room — that is the entire point of the model, and reversing it (letting
the most senior voice win by default) recreates command-and-control governance under a
participatory veneer. That caveat matters: the advice process decentralises *technical*
decision-making, it does not override an organisation's existing, legitimate controls —
budget authority, security/compliance sign-off, or commitments that bind other teams still
sit wherever they formally sit, and a junior engineer "deciding" doesn't manufacture
authority over spend or regulatory risk they were never delegated. Within the proposer's
actual delegated scope, though, their job is to *seek* advice, weigh it, and still decide,
remaining accountable for the outcome — the two senior architects' disagreement is input,
not a vote. The Group IT Architect only needs to step in if this is genuinely a
**group-scope** decision (per the job ad's blast-radius-scoped tiers) **and** if, after the
proposer has genuinely tried to reconcile the conflicting advice, no consensus is
reachable and the decision is now blocking real progress — arbitration is the explicit
last resort ("never as a default"), not a mechanism invoked just because two seniors
disagreed once. First move: ask the proposer what they've decided and why, given the
conflicting input, and confirm whether anything about the proposal actually requires a
non-negotiable sign-off (e.g., security, legal/regulatory, or a shared-platform commitment)
that sits outside the advice process entirely.
</details>

<details>
<summary><strong>Q2.</strong> A domain lead says: "We don't need to bring this to the AAF — I already approved it myself as domain lead, and that's my authority." The change introduces a new integration pattern that two other domains will likely need to adopt within the year. What's the governance failure here, and how do you address it without overriding the domain lead's authority?</summary>

The failure isn't that the domain lead lacks authority to decide — under the advice
process they do, at domain scope. The failure is that they skipped the **advice-seeking**
step for a decision whose blast radius plausibly extends beyond their domain (it's likely
to become a *cross-domain pattern*, which is squarely "Application architecture and
cross-domain coherence" — the Architect's mandate). The fix is not to override the
decision retroactively, but to correct the *process*: point out, non-confrontationally,
that decisions likely to set precedent for other domains are exactly the ones the advice
process asks you to surface, and invite them to bring it (even after the fact, as a
retrospective share) to the AAF so other domains get visibility and the decision gets
captured as case law others can reference before reinventing it independently. Authority
to decide was never in question; failure to seek relevant advice was.
</details>

<details>
<summary><strong>Q3.</strong> After six months, the AAF has a recurring pattern: only two of five departments regularly bring proposals; the other three "don't have anything significant." Is this evidence the process is working well for those three departments, or a red flag? How would you investigate?</summary>

Treat it as a red flag requiring investigation, not evidence of a quiet, low-risk
department — real software delivery over six months almost always generates *some*
decisions with cross-team or precedent-setting blast radius (new integration, a
significant library/framework choice, a data-ownership boundary change). Silence more
often means one of: (a) the department doesn't know what counts as "significant enough,"
i.e. entry criteria (Module 3 exercise item 2) aren't well understood there — a
process/communication gap, not a maturity signal; (b) decisions are being made and never
surfaced, i.e. the advice process isn't actually being followed, only nominally adopted;
or (c) genuine psychological unsafety/distrust of the forum (fear of being second-guessed)
suppressing participation. Investigate by sampling: pick a handful of recent
department-local ADRs (Module 2) from those three departments and check whether any should
plausibly have gone to the AAF and didn't — that tells you which of (a)/(b)/(c) you're
dealing with, and each has a different fix (clarify criteria; coach on the process;
address trust directly).
</details>

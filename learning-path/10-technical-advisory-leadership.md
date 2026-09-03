# Module 10 — Technical Advisory, Influence & Running Architecture Reviews

## Learning objectives

- Practise **technical advisory as hands-on pairing**, not memo-writing — the job ad's own
  distinction ("This is hands-on, not a memo factory").
- Build the specific soft-authority skills the job ad names explicitly: **influence without
  direct authority**, **holding a room during an architecture review**, and **disagreeing
  well**.
- Run a structured **architecture review** at domain/group scope on delegation, including
  how to prepare, facilitate, and close one out with a documented outcome (an ADR, per
  Module 2).
- Design and sustain a lightweight **community of practice** that raises architecture
  maturity across teams over time, rather than relying solely on top-down review gates.

## Curated resources

- **Book**: Gregor Hohpe, *The Software Architect Elevator* — read specifically for its
  "architect as translator/enabler" framing rather than "architect as decision-maker,"
  directly relevant to influence-without-authority.
- **Book**: Camille Fournier, *The Manager's Path* — chapters on technical leadership and
  influence apply directly even outside a people-management context; particularly the
  material on earning trust through being genuinely useful in the details, not through
  title.
- **Article/Talk**: search for material on **"disagree and commit"** (originating from
  Amazon's leadership principles, widely referenced elsewhere) as one concrete model for
  "disagreeing well" — know both its value (avoids consensus paralysis) and its limits
  (it should follow genuine advice-seeking, not replace it — see Module 3).
- **Book**: Patrick Kua, *Talking with Tech Leads* — grounded, practitioner-level material
  on the day-to-day texture of technical leadership without formal authority.
- **Article**: search "how to run an architecture review" and "RFC review process
  facilitation" from engineering-culture blogs of well-known tech companies for concrete
  facilitation techniques (timeboxing, round-robin input, parking-lot for tangents).
- **Community of Practice model**: search "communities of practice Wenger" (Etienne
  Wenger's original framing) for the underlying theory of why CoPs work as a maturity-
  raising mechanism distinct from mandatory training or top-down standards.

## Hands-on exercise

1. Draft a **facilitation checklist** for leading a 60-minute domain-scope architecture
   review at the Advice Forum (Module 3), covering: pre-read expectations, how you open the
   session, how you draw out dissenting views deliberately (rather than letting the loudest
   voice dominate), how you handle a proposal that clearly needs more work without
   demoralising the presenter, and how you close with a concrete next step (an ADR
   assignment, a follow-up spike, or an explicit "approved, proceed").
2. Write out, verbatim, how you would phrase **disagreeing well** in a specific scenario:
   a respected senior engineer proposes an approach you believe is architecturally unsound,
   in front of their team. Draft the actual sentence(s) you'd use — this is a skill that
   fails under pressure if not rehearsed.
3. Sketch a lightweight **community-of-practice plan** for architecture across five IT
   departments: cadence, who owns convening it (does it have to be you?), what keeps
   attendance genuinely voluntary/valuable rather than another mandatory meeting, and how
   its output (patterns, shared learnings) feeds back into the ADR case-law and standards
   documents from Modules 2 and 9.

## Validation quiz (difficult — scenario based)

<details>
<summary><strong>Q1.</strong> You're leading a domain-scope architecture review. A senior, well-respected engineer confidently presents a design with a clear architectural flaw (a single point of failure the team seems not to have considered), and the rest of the room — mostly junior — is nodding along, deferring to their seniority. How do you raise the concern without simply pulling rank as "the architect," and without letting the room's deference suppress a needed correction?</summary>

Pulling rank ("as the architect I'm saying this is wrong") would model exactly the kind of
authority-based governance the advice process is designed to avoid, and it teaches the
junior engineers in the room that disagreement only ever comes from the top, discouraging
them from ever raising concerns themselves. Instead, ask a genuine, specific question that
surfaces the flaw through inquiry rather than assertion — e.g., "walk me through what
happens if [the single point of failure] goes down mid-transaction — what's the
customer-visible impact and how do we recover?" This does three things: it invites the
presenter to reason through the gap themselves (often the strongest way to get genuine
buy-in on the fix, rather than a top-down correction they merely comply with), it models
for the junior attendees exactly the kind of question they should feel safe asking
regardless of who's presenting, and it keeps the focus on the concrete failure mode rather
than on seniority or ego. If the presenter's answer reveals they hadn't considered it, the
follow-up is collaborative ("worth spiking a mitigation before this goes further, want to
pair on it?") rather than a public verdict — the correction happens, but pulling rank
was never the mechanism.
</details>

<details>
<summary><strong>Q2.</strong> A team you've been "pairing on hard design problems" with (per the job ad's technical-advisory language) starts routing every non-trivial decision through you first, before even discussing it internally, because "you'll have a view anyway." Is this a sign the advisory relationship is working well? What would you change?</summary>

This is a warning sign, not a success signal — the explicit 18-month success marker in the
job ad is teams reaching a point where "ADRs are written without prompting" and
architecture maturity is "visibly improved at the team level," which is the opposite of a
team that has learned to defer every decision upward rather than build its own judgment.
If every non-trivial decision routes through you first, you've become a bottleneck (which
directly undermines the decentralised advice-process model in Module 3) and, worse, you've
inadvertently trained the team out of exercising their own architectural judgment, which is
the actual long-term goal of "raising architectural maturity." The fix: when a team brings
you a decision unprompted, before answering, ask what they've already considered and what
they'd decide if you weren't in the room — coach the reasoning rather than supplying the
answer, explicitly hand decision ownership back ("this sounds like a team-scope call — I
trust your judgment here, let me know if it turns out to have wider blast radius"), and
reserve your direct, substantive input for genuinely hard problems or ones with real
cross-domain implications, rather than becoming a default approval gate for everything.
</details>

<details>
<summary><strong>Q3.</strong> Six months into the role, attendance at your community-of-practice sessions has dwindled to the same three enthusiastic people from one department, despite five departments in scope. What are the likely root causes, and how do you diagnose which one applies before changing the format?</summary>

Don't guess-and-change the format immediately; diagnose first, since several plausible
root causes call for opposite fixes. Likely candidates: (1) **relevance mismatch** — the
topics chosen so far happen to map to one department's stack/problems, so the other four
correctly conclude it's "not for them"; fix is diversifying topic sourcing, e.g.
explicitly rotating which department proposes the next session's topic. (2) **time/
scheduling exclusion** — if the recurring slot was set based on one department's
calendar norms, other departments (different shifts, different meeting-heavy days) may be
silently unable to attend; fix is checking the schedule against all five departments, not
assuming the original slot was neutral. (3) **psychological safety** — if early sessions
featured pointed critique of specific teams' decisions in front of peers, people from
outside the original enthusiastic group may have quietly opted out to avoid being the next
target; this requires a values/format fix (make it explicitly about patterns and shared
learning, not team-specific critique) rather than a scheduling fix. (4) **genuine
irrelevance** — if the enthusiastic department's problems are actually unusually complex/
novel compared to the others' current work, low attendance elsewhere may be a reasonably
honest signal, at least for now, and the right move is patience plus periodic re-invitation
rather than forcing participation. The diagnostic step: ask a few people from
non-attending departments directly, informally, why they've stopped/never started coming —
their answer usually points cleanly at one of the above rather than requiring guesswork.
</details>

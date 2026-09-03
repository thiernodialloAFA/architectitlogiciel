# Module 12 — Interview Preparation, Portfolio & Case Study Presentation

## Why this module exists

Modules 1–11 build the knowledge and vocabulary this role expects. This module is about
the thing they don't teach automatically: **demonstrating it convincingly in the actual
hiring process** — presenting past decisions credibly, handling a live design challenge,
answering behavioural/authority questions specific to this role's "influence without
authority" framing, and negotiating scope/level once an offer is on the table.

## Learning objectives

- Structure and rehearse a **case-study presentation** of a past platform-scale decision you
  owned (a migration, a replatforming, a significant AI integration, or equivalent) using a
  format an interview panel can follow in 10–15 minutes.
- Prepare a **portfolio evidence pack**: artefacts you can actually show (redacted ADRs, a
  C4 diagram, a fitness-function CI run, a risk-register excerpt) rather than only verbal
  claims — directly leveraging the artefacts you built while working through Modules 1, 2,
  4, 5, and 11.
- Anticipate and rehearse answers to the **specific behavioural questions this job
  description all but writes for you**: "tell me about a time you disagreed with a senior
  engineer and how it resolved," "tell me about a hard design decision you made and lived
  with the consequences of," "tell me about influencing a team without formal authority."
- Prepare a credible **30/60/90-day plan** answer that mirrors the job ad's own "six months
  in / twelve months in / eighteen months in" success criteria, so your plan visibly maps
  to what they've already told you they're measuring.
- Understand the **basics of negotiating scope and level** for a role like this (why "I
  need X to succeed" framed as a resourcing/scope question lands better than a generic
  salary negotiation script), without treating this as a comprehensive negotiation course.

## Curated resources

- **Article/talk**: search "How to run a system design interview" from a well-known
  engineering org's hiring blog for the *interviewer's* perspective — understanding what
  they're scoring against (trade-off reasoning, not a "correct" answer) changes how you
  present.
- **Book**: Camille Fournier, *The Manager's Path* — the chapter on interviewing technical
  leaders is relevant even though you're the candidate, not the interviewer; it tells you
  what a good technical-leadership interview is trying to surface.
- **Article**: search "STAR method behavioural interview technical" for the
  Situation-Task-Action-Result structuring technique — a well-known, simple format for
  behavioural answers; don't over-script it into sounding rehearsed, use it as a skeleton.
- **Book**: Gregor Hohpe, *The Software Architect Elevator* — re-read the chapters on
  communicating with different audiences; the same skill of "translating" applies directly
  to translating your past work into a story a hiring panel (which may include
  non-architects) can follow.
- **Article**: search "how to negotiate a job offer software engineering" for current,
  reputable practitioner guides (avoid generic sales-negotiation content that doesn't fit a
  technical-leadership hire) — read for the general framing (multiple levers beyond base
  salary: scope, title, start date, signing bonus) rather than a rigid script.

## Hands-on exercise

### 1. Build your case-study presentation

Pick your strongest platform-scale delivery (a migration, replatforming, or significant AI
integration you led). Structure a 10-minute narrative:

- **Context** (1–2 min): what was the system, what was the pressure/forcing function
  (cost, risk, a business deadline)?
- **The hard decision** (3–4 min): what were the real alternatives (not a strawman — name
  at least two genuine options you considered), what made the choice hard, who disagreed
  and why, and what you actually decided.
- **Consequences you lived with** (2–3 min): what went right, what went wrong, and — this
  is the part candidates most often skip and interviewers most want — what you'd do
  differently now. A case study with zero acknowledged mistakes reads as either dishonest
  or lacking reflection; neither lands well for a role explicitly *not* seeking "a memo
  factory."
- **What it demonstrates about this role's requirements** (1 min): explicitly connect it
  to 1–2 of the job ad's pillars (e.g., "this is the closest prior example I have to
  running an architecture-advice-style process, even though we didn't call it that").

### 2. Assemble a portfolio evidence pack

Using the artefacts you built in earlier modules (or equivalent redacted material from
real past work), assemble:
- One ADR (real or from Module 2's exercise) that shows the *reasoning*, not just the
  decision.
- One C4 diagram (from Module 1's exercise).
- One risk-register excerpt (from Module 4's exercise, or the StayPoints case study from
  Module 11) showing prioritisation logic, not just a list.
- One CI screenshot or log excerpt of a fitness function catching something real (from
  Module 5's exercise).

This is a stronger signal in an interview than describing these practices verbally — it
shows you've actually operated them, not just read about them.

### 3. Draft your 30/60/90-day plan

Using the job ad's own "six months / twelve months / eighteen months" milestones as your
template, write a first-90-days plan: what would you actually do in week 1 (mostly
listening/mapping — don't propose the risk register before you've met the five
departments), what artefact would exist by day 30 (a first pass at the application
inventory?), and how it sets up the six-month "first-cut stocktake" milestone. Being able
to show you understood their own success criteria well enough to build toward them from
day one is a strong signal of genuine engagement with the job description, not a generic
"first 90 days" template.

### 4. Rehearse three specific behavioural answers

Using STAR, draft (and say out loud, don't just write) answers to:
- "Tell me about a time you disagreed with a senior or more experienced colleague on a
  technical decision. How did you handle it, and what happened?"
- "Tell me about a hard architectural decision you made, that you were personally
  accountable for, where you couldn't get full consensus."
- "Tell me about a time you influenced a team's technical direction without having formal
  authority over them."

## Validation quiz (difficult — scenario based)

<details>
<summary><strong>Q1.</strong> During your case-study presentation, an interviewer asks: "What would you do differently if you were doing this migration again today?" You genuinely believe the original approach was correct given what you knew at the time. How do you answer without either (a) inventing a fake regret to seem humble, or (b) sounding defensive/unreflective?</summary>

Neither fabricating a regret nor insisting nothing would change serves you well — the
question is really testing whether you can distinguish "the decision was right given the
information and constraints at the time" from "there's nothing I'd do differently with the
benefit of hindsight or different constraints," which are not the same claim. A strong
answer holds both: state clearly that you'd make the same *core* decision again given the
same constraints (this is honest and shows conviction, which the job ad explicitly values —
"hold a room during an architecture review, disagree well" implies you don't cave to
implied pressure to manufacture regret), but then identify something genuinely narrower
you'd change: the sequencing, how early you brought in a particular stakeholder, a
monitoring gap you only discovered post-migration, or a piece of communication you'd
handle differently. If you truly cannot identify anything, that itself is worth
interrogating honestly before the interview — it's rare for any platform-scale delivery to
have gone through with zero learnings, and panels are generally probing for
self-awareness, not for you to discover a flaw that doesn't exist.
</details>

<details>
<summary><strong>Q2.</strong> An interviewer presents a live design challenge loosely modelled on the job's "application landscape stocktake" and gives you only 20 minutes. You realize partway through that you won't have time to cover both the risk-prioritisation framework and a concrete example. Which do you prioritise, and why, given what this specific interview is actually testing?</summary>

Prioritise demonstrating the *prioritisation reasoning* on one concrete, worked example over
reciting a complete abstract framework — this mirrors the actual job, where the value is in
correctly triaging real, messy systems, not in producing a beautifully generic taxonomy
nobody has applied. Interviewers running a system-design-style exercise are almost always
scoring your reasoning process (how do you ask clarifying questions, how do you handle
ambiguity, how do you justify a trade-off out loud) far more than whether you produced a
complete deliverable in 20 minutes — nobody expects a finished risk register in that time.
Concretely: pick one plausible system from whatever context they've given you, walk through
2–3 risk dimensions for it out loud (technical debt? security exposure? EOL?), explicitly
say "in a real stocktake I'd repeat this systematically across the portfolio and prioritise
using [impact × likelihood, blast radius]," and use your remaining time on that one worked
example's depth rather than starting a second system you won't finish. Naming the framework
briefly while demonstrating it on one real example is stronger than an abstract framework
recited with no application.
</details>

<details>
<summary><strong>Q3.</strong> During offer negotiation, the recruiter says the salary band is fixed but asks if you have "any other requirements." Given this specific role's description (first-year foundational stocktake work, group-wide scope, cross-team authority-by-influence), what would be a substantively useful thing to negotiate for, beyond salary — and why does it matter more here than in a typical IC engineering offer?</summary>

Given that the job ad's own "what success looks like" section describes a first-year
mandate that depends on cross-team cooperation the role has no formal authority to compel
("influence without direct authority"), a substantively useful ask is an **explicit,
named executive sponsor commitment** — confirmation that the Group CTO relationship
described in the job ad (co-authoring the roadmap, delegating review authority) will
include a concrete, early joint communication to the five IT department heads establishing
the role's mandate and the advice-process model, rather than the new hire having to
manufacture that legitimacy alone from day one. This matters more here than in a typical IC
offer because the entire operating model depends on organisational buy-in the role cannot
compel by title alone — a Group IT Architect who arrives without that visible top-down
framing is set up to spend months re-litigating their own legitimacy before doing any of
the stocktake/governance work the six-month milestone requires. Other reasonable asks
(start date flexibility to complete notice/relocation, a defined budget for tooling in
year one, protected time before the first AAF session to actually complete the listening/
mapping phase from your 90-day plan) are also legitimate, but the sponsorship question is
the one most specific to *this* role's structural risk, not a generic negotiation lever.
</details>

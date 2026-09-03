# Module 4 — Application Landscape Stocktake, Technical Debt & the Risk Register

## Learning objectives

- Run an **honest inventory** of an application portfolio across multiple IT departments:
  what exists, who owns it, what it depends on, and what state it's in.
- Classify architectural risk across the five dimensions the job ad names explicitly:
  **technical debt, end-of-life systems, integration brittleness, security/compliance
  exposure, AI-readiness gaps** — and know how to score/prioritise across dissimilar risk
  types without false precision.
- Build and maintain a **risk register as a living artefact** (not a one-off slide deck)
  that feeds a multi-year roadmap and budget cycle.
- Use recognised frameworks for portfolio assessment (Gartner TIME model, application
  rationalisation matrices, technical debt quadrants) without over-indexing on any single
  consultancy's proprietary framework.

## Curated resources

- **Article/Framework**: Gartner **TIME model** (Tolerate / Invest / Migrate / Eliminate)
  for application portfolio rationalisation — search "Gartner TIME model application
  portfolio" for accessible summaries; useful vocabulary for stakeholder conversations even
  if you don't use Gartner's tooling.
- **Book**: Martin Fowler et al., chapters on **Technical Debt Quadrant** —
  https://martinfowler.com/bliki/TechnicalDebtQuadrant.html — reckless/prudent ×
  deliberate/inadvertent debt classification, essential vocabulary for a risk register
  that needs to distinguish "known trade-off" debt from "nobody realised" debt.
- **Book**: Nick Tune & Jean-Georges Perrin or similar — **"Architecture Modernization"**
  style references on portfolio-scale replatforming decisions (search for recent
  O'Reilly/Manning titles on legacy modernization strategy).
- **Article**: ThoughtWorks Technology Radar — search past radars for "risk-first
  architecture" and "technical debt" entries for how a respected industry radar frames
  prioritisation.
- **NIST / OWASP** for security-exposure classification baseline: OWASP Application
  Security Verification Standard (ASVS) — https://owasp.org/www-project-application-security-verification-standard/
  — useful as a checklist dimension for the "security and compliance exposure" risk type.
- **Article**: search "AI-readiness assessment framework application architecture" for
  recent (2023+) practitioner frameworks scoring systems on API-accessibility of data,
  latency tolerance for LLM calls, and data governance maturity — this dimension is newer
  and less standardised than the other four, so triangulate multiple sources.

## Hands-on exercise

Design a **risk register template** (a spreadsheet or a Markdown/CSV table you'd actually
use) with these columns, and populate it with 5 example entries (real or plausible) to
prove the model works in practice:

| Column | Purpose |
|---|---|
| System/Application | Unique identifier |
| Owning department/team | Accountability for the system itself |
| Risk owner | The named individual accountable for *treating this specific risk* — not always the same person as the system owner, and the field that actually makes the register auditable rather than just descriptive |
| Risk category | One of: technical debt / end-of-life / integration brittleness / security-compliance / AI-readiness |
| Description | What, specifically, is wrong |
| Current controls | What, if anything, already mitigates this today (monitoring, manual process, compensating control) — needed to assess *residual* risk, not just the raw description |
| Impact if realised | Business consequence, not just technical |
| Likelihood | Rough qualitative scale (Low/Med/High) — resist false-precision scoring |
| Inherent vs. residual risk | Score both: inherent (if nothing were done) and residual (given current controls) — collapsing these into one number hides how much the existing controls are actually doing |
| Blast radius | Team / domain / group — ties back to the governance model in Module 3 |
| Treatment decision | Tolerate / Invest / Migrate / Eliminate (TIME) or equivalent — a decision, not just a label |
| Cost-to-fix estimate | Rough order of magnitude, for budget-cycle conversations |
| Target date / status | Open / In progress / Closed, with a target date for the treatment — without this the register can't distinguish "we decided to tolerate this" from "nobody has looked at this since it was logged" |
| Review cadence | How often this entry gets re-assessed — this is what makes it "living" |
| Last assessed / evidence | Date of last review and a pointer to the evidence behind the current score (an incident ticket, an EOL vendor notice, a pen-test finding) — a register with no evidence trail is an opinion, not a risk assessment |

Then write one paragraph on how this register would be co-authored with a Group CTO and
fed into a **multi-year roadmap** — specifically, how you'd avoid the register becoming
a static document nobody revisits after the first stocktake.

## Validation quiz (difficult — scenario based)

<details>
<summary><strong>Q1.</strong> Two systems are flagged as high risk: System A has severe technical debt (hard to change, but stable, no incidents) and System B has moderate integration brittleness but has caused three production incidents in the last quarter. A stakeholder wants to fix System A first because "the code is worse." How do you prioritise, and what's wrong with "worst code first"?</summary>

On the evidence given, System B ranks first, and "worst code first" is the wrong ordering
principle — a risk register's job is to prioritise by *evidenced or imminent business
impact*, not by how unpleasant the code is to work in. But be careful not to over-correct
into an equally unsafe heuristic: "no incidents yet" does **not** mean System A's debt is
*by definition* tolerable — debt with zero incident history can still be sitting on an
EOL platform with a hard vendor cutoff date, blocking an already-committed roadmap item,
or masking a security/compliance exposure that simply hasn't been triggered yet. Absence
of incidents is evidence of lower *realised* impact so far, not proof of low risk. The
defensible answer is conditional: rank System B first *given the stated evidence*
(recurring, demonstrated business impact this quarter, a brittleness pattern suggesting a
fourth incident is likely), while explicitly checking whether System A has an unstated
forcing function — an imminent EOL date, a blocked strategic migration, a known compliance
gap — before finalising the order. If it does, that changes the ranking regardless of
incident count. Ugly code with no such forcing function and no incident history should
still be tracked (risk register: yes; entry stays open) but ranked below anything with a
demonstrated or imminent business-impact trail, because likelihood × impact — assessed
honestly, not just by incident count — is what a register exists to rank. The
stakeholder's instinct usually reflects legitimate frustration
(engineers dislike touching System A) rather than a risk-based argument; separate those
two conversations.
</details>

<details>
<summary><strong>Q2.</strong> The Group CTO asks you to give each risk register entry a single numeric score (1–100) "so we can rank everything precisely for the budget meeting." You know the five risk categories (debt, EOL, brittleness, security, AI-readiness) are not naturally comparable on one scale. What do you do?</summary>

Push back on false precision, but don't refuse to help rank — give a qualitative,
defensible ranking instead of a spurious single number. A single 1–100 score across five
incommensurable risk types manufactures precision that doesn't exist (why is a security
exposure scored 62 rather than 58, exactly?) and, worse, invites everyone to stop
questioning the reasoning behind it because "the algorithm said so" — which then becomes
a liability the moment someone challenges the ranking in the budget meeting and there's no
real justification behind the digits. The better approach: keep category-relative
qualitative bands (Low/Med/High likelihood × impact, per Module 4's register columns),
and produce a *forced-rank top-10* list with a one-line rationale per item for the budget
conversation — rankable and defensible, without hiding the judgment calls behind a fake
score. If the CTO still wants a number for a specific slide, you can derive a simple
Impact×Likelihood product (e.g. 3×3 grid → 1–9) as long as everyone understands it's an
ordinal heuristic, not a measurement.
</details>

<details>
<summary><strong>Q3.</strong> Six months after the first stocktake, you notice the risk register hasn't been updated — the same 40 entries from launch day, no new entries, no closed items despite two systems having been migrated. What does this indicate, and what's the structural fix (not just "remind people")?</summary>

It indicates the register has become a one-off artefact rather than the "living artefact"
the job ad and Module 4 both call for — almost always because updating it was never wired
into any team's actual workflow, only produced once for a stocktake presentation. Reminders
don't fix a structural problem. The fix is to attach register maintenance to events that
already happen: (1) closing a migration/remediation ticket that references a register
entry should be a required step that also updates/closes that entry (a checklist item in
the team's existing "definition of done" for such work, not a separate ceremony); (2) the
Architecture Advice Forum (Module 3) should surface new entries naturally, since
significant proposals often reveal or resolve risk items — make "does this open or close
a register entry?" a standing agenda prompt; (3) set an explicit review cadence per entry
(the column proposed in the exercise) with owners accountable for re-confirming or
updating status, and report staleness (entries untouched past their cadence) as its own
visible metric to the Group CTO.
</details>

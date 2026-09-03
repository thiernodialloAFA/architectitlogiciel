# Module 7 — AI Architecture: LLM Integration & Agentic Patterns at the Application Level

## Learning objectives

- Articulate the explicit scope boundary in the job ad: **application-level AI
  architecture** (feature design, agentic tooling patterns, security baseline for AI
  artefacts, architectural implications of LLM-based components) — **not** data science or
  ML platform work.
- Design **LLM integration patterns** for application features: retrieval-augmented
  generation (RAG), tool/function-calling, prompt-management-as-a-first-class-artefact,
  streaming responses, and graceful degradation when a model call fails or is slow.
- Evaluate **agentic harness/runtime patterns** — the frameworks and design choices that
  turn an LLM into a productive "agent" (tool use, planning loops, memory, guardrails) —
  and know the current landscape well enough to have an informed opinion, without needing
  ML-training-level depth.
- Define a **security baseline for AI artefacts**: prompt-injection defence, output
  validation/sanitisation before it reaches downstream systems, data-exfiltration risk via
  tool calls, secrets/PII handling in prompts and logs, and supply-chain risk of
  third-party models/agent frameworks.
- Feed AI-readiness assessment into the risk register from Module 4 (an application that
  can't safely expose its data via API, or that has poor observability, is not "AI-ready"
  regardless of how good the model is).

## Curated resources

- **OWASP Top 10 for LLM Applications**: https://owasp.org/www-project-top-10-for-large-language-model-applications/
  — the most widely referenced community-maintained **security baseline for AI
  artefacts** available today (not a formal industry standard, but the closest common
  reference point most practitioners and auditors will expect you to know);
  read all ten risks (prompt injection, insecure output handling, training data poisoning
  is more ML-side but still worth knowing, excessive agency, etc.).
- **Article**: Simon Willison's blog (https://simonwillison.net/) — search his posts on
  "prompt injection," "agents," and "tool use" — one of the most consistently rigorous
  practitioner voices on the application-security implications of LLM integration.
  Specifically read his writing on why prompt injection is architecturally different from
  SQL injection and harder to fully solve.
- **Anthropic engineering blog**: "Building effective agents" —
  https://www.anthropic.com/engineering/building-effective-agents — a clear, practitioner
  framing of agent design patterns (workflows vs. agents, when to use which) directly
  relevant to "the runtime patterns that turn LLMs into productive agents."
  applied at the application/UX level.
- **Article/pattern reference**: Retrieval-Augmented Generation (RAG) — read the original
  Lewis et al. paper abstract for grounding, then a current (2024+) practitioner guide on
  production RAG architecture pitfalls (chunking, retrieval quality, freshness) — search
  "production RAG architecture lessons learned."
- **Model Context Protocol (MCP)** — https://modelcontextprotocol.io — an open,
  Anthropic-originated protocol (not a universally adopted standard, but with growing
  multi-vendor support) for how applications expose tools/data to LLM agents; worth
  understanding as a concrete, live example of the kind of "agentic tooling" integration
  pattern and architectural standard the role is asked to "curate" — evaluate its actual
  adoption status in your stack rather than assuming it's already the settled norm.
- **Article**: search for recent (2024+) posts on "agent framework comparison" (e.g.,
  LangGraph, Semantic Kernel, or custom orchestration) for current landscape awareness —
  the tooling shifts fast, so prioritise sources within the last 12 months and cross-check
  more than one.

## Hands-on exercise

1. Pick one real or plausible application feature (e.g., "AI-assisted guest support
   triage" for a hospitality context) and write a **one-page architecture note** covering:
   - Which integration pattern applies (simple prompt call, RAG, tool-calling agent) and
     why, given the feature's needs.
   - Where the LLM call sits in the system (synchronous in the request path? async with a
     callback/webhook? streamed to the client?) and the failure-mode/fallback behaviour if
     the model call times out or returns something unusable.
   - The **security baseline** applied: how untrusted user input reaching the prompt is
     handled, how model output is validated before it triggers any downstream action
     (never let raw LLM output directly execute a privileged operation), and what's logged
     vs. deliberately not logged (PII/secrets hygiene).
2. Write an ADR (Module 2 format) documenting the choice of agentic harness/pattern for
   this feature, including alternatives considered and consequences.
3. Add this decision as a candidate discussion topic for the Architecture Advice Forum
   (Module 3) — write the one-paragraph pitch you'd bring to the forum.

## Validation quiz (difficult — scenario based)

<details>
<summary><strong>Q1.</strong> A product team wants to give an LLM-based support agent a tool that can directly execute refunds up to €500 without human review, arguing "the model is very accurate in testing." As the architect reviewing this, what's your primary concern, and how do you frame it — noting the job ad's explicit line that this is "not data science or ML platform work"?</summary>

The primary concern is architectural, not purely a model-quality/ML-accuracy question —
and this is a good place to be precise about the job ad's scope boundary rather than
over-apply it: "not data science or ML platform work" means you're not the one training or
statistically evaluating the model, but as the application architect you are still
responsible for *specifying* what evaluation and monitoring a feature like this needs
before its blast radius is widened — that's an architectural/governance requirement, not
a hand-off. So the answer has two parts, not one. First, "very accurate in testing" says
nothing about worst-case/adversarial behaviour, and this maps directly to OWASP LLM Top
10's "Excessive Agency" risk: granting an agent an action with real-world, hard-to-reverse
consequences (moving money) without a human-in-the-loop or a constrained, auditable
authorization boundary is a system design failure independent of how well the model
performs on average — it takes only one adversarial prompt-injection or edge-case failure
to cause real financial/reputational damage. Second, before *any* increase in autonomy is
even considered, require a risk-proportionate evaluation and monitoring plan (adversarial
test cases, production drift monitoring, a defined rollback trigger) — you don't need to
run that evaluation yourself, but you do need to insist it exists and gate the decision on
it. The architectural fix: require human approval above a much lower threshold (or for all
cases initially), log every proposed action with full context for audit, treat the
refund-execution tool as a privileged capability requiring its own security review, and
require the evaluation/monitoring plan above as a precondition for ever raising the
threshold — not a routine feature toggle.
</details>

<details>
<summary><strong>Q2.</strong> A team building a RAG-based internal knowledge assistant reports "it works great in the demo" but you notice the demo only ever asked questions the retrieval corpus clearly covered. What architectural risk are they missing, and what would you ask them to add before this ships more broadly?</summary>

They're missing the failure mode where the retrieval step returns weak/irrelevant context
but the LLM still confidently answers anyway — a very common RAG failure that demos
systematically hide because demo questions are cherry-picked to hit well-covered corpus
content. Architecturally, this is a graceful-degradation gap: the system needs an explicit
policy for "low-confidence retrieval," not just a happy-path prompt. One important
precision: raw vector-similarity/retrieval scores are usually *not* a calibrated
confidence measure — a high similarity score can still retrieve a passage that doesn't
actually answer the question, and a low score doesn't always mean "no answer exists," so
treating a similarity threshold alone as a safety control creates a false sense of
protection. Concretely, ask for: (1) an **evaluated** answerability policy — a held-out
test set spanning in-corpus, out-of-corpus, stale, and adversarial questions, used to tune
and validate (not just assume) whatever threshold or heuristic decides "I don't have
enough information" vs. answering; (2) visible source citations in every answer, *and*
a check (even a lightweight one, e.g. an LLM-graded or rule-based entailment check) that
the answer is actually supported by the cited passages — citations alone don't prove
grounding, they just give a human something to verify against; (3) production monitoring
of the abstention rate and spot-checked answer quality over time, since retrieval quality
degrades as the corpus grows or goes stale, not just at launch. None of this requires
ML-training expertise — it's an application-architecture requirement on how the system
behaves under uncertainty, evaluated with real held-out data rather than a demo script.
</details>

<details>
<summary><strong>Q3.</strong> A team wants to log full prompt and response text (including user messages) for every LLM call "for debugging," and plans to store these logs in the same centralized logging system used for all application logs, retained for 2 years per the company's standard log-retention policy. What's the architectural/governance concern, and what would you require instead?</summary>

Standard log-retention policy was almost certainly designed for operational log data, not
for content that may contain free-text user input (potential PII, sensitive personal
disclosures if the assistant handles anything remotely personal) sitting for two years in
a system with broader access than a security/compliance review of *this specific new data
category* would likely approve. This is exactly the "security baseline for AI artefacts"
and "security and compliance exposure" risk category (Module 4) the role must actively
manage; treating "full prompt/response text" as just another log line understates its
sensitivity and skips a required control point. Before shipping, require: (1) a data
classification pass on what a prompt/response log actually contains for this specific
feature (does it plausibly include PII, financial details, health information?); (2) if
so, either redaction/anonymisation before logging, a shorter retention period specific to
this data category, or storage in a more restricted-access system than default app logs —
whichever the compliance/security review determines; (3) explicit logging of *what wasn't
logged* (a documented decision) so future auditors know this was a deliberate choice, not
an oversight — tying back to the ADR practice from Module 2.
</details>

# architectitlogiciel

Preparation material and a working application for the **Group IT/Software Architect**
role (Pierre & Vacances Group).

## 📖 Read it on GitHub Pages

The learning path is published automatically as a browsable website via **GitHub Pages**:
the [`Deploy learning path to GitHub Pages`](.github/workflows/pages.yml) workflow builds
the Jekyll site and deploys it on every push to `main` (it can also be run manually from
the **Actions** tab). On its first run the workflow enables Pages for the repository
(source: **GitHub Actions**) — no manual settings are required as long as GitHub Actions
is allowed to manage Pages; otherwise enable it once under **Settings → Pages → Source →
GitHub Actions**. The published URL has the form
`https://thiernodialloafa.github.io/architectitlogiciel/` and is shown on the workflow's
deploy step and at the top of **Settings → Pages**.

The site is a plain Jekyll build using GitHub Pages' built-in, natively-supported plugins
(`_config.yml` at the repo root) — every Markdown file in this repo, including this one,
renders as a page with no further changes needed; there is no separate build step to
maintain. The `app/` directory is excluded from the site: it is software, not content.

## Contents

- **[`learning-path/`](learning-path/README.md)** — a 12-module learning path covering every
  responsibility in the job description (architecture governance, application architecture
  and cross-domain coherence, application landscape stocktake and risk prioritisation, AI
  architecture, cloud literacy, security architecture, technical advisory/leadership, and
  interview/portfolio preparation for this specific role). Each module has curated
  resources, a hands-on exercise, and a difficult, scenario-based validation quiz with an
  answer key.
- **[`app/`](app/README.md)** — the **Architecture Governance Platform** (React + TypeScript
  frontend, Spring Boot/Java 21 backend, PostgreSQL), implemented to the proposal's
  recommended v1 scope: application landscape, architecture risk register, and ADR decision
  case law with an append-only audit trail, seeded with the hospitality-group case study.
  Run it with `cd app && docker compose up --build`; CI builds and tests it on every change.
- **[`docs/architecture-governance-platform-proposal.md`](docs/architecture-governance-platform-proposal.md)**
  — the plan for the **Architecture Governance Platform** covering architecture governance,
  application landscape stocktake & risk register, cross-domain standards, and
  AI-architecture advisory. Its recommended v1 slice (§6) is implemented in
  [`app/`](app/README.md); the remaining modules are tracked on the app's Roadmap page.
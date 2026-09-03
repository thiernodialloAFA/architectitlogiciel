# architectitlogiciel

Preparation material and a proposed application for the **Group IT/Software Architect**
role (Pierre & Vacances Group).

## 📖 Read it on GitHub Pages

This repository is set up to publish as a website via **GitHub Pages**, so the learning
path can be read as a browsable site instead of raw Markdown files.

**One-time setup required (repository admin only):** GitHub Pages is not enabled
automatically — an admin must turn it on once:
1. Go to **Settings → Pages** in this repository.
2. Under **Build and deployment → Source**, choose **Deploy from a branch**.
3. Under **Branch**, choose `main` (or the default branch) and folder **`/ (root)`**, then
   **Save**.
4. After a minute or two, the site will be published at a URL of the form
   `https://thiernodialloafa.github.io/architectitlogiciel/` (the exact URL is always shown
   at the top of the **Settings → Pages** screen once enabled).

The site is a plain Jekyll build using GitHub Pages' built-in, natively-supported plugins
(`_config.yml` at the repo root) — every Markdown file in this repo, including this one,
renders as a page with no further changes needed; there is no separate build step to
maintain.

## Contents

- **[`learning-path/`](learning-path/README.md)** — a 12-module learning path covering every
  responsibility in the job description (architecture governance, application architecture
  and cross-domain coherence, application landscape stocktake and risk prioritisation, AI
  architecture, cloud literacy, security architecture, technical advisory/leadership, and
  interview/portfolio preparation for this specific role). Each module has curated
  resources, a hands-on exercise, and a difficult, scenario-based validation quiz with an
  answer key.
- **[`docs/architecture-governance-platform-proposal.md`](docs/architecture-governance-platform-proposal.md)**
  — a proposed plan for an **Architecture Governance Platform** (React frontend,
  Spring Boot backend, PostgreSQL) covering architecture governance, application landscape
  stocktake & risk register, cross-domain standards, and AI-architecture advisory. This is
  a **proposal awaiting validation** before any application code is written — see the open
  questions at the end of that document.
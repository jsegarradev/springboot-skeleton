---
name: scaffold-project
description: Scaffold a brand-new app to these conventions — Angular, Spring Boot, or (rarely) a monorepo of both. EMPTY repo only. Sets up toolchain, dependencies, structure, style, architecture enforcement, CI, the gate stack, and a green skeleton before any feature code. Trigger on "scaffold", "new project", "bootstrap this repo".
---

# scaffold-project

Bring an **empty** repository up to these conventions, ready for the `omc-pipeline.md` feature loop.

## Use when
- The repo is empty — no build files, no source.

## Do not use when
- The repo already has code/build files → use `adopt-conventions` instead.

## Guard (blocking, first)
Confirm the repo is empty: no `pom.xml` / `build.gradle`, no `package.json`, no `src/`. If anything
is present, stop and hand off to `adopt-conventions`.

## Steps
1. **Interview up front** (`AskUserQuestion`) — gather these **before generating anything or
   committing**; never infer them from ambient context (email domain, cwd name, etc.):
   - **Stack:** Spring Boot, Angular, or Monorepo (both). Monorepo is rare — only when the project
     genuinely needs backend + frontend together.
   - **Build tool** (if a Spring Boot backend): Maven (recommended) or Gradle.
   - **Git commit identity:** the author name/email to commit under — **offer the identities already
     configured** (`git config --global user.*`, plus any repo-local/known ones) and let the user pick.
   - **groupId / base package** (if a Spring Boot backend): ask explicitly (e.g. `com.<org>.<app>`).
   - **Backend shape** (if a Spring Boot backend): **single-context service** (default — one bounded
     context; enforcement A + C only) or **Spring Modulith monolith** (multiple bounded contexts as
     modules; adds Spring Modulith + enforcement B / `ModularityTest`). Default to single-context
     unless the project clearly spans several domains.
   - **Deploy target:** does this project deploy to the **shared OVH VPS**? If yes, include the
     optional `vps-deploy.md` module (deploy → tag → live-verify pipeline, `.env` single-source, PAT
     setup). If no / not deployed / a different target, omit it — generic `loop.md`/`gates.md`
     live-verify applies, and another target would get its own `<target>-deploy.md`.
2. **Read the conventions** for the chosen stack(s): `springboot.md` and/or `angular.md`, plus
   `loop.md` (Baseline), `gates.md`, `git.md`.
3. **Init the repo** — `git init -b main`; set the chosen commit identity locally
   (`git config user.name` / `user.email`).
4. **Scaffold each chosen framework** per its Scaffolding section:
   - **Spring Boot** — `spring init … --build <maven|gradle> -g <groupId> --package-name <base-package>`
     (the chosen tool, groupId, and package; Java 25; deps per `springboot.md §1`), single build module;
     hexagon by package (single-context) **or** per-context Modulith modules (monolith), per the chosen
     shape; add the OpenAPI generator, Lombok/MapStruct, ArchUnit, JMolecules, Spotless — and, **only
     for the Modulith-monolith shape**, Spring Modulith.
   - **Angular** — `ng new … --routing --style=scss --ssr=false --skip-git`; install PrimeNG +
     pinned deps; `core/features/shared` structure; ESLint `@stylistic`; Playwright.
   - **Monorepo** — `backend/` + `frontend/`, each scaffolded as above.
   - **Run surface (every stack)** — emit a `Makefile` (`run`/`test`/`build`/`down`), a multi-stage
     `Dockerfile`, and a `docker-compose.yml` wiring the app to its engine (healthcheck +
     `service_healthy`) from `templates/`, parameterized to the project's engine / build tool / JDK — so
     a clean machine runs it in ≤2 commands (`loop.md` baseline).
5. **Install the gate stack** (`gates.md` baseline) from `templates/`: hooks → `.githooks/`;
   `ci.yml` → `.github/workflows/` (**emit only the component jobs the repo holds** — a single-stack
   scaffold drops the other stack's job and its language setup; monorepo keeps both); `ArchitectureTest`
   (+ `ModularityTest` if monolith); 
   `post-deploy-verify.sh` → `scripts/`; run `protect-main.sh` (best-effort — it skips gracefully when
   the plan lacks private-repo protection; `main` being unprotected on a private Free-plan repo is
   expected); merge `pipeline-hook.json` into `.claude/settings.json`; copy `FRICTION.md` to the repo root.
6. **Copy the conventions set** into the repo (so it travels with the project) — **include
   `vps-deploy.md` only if the shared-VPS deploy target was chosen** (step 1); omit it otherwise; if
   the harness uses `.claude/skills/`, place these three skills there too.
   - **Git-ignore the agent/tooling artifacts by default** — `.conventions/`, `.claude/`, and
     `FRICTION.md` are the agent's local workflow scaffolding + process ledger, **not** project source;
     committing them bloats history and mixes convention/process churn into feature diffs. Add them to
     `.gitignore` unless the user **opts in** to committing them (e.g. wants a fully self-contained repo,
     or a team-shared friction log). Ask once; default is ignore. They stay on disk either way, so
     `sync-conventions` / `promote-friction` still operate on the local copies.
7. **Confirm a green skeleton** — build + tests green on the empty skeleton (`./mvnw verify`;
   `ng build` + `ng test --watch=false`) and the architecture tests pass; and **`make run` stands the
   app up on a clean machine (Docker only)** (the walking-skeleton run proves the served slice).
8. **Write the `README.md`** — what the app is, the stack, how to run + test locally, and a pointer to
   `AGENTS.md` (the conventions and how to start the session). Replace any generator boilerplate
   (e.g. Spring's `HELP.md`).
9. **Commit** — `[scaffold] initial <stack> skeleton` (one commit per component in a monorepo), under
   the chosen identity, no AI trace (`git.md`).

## Output
A conventions-compliant skeleton with the gate stack installed, a green build, a clean-machine run
surface (`make run`), and a `README`. Next:
run `walking-skeleton` to prove the stack end-to-end before real features.

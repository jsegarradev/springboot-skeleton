# Gates — what makes the conventions enforceable

Conventions and the loop are **context** an agent reads; **gates are enforcement** a machine runs. A
gate fails the build, commit, or release when violated, so it cannot be forgotten. Anything not
backed by a gate is advisory. **Install the full stack at baseline**, before feature work.

## The stack (5 layers)

| Layer | Runs | Enforces | Template |
|---|---|---|---|
| **pre-commit hook** | local, each commit | one-commit-one-component; format-check + compile | `templates/hooks/pre-commit` |
| **commit-msg hook** | local, each commit | bracket-tag scheme | `templates/hooks/commit-msg` |
| **CI** | server, each PR (unbypassable) | style + static analysis (magic-number lint) + full tests — **runs the architecture tests** | `templates/ci.yml` |
| **live-verify gate** | after deploy (remote) — or after `compose up` (local), when deploy is optional | live-verify on the **assembled system** at `APP_URL`: health + the **standing aggregate e2e journey** (drives every entry point, rolled back); non-zero exit fails the release/slice | `templates/post-deploy-verify.sh` |
| **branch protection** | platform | no merge to `main` without green CI; no force-push/deletion | `templates/protect-main.sh` (needs public repo or a paid plan — see step 5) |

## Install at baseline (the 5 gates + a pipeline-entry steering hook)
1. **Hooks** — copy `templates/hooks/*` → `.githooks/`, `chmod +x`, `git config core.hooksPath .githooks`. (Also runs the `post-commit` local live-loop when a project wires one.)
2. **CI** — from `templates/ci.yml`, write `.github/workflows/ci.yml` with **only the component jobs the
   repo holds** (backend-only → Backend + `setup-java`; frontend-only → Frontend + `setup-node`; monorepo
   → both). The `hashFiles(...)` guards stay on whatever is emitted, so a component added later still activates.
3. **Architecture tests** — add `ArchitectureTest` (always) and `ModularityTest` (monolith only) from `templates/` to the backend test sources, set the base package; CI gates them via its full test run — they run **inside the CI gate**, not as a separate gate (so the six steps here install the five gates + the step-6 steering hook). A fresh scaffold has empty rings, so also ship the arch-test framework's empty-match relaxation (ArchUnit: `archunit.properties` with `failOnEmptyShould=false`) — the rules activate as slices populate the rings (see `springboot.md §3.1`).
4. **Live-verify gate** — copy `templates/post-deploy-verify.sh` → `scripts/`, point it at the
   **standing aggregate e2e journey** (one secret-guarded endpoint that drives *every* entry point,
   rolled back) at `APP_URL`. Bind it to the target: where the app deploys, wire it as the **final step
   of the deploy workflow** (fail the release on non-zero); where **deploy is optional**, run it against
   the **local composed stack** (`make run`, with `APP_URL` = the composed stack's URL) as the loop's
   definition-of-done (`loop.md` step 8) — same script, real image + real infra, only `APP_URL` differs.
   *(Optional upgrade: a CI job that does `compose up → verifier → down` turns the local live-verify into
   an enforced gate.)* Coverage-completeness rule: **every new entry point (`port/in`) must be wired into
   the journey and asserted before its slice is done** — the verifier can't silently under-cover.
5. **Branch protection** — run `templates/protect-main.sh` once (`REPO=org/name`). **Known
   limitation, not a failure:** GitHub's **Free plan has no branch protection/rulesets on *private*
   repos** (personal or org — org protection needs Team/Enterprise; personal needs Pro). The script
   detects the 403 and **skips gracefully** (exit 0). Fallback: CI still runs on push/PR as an
   **advisory** gate; enable protection once the repo is public or on a supporting plan. So on a
   private Free-plan repo, `main` being *unprotected* is **expected**.
6. **Pipeline-entry hook** — merge `templates/pipeline-hook.json` into `.claude/settings.json`. A
   `UserPromptSubmit` hook injects the `omc-pipeline.md` directive every turn so OMC deterministically
   enters the pipeline. This is **steering, not a hard gate** — it guarantees the directive is
   *present*, not *obeyed*; steps 1–5 are the backstop.

## Guaranteed vs. advisory
- **Guaranteed (gated):** code style · types/compile · the hexagon (architecture tests) · commit format + granularity · all-green before merge · the live path works on the assembled system (deployed build, or the local composed stack when deploy is optional).
- **Advisory rules get a mechanism, not just hope** — lint what's cheaply lintable, checklist the rest:
  - **Gated:** magic *numbers* → Checkstyle `MagicNumber` (Java) / ESLint `no-magic-numbers` (TS),
    rolled out advisory-first then flipped to failing.
  - **Checklisted:** the semantic rules a linter can't cheaply catch (magic *strings* / enums, explicit
    types, empty·error states) are on the **independent-verify semantic-rules checklist** (`loop.md §1`),
    run against every diff.
  - **Proven-once + checklisted:** the clean-machine **run surface** (`make run` on a clean machine,
    Docker only) is verified once by `walking-skeleton` and carried on the independent-verify checklist — a
    convention, not a hard CI gate (a compose-smoke CI job is an optional later upgrade).
- **Advisory (still context only):** loop step *ordering*, "reset don't thrash," recording — mitigated
  by independent-verify, not guaranteed.

## Where the specifics come from
This file defines the gate **stack** (layers + install). The concrete *checks* each gate runs are
stack-specific and live in the per-framework docs — e.g. Spotless vs ESLint, ArchUnit / JMolecules /
Spring Modulith. Add a framework → its doc names its checks; the stack here is unchanged.

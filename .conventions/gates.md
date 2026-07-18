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
| **post-deploy verifier** | deploy pipeline, after deploy | live-verify on the deployed build: health + the **standing aggregate e2e journey** (drives every entry point, rolled back); non-zero exit fails the release | `templates/post-deploy-verify.sh` |
| **branch protection** | platform | no merge to `main` without green CI; no force-push/deletion | `templates/protect-main.sh` (needs public repo or a paid plan — see step 5) |

## Install at baseline (the 5 gates + a pipeline-entry steering hook)
1. **Hooks** — copy `templates/hooks/*` → `.githooks/`, `chmod +x`, `git config core.hooksPath .githooks`. (Also runs the `post-commit` local live-loop when a project wires one.)
2. **CI** — copy `templates/ci.yml` → `.github/workflows/ci.yml`.
3. **Architecture tests** — add `ArchitectureTest` (always) and `ModularityTest` (monolith only) from `templates/` to the backend test sources, set the base package; CI's `mvn verify` gates them.
4. **Post-deploy verifier** — copy `templates/post-deploy-verify.sh` → `scripts/`, point it at the
   **standing aggregate e2e journey** (one secret-guarded endpoint that drives *every* entry point,
   rolled back), and wire it as the **final step of the deploy workflow** (fail the release on
   non-zero). Coverage-completeness rule: **every new entry point (`port/in`) must be wired into the
   journey and asserted before its slice is done** — the verifier can't silently under-cover.
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
- **Guaranteed (gated):** code style · types/compile · the hexagon (architecture tests) · commit format + granularity · all-green before merge · the live path works on the deployed build.
- **Advisory rules get a mechanism, not just hope** — lint what's cheaply lintable, checklist the rest:
  - **Gated:** magic *numbers* → Checkstyle `MagicNumber` (Java) / ESLint `no-magic-numbers` (TS),
    rolled out advisory-first then flipped to failing.
  - **Checklisted:** the semantic rules a linter can't cheaply catch (magic *strings* / enums, explicit
    types, empty·error states) are on the **independent-verify semantic-rules checklist** (`loop.md §1`),
    run against every diff.
- **Advisory (still context only):** loop step *ordering*, "reset don't thrash," recording — mitigated
  by independent-verify, not guaranteed.

## Where the specifics come from
This file defines the gate **stack** (layers + install). The concrete *checks* each gate runs are
stack-specific and live in the per-framework docs — e.g. Spotless vs ESLint, ArchUnit / JMolecules /
Spring Modulith. Add a framework → its doc names its checks; the stack here is unchanged.

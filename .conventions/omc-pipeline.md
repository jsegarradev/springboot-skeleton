# OMC Pipeline

How OMC turns a request into shipped work in a repo governed by these conventions. **The only human
step is answering the deep-interview questions; everything after runs autonomously.**

## Entry
On any request to build or change something, **start with `deep-interview`** — do not plan or code
first. (Pure questions that aren't work requests skip this.)

## The chain
1. **`deep-interview`** *(interactive — the one human step)* — Socratic Q&A until the ambiguity
   threshold is met; writes the spec to `.omc/specs/`. Once the questions are answered, **continue
   autonomously** — take the consensus-refine path instead of pausing at `pending approval`.
2. **`ralplan`** *(autonomous)* — consensus-plan the spec (`--consensus --direct`): Planner →
   Architect → Critic to approval. **Plan to `loop.md` from the start:** decompose into vertical
   slices in dependency order (per each `<framework>.md`), **plan each slice's live-test surface** (the
   endpoints/specs it must provision to be live-verifiable), and set each slice's acceptance criteria
   to the `gates.md` gate stack (arch tests · CI green · live-verify on the assembled system — the
   deployed build where there's a deploy target, else the local composed stack). Proceed past the
   built-in `pending approval` stop.
3. **`ultragoal create-goals`** *(autonomous)* — turn the consensus plan into an ordered goal set
   where **each goal = one `loop.md` slice**; persist plan + ledger under `.omc/ultragoal/`.
4. **`ultragoal complete-goals`** *(autonomous, looped)* — execute each goal through the **`loop.md`
   per-slice loop** (implement + tests → mechanical gate → independent verify → commit → CI →
   (deploy if targeted) → live-verify on the assembled system). A goal is done only when the full
   `gates.md` stack is green; the final goal also
   passes ai-slop-cleaner + verification + `$code-review`. On any gate/CI/live-verify failure, loop
   (fix, ≤3 attempts, else re-slice); `record-review-blockers` instead of marking a goal complete.
   At each goal checkpoint, record a one-line **process-friction** note to `FRICTION.md`
   (`none` / `<what + fix + doc it implicates>`); the ledger's blockers/failures/re-slices are the
   automatic floor.

## Autonomy
Full autonomy after the interview. The interview *is* the consent step, so the chain runs
`ralplan → create-goals → complete-goals` on its own, overriding the skills' default
human-approval pauses.

## Loop rules baked in from the start
`loop.md` is not applied only at execution — it shapes **planning** too: ralplan and create-goals
produce slices and acceptance criteria that already encode the loop and its gates, so complete-goals
is executing a plan that was loop-shaped from the beginning. The gate stack must be installed at
baseline (`gates.md`), or the loop is advisory rather than enforced.

## Making OMC always start here
- **Context:** `AGENTS.md` references this file, so it is in scope each session.
- **On every prompt (mechanical):** a `UserPromptSubmit` hook injects "begin with deep-interview, then
  run the OMC pipeline," so the directive is present on every turn. A doc raises the odds; the hook
  makes the injection deterministic. Obedience is still the model's — the `gates.md` execution gates
  are the real backstop.

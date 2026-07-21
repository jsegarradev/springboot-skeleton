# Development Loop (OMC)

**Baseline (once):** these conventions govern the repo — all work follows them. On an empty repo,
scaffold to them and confirm a green skeleton before feature code. In an existing repo, align code to
them as a slice touches it (no separate big-bang retrofit). **A green skeleton runs on a clean machine**
(Docker the only prerequisite) via a **uniform entrypoint** — `run` / `test` / `build` / `down` targets
(a Makefile by default; a stack may bind them to its native idiom) that stand up the app and its backing
services in ≤2 commands. `run` uses the real engine; `test` is the fast container-free suite (per-framework doc).

**Unit of work:** a vertical slice — one component-scoped commit, built in dependency order (a
consumer lands after what it consumes; typically persistence → API → UI).

## 1. The per-slice loop
1. **Plan** — the slice, the layers it touches, the interface/contract shape, and **how it will be
   live-verified**: which entry point it adds to the **standing aggregate e2e journey** (§2) so it's
   exercised on the assembled system (step 8).
2. **Implement + tests together** — including the planned live-test surface.
3. **Mechanical gate** — formatter, compiler/type-check, and architecture-enforcement tests green
   before moving on.
4. **Independent verify** — a **fresh agent that did not write the code** adversarially reviews the
   diff for correctness and convention adherence, running an explicit **semantic-rules checklist** (the
   rules no formatter or gate catches): magic values named / fixed-sets as enums · explicit types (no
   `var` / no implicit `any`) · loading·empty·error states handled.
5. **Commit the slice.**
6. **CI to green** — watch the run to conclusion; red → fix → re-push.
7. **Deploy** — release the slice to its target.
8. **Live-verify the assembled system (definition of done)** — exercise the path against the
   **running assembled system** via the **standing aggregate e2e journey**: one rolled-back,
   real-dependency run that drives *every* entry point (real request/response; for a UI, drive it). The
   target is the **deployed build** where there's a deploy target, otherwise the **local composed stack**
   (`make run` — real image + real infra, with the verifier pointed at `localhost`); a dev-mode /
   in-memory boot does **not** count. **Wire this slice's new entry point into that journey and assert
   it** before the slice is done, so live coverage tracks the entry-point set by construction. Capture
   the evidence. If it misbehaves, fix and loop back through the cycle.

## 2. Verification standard (non-negotiable)
- **Tests exercise the full journey and assert content**, not structure — drive the real path end to
  end and reject stub/placeholder output.
- **Live means the assembled system** — real I/O, real dependencies, not mocks — the built image + real
  infra, whether **deployed** or **stood up locally via `make run`** (compose); confirmed by the
  live-verify (§ per-slice loop step 8).
- **Responsible test pyramid.** Push behavior into the core and cover the **bulk** with fast
  container-free tests (unit over mocked ports · web slices · in-memory DB). A **small, justified,
  contained** real-dependency integration layer covers only genuine engine behavior that the
  container-free layer can't (e.g. vendor SQL/operators, real migrations); each such test must *name*
  the behavior it exercises, and anything that *could* be a unit/slice test *must* be one — never let
  integration tests substitute for missing unit coverage (no inverted pyramid). Keep them narrow
  (adapter-level, one shared reused real DB), data-isolated, and **tagged/segregated so the fast suite
  runs with no container at all**; the real-DB layer runs as its own stage.
- **The live-test surface is a planned deliverable** — the endpoints/specs a feature needs to be
  exercised are planned and built *with* the slice, never bolted on afterward. Prefer **one standing
  aggregate e2e journey** that drives every entry point (rolled back, real dependencies) over per-slice
  bespoke checks, so coverage can't silently erode: every new entry point is wired into it.
- **Done means it ran on the assembled system with evidence captured** — the deployed build, or the
  local **composed** stack (`make run`) when deploy is optional. **Local-composed counts; local-dev does
  not** — a dev-mode boot over the in-memory test DB, or green-tests-over-a-blank-page, is not done.

## 3. Stop condition (full autonomy)
The loop runs **fully autonomously — no human gate.** A slice is **done** when all hold: mechanical
gate green · independent verify passed · CI green · live-verify on the assembled system passed with
evidence (against the deployed build where there's a deploy target, else the local composed stack). That
combined gate is the sole stop condition — the loop proceeds to the next
slice on its own once it is met. Loop-until-verified: on any gate, CI, or live-verify failure, fix and
re-run; **bound each fix loop to ~3 attempts**, then re-slice (§4) rather than grind.

## 4. When a slice stalls
- **Reset, don't thrash** — if tests won't go green or the design fights you, discard and re-slice
  smaller instead of grinding to a dirty solution.
- **Make real design/architecture choices explicit** — pick the sensible default and record it in the
  decision log (§5) with its reasoning.

## 5. Record
- After each merged slice, update the project's status/decision record: what shipped, the live
  evidence, and any decisions made.
- **Log process friction to `FRICTION.md`** — whenever the loop, gates, or conventions (not the
  feature) cost you (a re-slice, a fix-loop bound hit, an ambiguous/missing/wrong convention, a gate
  misfire, a required workaround), append an entry: what happened, how it was resolved, and which
  doc + section it implicates. Captured here, promoted to the conventions by a human later.

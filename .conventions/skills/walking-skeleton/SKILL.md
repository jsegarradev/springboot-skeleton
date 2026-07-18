---
name: walking-skeleton
description: Run once right after scaffold-project — builds the thinnest end-to-end dummy slice through the loop + gates to prove the whole stack works before any real feature. Backend → an endpoint serving a dummy field from the DB; frontend → a dummy page; monorepo → a dummy page rendering a DB field fetched over HTTP. Trigger on "walking skeleton", "smoke slice", "prove the stack".
---

# walking-skeleton

The first, trivial vertical slice. Its content is throwaway; its purpose is to **prove the assembled
stack + the loop + the gates all work** on real infrastructure before real features start. No
`deep-interview` — the slice is fixed, not elicited.

## Use when
- Immediately after `scaffold-project`, on a fresh conventions-compliant skeleton with no features yet.

## Do not use when
- The project already has real feature slices (this is a one-time bootstrap).

## Guard (blocking, first)
Confirm the conventions skeleton + gate stack are present (build files, hexagon/`core` structure,
`ArchitectureTest`, CI) and no feature endpoints/pages exist yet. Detect the stack: **backend**,
**frontend**, or **monorepo**.

## Pre-flight — deployment & scope (ask before starting)
1. **Detect deployment** — is this a **living (deployed) app**? Look for a deploy pipeline/workflow, a
   configured deploy target/host, or a `post-deploy-verify.sh` wired to a real environment.
2. **Ask** (`AskUserQuestion`):
   - **Live-test this run?** yes / no.
   - **Use the `loop.md` loop when implementing?** yes / no.
   - **If not currently deployed:** is **converting it to a living (deployed) app** in scope for this
     run? yes / no.
3. **Wire the answers:**
   - **loop = yes** → implement via `loop.md`; its plan step **provisions the live-test surface**
     (e2e endpoint / Playwright spec) as a planned deliverable.
   - **live-test = yes** → living app (deployed, or convert = yes) ⇒ live-verify against the **deployed
     build** (the post-deploy verifier hits the provisioned e2e surface); not deployed & not converting
     ⇒ live-verify the **locally-run** app and note it.
   - **live-test = no** → build + gates only; no e2e surface is provisioned.

## Build it through `loop.md` (this also validates the loop + gates)
Drive the dummy slice with the normal per-slice loop and the full `gates.md` stack — that is the point.

- **Backend (Spring Boot) — an endpoint serving a dummy field from the DB:**
  1. Migration: a `dummy` table (`id`, `value`) + one seeded row (idempotent Java loader).
  2. Contract: `GET /dummy` → `DummyResponseBody { value }` in the OpenAPI spec; generate.
  3. Core: `Dummy` domain record · `GetDummy` port/in · `DummyPort` port/out · `GetDummyUseCase` impl.
  4. Adapters: JPA `DummyEntity` + repo (impl port/out) · `DummyController` (impl port/in) · mapper;
     wire in `UseCaseConfig`.
  5. Tests: a usecase unit test + a web/slice test.
  - **Done:** `GET /dummy` returns the value read from the seeded DB row.

- **Frontend (Angular) — a dummy page:**
  1. A `dummy` feature: a standalone component + route that renders a dummy field, using the
     `core/features/shared` structure and the loading/empty/error primitives.
  2. Tests: a component render spec + a Playwright e2e that the page renders.
  - **Done:** navigating to the route renders the dummy value.

- **Monorepo — a dummy page rendering a DB field over HTTP:**
  - Do the **backend** slice above, then an **Angular** page that fetches `/dummy` through the dev
    proxy and renders `value`. Two commits (backend, then frontend — one component each).
  - **Done:** the page displays the value that originated in the DB and arrived via HTTP.

## Close out
- Mechanical gate (formatter · compile · ArchUnit/JMolecules) green → commit per `git.md` (one
  component per commit) → CI green → deploy → **live-verify the deployed dummy** (real request/response
  for the endpoint; drive the page for the UI). If no deploy target is configured yet, live-verify the
  locally-run app and note it.
- Log any process friction to `FRICTION.md` — this run is where the verify-on-first-use items
  (Boot ↔ Java version, ArchUnit/JMolecules APIs, codegen) get validated.

## Output
A running app with a working dummy slice end-to-end, proving the stack, loop, and gates before real
features. The dummy can be removed when the first real slice lands.

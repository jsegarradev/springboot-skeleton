# FRICTION log

Append-only log of **process friction** — times the loop, gates, or conventions (not the feature
itself) cost effort. Agents capture entries at the moment of resolution; a **human promotes**
recurring items into the central conventions (`loop.md` / `gates.md` / `<framework>.md`). Recurrence
is the signal — a one-off is noise; the same friction three times is a convention bug.

**Log friction when:** a fix-loop hit its attempt bound or a slice was re-sliced · a convention/gate
was ambiguous, missing, wrong, or contradictory for the situation · a workaround/deviation was
required · a gate misfired (false pass/fail). OMC's ledger (re-slices, blockers, failed attempts) is
the automatic floor; these entries add the human-readable *what + why + fix*.

---

<!-- Newest on top. Copy the block below for each entry. -->

## 2026-07-21 — Run-surface template can't satisfy the live-verify gate out of the box (no e2e-secret wiring, no `.dockerignore`)
- **Where:** walking-skeleton / scaffold-project (run surface + live-verify gate) · framework-agnostic (the gap) + Spring Boot (the concrete wiring) · implicates `templates/docker-compose.yml` + `templates/Dockerfile` + `skills/walking-skeleton`
- **What happened:** proving `make run` end-to-end against the local composed stack (the loop's live-verify DoD when
  deploy is optional), the freshly-synced run surface fell two artifacts short: (1) `docker-compose.yml` never fed the
  **e2e secret** into the `app` container, so the secret-guarded `/internal/e2e` surface 404s and
  `post-deploy-verify.sh` — the live-verify gate itself — cannot run; (2) no **`.dockerignore`**, so `COPY . .` pulls the
  host's `target/` into the build context, risking the runtime stage's `COPY target/*.jar` matching a stale host jar. The
  run surface makes the app *boot*, but not *live-verifiable*, which is the actual gate.
- **How resolved:** added `E2E_SECRET: ${E2E_SECRET:-local-e2e-secret}` to the compose `app` env (binds `e2e.secret` via
  Spring relaxed binding; verifier uses the same value) + a `.dockerignore` (target/, .git/, .conventions/, .claude/).
  Proven: `make run` → db healthy → app UP → `post-deploy-verify.sh` PASS (`{"ran":true,"fields":{"dummy":"walking-skeleton"}}`).
- **Proposed change:** the run-surface templates should ship **live-verify-ready**, not just boot-ready — compose wires the
  e2e-secret env (parameterized, matching the verifier's `E2E_SECRET`), and a `.dockerignore` ships alongside the
  Dockerfile. Tie to `walking-skeleton`: its "prove `make run` once" step should run the **verifier**, not just a health
  check, so this gap can't pass unnoticed.

## 2026-07-20 — Scaffold ships no Makefile / container entrypoint → not runnable on a clean machine in ≤2 commands
- **Where:** scaffold-project / walking-skeleton (green-skeleton output) · framework-agnostic (the entrypoint contract) + Spring Boot (the concrete artifacts) · implicates the scaffold-project & walking-skeleton skills, and `gates.md` (the "human-visible running app" gate)
- **What happened:** the freshly scaffolded skeleton has **no Makefile and no Dockerfile/docker-compose**. The only documented run path is `./mvnw spring-boot:run` against H2 — which silently assumes the author's box already has **JDK 25 + Maven + (for the prod profile) Postgres** installed and configured. To run it as intended, a fresh machine needs several manual setup steps, and JDK 25 is bleeding-edge so a clean machine almost certainly lacks it. There is no single toolchain-agnostic entrypoint and nothing that stands up the datastore. Surfaced while adapting the skeleton for an external take-home that explicitly **requires a Makefile with a `run` target** and states the app "will be run on a clean machine with almost no dependencies … one or two commands at most." The conventions optimize the *inner dev-loop* but never produce a portable *"here is how anyone runs this"* surface — so the walking-skeleton's own "human-visible running app" gate only holds on the author's pre-provisioned machine, not a clean one.
- **How resolved:** (for the challenge, pattern to generalize) — added three artifacts: (1) a **multi-stage Dockerfile** (build on `maven:…-temurin-25` → ship on `temurin:25-jre` + the jar; host needs only Docker); (2) a **docker-compose.yml** wiring `app` + `postgres`, with a DB **healthcheck** + `depends_on: { condition: service_healthy }` — required because Liquibase runs on app startup and otherwise races an unready DB; (3) a **Makefile** as the single entrypoint: `run` (`docker compose up --build`), `test` (`./mvnw verify`, H2, no Docker), `down`, `dev` (`spring-boot:run`).
- **Proposed change:** the green skeleton is **not "green" until a clean machine can run it in ≤2 commands.** walking-skeleton (and scaffold-project) should emit, as part of that skeleton: a **Makefile** with a framework-agnostic target set (`run` / `test` / `build` / `down`) as the toolchain-agnostic entrypoint; a **multi-stage Dockerfile**; and a **docker-compose.yml** wiring the app to its datastore with a healthcheck + `service_healthy` gate (the migration-races-DB pitfall is general, not challenge-specific). Per-framework fillings behind the same targets (Spring: compose + `mvnw`; Angular: `ng serve` / `ng test`). Tie it to `gates.md`: extend the "human-visible running app" gate to "…runnable by *anyone* on a clean machine, not just on the author's configured box."
- **→ promoted 2026-07-20** to `loop.md` (baseline: green skeleton runs on a clean machine via a uniform
  `run`/`test`/`build`/`down` entrypoint), `gates.md` (proven-once + checklisted — a convention, not a
  hard gate; **note:** no named "human-visible running app" gate actually existed, so it was *added*),
  `springboot.md` §2 (Spring binding) + §7 (datastore inverted — real engine is the default run/dev/prod
  datasource, H2 demoted to `test` scope) + §1 + §10 (H2-fidelity tripwire), `angular.md` §2, and
  `skills/{scaffold-project,walking-skeleton}`. New `templates/{Makefile,Dockerfile,docker-compose.yml}`.
  Datastore refinement (H2 tests-only / real-DB default) decided with the user during promotion.

## 2026-07-20 — `ci.yml` ships Angular/monorepo frontend steps into a backend-only repo
- **Where:** scaffold-project (gate install) · framework-agnostic · implicates `gates.md` + `templates/ci.yml`
- **What happened:** this is a Spring-Boot-only repo (no `frontend/`), yet the copied `.github/workflows/ci.yml`
  is the verbatim canonical template — it unconditionally runs `actions/setup-node@v5` and carries the whole
  `Frontend` step (npm ci / eslint / tsc / ng test). The Frontend step self-gates via
  `hashFiles('frontend/package.json')` so it's inert, but the `setup-node` step has no guard and always runs, and
  the dead block sits in the file as Angular/monorepo clutter that doesn't apply. The "one conditional `ci` file"
  design adapts at *runtime* only; a backend-only repo still ships the frontend scaffolding.
- **How resolved:** (pending) — flagged during a conventions-fidelity audit; no change applied to the skeleton yet.
- **Proposed change:** make `ci.yml` component-shaped at install time, not just runtime. Options: (a) scaffold-project
  emits only the components the repo holds (strip the Frontend job + `setup-node` for backend-only; strip Backend +
  `setup-java` for frontend-only), or (b) keep one file but guard `setup-node` with the same `hashFiles(frontend/...)`
  condition so an absent frontend costs nothing and reads as intentionally-multi-stack. Prefer (a) for a single-stack
  scaffold; (b) only if the monorepo-from-day-one case is common.
- **→ promoted 2026-07-20** to `gates.md` (install step 2), `templates/ci.yml` (header + guarded language
  setups), and `skills/scaffold-project` step 5 — approach (a): scaffold emits only the components present.

## 2026-07-20 — `springboot.md` §1 names a jMolecules artifact that does not exist
- **Where:** scaffold-project (dependency declaration) · Spring Boot · implicates `springboot.md#1`
- **What happened:** §1 lists the JMolecules deps as `jmolecules-ddd` + **`jmolecules-architecture-hexagonal`** +
  `jmolecules-archunit`. The middle coordinate is wrong: only `.lastUpdated` failed-download markers exist for it in
  `~/.m2` — the real artifact is **`jmolecules-hexagonal-architecture`** (word order flipped vs the *package*
  `org.jmolecules.architecture.hexagonal`, which is the trap). The skeleton's `pom.xml` silently uses the correct id,
  so the build is green, but anyone copying the literal §1 text gets an unresolvable dependency.
- **How resolved:** skeleton already uses the correct `jmolecules-hexagonal-architecture` (managed by `jmolecules-bom`).
- **Proposed change:** fix §1 to `jmolecules-hexagonal-architecture`. This is a plain doc bug, not a design change.
- **→ promoted 2026-07-20** to `springboot.md#1` (id corrected + word-order-trap note added).

## 2026-07-20 — Generated skeleton beans hand-write constructors instead of `@RequiredArgsConstructor`
- **Where:** walking-skeleton / scaffold-project (generated Spring-side code) · Spring Boot · implicates `springboot.md#3`
- **What happened:** §3 says annotate Spring-side beans with `@RequiredArgsConstructor` and inject through `final`
  fields — hand-write a constructor only when it transforms/validates a dependency or needs `@Value`/`@Qualifier`.
  The generated skeleton violates its own rule in the plain cases: `DummyController`, `DummyPersistenceAdapter`, and
  `DummyDataLoader` each hand-write a boilerplate constructor with no such justification. (The two hand-written ctors
  that *are* correct — `E2eController` `@Value`, `RollbackTransactionRunner` transform — prove the exception was
  understood, just not the default.)
- **How resolved:** (pending) — audit finding; skeleton code not yet aligned.
- **Proposed change:** the scaffold/walking-skeleton generators should emit `@RequiredArgsConstructor` for plain
  Spring-side beans by default (convention text is already correct — the generated exemplar drifted from it). Consider
  a semantic-rules-checklist item so independent-verify catches an unjustified hand-written ctor.
- **→ not promoted (2026-07-20):** §3 already mandates `@RequiredArgsConstructor` — this is skeleton-code
  drift, not a convention change. Fix belongs in the skeleton sources; kept here as an open project fix.

## 2026-07-20 — §5 references `DomainException` but nothing defines or scaffolds it
- **Where:** scaffold-project (base scaffolding) · Spring Boot · implicates `springboot.md#3` (base scaffolding) + `#5`
- **What happened:** §5 tells value objects to "throw `DomainException` on violation" in their compact constructor, but
  no `DomainException` base type is defined anywhere — not in §3's "generic base scaffolding (always present)" list,
  not in the templates, not in the skeleton (`domain/exception/` holds only `DummyNotFoundException extends
  RuntimeException`). Nothing violates it today because the skeleton has no invariant-validating value object yet, but
  the first VO with invariants will have no base to extend and will diverge per-author.
- **How resolved:** (pending) — audit finding.
- **Proposed change:** add a `DomainException` base (unchecked, in `domain/exception/`) to §3's always-present base
  scaffolding list, so §5's reference resolves and the base ships with every scaffold.
- **→ promoted 2026-07-20** to `springboot.md#3` (added `DomainException` base to the always-present
  base-scaffolding list, mapped to `ApiError` by `GlobalExceptionHandler`).

## 2026-07-20 — ArchUnit A2 omits the `port` ring (asymmetric with A1)
- **Where:** scaffold-project (ArchitectureTest template) · Spring Boot · implicates `templates/ArchitectureTest.java` + `springboot.md#3.1`
- **What happened:** §3.1(A2) says "framework annotations appear only on the Spring side" — ports are core. But the
  `ArchitectureTest` template's A2 (`core_has_no_framework_annotations`) checks only `..domain..`,`..usecase..`,
  omitting `..port..`, while A1 (`core_is_framework_free`) *does* include `..port..`. So a framework annotation on a
  port type would slip past A2 (A1 still catches it as a dependency, so impact is low — this is a symmetry/clarity gap,
  not an open hole).
- **How resolved:** (pending) — audit finding.
- **Proposed change:** add `..port..` to A2's `resideInAnyPackage(...)` in the template so A1 and A2 cover the same
  core rings. Trivial, low-risk.
- **→ promoted 2026-07-20** to `templates/ArchitectureTest.java` (A2 now includes `..port..`).

## 2026-07-20 — Agent/tooling artifacts (`.conventions/`, `.claude/`, `FRICTION.md`) committed by default instead of gitignored
- **Where:** scaffold-project / adopt-conventions (repo setup) · framework-agnostic · implicates the scaffold-project & adopt-conventions skills
- **What happened:** after scaffold, agent-facing tooling/reference is tracked in the repo — `.conventions/` (23 files),
  `.claude/`, and the `FRICTION.md` process log. None of these are project source: they're the agent's local workflow
  scaffolding + process ledger. Committing them bloats the repo, invites in-repo drift edits, and mixes convention/process
  churn into feature history. Neither skill gitignores them or asks the user; the default is silently "commit them".
- **How resolved:** (pending) — expectation is that all three be added to `.gitignore` by default.
- **Proposed change:** scaffold-project and adopt-conventions should **force `.conventions/`, `.claude/`, and `FRICTION.md`
  into `.gitignore` by default**, committing any of them only when the user explicitly opts in (e.g. wants a self-contained
  repo, or wants the friction log shared with the team). Make it a documented default / interview question, not implicit.
- **→ promoted 2026-07-21** to `skills/{scaffold-project,adopt-conventions}` (git-ignore the three
  artifacts by default; ask once to opt into committing) + relaxed the `skills/{sync-conventions,promote-friction}`
  guards to read the local working-tree copies (present-on-disk, not necessarily committed). Interview-default
  approach (not a hard force) chosen with the user to preserve the travel-with-project / reviewable-sync model.

## 2026-07-18 — OpenAPI generator emits Swagger `@Schema`, breaking compile on a no-swagger setup
- **Where:** walking-skeleton (contract-first codegen) · Spring Boot · implicates `springboot.md#6`
- **What happened:** `openapi-generator-maven-plugin` (spring generator, 7.14.0) generated models importing
  `io.swagger.v3.oas.annotations.media.Schema`, which is not on the classpath — compile failed. §6 lists the
  Boot-4 generator config (`useSpringBoot4`, `importMappings`, `jspecify`) but nothing to suppress Swagger
  annotations, so the default pulls in a dependency the conventions never add.
- **How resolved:** added `configOptions` `documentationProvider=none` + `annotationLibrary=none` → plain POJOs,
  no swagger dep. (Also `skipDefaultInterface=true` to avoid needing the `ApiUtil` supporting file.)
- **Proposed change:** add these three configOptions to the §6 generator config snippet (default to plain POJOs
  unless the project deliberately wants springdoc).
- **→ promoted 2026-07-21** to `springboot.md#6` (generator now defaults to plain POJOs —
  `documentationProvider=none` + `annotationLibrary=none` + `skipDefaultInterface=true`).

## 2026-07-18 — ArchUnit rules fail on the empty skeleton (`failOnEmptyShould`)
- **Where:** scaffold-project (bare skeleton, empty hexagon rings) · Spring Boot · implicates `gates.md` (ArchitectureTest template) + `springboot.md#3.1`
- **What happened:** on the freshly scaffolded skeleton the hexagon rings are empty, so several A1–A7 rules (and
  the jMolecules rules) match zero classes; ArchUnit's default `failOnEmptyShould=true` then fails the build —
  before any feature code exists. The `ArchitectureTest` template doesn't mention this.
- **How resolved:** added `src/test/resources/archunit.properties` with `archRule.failOnEmptyShould=false`; the
  rules activate automatically as slices populate the rings (validated: they went active on the walking-skeleton slice).
- **Proposed change:** ship `archunit.properties` (with `failOnEmptyShould=false`) alongside the `ArchitectureTest`
  template, and note in `springboot.md#3.1` that a scaffold needs it until the first slice lands.
- **→ promoted 2026-07-21** to `springboot.md#3.1` (empty-rings note + `archunit.properties` with
  `failOnEmptyShould=false`) and `gates.md` install step 3 (pointer).

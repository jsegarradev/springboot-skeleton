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

## 2026-07-18 — ArchUnit rules fail on the empty skeleton (`failOnEmptyShould`)
- **Where:** scaffold-project (bare skeleton, empty hexagon rings) · Spring Boot · implicates `gates.md` (ArchitectureTest template) + `springboot.md#3.1`
- **What happened:** on the freshly scaffolded skeleton the hexagon rings are empty, so several A1–A7 rules (and
  the jMolecules rules) match zero classes; ArchUnit's default `failOnEmptyShould=true` then fails the build —
  before any feature code exists. The `ArchitectureTest` template doesn't mention this.
- **How resolved:** added `src/test/resources/archunit.properties` with `archRule.failOnEmptyShould=false`; the
  rules activate automatically as slices populate the rings (validated: they went active on the walking-skeleton slice).
- **Proposed change:** ship `archunit.properties` (with `failOnEmptyShould=false`) alongside the `ArchitectureTest`
  template, and note in `springboot.md#3.1` that a scaffold needs it until the first slice lands.

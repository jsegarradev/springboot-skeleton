# skeleton

A Spring Boot backend skeleton, scaffolded to the OMC engineering conventions. It is a **single
bounded context** with a **hexagonal architecture** (the hexagon expressed as packages), ready for
the feature loop. There is no feature code yet — the next step is the **walking skeleton** (a thin
end-to-end slice) before real features.

## Stack

| Concern | Choice |
|---|---|
| Language / runtime | Java 25 (LTS) |
| Framework | Spring Boot 4.1 (Web MVC, Actuator, Data JPA, Validation) |
| Build | Maven (`./mvnw`) |
| Schema | Liquibase (sole schema owner) |
| DB | PostgreSQL (prod) · H2 in PostgreSQL-compat mode (fast tests / local) |
| API | Contract-first — OpenAPI spec → generated contracts (`org.openapitools`) |
| Mapping / boilerplate | MapStruct · Lombok |
| Architecture enforcement | ArchUnit (A) + jMolecules hexagonal & DDD rules (C) |
| Style / static analysis | Spotless (Eclipse profile, 4-space / 120-col) · Checkstyle magic-number gate |

Base package: `dev.jsegarra.skeleton`.

## Architecture (hexagon by package)

```
dev.jsegarra.skeleton
├─ domain/                      core: framework-free domain model (DDD stereotypes)
├─ port/in/  · port/out/        use-case interfaces (primary) · outbound ports (secondary)
├─ usecase/                     application core — one class per operation
├─ adapter/in/                  REST controllers + contracts (generated under contracts/)
│  └─ contracts/response/       hand-written response DTOs (e.g. ApiError)
├─ adapter/out/                 JPA entities, repos, persistence adapters (@Transactional)
└─ config/                      composition root: UseCaseConfig, GlobalExceptionHandler
```

Each ring carries its jMolecules stereotype on `package-info.java`; the boundaries are held by tests
(`ArchitectureTest` = A1–A7, `JMoleculesArchitectureTest` = hexagonal + DDD). A violation fails the
build.

## Run & test locally

Prerequisites: JDK 25 on the path. Everything else comes through the `./mvnw` wrapper.

```bash
# Build, run all gates (Spotless check, tests, ArchUnit/jMolecules, Checkstyle):
./mvnw verify

# Auto-format to the shared Eclipse profile before committing:
./mvnw spotless:apply

# Run the app (http://localhost:8080; health at /actuator/health):
./mvnw spring-boot:run
```

The pre-commit hook runs a fast format-check + compile; the full suite runs in CI (`.github/workflows/ci.yml`)
and gates merges to `main`.

## Working in this repo (conventions)

This project travels with its conventions under [`.conventions/`](.conventions/). Start with
**[`.conventions/AGENTS.md`](.conventions/AGENTS.md)** — it links the per-framework rules
([`springboot.md`](.conventions/springboot.md)), the development loop
([`loop.md`](.conventions/loop.md)), the gate stack ([`gates.md`](.conventions/gates.md)), commit
rules ([`git.md`](.conventions/git.md)), and how to start an autonomous session.

- **Commits** follow the bracket-tag scheme (`git.md`): `[scaffold] …`, `[<slug>][impl] …`, etc.,
  enforced by the `commit-msg` hook.
- **Process friction** goes in [`FRICTION.md`](FRICTION.md).
- **Next step:** run the `walking-skeleton` skill to prove the stack end-to-end.

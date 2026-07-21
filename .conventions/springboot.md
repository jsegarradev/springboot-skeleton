# Spring Boot / Java — Engineering Conventions

## 1. Toolchain, dependencies & versions
**Toolchain (system):**
- **JDK 25** (current LTS) — e.g. via SDKMAN or the distro package.
- **Maven or Gradle** — or rely on the `mvnw`/`gradlew` wrapper the generated project ships.
- **Spring CLI** (`spring init`) — scaffolds the skeleton; install via SDKMAN (`sdk install
  springboot`). Fallback with no CLI: `curl` + `unzip` against start.spring.io.

**Versions:**
- **Java 25** (current LTS).
- **Spring Boot version-adaptive:** read the major from the generated build file (`pom.xml` /
  `build.gradle`); select starters by their stable dependency IDs (start.spring.io maps them to the
  right artifacts); resolve moved packages from the classpath.
- **DB (per-project choice):** a real engine for **every run** (dev / `make run` / prod — **default
  PostgreSQL**) + **H2 as the fast-test stand-in** (default; the prod engine's compatibility mode). Swap
  either for the project's actual stack — engine-agnostic; Postgres/H2 are just the defaults. Datasource
  wiring is **§7's** concern (real engine is the default datasource; H2 is test-scoped).
- **Port:** backend runs on `8080` (the frontend dev proxy targets it).

**Project dependencies** (declare in the build file):
- **Spring Boot starters:** `web`, `actuator`, `data-jpa`, `validation`, `h2` (test DB — `test` scope,
  §7). Actuator provides `/actuator/health` for the live-verify gate / health check.
- **Liquibase** (`liquibase` starter) — the schema owner.
- **Real-engine driver (every run — dev / `make run` / prod)** — default `org.postgresql:postgresql`;
  swap for the project's engine. H2 backs only the fast test suite (§7, §10).
- **OpenAPI generator** (`openapi-generator-maven-plugin`, or the Gradle OpenAPI Generator plugin) +
  the **`org.jspecify:jspecify`** dep — contract-first codegen.
- **Lombok** — JPA entity boilerplate + Spring-side constructor injection (`@RequiredArgsConstructor`, §3).
- **MapStruct** + **`lombok-mapstruct-binding`** — domain↔entity and domain↔contract mappers.
- **Spring Boot test starter** — brings JUnit 5 + Mockito + AssertJ.
- **ArchUnit** (`archunit-junit5`) — enforcement layer (A).
- **Spring Modulith** (`spring-modulith-starter-core` + `spring-modulith-starter-test`) — enforcement
  layer (B) + module events. **Monolith only** — optional for a single-context service.
- **JMolecules** (`jmolecules-ddd` + `jmolecules-hexagonal-architecture` + `jmolecules-archunit`) —
  enforcement layer (C). (The artifact id word order is `hexagonal-architecture`, unlike the package
  `org.jmolecules.architecture.hexagonal` — the flipped order is the trap.)
- **Spotless plugin** (`spotless-maven-plugin`, or Gradle `com.diffplug.spotless`) + the shared
  `eclipse-formatter.xml` — style.
- **Checkstyle** (`maven-checkstyle-plugin` / Gradle `checkstyle`) + the shared `checkstyle.xml` —
  static-analysis gate for magic numbers (enforces §9 "name every literal").

## 2. Scaffolding
- **Scaffold with `spring init`** — e.g.
  `spring init -d web,actuator,data-jpa,validation,liquibase,h2 -j 25 --build maven <name>` (use
  `--build gradle` for Gradle); supply the **asked** `-g <groupId>` / `--package-name <base>` (never
  inferred from an email domain or dir name); omit the Boot version to get current GA. Without the CLI, fall back to
  `curl -fSs https://start.spring.io/starter.zip -d dependencies=… -o s.zip && unzip s.zip` (the `-f`
  is load-bearing so an HTTP error fails fast instead of writing an error body into the zip).
- **Replace generator boilerplate** (Spring's `HELP.md`) with a real project `README`.
### 2.1 Run surface (clean-machine runnable — `loop.md` baseline)
Ship, from `templates/`, parameterized to the chosen build tool + pinned JDK:
- a **multi-stage `Dockerfile`** — build stage on a JDK build image running the wrapper, runtime stage
  on the matching JRE image with the jar — so the host needs only Docker.
- a **`docker-compose.yml`** wiring `app` + the real engine (default Postgres); the app waits on a DB
  **healthcheck** (`depends_on: { db: { condition: service_healthy } }`) — load-bearing because
  Liquibase applies migrations on boot and otherwise races an unready DB (§7).
- a **`Makefile`** entrypoint: `run` (`docker compose up --build`, real engine) · `test`
  (`./mvnw verify` / `./gradlew test` — H2, no Docker) · `build` · `down` · `dev` (`docker compose up
  -d db`, then native `spring-boot:run` for fast reload).

## 3. Architecture — hexagonal
- **Single build module, hexagon expressed as packages — in all cases** (single-context service and
  the Spring Modulith monolith). The hexagon boundary is a **package** boundary, held by tests (see
  *Enforcement*). (Maven or Gradle — same layout.)
- **Package layers:**
  - **Core (plain Java, framework-free):** `domain/` (the domain model — see §4 Package organization &
    §5 Domain modeling) ·
    `port/in/` (use-case interfaces) · `port/out/` (outbound ports) · `usecase/` — **one class per
    operation**, a single `execute(Command)` / `execute(id)` method; plain classes, constructor
    injection, implement `port/in`, call `port/out`; write ops return the created/affected ID or
    `void`, read queries return the domain object. Keep annotations off core types.
  - **Spring side:** `adapter/in/` (REST controllers + `adapter/in/contracts`) · `adapter/out/` (JPA
    entities + Spring Data repos + a **persistence adapter class** that implements `port/out`,
    delegating to the Spring Data repo and mapping entity↔domain; `@Transactional` here) · `config/`
    (bean wiring, `@RestControllerAdvice`, app config) · `Application.java`.
- **Wiring:** a `config` `@Configuration` with one `@Bean` per use-case, `new`-ing the impls and
  injecting the adapter beans — so use-case impls stay plain classes.
- **Constructor injection on the Spring side:** annotate Spring-side beans (controllers, `adapter/out`
  persistence adapters, `config` components) with **`@RequiredArgsConstructor`** and inject through
  `final` fields — don't hand-write the constructor. Keep an explicit constructor only when it
  transforms/validates a dependency or a parameter needs an annotation Lombok won't copy (`@Value`,
  `@Qualifier`). **Core types stay annotation-free** — use-case impls are plain classes wired by `new`
  in `config`, so their constructors stay hand-written.
- **Segregated ports (ISP):** each port interface declares exactly **one method**. Use cases declare
  only the ports they actually need as constructor dependencies — never a fat repository interface.
  `port/in/` follows the same rule: one interface per use case (or per query).
- **No type-code multiplexing:** never branch behavior on a type-code discriminator — a
  `type`/`kind`/`entityType`/`updateType` field switched inside one class. Split into **one use-case
  per (entity × operation)** — the one-operation-per-class rule applied to the whole matrix of
  variants (A7 already forbids the multi-method class this would otherwise become). Model partial
  updates by **field presence** (PATCH semantics: a field present means write it), not an
  update-type enum — enumerating field combinations blows up combinatorially.
- **Base package** `com.<org>.<app>`, shared across the codebase.
- **Optional `business/` grouping:** teams may group the framework-free core under `business/`
  (`business/{domain,port,usecase}`), leaving `adapter`/`config` at the base package — the ArchUnit
  `..domain..`/`..port..`/`..usecase..` wildcards accommodate it, so A1–A7 + JMolecules stay green with
  no rule changes.
- **Generic base scaffolding (always present):** `UseCaseConfig` (the wiring root) · a
  `GlobalExceptionHandler` (`@RestControllerAdvice`) returning a shared **`ApiError`** record (the
  standard error-response shape) · a **`DomainException`** base (unchecked, in `domain/exception/`) that
  value-object invariant checks throw (§5) and `GlobalExceptionHandler` maps to `ApiError` ·
  `AbstractIntegrationTest` base test.
- **Modulith monolith:** the *vertical* split = Spring Modulith modules as top-level packages
  (`com.app.train`, `com.app.jobs`, …), each internally layered as above. Modules talk through
  published API packages + Modulith events.
- **Transactions** live on the Spring side: annotate the outbound-adapter method; for multi-outbound
  atomicity, add a thin `@Transactional` decorator on the Spring side.
- **Controllers are thin** (HTTP only): one `@RestController` class per endpoint with a single public
  `execute(...)` method. Return response contracts, constructor injection, `@RestControllerAdvice` for
  errors. **Request → Command separation:** request records carry the Jakarta validation annotations;
  the controller maps them to command records, which carry **none** — so Jakarta never leaks into the
  core. (Homes in §4: requests in `adapter/in/contracts/request/`, commands in `port/in/command/` — the
  latter because `adapter/in → port/in` is allowed but A4 forbids `adapter/in → usecase`.)
- **Core outputs stay annotation-free — map them, don't serialize them raw.** A type named in a
  `port/in` signature (a use-case return) is a **core** type: no serialization (Jackson) annotations,
  no `Response`-style naming. The controller **maps** it to an `adapter/in/contracts/response/` DTO
  (e.g. `MatchItem` → `MatchResponse`); never return a core type as the HTTP body. (Enforced by A1 —
  core may not depend on `com.fasterxml.jackson..`.)
- **Cross-origin:** rely on the dev proxy; add a CORS bean only for a deliberate split-origin deploy.
### 3.1 Enforcement — wire these as CI tests
They hold the hexagon; a violation fails the build.
- **(A) ArchUnit — always** — (A1) core packages (`..domain..`, `..usecase..`, `..port..`) depend
  only on Java + each other; (A2) framework annotations appear only on the Spring side; (A3)
  dependencies point inward — adapters and use-cases depend on the ports, ports on the domain
  (composition root `config` exempt); (A4) inbound adapters depend on `port/in`, never on the
  use-case impls; (A5) no JPA at the web edge; (A6) outbound-port implementations live in
  `adapter/out`; (A7) each class in `..usecase..` exposes at most one public non-constructor method —
  enforces one-operation-per-class. On a freshly scaffolded skeleton the rings are empty, so these rules
  match zero classes and ArchUnit's default `failOnEmptyShould=true` fails the build before any feature
  code exists — ship `src/test/resources/archunit.properties` with `archRule.failOnEmptyShould=false`;
  the rules activate automatically as slices populate the rings.
- **(B) Spring Modulith — monolith only** — `ApplicationModules.of(App.class).verify()` guards the
  module boundaries. Skip it for a single-context service; A + C already hold the hexagon there.
- **(C) JMolecules — always** — annotate the hexagon with the real stereotypes (on each ring's
  `package-info`), all in `org.jmolecules.architecture.hexagonal`: `port/in` → `@PrimaryPort`,
  `port/out` → `@SecondaryPort`, `adapter/in` → `@PrimaryAdapter`, `adapter/out` → `@SecondaryAdapter`,
  and the application core (`usecase`) → `@Application`. Mark domain types with the `jmolecules-ddd`
  building blocks (`@AggregateRoot`/`@Entity`/`@ValueObject`/`@Repository`/`@Service` from
  `org.jmolecules.ddd.annotation`) — there is **no `@Domain` annotation**. The `jmolecules-archunit`
  ready-made rules then enforce the hexagonal + DDD constraints.

## 4. Package organization & type taxonomy
- **Type taxonomy — one home per kind of type:**
  - `port/in/command/` — use-case **inputs** (records; no Jakarta annotations).
  - `port/in/result/` — use-case **outputs** (the inbound port's return shape).
  - `port/out/result/` — outbound-port **query / read results**.
  - `adapter/in/contracts/request/` — inbound **request** DTOs (Jakarta validation annotations).
  - `adapter/in/contracts/response/` — outbound **response** DTOs (serialization annotations). Generated
    (contract-first) contracts instead stay flat in `adapter/in/contracts` (naming carries direction — §6).
  - `domain/{entity,value,enums,service,exception}` — the domain model, sub-split by kind.
  - **Litmus:** a type used by a domain entity or domain service **is domain**; a type that only shapes
    a port's input or output belongs in that **port** ring — not in `domain`.
- **Name use-case outputs `...Result`** (not `...Dto` / `...Response`) — they are core outputs, so §3's
  "annotation-free, mapped to a `contracts/response/` DTO" rule applies.
- **No I/O in the core:** use-cases and the domain **never do file/network I/O or parsing directly**.
  Any read from a file, an HTTP endpoint, or a serialized payload goes through an **outbound port**
  whose adapter owns the access and the wire/parse annotations (e.g. an `ObjectMapper` lives in the
  adapter, never in a use-case). The use-case orchestrates; the adapter fetches.
- **Sub-divide a large ring by feature — ring primary, feature secondary.** Once a ring exceeds ~10
  types, add a feature sub-axis *inside* it while keeping the ring the top level:
  `usecase/<feature>`, `port/in/<feature>`, `adapter/in/<feature>`, `port/out/<by-target>`.
  Cross-feature types go in a `<ring>/shared` sub-package. Repeat the ring's JMolecules stereotype in
  each sub-package's `package-info` (unannotated rings like `command`/`result` need none). **Never
  invert to feature-primary / ring-secondary** in a single bounded context — that reintroduces
  ring-mixing. The ArchUnit `..<ring>..` wildcards are transparent to the sub-axis, so A1–A7 stay green.

## 5. Domain modeling (tactical DDD)
- **Value objects and domain events are `record`s.** Value objects **validate their own invariants**
  in the compact constructor (predicate-based, throw `DomainException` on violation) — never validate
  in the use case or controller.
- **Aggregate roots** are plain classes with **factory methods named after the domain action**
  (`Course.create`, not `Course.of`).
- **No public setters or `@Data` on domain types.** Construct through the named factory (never a
  setter chain); an aggregate changes state only through **intention-revealing command methods** (which
  register events), never through exposed field access. **Choose record vs. class by lifecycle:** a
  type with identity and command-driven state changes is an **encapsulated class**; a value object,
  use-case result, or query/read model — no identity, replaced wholesale — is an **immutable `record`**.
  (An anemic `@Data` bag in `domain` is usually either an entity that needs encapsulation or a read
  model that belongs in a `port/in/result` / `port/out/result` ring — see §4.)
- **Domain events** are named **past tense** (`CourseCreated`, `LessonAdded`) and **registered inside
  the aggregate's command methods** (the aggregate calls `registerEvent(...)`; use cases must not
  create events). After a successful `save()`, the use case publishes then clears:
  `aggregate.domainEvents().forEach(publisher::publish); aggregate.clearDomainEvents()`.
- **Cross-context references use IDs only** — an aggregate holds a foreign identity as a value object
  (e.g. `CourseId`), never a direct reference to another context's aggregate. Cross-context validation
  belongs in the application-layer use case, not the domain.

## 6. API contracts — contract-first
- **The OpenAPI spec is the source of truth** (e.g. `src/main/resources/openapi/api.yaml`); generate
  the contracts from it with the OpenAPI generator (build-tool plugin). A mapper turns generated
  contract ↔ domain.
- **Generate into a contracts package** under `adapter/in` (e.g. `adapter/in/contracts`).
- **Name schemas so generated types end `...RequestBody` / `...ResponseBody`** — the generator names
  classes after the schema, so the suffix carries the request/response distinction (e.g.
  `CreateAccountRequestBody`, `AccountResponseBody`).
- **Generator config (Boot 4):** `useSpringBoot4=true` +
  `importMappings=Nullable=org.jspecify.annotations.Nullable` + the `org.jspecify:jspecify` dep. Default
  to **plain POJOs, no springdoc/Swagger dependency**: set `documentationProvider=none` +
  `annotationLibrary=none` — otherwise the generator emits `io.swagger.v3.oas.annotations…@Schema`
  imports that the conventions never put on the classpath, and compilation fails — plus
  `skipDefaultInterface=true` (skips the `ApiUtil` supporting file). Opt back into springdoc annotations
  only if a project deliberately serves its own API docs.
- **Hand-written contracts** (when a project doesn't generate them): organized by direction per §4
  (`adapter/in/contracts/request/` + `/response/`). Generated contracts instead stay flat in
  `adapter/in/contracts` — the `...RequestBody`/`...ResponseBody` suffix carries direction there.

## 7. Persistence — schema, entities & mapping
**Base config:**
- `spring.jpa.open-in-view: false` · `spring.jpa.hibernate.ddl-auto: none` ·
  `spring.liquibase.change-log: classpath:db/changelog/db.changelog-master.yaml`.
- **Default datasource = the real engine (default Postgres), env-driven.** The running app (dev,
  `make run`, prod) reads `SPRING_DATASOURCE_*` from the environment (compose/host supplies them); the
  `run` surface (§2) points it at the compose engine container. You develop and ship against the same
  engine — no separate "prod profile" bolted on; the default *is* the real engine.
- **H2 is test-only.** The fast container-free suite runs on H2 in the prod engine's compatibility mode
  (`jdbc:h2:mem:appdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`), configured on the **test classpath**
  (`src/test/resources/application.yaml`) with the H2 dependency at **`test` scope**; h2-console is a
  test-run aid. H2 never backs a running app.

**Schema (Liquibase):**
- **Liquibase is the sole schema owner** (SQL formatted changelogs).
- **Changesets:** `db/changelog/changes/NNN_<verb>_<subject>.sql`; first line
  `--liquibase formatted sql`, then `--changeset author:id`, and include a `--rollback`. The master
  changelog (`db/changelog/db.changelog-master.yaml`) is a thin `includeAll` with
  `errorIfMissingOrEmpty: false` and `relativeToChangelogFile: true` — so its `path` is relative to the
  master's own directory: **`changes/`**, not `db/changelog/changes/` (the latter double-prefixes and
  silently runs 0 changesets).
- **Write the migration before the entity** (the table must exist for the app to boot).
- **Use reserved-word-safe column names** matching `@Column` (`start`/`end`/`user`/`order` →
  `start_date`/`end_date`/…).
- **Load seed data in Java** (e.g. a `CommandLineRunner`) into Liquibase-created tables; make loaders
  idempotent.
- **Keep changesets portable where possible** — run the SQL stand-in in the prod engine's
  compatibility mode (e.g. H2 `;MODE=PostgreSQL`) and favor standard SQL so the changelog applies on
  both. **Engine-specific changesets** (e.g. Postgres `jsonb`, a `pgvector` index, engine-native
  upsert) aren't stand-in-portable: gate them with an engine-only Liquibase precondition (e.g.
  `dbms:postgresql`) so the stand-in skips them, and cover that behavior with the
  real-DB IT (Testcontainers) + live-verify (§10).

**Entities & mapping:**
- **Lombok** on mutable JPA entities (`@Getter @Setter @NoArgsConstructor`); **MapStruct** for
  mappings (domain↔entity and contract↔domain). Domain objects stay **records** (contracts come from
  the generator).
- **Declare annotation processors in this order:** `lombok`, `mapstruct-processor`,
  `lombok-mapstruct-binding` — the binding lets MapStruct read Lombok's getters.
- **Use `@Mapper(componentModel = "spring")`;** confirm the generated `*MapperImpl` reads the source
  getters.
- **Give JPA entities a public no-arg constructor** when they are instantiated cross-package.

## 8. Code style & formatting (owned by the formatter)
- **4-space indent, K&R / 1TBS braces, 120-column width.**
- **Spotless with the Eclipse profile** (or `palantirJavaFormat()` for a zero-config 4-space/120
  setup), configured at the **build root** (aggregator/root project) and referencing the shared
  **`eclipse-formatter.xml`** (in this conventions directory: 4-space, `lineSplit=120`,
  insert-newline-at-EOF), so a root-level format-check finds it. (On Maven, wire the plugin on the
  root aggregator pom.)

## 9. Naming & semantics
- **Java locals:** explicit types; `final` for locals/params that stay constant.
- **Name every literal:** extract constants; use an **enum** for a value from a fixed known set
  (statuses, types, modes). Name route/config keys, status codes, limits, and labels.
  - **Gated (numbers):** a **Checkstyle `MagicNumber`** check (shared `checkstyle.xml`, main sources
    only; ignore `0/1/-1/2`, HTTP codes via `HttpStatus`, `@ConfigurationProperties` defaults) runs in
    the mechanical gate + CI. Roll out **advisory-first** (`severity=warning`) → flip to failing once
    clean.
  - **Review-checked (strings/enums):** magic strings and "should be an enum" are too noisy to lint —
    they're on the independent-verify semantic-rules checklist (`loop.md §1`).

## 10. Testing
The **responsible test pyramid** — principle in `loop.md §2`; the Spring specifics follow.

- **Stack:** JUnit 5 + Mockito + AssertJ. Favor a few meaningful tests. **Resolve test-annotation
  imports from the classpath** — they move between Boot majors (Boot 4.1: `@WebMvcTest` =
  `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`; `@MockitoBean`).
- **Bulk (always, no Docker):** plain-Mockito `usecase` unit tests (over mocked ports) · `@WebMvcTest`
  slices · H2 `@DataJpaTest` for portable persistence · optionally a wired HTTP smoke via
  `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `RestClient` + `@LocalServerPort` (Boot 4 no longer
  auto-registers `TestRestTemplate`). This is where behavioral coverage lives.
  - **H2-fidelity tripwire:** when a `@DataJpaTest` / slice needs behavior H2 can't faithfully reproduce
    (engine-specific SQL/operators, real migration apply), **promote that test to the real-DB IT stage
    below** — never soften the assertion or add engine-specific SQL just to keep it green on H2.
- **Real-DB integration layer (small, justified, contained):** only for genuine engine behavior the
  stand-in can't fake — engine-specific SQL (e.g. Postgres `jsonb`/window functions, a `pgvector`
  vector index, engine-native upsert), real migration apply. Each IT must *name* that behavior;
  anything that could be a unit/slice test *must* be one — ITs never substitute for missing unit
  coverage.
  - **Containment:** test the **persistence adapter in isolation** — *not* a whole-app `@SpringBootTest`
    (that form is only the container-free wired smoke above); **one shared, reused** real DB for the
    whole IT set; data isolated per test (rollback/truncate), order-independent.
  - **Segregation:** tag ITs (`@Tag("it")`) and split them (surefire=unit / **failsafe**=IT, or a
    `-Pit` profile) into their **own CI stage** — the fast suite stays green with **no container**.
  - **Mechanism:** run the IT stage against the **real prod engine** via **Testcontainers (Docker)** —
    **one shared, reused** container (the project's engine image; never per-test). This *is* the normal
    tool for engine-specific ITs; the discipline is **responsibility** (few, contained, segregated),
    **not** avoiding Docker. *(Optional no-Docker alternative: install the engine — plus any extension —
    natively on the runner, e.g. `pgvector/setup-pgvector` for a pgvector project.)*

### 10.1 Live-verify surface — the standing aggregate e2e journey
Expose **one** secret-guarded `POST /internal/e2e` backed by a framework-free `E2eUseCase` that drives
**every inbound port** (`port/in`) against the real DB + real providers and returns a content-asserting
`E2eResult` the live-verify gate checks field-by-field. Run it inside a **transaction that always
rolls back** (`TransactionRunner.runAndRollback` — rollback on the adapter side, orchestration in the
core) so it mutates nothing. Secret-guarded and **safe-by-default: 404 when the secret is unset**.
**Rule: every new `port/in` is wired into `E2eUseCase` and asserted before its slice is done** — so
live coverage tracks the use-case set by construction, not per-slice bespoke scripts. (This is the
assembled-system check in `loop.md` step 8 — run against the deployed build, or the local composed stack
via `make run` when deploy is optional.)

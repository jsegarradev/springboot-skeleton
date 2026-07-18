# Git & Commit Conventions

## 1. Repository
- **Default branch `main`** — initialize with `git init -b main`.

## 2. Commit granularity
- **One commit = one component.** A component = a top-level directory containing `pom.xml` or
  `package.json`; a commit must not span more than one. In a monorepo, split backend and frontend
  into separate commits; root-level files (README, conventions) may ride with either. (With a single
  component root — single/polyrepo — this is inert.)

## 3. Commit-message bracket-tag scheme
The subject (first non-comment, non-empty line) is a tag followed by a space and a message, matching:
- **Foundational (single tag):** `[scaffold]` `[arch]` `[ci]` `[chore]` `[docs]` `[refactor]`
- **Feature (double tag):** `[<slug>][<type>]` — slug kebab-case (`[a-z0-9]+(-[a-z0-9]+)*`), type ∈
  `impl` `test` `fix` `docs` `refactor`.
- e.g. `[scaffold] init spring boot`, `[accounts][impl] add list endpoint`.

## 4. Identity
- **Commit as the real author;** where a repo forbids AI-authorship traces, author commits under the
  human identity only.
- **Choose the commit identity up front — never infer it** from ambient context (email domain, cwd
  name, etc.). Offer the identities already configured and confirm which to use before the first
  commit.

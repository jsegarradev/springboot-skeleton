---
name: sync-conventions
description: Reconcile a conventions-driven project's local `.conventions/` copy with the canonical source it was copied from — pull in central convention updates, promote any un-promoted local friction UP first (never clobber it), then flag project artifacts (arch tests, scripts, CI, hooks, formatter configs) that drifted from the updated templates. Runs inside a project that has `.conventions/`. Trigger on "sync conventions", "reconcile conventions", "update conventions from source".
---

# sync-conventions

Bring a project's **`.conventions/` mirror** back in line with the **canonical conventions source** it
was copied from. The **pull** counterpart to `promote-friction` (which pushes project learnings *up*
to central); this pulls central updates *down* — safely, without losing un-promoted local work.

## Use when
- A conventions-driven project (has `.conventions/`) has fallen behind the canonical source after
  central promotions / optimizations.

## Do not use when
- The project has no `.conventions/` → it isn't conventions-driven yet (`adopt-conventions`).
- You want to send a project's friction *up* to central → `promote-friction`.

## Guard (blocking, first)
Run inside the project (a `.conventions/` directory is present on disk — it may be git-ignored rather
than committed, which is the scaffold default; either is fine). Confirm the working tree is clean for
whatever **is** tracked (committed wired artifacts, and `.conventions/` if the project opted to commit
it) so the sync is reviewable; when `.conventions/` is git-ignored, review the diff directly instead.

## Step 1 — Find the canonical source (provenance)
Read `.conventions/SOURCE` (stamped at copy time): the **repo URL** and the **commit** the copy came
from. Resolve it:
- Prefer a **local clone** whose `origin` matches the URL (e.g. `~/develop/conventions`); else
  **clone/fetch** the remote into a temp dir.
- Target ref = the source's `main` HEAD (or a pinned tag if `SOURCE` names one).
- **No marker (older copy)?** Fall back to the known default source and **write `SOURCE` now**, so
  future syncs are exact.

## Step 2 — Detect divergence (both directions)
- **Incoming (central → project):** diff canonical@target vs `.conventions/`. Summarize the **rule
  changes** the project will adopt — what shifted, what's new — not just a file list.
- **Local drift (project → central):** does `.conventions/` differ from its recorded commit (someone
  edited the mirror locally)? Are there **un-promoted entries in the project's `FRICTION.md`**? Either
  means local work would be lost by a blind pull.

## Step 3 — Reconcile (on approval)
- **Local drift / un-promoted friction → feed it up first.** Surface it and hand off to
  `promote-friction` so the improvement reaches central **before** the mirror is overwritten. Never
  silently clobber.
- **Then update the mirror:** wipe `.conventions/` and re-import the canonical snapshot
  (`git -C <source> archive <ref> | tar -x -C .conventions`); update `.conventions/SOURCE` (new commit
  + date).

## Step 4 — Reconcile the *wired* artifacts (the real work)
`.conventions/` is a reference **mirror**; the project also has **wired-in** files derived from
templates that do **not** auto-update. Diff each against the newly-synced template and **flag drift**:
`ArchitectureTest` / `ModularityTest`, `scripts/post-deploy-verify.sh`, `.github/workflows/ci.yml`,
`.githooks/*`, `.claude/settings.json` (pipeline hook), `eclipse-formatter.xml` / `eslint.config.mjs`.
For each drift, propose the update (or hand to `adopt-conventions` for a re-install pass). Call out any
**rule change that requires code changes** to stay green (e.g. a new ArchUnit rule the code must now
satisfy) — that's a normal slice through `loop.md`, not part of the sync.

## Step 5 — Report + commit
- Report: the source ref synced to · the rule changes adopted · wired-artifact drift found · anything
  the project must now change to stay green.
- Commit in the **project** per its `git.md` (`[chore] sync .conventions to <source>@<short-sha>`);
  wired-artifact updates commit separately (one commit per component). Leave push/PR to the human.

## Output
The project's `.conventions/` mirror matches the canonical source at a recorded commit; local
improvements were promoted up first (not lost); and any wired-artifact or code drift the update
requires is surfaced (and optionally applied).

## Note — provenance depends on the stamp
This skill reads `.conventions/SOURCE`. For it to be exact, `scaffold-project` / `adopt-conventions`
should **write that marker** (source URL + commit + date) when they copy the conventions in. If the
marker is absent the skill self-heals (default source + writes it), but stamping at copy time is the
clean path.

# AGENTS.md

Working guide for AI coding agents (OMC and any other harness) in this workspace. It links the docs;
each doc owns its own scope.

## Running the session
Start prompt-free (full autonomy — permissions bypassed), then state the request; `deep-interview`
asks its questions (the one human step) and the rest runs unattended:

```
omc --madmax              # or: omc --yolo   — permission bypass (the --dangerously-skip-permissions equivalent)
omc --xhigh --madmax      # same, plus max reasoning effort
```

Fallback without the `omc` launcher:

```
claude --dangerously-skip-permissions
```

`--madmax`/`--yolo` remove only *permission* prompts — the `gates.md` gate stack still enforces
correctness. (`--xhigh` sets reasoning effort, a separate axis — confirm your OMC build accepts it on
the launch line.)

## Conventions — how the code looks
- **One `<framework>.md` per framework** holds that stack's rules (toolchain, layout, style,
  architecture enforcement, tests). **Read the matching `<framework>.md` before working in a stack.**
  Current: `springboot.md`, `angular.md`. Add a framework by adding a new `<framework>.md`.
- **Commit rules:** `git.md`.
- **Deploy (optional, per target):** `vps-deploy.md` holds deploy + live-verify conventions for the
  shared OVH VPS. It is **opt-in** — `scaffold-project` / `adopt-conventions` asks whether the project
  deploys there and includes it only if so. A project on another target adds its own
  `<target>-deploy.md`; a non-deployed project needs none.

## Process — how the work flows
- **Entry point:** every build request runs the `omc-pipeline.md` chain — `deep-interview` (the one
  human step) → autonomous `ralplan` → `ultragoal create-goals` → `complete-goals`.
- That chain drives `loop.md` — the framework-agnostic development loop — for each slice/goal. Fully
  autonomous after the interview.
- **Install the gate stack at baseline** — `gates.md` (+ `templates/`). The gates are what make these
  conventions and the loop *enforced* rather than advisory; without them, steps can be silently
  skipped.
- **Friction is captured, not auto-applied** — agents log process friction to `FRICTION.md`
  (template in `templates/`); a human promotes recurring items into these docs. Recurrence is the
  signal.

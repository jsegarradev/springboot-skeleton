---
name: promote-friction
description: Review a project's FRICTION.md, interview which recurring frictions to promote into the conventions, and propose (then, on approval, apply) the concrete doc edits. Requires a FRICTION.md with entries. Trigger on "promote friction", "review the friction log", "improve the conventions from friction".
---

# promote-friction

Feed recurring process friction back into the central conventions. Promotion is a **human decision** —
this skill clusters, interviews, proposes, and applies only what the user approves.

## Guardrail — promote patterns, not one project's stack
A friction is logged in **one** project, but a convention serves **all** of them. Promote the
**underlying pattern** and **parameterize away anything specific to the source project** — its DB
engine, extensions, libraries, service names, naming. Litmus test: *"would this rule read sensibly for
a project on a different stack?"* If it only makes sense with the source's specific tech, either
**demote that tech to a default/example** ("default X; swap per project", "e.g. Y") or **hold it** —
never stamp one project's stack onto the shared conventions. (This is exactly how a single pgvector
project once leaked `pgvector`/Postgres into the shared docs as if universal.)

## Guard (blocking, first)
Confirm `FRICTION.md` exists on disk and has entries (it may be git-ignored rather than committed — the
scaffold default — which is fine; read it from the working tree). If absent or empty, stop.

## Steps
1. **Read + cluster** — parse `FRICTION.md` (and, if present, OMC's ledger blocker/failure/re-slice
   events). Group entries by the doc + section they implicate and by theme.
2. **Rank by recurrence — across projects/contexts, not repetition within one.** The signal is the
   *pattern* recurring across projects; a friction seen only in the source project is a **candidate**,
   promoted only if its pattern generalizes (see Guardrail). One-offs / single-project quirks are noise.
3. **Summarize** — present each cluster: theme · times seen · doc(s) implicated · the change the
   entries propose.
4. **Interview** — `AskUserQuestion` (multi-select) on which clusters to **promote / defer / drop**.
5. **Generalize, then propose the edit** — first strip the source project's specifics per the
   Guardrail (parameterize engine/extensions/libs/service-names to defaults or examples). Then draft
   the concrete wording for the target doc (`loop.md` / `gates.md` / `<framework>.md` / …), in the
   docs' style: positive "do this" phrasing, one scope per file, framework specifics only in
   `<framework>.md`. Show before/after.
6. **Apply on approval** — edit the **canonical** conventions set (`~/develop/conventions`, the source
   projects copy from). Because projects hold copies, note that they must be re-copied to propagate.
7. **Mark promoted entries** — append `→ promoted <date> to <doc>#<section>` in `FRICTION.md` so they
   are not re-promoted.

## Notes
- Do not auto-apply — the user picks what promotes.
- Keep `loop.md` framework-agnostic: stack-specific fixes go into the relevant `<framework>.md`.
- **Generalize source specifics** — the friction came from one repo; the convention is universal.
  Parameterize its stack (engine / extensions / libraries / names) to defaults or examples; never
  assume the source project's tech.

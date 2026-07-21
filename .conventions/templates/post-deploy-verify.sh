#!/usr/bin/env bash
# Live-verify the ASSEMBLED SYSTEM at APP_URL (loop.md step 8) as a hard gate — a non-zero exit fails
# the release/slice. APP_URL is the DEPLOYED build (run as the deploy pipeline's final step), or, where
# deploy is optional, the LOCAL COMPOSED stack (`make run` → http://localhost:8080). Same script either
# way — only APP_URL differs. Adapt the e2e endpoint + assertions per project.
# Env: APP_URL (assembled-system base url), E2E_SECRET (guards the e2e endpoint).
# Requires spring-boot-starter-actuator (provides /actuator/health) + python3 for the JSON assertions
# (portable, present on the deploy box — no jq dependency).
set -euo pipefail

: "${APP_URL:?set APP_URL}"
: "${E2E_SECRET:?set E2E_SECRET}"

echo "[verify] health"
curl -fsS "$APP_URL/actuator/health" | grep -q '"status":"UP"'

echo "[verify] live e2e — real path + content assertions (not just 200/structure)"
curl -fsS --max-time 300 -X POST "$APP_URL/internal/e2e" \
     -H "x-e2e-secret: $E2E_SECRET" -H 'content-type: application/json' -d '{"mode":"live"}' \
  | python3 -c '
import json, re, sys
d = json.load(sys.stdin)
assert d.get("ran") is True, "e2e did not run"
assert not re.search(r"PLACEHOLDER|TODO|lorem|xxx", str(d.get("output", "")), re.I), "placeholder output"
assert len(d.get("fields") or {}) > 0, "no asserted fields"
'
# For LLM paths, also assert real billed calls: distinct model ids and tokens > 0.

echo "[verify] PASS"

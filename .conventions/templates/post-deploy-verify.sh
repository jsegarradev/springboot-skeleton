#!/usr/bin/env bash
# Live-verify the DEPLOYED build (loop.md step 8) as a hard gate. Run as the final step of the deploy
# pipeline — a non-zero exit must fail the release. Adapt the e2e endpoint + assertions per project.
# Env: APP_URL (deployed base url), E2E_SECRET (guards the e2e endpoint).
# Requires spring-boot-starter-actuator on the app (provides /actuator/health).
set -euo pipefail

: "${APP_URL:?set APP_URL}"
: "${E2E_SECRET:?set E2E_SECRET}"

echo "[verify] health"
curl -fsS "$APP_URL/actuator/health" | grep -q '"status":"UP"'

echo "[verify] live e2e — real path + content assertions (not just 200/structure)"
curl -fsS --max-time 300 -X POST "$APP_URL/internal/e2e" \
     -H "x-e2e-secret: $E2E_SECRET" -H 'content-type: application/json' -d '{"mode":"live"}' \
  | jq -e '
      .ran == true
      and ((.output // "") | test("PLACEHOLDER|TODO|lorem|xxx"; "i") | not)
      and ((.fields // {}) | length > 0)
    '
# For LLM paths, also assert real billed calls: distinct model ids and tokens > 0.

echo "[verify] PASS"

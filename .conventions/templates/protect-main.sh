#!/usr/bin/env bash
# Branch-protection gate — require green CI before merge to main; block force-push. Run once per repo.
# Full autonomy: no human review is required (required_pull_request_reviews = null), so OMC can
# self-merge on green — but nothing merges without the `ci` check passing.
# Usage: REPO=org/name ./protect-main.sh
set -euo pipefail
: "${REPO:?set REPO=org/name}"

if out=$(gh api -X PUT "repos/$REPO/branches/main/protection" --input - 2>&1 <<'JSON'
{
  "required_status_checks": { "strict": true, "contexts": ["ci"] },
  "enforce_admins": true,
  "required_pull_request_reviews": null,
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
JSON
); then
  echo "main protected: 'ci' required, force-push/deletion blocked."
elif printf '%s' "$out" | grep -q "Upgrade to GitHub Pro or make this repository public"; then
  # Known limitation, not a failure: GitHub Free has no branch protection/rulesets on PRIVATE repos
  # (personal or org). CI still runs on push/PR as an ADVISORY gate. Enable once the repo is public or
  # the owner is on GitHub Pro / Team / Enterprise.
  echo "SKIP: branch protection unavailable (private repo on a Free plan). CI-on-push remains advisory." >&2
  exit 0
else
  echo "$out" >&2
  exit 1
fi

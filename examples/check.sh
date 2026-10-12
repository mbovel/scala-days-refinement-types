#!/usr/bin/env bash
# Checks the examples:
# 1. every file compiles on its own, and all files compile together;
# 2. every commented-out line ending with `// error` fails to compile once
#    uncommented.
#
# Usage: ./check.sh [files...]
# Set SCALA to override the runner (default: `scala --cli-version 1.18.0`).
set -uo pipefail
cd "$(dirname "$0")"

read -r -a SCALA <<< "${SCALA:-scala --cli-version 1.18.0}"
files=("$@")
[ ${#files[@]} -eq 0 ] && files=(*.scala)
failures=()

for f in "${files[@]}"; do
  echo "::group::$f"
  "${SCALA[@]}" compile "$f" || failures+=("$f: does not compile")
  for n in $(grep -nE '^[[:space:]]*// .* // error(:.*)?$' "$f" | cut -d: -f1); do
    tmp="$(mktemp -d)/$f"
    sed -E "${n}s|^([[:space:]]*)// |\1|" "$f" > "$tmp"
    if "${SCALA[@]}" compile "$tmp" > /dev/null 2>&1; then
      failures+=("$f:$n: compiles but is marked // error")
    else
      echo "$f:$n: fails to compile, as expected"
    fi
  done
  echo "::endgroup::"
done

if [ $# -eq 0 ]; then
  echo "::group::all files together"
  "${SCALA[@]}" compile . || failures+=("all files together: do not compile")
  echo "::endgroup::"
fi

if [ ${#failures[@]} -gt 0 ]; then
  printf 'FAILED: %s\n' "${failures[@]}"
  exit 1
fi
echo "All examples OK."

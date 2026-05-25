#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
INDEX_FILE="$ROOT_DIR/tmp/api-cache/source-index.tsv"

if [ "$#" -lt 1 ]; then
  echo "Usage: $0 <class-or-package-fragment>" >&2
  exit 1
fi

if [ ! -f "$INDEX_FILE" ]; then
  echo "Source index not found: $INDEX_FILE" >&2
  echo "Run: python3 tools/extract_api_sources.py" >&2
  exit 2
fi

QUERY="$1"
grep -i "$QUERY" "$INDEX_FILE" || true

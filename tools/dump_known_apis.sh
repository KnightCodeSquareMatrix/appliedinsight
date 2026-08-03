#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TARGETS_FILE="$ROOT_DIR/tools/javap_targets.txt"
OUT_DIR="$ROOT_DIR/tmp/api-cache/dumps"

mkdir -p "$OUT_DIR"
"$ROOT_DIR/tools/build_api_index.sh" >/dev/null

while IFS= read -r class_name; do
  [ -z "$class_name" ] && continue
  out_file="$OUT_DIR/${class_name}.txt"
  mkdir -p "$(dirname "$out_file")"
  "$ROOT_DIR/tools/javap_lookup.sh" "$class_name" "$out_file" >/dev/null
  echo "$class_name -> $out_file"
done < "$TARGETS_FILE"

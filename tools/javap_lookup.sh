#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 1 ]; then
  echo "Usage: $0 <fully.qualified.ClassName> [output-file]" >&2
  exit 1
fi

CLASS_NAME="$1"
OUTPUT_FILE="${2:-}"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CACHE_DIR="$ROOT_DIR/tmp/api-cache"
CLASS_INDEX_FILE="$CACHE_DIR/class-index.tsv"
CLASS_PATH="${CLASS_NAME//./\/}.class"

ensure_index() {
  if [ ! -f "$CLASS_INDEX_FILE" ]; then
    "$ROOT_DIR/tools/build_api_index.sh" >/dev/null
  fi
}

list_classes_in_jar() {
  local jar_path="$1"

  if command -v jar >/dev/null 2>&1; then
    jar tf "$jar_path" 2>/dev/null || true
    return
  fi

  if command -v unzip >/dev/null 2>&1; then
    unzip -Z1 "$jar_path" 2>/dev/null || true
    return
  fi

  return 0
}

append_to_index() {
  local classpath_root="$1"
  printf '%s\t%s\n' "$CLASS_NAME" "$classpath_root" >> "$CLASS_INDEX_FILE"
}

lookup_from_index() {
  awk -F '\t' -v cls="$CLASS_NAME" '$1 == cls { print $2; exit }' "$CLASS_INDEX_FILE"
}

find_classpath_roots() {
  printf '%s\n' \
    "$HOME/.gradle/caches" \
    "$ROOT_DIR/.gradle" \
    "$ROOT_DIR/build" \
    "$ROOT_DIR/build/classes/java/main" \
    "$ROOT_DIR/build/classes/java/client" \
    "$ROOT_DIR/build/moddev" \
    "$ROOT_DIR/build/tmp" \
    "/home/knightcode/.gradle/caches"
}

fallback_lookup() {
  while IFS= read -r root; do
    [ -d "$root" ] || continue

    local class_file
    class_file="$(find "$root" -type f -path "*/$CLASS_PATH" 2>/dev/null | head -n 1 || true)"
    if [ -n "$class_file" ]; then
      local classpath_root="${class_file%/$CLASS_PATH}"
      append_to_index "$classpath_root"
      printf '%s' "$classpath_root"
      return 0
    fi

    local jar_path
    while IFS= read -r jar_path; do
      [ -n "$jar_path" ] || continue
      if list_classes_in_jar "$jar_path" | grep -Fxq "$CLASS_PATH"; then
        append_to_index "$jar_path"
        printf '%s' "$jar_path"
        return 0
      fi
    done < <(find "$root" -type f -name '*.jar' 2>/dev/null)
  done < <(find_classpath_roots)

  return 1
}

ensure_index
CLASSPATH_ROOT="$(lookup_from_index)"
if [ -z "$CLASSPATH_ROOT" ]; then
  CLASSPATH_ROOT="$(fallback_lookup || true)"
fi

if [ -z "$CLASSPATH_ROOT" ]; then
  echo "Class not found in index or fallback scan: $CLASS_NAME" >&2
  exit 2
fi

if [ -n "$OUTPUT_FILE" ]; then
  mkdir -p "$(dirname "$OUTPUT_FILE")"
  javap -classpath "$CLASSPATH_ROOT" -public "$CLASS_NAME" > "$OUTPUT_FILE"
  echo "$OUTPUT_FILE"
else
  javap -classpath "$CLASSPATH_ROOT" -public "$CLASS_NAME"
fi

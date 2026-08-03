#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CACHE_DIR="$ROOT_DIR/tmp/api-cache"
INDEX_FILE="$CACHE_DIR/jar-index.txt"
CLASS_INDEX_FILE="$CACHE_DIR/class-index.tsv"

mkdir -p "$CACHE_DIR"

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

: > "$INDEX_FILE"
while IFS= read -r root; do
  [ -d "$root" ] || continue
  find "$root" -type f -name '*.jar' 2>/dev/null || true
done < <(find_classpath_roots) | sort -u > "$INDEX_FILE"

: > "$CLASS_INDEX_FILE"
while IFS= read -r jar_path; do
  [ -n "$jar_path" ] || continue
  list_classes_in_jar "$jar_path" | \
    grep '\.class$' | \
    grep -v '/module-info\.class$' | \
    sed 's#/#.#g; s#\.class$##' | \
    awk -v cp="$jar_path" '{print $0 "\t" cp}' >> "$CLASS_INDEX_FILE" || true
done < "$INDEX_FILE"

while IFS= read -r root; do
  [ -d "$root" ] || continue
  find "$root" -type f -name '*.class' 2>/dev/null | \
    grep -v '/module-info\.class$' | \
    while IFS= read -r class_file; do
      rel_path="${class_file#"$root"/}"
      class_name="${rel_path//\//.}"
      class_name="${class_name%.class}"
      printf '%s\t%s\n' "$class_name" "$root"
    done >> "$CLASS_INDEX_FILE" || true
done < <(find_classpath_roots)

sort -u "$CLASS_INDEX_FILE" -o "$CLASS_INDEX_FILE"

echo "JAR index: $INDEX_FILE"
echo "Class index: $CLASS_INDEX_FILE"
echo "Jar count: $(wc -l < "$INDEX_FILE")"
echo "Class count: $(wc -l < "$CLASS_INDEX_FILE")"

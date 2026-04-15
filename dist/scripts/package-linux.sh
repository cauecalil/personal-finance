#!/usr/bin/env bash
set -euo pipefail

APP_VERSION="${1:-1.0.0}"
JAR_PATH="${2:-}"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
INPUT_DIR="$REPO_ROOT/dist/input"
OUTPUT_DIR="$REPO_ROOT/dist/output"
APP_NAME="Personal Finance"
PORTABLE_DIR_NAME="Personal-Finance"

if [[ -z "$JAR_PATH" ]]; then
  JAR_PATH="$(find "$REPO_ROOT/backend/target" -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | head -n 1)"
fi

if [[ -z "$JAR_PATH" || ! -f "$JAR_PATH" ]]; then
  echo "No executable JAR found." >&2
  exit 1
fi
if [[ -z "${JAVA_HOME:-}" ]]; then
  echo "JAVA_HOME is required to package with jpackage." >&2
  exit 1
fi

mkdir -p "$INPUT_DIR" "$OUTPUT_DIR"
JAR_FILE_NAME="$(basename "$JAR_PATH")"
cp "$JAR_PATH" "$INPUT_DIR/$JAR_FILE_NAME"

PORTABLE_SOURCE_PATH="$OUTPUT_DIR/$APP_NAME"
if [[ -e "$PORTABLE_SOURCE_PATH" ]]; then
  rm -rf "$PORTABLE_SOURCE_PATH"
fi

ARGS=(
  --type app-image
  --name "$APP_NAME"
  --app-version "$APP_VERSION"
  --input "$INPUT_DIR"
  --main-jar "$JAR_FILE_NAME"
  --main-class org.springframework.boot.loader.launch.JarLauncher
  --dest "$OUTPUT_DIR"
  --runtime-image "$JAVA_HOME"
)

jpackage "${ARGS[@]}"

if [[ ! -d "$PORTABLE_SOURCE_PATH" ]]; then
  echo "Portable app-image not found: $PORTABLE_SOURCE_PATH" >&2
  exit 1
fi

PORTABLE_ARCHIVE_PATH="$OUTPUT_DIR/$PORTABLE_DIR_NAME-$APP_VERSION-linux-portable.tar.gz"
if [[ -f "$PORTABLE_ARCHIVE_PATH" ]]; then
  rm -f "$PORTABLE_ARCHIVE_PATH"
fi

tar -C "$OUTPUT_DIR" -czf "$PORTABLE_ARCHIVE_PATH" "$APP_NAME"

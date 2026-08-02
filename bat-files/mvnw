#!/bin/sh
set -eu
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DIST_URL=$(sed -n 's/^distributionUrl=//p' "$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.properties" | tr -d '\r')
NAME=$(basename "$DIST_URL" .zip)
NAME=${NAME%-bin}
M2=${MAVEN_USER_HOME:-$HOME/.m2}
HOME_DIR="$M2/wrapper/dists/$NAME"
MVN="$HOME_DIR/bin/mvn"
if [ ! -x "$MVN" ]; then
  TMP=$(mktemp -d)
  trap 'rm -rf "$TMP"' EXIT
  mkdir -p "$(dirname "$HOME_DIR")"
  if command -v curl >/dev/null 2>&1; then curl -fsSL "$DIST_URL" -o "$TMP/maven.zip"; else wget -q "$DIST_URL" -O "$TMP/maven.zip"; fi
  unzip -q "$TMP/maven.zip" -d "$TMP"
  mv "$TMP/$NAME" "$HOME_DIR"
fi
exec "$MVN" "$@"

#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# scripts/setup-oorexx.sh -- install the ooRexx this repository builds and
# tests against: SourceForge's own build of one pinned trunk revision.
#
# Usage:  scripts/setup-oorexx.sh portable [DIR]   (CI; default DIR: ~/oorexx)
#         scripts/setup-oorexx.sh deb                (a session; uses sudo)
#
# The revision is OOREXX_REV if set, else the one in .github/workflows/ci.yml
# (the line "OOREXX_REV: ..."), so that CI and sessions never differ.
#
#   portable  unzips oorexx-5.3.0-<rev>.ubuntu2404.x86_64-portable-release.zip
#             into DIR and prints the lines to add to the environment
#             (appended to $GITHUB_PATH / $GITHUB_ENV under GitHub Actions)
#   deb       installs oorexx-5.3.0-<rev>.ubuntu2404.x86_64.deb (/usr/local)
# -----------------------------------------------------------------------------
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REV="${OOREXX_REV:-$(sed -n 's/^ *OOREXX_REV: *"\{0,1\}\([0-9]*\)"\{0,1\}.*/\1/p' "$ROOT/.github/workflows/ci.yml" | head -1)}"
[ -n "$REV" ] || { echo "no OOREXX_REV (environment or .github/workflows/ci.yml)"; exit 1; }
BASE="https://downloads.sourceforge.net/project/oorexx/oorexx/5.3.0beta"
MODE="${1:-portable}"

case "$MODE" in
  portable)
    DIR="${2:-$HOME/oorexx}"
    NAME="oorexx-5.3.0-$REV.ubuntu2404.x86_64-portable-release"
    mkdir -p "$DIR"
    curl -fsSL --retry 3 -o "$DIR/$NAME.zip" "$BASE/portable/$NAME.zip"
    unzip -qo "$DIR/$NAME.zip" -d "$DIR"
    HOME_DIR="$DIR/$NAME"
    echo "ooRexx r$REV (portable) in $HOME_DIR"
    if [ -n "${GITHUB_PATH:-}" ]; then
      echo "$HOME_DIR/bin" >> "$GITHUB_PATH"
      echo "OOREXX_HOME=$HOME_DIR" >> "$GITHUB_ENV"
      echo "LD_LIBRARY_PATH=$HOME_DIR/lib${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}" >> "$GITHUB_ENV"
    else
      echo "export PATH=\"$HOME_DIR/bin:\$PATH\" OOREXX_HOME=\"$HOME_DIR\""
      echo "export LD_LIBRARY_PATH=\"$HOME_DIR/lib\${LD_LIBRARY_PATH:+:\$LD_LIBRARY_PATH}\""
    fi
    ;;
  deb)
    NAME="oorexx-5.3.0-$REV.ubuntu2404.x86_64.deb"
    TMP="$(mktemp -d)"
    curl -fsSL --retry 3 -o "$TMP/$NAME" "$BASE/$NAME"
    sudo dpkg -i "$TMP/$NAME"
    rm -rf "$TMP"
    rexx -v | head -1
    ;;
  *) echo "usage: $0 portable [DIR] | deb"; exit 2 ;;
esac

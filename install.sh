#!/usr/bin/env bash
# Installs a pinned Stainless release (with bundled z3 and cvc5) into ./.stainless/
# next to this script. Requires curl, unzip and a JDK (java on PATH) to run it.
# Usage: ./install.sh && ./.stainless/stainless File.scala
set -euo pipefail

VERSION="0.10.2"

case "$(uname -s)-$(uname -m)" in
  Linux-x86_64)  platform="linux";     sha="a2b0c73f3ce030c7dca034e6b20ec0e165c19909713bfc792d057ef4d9d58ca5" ;;
  Darwin-arm64)  platform="mac-arm64"; sha="169ad82bdd7d0d77dc92acacd70b6b8e78d9cb0f7459ee7300cf5361cc1d04ff" ;;
  Darwin-x86_64) platform="mac-x64";   sha="13b36cf327a5f9bb04a22ad4a64c0b50272665bc60e5ae2ab0b314389e55a35a" ;;
  *) echo "Unsupported platform: $(uname -s)-$(uname -m)" >&2; exit 1 ;;
esac

dest="$(cd "$(dirname "$0")" && pwd)/.stainless"
if [[ "$(cat "$dest/VERSION" 2>/dev/null)" == "$VERSION" ]]; then
  echo "Stainless $VERSION already installed in $dest"; exit 0
fi

url="https://github.com/epfl-lara/stainless/releases/download/v$VERSION/stainless-dotty-standalone-$VERSION-$platform.zip"
zip="$(mktemp)"
trap 'rm -f "$zip"' EXIT

echo "Downloading $url"
curl -fL --retry 3 --retry-all-errors --progress-bar -o "$zip" "$url"
echo "$sha  $zip" | shasum -a 256 -c - >/dev/null

rm -rf "$dest" && mkdir -p "$dest"
unzip -q "$zip" -d "$dest"
echo "$VERSION" > "$dest/VERSION"
echo "Installed Stainless $VERSION ($platform) in $dest"

#!/usr/bin/env bash
# Re-download the vendored slide runtime (needs network). Run from anywhere:
#   ./lib/fetch.sh
set -euo pipefail

REVEAL_VERSION=5.2.1
KATEX_VERSION=0.18.7
HLJS_VERSION=11.9.0
INTER_VERSION=4.1.1

OUT="$(cd "$(dirname "$0")" && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

fetch() { # fetch <npm-path> <dir>
  curl -fsSL "https://registry.npmjs.org/$1" -o "$TMP/pkg.tgz"
  mkdir -p "$TMP/$2"
  tar xzf "$TMP/pkg.tgz" -C "$TMP/$2" --strip-components=1
}

fetch "reveal.js/-/reveal.js-$REVEAL_VERSION.tgz" reveal
fetch "katex/-/katex-$KATEX_VERSION.tgz" katex
fetch "@highlightjs/cdn-assets/-/cdn-assets-$HLJS_VERSION.tgz" hljs
fetch "inter-ui/-/inter-ui-$INTER_VERSION.tgz" inter

rm -rf "$OUT/reveal.js" "$OUT/katex" "$OUT/highlight.js" "$OUT/inter"
mkdir -p "$OUT/reveal.js/dist/theme" "$OUT/katex/fonts" \
         "$OUT/highlight.js/languages" "$OUT/highlight.js/styles" "$OUT/inter/variable"

# reveal.js: core, every theme, theme fonts in .woff only.
cp "$TMP"/reveal/dist/{reset.css,reveal.css,reveal.js} "$OUT/reveal.js/dist/"
cp "$TMP"/reveal/dist/theme/*.css "$OUT/reveal.js/dist/theme/"
for f in $(cd "$TMP/reveal/dist/theme" && find fonts -type f \( -name '*.woff' -o -name '*.css' -o -name 'LICENSE' \)); do
  mkdir -p "$OUT/reveal.js/dist/theme/$(dirname "$f")"
  cp "$TMP/reveal/dist/theme/$f" "$OUT/reveal.js/dist/theme/$f"
done
for p in notes search zoom; do  # the plugins pandoc's template loads
  mkdir -p "$OUT/reveal.js/plugin/$p"
  find "$TMP/reveal/plugin/$p" -maxdepth 1 -type f ! -name '*.esm.js' ! -name '*.map' \
    -exec cp {} "$OUT/reveal.js/plugin/$p/" \;
done
cp "$TMP/reveal/LICENSE" "$OUT/reveal.js/LICENSE"

# KaTeX: .woff2 and .woff faces (the .ttf fallback is never reached).
cp "$TMP"/katex/dist/{katex.min.css,katex.min.js} "$OUT/katex/"
cp "$TMP"/katex/dist/fonts/*.woff2 "$TMP"/katex/dist/fonts/*.woff "$OUT/katex/fonts/"
cp "$TMP/katex/LICENSE" "$OUT/katex/LICENSE"

# highlight.js: browser bundle plus the grammars these decks use.
cp "$TMP/hljs/highlight.min.js" "$OUT/highlight.js/"
cp "$TMP"/hljs/languages/{scala,haskell}.min.js "$OUT/highlight.js/languages/"
# Highlight `opaque` as a keyword: 11.9.0's Scala grammar lacks it.
perl -pi -e 's/given transparent"/given transparent opaque"/' "$OUT/highlight.js/languages/scala.min.js"
printf '\n' >> "$OUT/highlight.js/languages/scala.min.js"
cp "$TMP"/hljs/styles/atom-one-light.min.css "$OUT/highlight.js/styles/"
cp "$TMP/hljs/LICENSE" "$OUT/highlight.js/LICENSE"

# Inter: the variable font only (the decks' main font, `InterVariable`).
cp "$TMP/inter/inter-variable.css" "$OUT/inter/"
cp "$TMP"/inter/variable/{InterVariable.woff2,InterVariable-Italic.woff2} "$OUT/inter/variable/"
cp "$TMP/inter/LICENSE.txt" "$OUT/inter/LICENSE.txt"

echo "Vendored into $OUT: reveal.js $REVEAL_VERSION, KaTeX $KATEX_VERSION, highlight.js $HLJS_VERSION, Inter $INTER_VERSION"

# First-class Logical Refinement Types for Scala

Slides and examples for the Scala Days 2026 talk by Matt Bovel (EPFL).

- Slides: [presentation/presentation.html](https://mbovel.github.io/scala-days-refinement-types/) ([PDF](presentation/presentation.pdf))
- Paper: [First-Class Refinement Types for Scala](https://doi.org/10.1145/3839541) (OOPSLA 2026)
- Playground: [matt.bovel.net/scala-refinement-types/playground](https://matt.bovel.net/scala-refinement-types/playground/)

## Examples

[examples/](examples/) contains every example from the slides, plus a few more.
Each file runs on its own with [Scala CLI](https://scala-cli.virtuslab.org/):

```sh
scala --cli-version 1.18.0 run examples/2_fansi.scala
```

Lines marked `// error` are commented out because they are expected not to
compile; uncomment one to see the error message. `examples/check.sh` compiles
every file and checks that each `// error` line indeed fails; CI runs it on
every push.

To use refinement types in your own project, add these directives to your file:

```scala
//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes
```

or, with sbt, in `build.sbt`:

```scala
scalaOrganization := "ch.epfl.lara"
scalaVersion := "3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY"
scalacOptions += "-language:experimental.qualifiedTypes"
```

## Verifying with Stainless

[stainless/](stainless/) contains examples verified by
[Stainless](https://epfl-lara.github.io/stainless/refinementtypes.html), which
checks refinement types with SMT solvers. `install.sh` installs a pinned
Stainless release (with z3 and cvc5) into `.stainless/`:

```sh
./install.sh
.stainless/stainless stainless/sum.scala stainless/merge_sort.scala
```

The files also compile with Scala CLI, in `silent` mode, once Stainless is
installed (`merge_sort.scala` uses the Stainless library from `.stainless/`).

## Building the slides

The slides are written in [presentation/presentation.md](presentation/presentation.md).
`presentation.html` and `presentation.pdf` are committed; to rebuild them
(requires Docker):

```sh
cd presentation && make
```

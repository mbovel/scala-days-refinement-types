---
title: First-class Logical<br/>Refinement Types for Scala
author: "Matt Bovel, EPFL<br/>[matt.bovel.net](https://matt.bovel.net)"
title-slide-attributes:
  data-background-image: scala_days.jpg
  data-background-size: cover
---

## What is this about?

A *prototype* implementation of (logical) refinement types for Scala.

<div class="fragment">

A _refinement type_ is a  type _refined_ with a logical predicate. For example:

```scala
type Pos = {x: Int with x >= 0}
```

</div>

## Syntax

Long form, with an explicit binder:

```scala
type NonEmpty[A] = {l: List[A] with l.nonEmpty}
```

<div class="fragment">

Short form, reusing a name already in scope:

```scala
val x: (Int with x % 2 == 0) = 42
// desugars to:
val x: {v: Int with v % 2 == 0} = 42
```

</div>

<div class="fragment">

Surveyed and implemented by [Quentin Bernet](https://www.linkedin.com/in/quentin-bernet-983878152/) for his Master thesis.

</div>

## Example 1: Sized lists

```scala
def zip[A, B](
  xs: List[A],
  ys: List[B] with ys.size == xs.size
): {zs: List[(A, B)] with zs.size == xs.size}
```

```scala {.fragment}
def concat[T](
  xs: List[T],
  ys: List[T]
): {zs: List[T] with zs.size == xs.size + ys.size}
```

```scala {.fragment}
val xs: List[Int] = ???
val ys: List[Int] = ???
zip(concat(xs, ys), concat(ys, xs))
zip(concat(xs, ys), concat(xs, xs)) // ⛔️
```

## whoami

<div class="columns">

<div class="column">

<img src="./baby_matt.png" height="500em" class="fragment" />


</div>

<div class="column">

<div class="fragment">

PhD student at EPFL, co-advised by:

- Martin Odersky (LAMP/Scala)
- Viktor Kunčak (LARA/Stainless)

</div>

</div>

</div>

## OOPSLA 2026 paper

<a href="https://doi.org/10.1145/3839541"><img src="./paper.png" height="480" class="paper" /></a>

## Refinement vs. Assertions

<div class="columns">
<div class="column">

Why not use assertions instead?

```scala
def zip[A, B](
  xs: List[A],
  ys: List[B]
) : List[(A, B)] = {
  require(xs.size == ys.size)
  ???
}.ensuring(_.size == xs.size)
```

<div class="fragment">

1. **Runtime overhead**: assertions are checked at runtime, refinement types are checked at compile time (by default).

</div>

</div>
<div class="column">


<div class="fragment">

2. **No static guarantees**: assertions are only tested with the inputs you run them with. Refinement types hold _for all values_.

</div>

<div class="fragment">

3. **Not part of the API**: assertions are not visible in the function type / scaladoc.

</div>

<div class="fragment">

4. **Hard to compose**: assertions cannot be passed as type argument:

    ```scala
    type Pos = {x: Int with x >= 0}
    def foo(l: List[Pos])
    def bar(f: Pos => Pos)
    ```

</div>

</div> <!-- .column -->

</div> <!-- .columns -->


## Example 2: Fansi

From Li Haoyi's [Fansi](https://github.com/com-lihaoyi/fansi) library for colored terminal strings:

<div class="columns">

<div class="column">

With assertions:

```{.scala .small}


def True(r: Int, g: Int, b: Int) =
  True0(r, g, b, trueIndex(r, g, b))

def trueIndex(r: Int, g: Int, b: Int) =
  require(0 <= r && r < 256)
  require(0 <= g && g < 256)
  require(0 <= b && b < 256)
  r << 16 | g << 8 | b

True(256, 0, 0) // 💥 at run time
```

</div>

<div class="column">

<div class="fragment">

With refinement types:

```{.scala .small}
type Comp = {v: Int with 0 <= v && v < 256}

def True(r: Comp, g: Comp, b: Comp) =
  True0(r, g, b, trueIndex(r, g, b))

def trueIndex(r: Comp, g: Comp, b: Comp) =
  r << 16 | g << 8 | b




True(256, 0, 0) // ⛔️ at compile time
```

</div>

</div>

</div>

## _first-class_?

In our prototype, refinement types are ordinary Scala types, participating in inference, **overload resolution** (example 3), **subtyping** (example 4), etc.

<div class="fragment">

In contrast, Liquid Haskell is a plugin that runs after type checking:

```haskell
{-@ x :: {v:Int | v mod 2 == 0 } @-}
let x = 42 :: Int in ...
```

“It's sort of like you're doing two things at once [...] you're also talking to GHC, but you're also talking to LiquidHaskell.” <small>— a participant in [Usability Barriers for Liquid Types](https://dl.acm.org/doi/10.1145/3729327) (Gamboa et al., PLDI 2025)</small>

</div>

<div class="fragment">

Our implementation is directly integrated into the Scala 3 compiler:

```scala
val x: (Int with x % 2 == 0) = 42
```

</div>

## Error messages

Predicates are type-checked like other Scala expressions:

```scala
def f[A](l: List[A] with l.notEmpty) = () // error
```

```text
-- [E008] Not Found Error: 03_error_messages.scala:9:27
9 |def f[A](l: List[A] with l.notEmpty) = () // error
  |                         ^^^^^^^^^^
  |      value notEmpty is not a member of List[A] - did you mean l.nonEmpty?
```

## Error messages and inference

Same inference and error reporting as for other Scala types:

```scala
type PosInt = {v: Int with v > 0}
def g[T](f: T => Unit, x: T) = f(x)
g((x: PosInt) => x * 2, -2) // error
```

```text
-- [E007] Type Mismatch Error: 03_error_messages.scala:17:26
17 |  g((x: PosInt) => x * 2, -2) // error
   |                          ^^
   |                          Found:    (-2 : Int)
   |                          Required: {x: Int with x > 0}
```

## Example 3: Overload resolution

```scala
extension (l: List[Int]) def isSorted: Boolean =
  l.zip(l.drop(1)).forall((x, y) => x <= y)

@targetName("minSorted")
def min(l: List[Int] with l.isSorted) =
  l.head // O(1)

def min(l: List[Int]) =
  l.min // O(n)

def ex1(l: List[Int] with l.isSorted) =
  min(l) // calls the first overload
```

## Example 4: Bounded polymorphism

<p style="margin-bottom: -1em"><small>Inspired by [Abstract Refinement Types](https://goto.ucsd.edu/~rjhala/liquid/abstract_refinement_types.pdf) (Vazou et al., ESOP 2013).</small></p>

```scala
given Ord[Int] = ...

type Even = {v: Int with v % 2 == 0}

def ex2: Even =
  maximum(List(2, 4, 6))
```

<div class="fragment">

```scala
def max[T: Ord as ord, U <: T](x: U, y: U): U =
  if ord.compare(x, y) >= 0 then x else y

def maximum[T: Ord, U <: T](xs: List[U]): U =
  xs.reduce(max)
```

</div>

## Refinement types vs. libraries

With [Iron](https://github.com/Iltotore/iron), predicates are defined as type-level constraints:

```scala
extension (l: List[Int]) def isSorted(): Boolean =
  l.zip(l.drop(1)).forall((x, y) => x <= y)

final class IsSorted
given Constraint[List[Int], IsSorted] with
  override inline def test(inline l: List[Int]): Boolean = l.isSorted()
  override inline def message: String = "List must be sorted"

def min(l: List[Int] :| IsSorted): Int = l.head
```

<div class="fragment">

```scala
min(List(1, 2, 3))              // ⛔️ cannot be verified at compile time
min(List(1, 2, 3).refineUnsafe) // ✅ checked at run time
```

</div>

## Limitations of existing approaches

Existing constructs (structural refinements, singleton types, compile-time operations) and libraries (Iron) suffer from:

<div class="fragment">

1. **Limited reasoning**: only fields, literals, constant folding, etc.

</div>

<div class="fragment">

2. **Not inferred**: types must be annotated manually, or programs are not typable at all.

</div>

<div class="fragment">

3. **Different languages**: predicates are written at the type level (`IsSorted`, `as.size.type`), not at the term level (`isSorted`, `as.size`).

</div>

<div class="fragment">

4. **Manual reflection**: each term-level method must be reflected at the type level by hand, like `isSorted` as `IsSorted` and its `Constraint`.

</div>

## What are valid predicates?

<div class="fragment">

```scala
var x = 3
val y: Int with y == 3 = x // ⛔️ x is mutable
```

</div>

<div class="fragment">

```scala
class Box(val value: Int)
val b: Box with b == Box(3) = Box(3) // ⛔️ Box has equality by reference
```

</div>

<div class="fragment">

The predicate language is restricted to a fragment of Scala consisting of constants, stable identifiers, field selections over `val` fields, pure term applications, type applications, and constructors of case classes without initializers.

</div>

## Purity of predicates

By default, functions called in predicates are _assumed_ to be pure:

```scala
class Cell { var field: Int = 0 }
def test(c: Cell) =
  def getAndBump(): Int = { c.field += 1; c.field }
  val v: Int with v == getAndBump() = getAndBump() // ✅ accepted
```

<div class="fragment">

With [capture](https://docs.scala-lang.org/scala3/reference/experimental/cc.html) and [separation checking](https://docs.scala-lang.org/scala3/reference/experimental/capture-checking/mutability.html), predicates are required to be pure:

```scala
import language.experimental.{captureChecking, separationChecking}
class Cell extends caps.Stateful { var field: Int = 0 }
def test(c: Cell^) =
  def getAndBump(): Int = { c.field += 1; c.field }
  val v: Int with v == getAndBump() = getAndBump() // ⛔️ not pure: uses {c}
```

</div>

## When are refinements inferred?

<div class="columns">

<div class="column">

Refinement types are **not** inferred from terms by default; the wider type is:

```scala
val x = 42
// x: Int, not {v: Int with v == 42}
```

<div class="fragment" data-fragment-index="1">

Why not always infer the precise type?

1. **Backward compatibility:** implicit and overload resolution depend on inferred types; a more precise type changes which instances are found.

</div>

</div>

<div class="column">

<div class="fragment" data-fragment-index="2">

2. **Performance:** bigger types, slower comparisons.

</div>

<div class="fragment" data-fragment-index="3">

3. **Developer eXperience:** users would be flooded with complex types.

</div>

<div class="fragment" data-fragment-index="4">

Instead, precision is recovered **on demand**, by a standard mechanism called _selfification_, triggered by the bidirectional type-inference algorithm:

```scala
val x: (Int with x == 42) = 42
```

</div>

</div>

</div>

## Flow-sensitive typing

The condition of an `if` is assumed in its branches:

```scala
def pos(x: Int): {v: Int with v > 0} =
  if x > 0 then x else 1
```

<div class="fragment">

In the [bound-checked merge sort](https://github.com/mbovel/scala-days-refinement-types/blob/main/dotty-examples/11_merge_sort_bounds.scala), `head` and `tail` require a non-empty sequence (`SafeSeq[T] with a.len > 0`):

```{.scala .small}
def merge[T: Ordering as ord](left: SafeSeq[T], right: SafeSeq[T]): SafeSeq[T] =
  if left.len > 0 && right.len > 0 then
    if ord.lt(left.head, right.head) then SafeSeq(left.head) ++ merge(left.tail, right)
    else SafeSeq(right.head) ++ merge(left, right.tail)
  else if left.len == 0 then right
  else left
```

</div>

## Solver

<div class="columns">

<div class="column">

How does the compiler check `{x: T with p(x)} <: {x: T with q(x)}`?


<div class="fragment">

Need to check that `p(x)` implies `q(x)` for all `x: T`.

</div>

<div class="fragment">

Existing implementations in other languages lower to SMT solvers.

</div>

<div class="fragment">

Due to packaging and performance concerns, we instead implemented our own **lightweight e-graph-based solver**.

</div>

</div>

<div class="column">

<div class="fragment">

```scala
{v: Int with v == a && a == b}
  <: {v: Int with v == b}
{v: Int with a == b}
  <: {v: Int with f(a) == f(b)}
```

</div>

<div class="fragment">

And domain-specific normalizations such as:

```scala
{v: Int with v == x + 3 * y}
  <: {v: Int with v == 2 * y + (x + y)}
```

Also beta-reduction, ADT selections reductions and limited reasoning on LIA inequalities.
</div>

</div>

</div>

<div class="notes">

- Strictly stronger than substitution alone; intentionally weaker than equality saturation: one representative per class, so only one form is explored.

</div>

## Run-time checks

**Pattern matching** branches on whether the predicate holds at run time:

```scala
val idRegex = "[0-9a-f]{4}-[0-9a-f]{4}"
type ID = {s: String with s.matches(idRegex)}

"a2e7-e89b" match
  case id: ID => ... // holds, id has type ID
  case _      => ... // does not hold
```

## Run-time checks: `.runtimeChecked`

You can also use `.runtimeChecked` ([SIP-57](https://docs.scala-lang.org/sips/replace-nonsensical-unchecked-annotation.html)) when you expect the check to always pass:

```scala
val id: ID = "a2e7-e89b".runtimeChecked
```

<div class="fragment">

Desugars to:

```scala
val id: ID =
  if ("a2e7-e89b".matches(idRegex)) "a2e7-e89b".asInstanceOf[ID]
  else throw new IllegalArgumentException()
```

</div>

<div class="fragment">

Note: like with other types, you can also use `.asInstanceOf[ID]` directly to skip the check altogether.

</div>

<div class="fragment">

`.runtimeChecked` for refinement types was implemented by [Valentin Schneeberger](https://www.linkedin.com/in/valentin-schneeberger-b46b77271/).

</div>

## Gradual typing

By default, a refinement that cannot be verified statically is a type error:

```scala
def getInt(): Int = 1
val x: {v: Int with v == 1} = getInt() // ⛔️ type error
```

<div class="fragment">

A language import turns these errors into something else:

```scala
import language.experimental.qualifiedTypes.runtimeChecks // or warn, or silent
```

<div style="margin-top: 1.5em;">

| Mode | Unverified refinement becomes |
|---|---|
| `runtimeChecks` | run-time check, throws if it fails |
| `warn` | warning, no run-time check |
| `silent` | accepted, no run-time check |

</div>

</div>

## Verifying with Stainless

The [Stainless](https://epfl-lara.github.io/stainless/refinementtypes.html) verifier can use Dotty's refinement types (support implemented by [Kasia Marek](https://www.linkedin.com/in/kasiamarek/))! It compiles in `silent` mode, then verifies refinements itself with SMT solvers:

```scala
def sum(n: BigInt with n >= 0): {r: BigInt with 2 * r == n * (n + 1)} =
  if n == 0 then BigInt(0) else n + sum(n - 1) // ✅ verified
def abs(x: Int): {r: Int with r >= 0} =
  if x >= 0 then x else -x // ⛔️ counter-example: x = -2147483648
```

<div class="fragment">

Install it with the [script](https://github.com/mbovel/scala-days-refinement-types/blob/main/install.sh) from this talk's repository:

```sh
./install.sh # Stainless 0.10.2, with z3 and cvc5, in .stainless/
.stainless/stainless stainless-examples/sum.scala
```

</div>

<div class="fragment">

See also: a verified [merge sort](https://github.com/mbovel/scala-days-refinement-types/blob/main/stainless-examples/merge_sort.scala): sorted, same size, same elements.

</div>

## Example 5: Memoized Fibonacci

A map whose values are related to their keys by a predicate `p`:

```scala
case class DepMap[K, V](p: (K, V) => Boolean):
  def put(n: K, v: V with p(n, v)): Unit = ???
  def get(n: K): Option[{res: V with p(n, res)}] = ???
```

<div class="fragment">

A function whose result type is its own definition:

```scala
def fib(n: Int): {r: Int with r == (if n <= 1 then 1 else fib(n - 1) + fib(n - 2))} =
  if n <= 1 then 1 else fib(n - 1) + fib(n - 2)
```

</div>

## Example 5: Memoized Fibonacci (2)

A memoized version, checked to always return `fib(n)`:

```scala
val cache: (DepMap[Int, Int] with (cache == DepMap[Int, Int]((k, v) => v == fib(k)))) =
  DepMap[Int, Int]((k, v) => v == fib(k))

def fibMemo(n: Int): {res: Int with res == fib(n)} =
  cache.get(n) match
    case Some(res) => res
    case None =>
      val res: (Int with (res == fib(n))) =
        if n <= 1 then 1 else fibMemo(n - 1) + fibMemo(n - 2)
      cache.put(n, res)
      res
```

## Evaluation

**Compilation overhead**, against each system's own unchecked baseline:

| System | Overhead |
|---|---|
| First-class (ours) | **0–13%** |
| Schmid and Kunčak | 20–41% |
| Stainless | ≥ 53% |

<div class="fragment">

**When the feature is unused**, the full Dotty CI passes unchanged: the compiler itself (≈200 000 LoC), ≈10 000 tests, a 50-project community build.

≤ 4% slowdown on the official benchmark suite.

</div>

## Future work

- **Pre-SIP and SIP** to make refinement types part of Scala.

- **Better Solver** for what our lightweight solver cannot do.

- **A termination checker**, needed only for the termination-sensitive entailment rules, never systematically for every function in a predicate.

- **Term-parameterized types**, to modularize predicates:

```scala
    type Range(from: Int, to: Int) =
      {v: Int with v >= from && v < to}
```

## How to use

Try it online in the [playground](https://matt.bovel.net/scala-refinement-types/playground/).

<div class="fragment">

**[Scala CLI](https://scala-cli.virtuslab.org/):** add these directives and run `scala --cli-version 1.18.0 run hello.scala`:

```scala
//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes
```

</div>

<div class="fragment">

**[sbt](https://www.scala-sbt.org/):** in `build.sbt`:

```scala
scalaOrganization := "ch.epfl.lara"
scalaVersion := "3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY"
scalacOptions += "-language:experimental.qualifiedTypes"
```

</div>

## Conclusion

<div class="columns">

<div class="column qr-codes">

<figure>
<a href="https://mbovel.github.io/scala-days-refinement-types/presentation/presentation.html"><img src="./qr-code-presentation.svg" alt="QR code to the slides" /></a>
<figcaption><a href="https://mbovel.github.io/scala-days-refinement-types/presentation/presentation.html">Slides</a></figcaption>
</figure>

<figure>
<a href="https://matt.bovel.net/scala-refinement-types"><img src="./qr-code-paper.svg" alt="QR code to the paper" /></a>
<figcaption><a href="https://matt.bovel.net/scala-refinement-types">Paper</a></figcaption>
</figure>

<figure>
<a href="https://matt.bovel.net/scala-refinement-types/playground/"><img src="./qr-code-playground.svg" alt="QR code to the playground" /></a>
<figcaption><a href="https://matt.bovel.net/scala-refinement-types/playground/">Playground</a></figcaption>
</figure>

</div>

<div class="column" style="flex: 0 0 auto;">

<figure style="text-align: center; margin: -1.2em 0 0 0;">
<img src="./refined_type.png" style="height: 450px; width: auto;" />
<figcaption><small><em>Un type raffiné</em>,<br/>by Marina Granados Castro</small></figcaption>
</figure>

</div>

</div>

# Bonus slides

## Refinement types vs. existing constructs

<div class="columns">
<div class="column">

<div class="column fragment">

Scala already has _structural refined types_:

```scala
def zip[A, B](
  as: List[A],
  bs: List[B] {
    val size: as.size.type
  }
): List[(A, B)] {
  val size: as.size.type
}
```

</div>

</div>

<div class="column">

<div class="fragment">

And singleton types, and opaque types, and compiletime operations:

```scala
opaque type SizedList[
  X <: Int: Singleton,
  A
] <: List[A]

def zip[A, B, Size <: Int: Singleton](
  as: SizedList[Size, A],
  bs: SizedList[Size, B]
): SizedList[Size, (A, B)]
```

</div>

</div>

</div> <!-- .columns -->

## Subtyping: Constant folding

```scala
{v: Int with v == 1 + 1}     <: {v: Int with v == 2}
```

## Subtyping: Normalization

Arithmetic expressions are normalized using standard algebraic properties, for example commutativity of addition:

```scala
{v: Int with v == x + 1}     <: {v: Int with v == 1 + x}
```

<div class="fragment">

```scala
{v: Int with v == y + x}     <: {v: Int with v == x + y}
```

</div>

<div class="fragment">

Or grouping operands with the same constant factor in sums of products:

```scala
{v: Int with v == x + 3 * y} <: {v: Int with v == 2 * y + (x + y)}
```

</div>

## Subtyping: Equality reasoning

Transitivity of equality:

```scala
{v: Int with v == a && a == b} <: {v: Int with v == b}
```

<div class="fragment">

Congruence of equality:

```scala
{v: Int with a == b}           <: {v: Int with f(a) == f(b)}
```

</div>

<div class="fragment">

This is implemented using an E-Graph-like data structure.

</div>

## Subtyping: With other Scala types

Literal types are subtypes of singleton refinement types:

```scala
3 <: {v: Int with v == 3}
```

<div class="fragment">

We plan to support subtyping with other Scala types in the future.

</div>

## Backup: LH Usability Barriers

From [“Usability Barriers for Liquid Types”](https://dl.acm.org/doi/10.1145/3729327) [1]:

- 4.2 Unclear Divide between Haskell and LiquidHaskell:
  - <small>“comments are usually seen as just optional information in the code and not something that is directly used by the compiler”</small>
  - <small>“It's sort of like you're doing two things at once because you're implementing in Haskell. But you're also talking to GHC, but you're also talking to LiquidHaskell.”</small>
- 4.7 Unhelpful Error Messages
  - <small>“[...] error messages produced from typing errors inside the predicates, seemed indistinguishable from those produced by verification errors.”</small>
- 4.8 Limited IDE Support 
  - <small>“[user] tried to use the function <code>length</code>, but since it was not imported, it was impossible to use in this case.”</small>

<small>[1] Catarina Gamboa, Abigail Reese, Alcides Fonseca, and Jonathan Aldrich. 2025. Usability Barriers for Liquid Types. Proc. ACM Program. Lang. 9, PLDI, Article 224 (June 2025), 26 pages. <a href="https://dl.acm.org/doi/10.1145/3729327">doi:10.1145/3729327</a></small>


## Backup: `List.collect`

Scala type parameters are _erased_ at runtime, so we cannot match on a `List[T]`.

<div class="fragment">

However, we can use `.collect` to filter and convert a list:

```scala
type Pos = { v: Int with v >= 0 }

val xs = List(-1,2,-2,1)
xs.collect { case x: Pos => x } : List[Pos]
```

</div>

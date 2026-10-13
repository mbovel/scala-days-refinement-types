# First-class Logical Refinement Types for Scala

## Abstract

What if assertions like `x > 0` could be part of the type system? We present a prototype of *logical refinement types* for Scala 3: types like `{x: Int with x > 0}`. In our system, they are first-class: ordinary types that nest, serve as type arguments, and relate by subtyping. We present its design and expressiveness through examples.

## Description

This talk introduces *logical refinement types* for Scala 3 as a first-class language feature, backed by a working compiler prototype. A refinement type refines a base type `T` with a logical predicate `p(x)`, written `{x: T with p(x)}`: the set of values `x: T` for which `p(x)` holds. For example, `{x: Int with x > 0}` is the type of positive integers and `{x: List[Int] with x.nonEmpty}` the type of non-empty lists.

The idea appears in many languages under different names: [*refinement types* in Liquid Haskell](https://ucsd-progsys.github.io/liquidhaskell-tutorial/Tutorial_01_Introduction.html), [*boolean refinement types* in F\*](https://fstar-lang.org/tutorial/book/part1/part1_getting_off_the_ground.html#boolean-refinement-types), [*subset types* in Dafny](https://dafny.org/latest/DafnyRef/DafnyRef#sec-subset-types), and [*subtypes* in Lean](https://lean-lang.org/doc/reference/latest/Basic-Types/Subtypes/). We call them *logical refinement types*, or *refinement types* for short, to distinguish them from Scala's existing [*refined types*](https://scala-lang.org/files/archive/spec/3.4/03-types.html#refined-types), which add members to a type rather than constrain its values with a predicate.

In short, refinement types move assertions into the type system. For the compiler to accept `e: {x: T with p(x)}`, it must prove that `p(e)` holds for every `e: T`, so `assert(p(e))` can never fail at runtime. This buys three things:

1. **No runtime cost, total guarantees.** Predicates are checked at compile time, so there is no runtime overhead, and the property holds for *all* values rather than only those a test happens to exercise.
2. **Composability.** Refinement types are ordinary types, so they nest without ceremony: `{x: Int with x > 0} => String` is a function that rejects non-positive inputs, and `List[{x: Int with x > 0}]` a list of positive integers.
3. **Self-documenting APIs.** Pre- and post-conditions live in the signature instead of scattered `require`/`assert` calls, which also improves IDE support and documentation.

### Syntax and Predicate Language

`{x: T with p(x)}` is the *long form*. It introduces an explicit binder `x` for the value being refined, which is useful when no name is otherwise available, for example in type aliases or return types:

```scala
type Pos = {x: Int with x > 0}
def fill(n: Pos, v: Int):
  {res: List[Int] with res.size == n} = ???
```

When the value already has a name, as in a `val` or a parameter, the binder can be dropped. This *short form* reuses the existing name and desugars to the long form:

```scala
val x: Int with x > 0
// desugars to:
val x: {x: Int with x > 0}
```

Predicates also have to be something the compiler can reason about statically, so they must be *pure* and *terminating*. Concretely, the predicate language is a fragment of Scala: constants, stable identifiers, field selections over `val` fields, term and type applications, and constructors of case classes without initializers. Functions called from predicates are assumed pure and terminating; the prototype does not yet enforce this, so for now it is the programmer's responsibility.

### Selfification

Scala already has precise types it usually throws away: the literal `42` has singleton type `42`, but `val x = 42` widens to `Int` unless annotated as `val x: 42 = 42`. Refinement types follow the same rule: they are *not* inferred from terms by default. This keeps the types Scala assigns unchanged, preserving source compatibility and avoiding a proliferation of fine-grained types that could hurt performance.

Instead, the refinement type is written explicitly where it is wanted. When one is expected, the compiler tries to *selfify* the expression: it gives `e: T` the type `{x: T with x == e}`, lifting the expression into the type as long as it is a valid predicate.

```scala
val x: (Int with x == 42) = 42
val y: (Int with y == n + 2) = n + 2
```

### Runtime checks

When a property cannot be established statically, it can be recovered at runtime through pattern matching:

```scala
type ID = {s: String with s.matches(idRegex)}
"12e7-e89b-12d3" match
  case _: ID => // s matches idRegex
  case _     => // s does not match idRegex
```

When the program should simply fail if the predicate does not hold, the `runtimeChecked` method performs a dynamic check and throws otherwise.

As elsewhere in Scala, matching on erased type arguments is not supported: a `List[ID]` or an `ID => String` cannot be matched directly. For collections and similar cases, a custom [`TypeTest`](https://docs.scala-lang.org/scala3/reference/other-new-features/type-test.html) lifts the restriction.

### Subtyping

The interesting question is how to decide whether one refinement type is a subtype of another. By definition, `{x: T with p(x)}` is a subtype of `{y: S with q(y)}` when `T <: S` and the predicates are related by implication: `p(x)` must imply `q(x)` for all `x`. The system has to establish both conditions.

Rather than calling out to an external SMT solver, we built a lightweight solver into the compiler, tuned for the cases that come up in everyday code. It combines three techniques: *equality reasoning*, using the E-Graph data structure to compute congruence closure (from `v == a` and `a > 3` it derives `v > 3`); *normalization*, putting arithmetic into canonical form so that `x + 3 * y` and `2 * y + x + y` are seen as equal; and *inlining of known refinements and definitions*, so that a value of type `Int with y == x + 1` (or even a plain `val y: Int = x + 1`) contributes `y == x + 1` to the reasoning. Refinement types also interoperate with the rest of the type system. For instance, the literal type `1` is a subtype of `{x: Int with x == 1}`, and hence of `{x: Int with x > 0}`.

### Case studies

The heart of the talk is a tour of what refinement types can express. We present a few examples below.

**Pre- and post-conditions in a signature.** The simplest use is constraining inputs. A color channel is a byte-ranged integer, and stating that once removes a whole class of bounds checks downstream:

```scala
type ChannelValue = {v: Int with 0 <= v && v < 256}
def rgb(r: ChannelValue, g: ChannelValue, b: ChannelValue): Int
```

**Length-indexed collections.** Tracking a collection's size in its type turns length mismatches into compile errors. Here `concat` and `zip` carry exact length relations, and the compiler chains them to verify the final length on its own:

```scala
type Vec[T]

object Vec:
  def fill[T](n: Int, v: T): {r: Vec[T] with r.len == n} = ???

extension [T](a: Vec[T])
  def len: Int = ???
  def concat(b: Vec[T]): {r: Vec[T] with r.len == a.len + b.len} = ???
  def zip[S](b: Vec[S] with b.len == a.len): {r: Vec[(T, S)] with r.len == a.len} = ???

def example(n: Int, m: Int): {r: Vec[(String, Int)] with r.len == m + n} =
  val v1 = Vec.fill(n, 0)
  val v2 = Vec.fill(m, 1)
  val v3 = v1.concat(v2)          // len == n + m
  Vec.fill(m + n, "").zip(v3)     // checked: m + n == n + m
```

**Predicates as a dispatch key.** Because refinement types are real types, they participate in overload resolution. This for example allows us to ship a fast path that is only valid under a precondition, and let the type checker pick it when the precondition is known:

```scala
def min(l: List[Int] with l.isSorted): Int = l.head  // O(1)
def min(l: List[Int]): Int = l.min                   // O(n)

def example(l: List[Int] with l.isSorted): Int = min(l)  // resolves to the O(1) overload
```

**A verified memoized Fibonacci.** Finally, we scale up to a more verification-flavored example: a memo table whose values are tied to the keys by a predicate, used to prove that a memoized `fib` returns exactly the same results as the reference definition:

```scala
case class DepMap[K, V](p: (K, V) => Boolean):
  def put(n: K, v: V with p(n, v)): Unit = ???
  def get(n: K): Option[{res: V with p(n, res)}] = ???

def fib(n: Int): {r: Int with r == (if n <= 1 then 1 else fib(n - 1) + fib(n - 2))} =
  if n <= 1 then 1 else fib(n - 1) + fib(n - 2)

val cache: (DepMap[Int, Int] with cache == DepMap[Int, Int]((k, v) => v == fib(k))) =
  DepMap[Int, Int]((k, v) => v == fib(k))

def fibMemo(n: Int): {res: Int with res == fib(n)} =
  cache.get(n) match
    case Some(res) => res
    case None =>
      val res: (Int with res == fib(n)) =
        if n <= 1 then 1 else fibMemo(n - 1) + fibMemo(n - 2)
      cache.put(n, res)
      res
```

### Related Work

In the Scala ecosystem, libraries such as [Refined](https://github.com/fthomas/refined) and [Iron](https://github.com/Iltotore/iron) offer user-level encodings of refinement types using opaque type aliases and implicit evidence. The talk will compare our approach with these libraries, showing how integrating the checks directly into the compiler increases expressiveness, reduces boilerplate, and improves performance.

## Biography

Matt Bovel is a PhD student at EPFL, where he works on type systems and program verification across two labs: the LAMP, led by Martin Odersky, and the LARA, led by Viktor Kunčak.

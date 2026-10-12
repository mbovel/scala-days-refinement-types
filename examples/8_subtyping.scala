//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package subtyping

// Constant folding
def constantFolding(v: {v: Int with v == 1 + 1}): {v: Int with v == 2} = v

// Normalization
def normalization1(x: Int, v: {v: Int with v == x + 1}): {v: Int with v == 1 + x} = v
def normalization2(x: Int, y: Int, v: {v: Int with v == y + x}): {v: Int with v == x + y} = v
def normalization3(x: Int, y: Int, v: {v: Int with v == x + 3 * y}): {v: Int with v == 2 * y + (x + y)} = v

// Local unfolding
def localUnfolding(x: Int) =
  val y: Int = x + 1
  val v1: {v: Int with v == y} = ???
  val v2: {v: Int with v == x + 1} = v1
  val v3: {v: Int with v == y} = v2

// Equality reasoning
def f(x: Int): Int = x * 2
def transitivity(a: Int, b: Int, v: {v: Int with v == a && a == b}): {v: Int with v == b} = v
def congruence(a: Int, b: Int, v: {v: Int with a == b}): {v: Int with f(a) == f(b)} = v

// With other Scala types
def literal(x: 3): {v: Int with v == 3} = x

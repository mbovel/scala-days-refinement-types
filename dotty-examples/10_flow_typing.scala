//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package flowtyping

// The condition of an `if` is assumed in its branches.
// See also 11_merge_sort_bounds.scala.

def pos(x: Int): {v: Int with v > 0} =
  if x > 0 then x else 1

// def nonNeg(x: Int): {v: Int with v > 0} = if x >= 0 then x else 1 // error

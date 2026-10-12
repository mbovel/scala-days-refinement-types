//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package puritydefault

// By default, predicates are assumed to be pure: this impure one is accepted.

class Cell { var field: Int = 0 }
def test(c: Cell) =
  def getAndBump(): Int = { c.field += 1; c.field }
  val v: Int with v == getAndBump() = getAndBump()

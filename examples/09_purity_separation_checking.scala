//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package puritysepcheck

// With capture and separation checking, predicates must be pure.
// Uncomment the line marked `// error` to see the error message.

import language.experimental.{captureChecking, separationChecking}
class Cell extends caps.Stateful { var field: Int = 0 }
def test(c: Cell^) =
  def getAndBump(): Int = { c.field += 1; c.field }
  // val v: Int with v == getAndBump() = getAndBump() // error

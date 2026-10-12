//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package validpredicates

// Uncomment the lines marked `// error` to see the error messages.

def mutable =
  var x = 3
  // val y: Int with y == 3 = x // error: x is mutable

class Box(val value: Int)

def reference =
  // val b: Box with b == Box(3) = Box(3) // error: Box has equality by reference
  ()

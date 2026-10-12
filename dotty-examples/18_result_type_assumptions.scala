//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package resulttypes

// The solver uses the result types of called functions as assumptions.

def double(x: Int): {res: Int with res == x + x} = x + x
val d: {res: Int with res == 6} = double(3)

def inc(x: Int): {res: Int with res == x + 1} = x + 1
val v: {res: Int with res == 4} = inc(3)
val w: {res: Int with res == 5} = inc(inc(3))
// val wrong: {res: Int with res == 6} = inc(inc(3)) // error

//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package errormessages

// Uncomment the lines marked `// error` to see the error messages.

// def f[A](l: List[A] with l.notEmpty) = () // error

type PosInt = {v: Int with v > 0}

def g[T](f: T => Unit, x: T) = f(x)

def test =
  g((x: PosInt) => x * 2, 2)
  // g((x: PosInt) => x * 2, -2) // error

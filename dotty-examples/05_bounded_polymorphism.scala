//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package boundedpoly

trait Ord[T]:
  def compare(x: T, y: T): Int

def max[T: Ord, U <: T](x: U, y: U): U =
  if summon[Ord[T]].compare(x, y) >= 0 then x else y

def maximum[T: Ord, U <: T](xs: List[U]): U =
  xs.reduce(max)

given Ord[Int] = (x, y) => x.compare(y)

type Even = {v: Int with v % 2 == 0}

def ex2: Even =
  maximum(List(2, 4, 6))

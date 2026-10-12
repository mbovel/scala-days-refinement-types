//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes
//> using dep io.github.iltotore::iron:3.4.0

package ironexample

import io.github.iltotore.iron.*

extension (l: List[Int]) def isSorted(): Boolean =
  l.zip(l.drop(1)).forall((x, y) => x <= y)

final class IsSorted

given Constraint[List[Int], IsSorted] with
  override inline def test(inline l: List[Int]): Boolean = l.isSorted()

  override inline def message: String = "List must be sorted"

def min(l: List[Int] :| IsSorted): Int =
  l.head

@main def ex5 =
  val l: List[Int] :| IsSorted = List(1, 2, 3).refineUnsafe
  println(min(l))
  println(List(3, 1, 2).refineEither[IsSorted])

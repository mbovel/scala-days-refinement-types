//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package overloads

import scala.annotation.targetName

extension (l: List[Int]) def isSorted: Boolean =
  l.zip(l.drop(1)).forall((x, y) => x <= y)

@targetName("minSorted")
def min(l: List[Int] with l.isSorted) =
  l.head // O(1)

def min(l: List[Int]) =
  l.min // O(n)

def ex1(l: List[Int] with l.isSorted) =
  min(l) // calls the first overload

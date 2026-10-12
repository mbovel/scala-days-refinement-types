//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes
//> using jar ../.stainless/lib/stainless-library.jar

package stainlessmergesort

import language.experimental.qualifiedTypes.silent // checked by Stainless instead
import stainless.collection.{List, Cons, Nil, ListOps}
import stainless.lang.Bag
import stainless.annotation.ignore

// Same structure as examples/15_merge_sort_bounds.scala, with full
// specifications: the result is sorted, has the same size and the same
// elements (as a multiset) as the input.

type Pos = {x: BigInt with x >= 0}

// Stainless refutes the spec `res < x` of examples/15_merge_sort_bounds.scala
// with the counter-example x = 0, y = 2; hence the `|| x == 0`.
def safeDiv(x: Pos, y: Pos with y > 1): {res: Pos with res < x || x == 0} =
  x / y

extension (l: List[BigInt])
  def isSorted: Boolean = ListOps.isSorted(l)

  def bag: Bag[BigInt] = l match
    case Nil() => Bag.empty[BigInt]
    case Cons(h, t) => t.bag + h

  def divideAt(i: Pos with i <= l.size): {res: (List[BigInt], List[BigInt]) with
    res._1.size == i && res._2.size == l.size - i && res._1.bag ++ res._2.bag == l.bag
  } =
    if i > 0 && l.size > 0 then
      val (a, b) = l.tail.divideAt(i - 1)
      (Cons(l.head, a), b)
    else (Nil[BigInt](), l)

def merge(left: List[BigInt] with left.isSorted, right: List[BigInt] with right.isSorted):
  {res: List[BigInt] with
    res.isSorted && res.size == left.size + right.size && res.bag == left.bag ++ right.bag
  } =
  if left.size > 0 && right.size > 0 then
    if left.head < right.head then Cons(left.head, merge(left.tail, right))
    else Cons(right.head, merge(left, right.tail))
  else if left.size == 0 then right
  else left

def mergeSort(list: List[BigInt]): {res: List[BigInt] with
  res.isSorted && res.size == list.size && res.bag == list.bag
} =
  val len = list.size
  val middle = safeDiv(len, BigInt(2))
  if middle == 0 then list
  else
    val (left, right) = list.divideAt(middle)
    merge(mergeSort(left), mergeSort(right))

@ignore @main def example =
  println(mergeSort(List[BigInt](5, 3, 8, 1, 2, 7, 4, 6)))
  println(mergeSort(List[BigInt]()))

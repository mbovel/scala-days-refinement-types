//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes
//> using jar ../.stainless/lib/stainless-library.jar

package stainlessmergesort

import language.experimental.qualifiedTypes.silent // checked by Stainless instead
import stainless.lang.Bag
import stainless.annotation.ignore

type Pos = {x: BigInt with x >= 0}

// Stainless refutes the spec `res < x` of examples/15_merge_sort_bounds.scala
// with the counter-example x = 0, y = 2; hence the `|| x == 0`.
def safeDiv(x: Pos, y: Pos with y > 1): {res: Pos with res < x || x == 0} =
  x / y

// Stainless does not support scala.List or Seq: we use our own list.
enum IList:
  case Nil
  case Cons(head: Int, tail: IList)
import IList.*

extension (l: IList)
  def len: Pos = l match
    case Nil => BigInt(0)
    case Cons(_, t) => t.len + 1

  def content: Bag[Int] = l match // multiset of elements
    case Nil => Bag.empty[Int]
    case Cons(h, t) => t.content + h

  def isSorted: Boolean = l match
    case Cons(x, t @ Cons(y, _)) => x <= y && t.isSorted
    case _ => true

  def splitAt(i: Pos with i <= l.len): {res: (IList, IList) with
      res._1.len == i && res._2.len == l.len - i &&
      res._1.content ++ res._2.content == l.content} =
    l match
      case Cons(h, t) if i > 0 =>
        val (a, b) = t.splitAt(i - 1)
        (Cons(h, a), b)
      case _ => (Nil, l)

def merge(a: IList with a.isSorted, b: IList with b.isSorted): {res: IList with
  res.isSorted && res.len == a.len + b.len && res.content == a.content ++ b.content
} =
  (a, b) match
    case (Cons(x, xs), Cons(y, ys)) =>
      if x <= y then Cons(x, merge(xs, b)) else Cons(y, merge(a, ys))
    case (Nil, _) => b
    case (_, Nil) => a

def mergeSort(l: IList): {res: IList with
  res.isSorted && res.len == l.len && res.content == l.content
} =
  val middle = safeDiv(l.len, BigInt(2))
  if middle == 0 then l
  else
    val (left, right) = l.splitAt(middle)
    merge(mergeSort(left), mergeSort(right))

@ignore @main def example =
  println(mergeSort(Cons(5, Cons(3, Cons(8, Cons(1, Cons(2, Nil)))))))
  println(mergeSort(Nil))

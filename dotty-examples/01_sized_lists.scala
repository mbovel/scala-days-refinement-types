//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package sizedlists

def zip[A, B](
  xs: List[A],
  ys: List[B] with ys.size == xs.size
): {zs: List[(A, B)] with zs.size == xs.size} = ???

def concat[T](
  xs: List[T],
  ys: List[T]
): {zs: List[T] with zs.size == xs.size + ys.size} = ???

def ex1 =
  val xs: List[Int] = ???
  val ys: List[Int] = ???
  zip(concat(xs, ys), concat(ys, xs)) // ✅
  // zip(concat(xs, ys), concat(xs, xs)) // error

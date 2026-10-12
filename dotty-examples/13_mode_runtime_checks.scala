//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package moderuntimechecks

import language.experimental.qualifiedTypes.runtimeChecks

def getInt(): Int = 1

@main def example =
  val x: {v: Int with v == 1} = getInt() // checked at run time: passes
  println(x)
  val y: {v: Int with v == 2} = getInt() // checked at run time: throws IllegalArgumentException
  println(y)

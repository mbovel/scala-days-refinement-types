//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package modesilent

import language.experimental.qualifiedTypes.silent

def getInt(): Int = 1

@main def ex12 =
  val x: {v: Int with v == 1} = getInt() // accepted silently
  println(x)
  val y: {v: Int with v == 2} = getInt() // accepted silently, no run-time check: y is 1!
  println(y)

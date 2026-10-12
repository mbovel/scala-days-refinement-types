//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package stainlesssum

import language.experimental.qualifiedTypes.silent // checked by Stainless instead

// Needs induction and non-linear arithmetic: rejected by the compiler alone,
// verified by Stainless.
def sum(n: BigInt with n >= 0): {r: BigInt with 2 * r == n * (n + 1)} =
  if n == 0 then BigInt(0) else n + sum(n - 1)

// Without `x > Int.MinValue`, Stainless finds the counter-example
// x = -2147483648, for which -x overflows and is negative.
def abs(x: Int with x > Int.MinValue): {r: Int with r >= 0} =
  if x >= 0 then x else -x

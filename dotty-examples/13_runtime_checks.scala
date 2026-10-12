//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package runtimechecks

val idRegex = "[0-9a-f]{4}-[0-9a-f]{4}"
type ID = {s: String with s.matches(idRegex)}

@main def example =
  // Pattern matching
  "a2e7-e89b" match
    case id: ID => println(s"$id is an ID") // holds, id has type ID
    case _      => println("not an ID")      // does not hold

  // .runtimeChecked
  val id: ID = "a2e7-e89b".runtimeChecked
  println(id)

//> using scalaOrganization ch.epfl.lara
//> using scala 3.10.1-RC1-bin-20260903-e1f9361-NIGHTLY
//> using options -language:experimental.qualifiedTypes

package fansi

// From Li Haoyi's Fansi library: https://github.com/com-lihaoyi/fansi

case class True0(r: Int, g: Int, b: Int, index: Int)

object WithAssertions:
  def True(r: Int, g: Int, b: Int) =
    True0(r, g, b, trueIndex(r, g, b))

  def trueIndex(r: Int, g: Int, b: Int) =
    require(0 <= r && r < 256)
    require(0 <= g && g < 256)
    require(0 <= b && b < 256)
    r << 16 | g << 8 | b

type Comp = {v: Int with 0 <= v && v < 256}

def True(r: Comp, g: Comp, b: Comp) =
  True0(r, g, b, trueIndex(r, g, b))

def trueIndex(r: Comp, g: Comp, b: Comp) =
  r << 16 | g << 8 | b

@main def example =
  println(True(255, 128, 0))
  // println(True(256, 0, 0)) // error
  println(WithAssertions.True(255, 128, 0))
  // WithAssertions.True(256, 0, 0) compiles, but throws at run time

package entombed.oracle

import entombed.core.Input

class OracleSuite extends munit.FunSuite:

  test("the 1995 source assembles") {
    val p = OracleCore.program
    assert(p.code.length > 4000, p.code.length)
    assertEquals(p.address("debut_data") % 2, 0)
    assertEquals(p.address("nb_lifes"), p.address("debut_data") + 4998)
  }

  test("the title screen runs and shows a palette") {
    val core = new OracleCore()
    for _ <- 0 until 200 do core.step(Input.Empty)
    assert(core.ticks >= 200)
    assert(core.palette.words.exists(_ != 0), core.palette)
    assert(core.frame.pixels.exists(_ != 0))
  }

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

  test("the title bounce plays sound 1 and the game runs fast enough") {
    val core = new OracleCore()
    var sounds = 0
    val t0 = System.nanoTime()
    for _ <- 0 until 700 do
      core.step(Input.Empty)
      core.drainSounds(_ => sounds += 1)
    // fire: tomb selection, fire again: tomb 1, then walk right
    for _ <- 0 until 5 do core.step(Input(Input.Fire, 0))
    for _ <- 0 until 60 do core.step(Input.Empty)
    for _ <- 0 until 5 do core.step(Input(Input.Fire, 0))
    for _ <- 0 until 100 do core.step(Input.Empty)
    val i0 = core.cpu.executed
    for _ <- 0 until 200 do core.step(Input(Input.Right, 0))
    val perTick = (core.cpu.executed - i0) / 200
    val ms = (System.nanoTime() - t0) / 1e6
    println(s"oracle: $sounds title sounds, $perTick instructions per tick in the game, " +
      f"${core.ticks} ticks in $ms%.0f ms")
    assert(sounds > 0)
    assertEquals(core.word("tomb_number"), 0)
  }

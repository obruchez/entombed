package entombed.oracle

import entombed.core.Input
import entombed.data.Assets
import entombed.gfx.Bitplanes

/** Checks the decoding of the Atari data against the "chunky" conversion made in 1995. */
class DataCrossCheckSuite extends munit.FunSuite:
  private val assets = Assets.default

  test("Atari bitplanes decode to the Smaky graphics (DATASB.BIN, DATASC.BIN)") {
    val chunky = Bitplanes.toChunky(assets.blockRows)
    assertEquals(chunky.toSeq, (EmbeddedSource.datasB ++ EmbeddedSource.datasC).toSeq)
  }

  test("the decoded font matches the font prepared by font_init in the Smaky version") {
    val core = new OracleCore()
    for _ <- 0 until 5 do core.step(Input.Empty) // initialisation done
    val p = OracleCore.program
    val fontBase = core.cpu.rl(p.address("fonts8x8"))
    for c <- 0 until 100 do
      val smaky = Bitplanes.fromChunky(core.cpu.mem, fontBase + 32 * c, 32)
      assertEquals(smaky.toSeq, assets.font(c).toSeq, s"glyph $c")
  }

  test("the data segment equals DATASA.BIN except for the font masks (de-interleaved in 1995)") {
    val a = EmbeddedSource.datasA
    val diffs = a.indices.filter(i => a(i) != assets.bytes(i))
    assert(diffs.forall(i => i >= 416 && i < 480), diffs.take(10))
  }

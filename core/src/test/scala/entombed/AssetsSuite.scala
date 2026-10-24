package entombed

import entombed.data.Assets

class AssetsSuite extends munit.FunSuite:
  private val assets = Assets.default

  test("palette 2 is the game palette") {
    assertEquals(
      assets.palettes(2).words.toSeq,
      Seq(0x000, 0x110, 0x332, 0x554, 0x333, 0x555, 0x740, 0x772, 0x043, 0x653, 0x322, 0x544,
        0x320, 0x431, 0x542, 0x777)
    )
  }

  test("20 sounds, each ending with a Dosound end marker") {
    assertEquals(assets.sounds.length, 20)
    for (s, i) <- assets.sounds.zipWithIndex do
      val seq = s.sequence.map(_ & 0xff)
      val end = seq.indices.sliding(2).collectFirst {
        case Seq(a, b) if seq(a) == 0x82 && seq(b) == 0 && a % 2 == 0 => a
      }
      assert(end.isDefined, s"sound $i has no end marker")
  }

  test("every sound sets the 14 registers before its first wait") {
    for (s, i) <- assets.sounds.zipWithIndex do
      val seq = s.sequence.map(_ & 0xff)
      val regs = (0 until 28 by 2).map(seq(_))
      assertEquals(regs, (0 until 14).toIndexedSeq, s"sound $i")
  }

  test("tombs are where the 1995 notes say") {
    assertEquals((0 until 5).map(assets.tombOffset), IndexedSeq(5304, 8086, 11824, 16268, 21488))
    // First tomb: the man starts at (416, 152)
    assertEquals(assets.u16(5304), 416)
    assertEquals(assets.u16(5306), 152)
  }

  test("font: 100 glyphs, space is empty, 'A' is not") {
    assertEquals(assets.font.length, 100)
    assert(assets.font(0).forall(_ == 0))
    assert(assets.font('A' - 32).exists(_ != 0))
  }

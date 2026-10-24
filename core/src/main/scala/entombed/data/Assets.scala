package entombed.data

import entombed.gfx.{Bitplanes, Palette}

/** A sound of the game: an XBIOS `Dosound` sequence with the priority and the minimum duration (in
  * game frames) used by the game to decide whether another sound may interrupt it.
  */
final case class Sound(priority: Int, pause: Int, sequence: Array[Byte])

/** The original data segment of the game (`DATAS.BIN`, 96884 bytes, Atari format), decoded.
  *
  * Layout, from the 1995 study notes (`reference/notes-1995/ENTOMBED-notes.md`):
  *
  * | Offset     | Content                                                                        |
  * |:-----------|:-------------------------------------------------------------------------------|
  * | 14         | 4 palettes of 16 colours                                                       |
  * | 1492       | 17 sounds of 42 bytes (priority, pause, `Dosound` sequence), then sounds 17-19 |
  * | 2296-5196  | texts (end of game, best scores, instructions, tomb selection)                 |
  * | 4768       | addresses of the 5 tombs (relative to the start of the program)                |
  * | 5196-31348 | tomb descriptions and tomb data                                                |
  * | 31412      | 113 blocks of 32x16 (background)                                               |
  * | 60340      | other graphics (32 pixels wide, addressed by byte offset from here)            |
  * | 92980      | font: 100 characters of 8x8                                                    |
  */
final class Assets(val bytes: Array[Byte]):
  require(bytes.length == Assets.Size, s"DATAS.BIN must be ${Assets.Size} bytes")

  def u8(offset: Int): Int = bytes(offset) & 0xff
  def u16(offset: Int): Int = (u8(offset) << 8) | u8(offset + 1)
  def s16(offset: Int): Int = u16(offset).toShort.toInt
  def u32(offset: Int): Int = (u16(offset) << 16) | u16(offset + 2)

  /** The 4 palettes. Palette 2 is the game palette; palette 0 receives the faded versions. */
  val palettes: IndexedSeq[Palette] = (0 until 4).map(i => Palette.read(bytes, 14 + 32 * i))

  /** Sounds 0-16 have a header; 17-19 are the parts of the "tomb completed" tune. */
  val sounds: IndexedSeq[Sound] =
    val withHeader = (0 to 16).map { i =>
      val o = Assets.SoundsOffset + 42 * i
      Sound(u16(o), u16(o + 2), bytes.slice(o + 4, o + 42))
    }
    val tune = Assets.TuneOffsets.map(o => Sound(0, 0, bytes.slice(o, o + 30)))
    withHeader ++ tune

  /** Pixels of the background blocks and of the other graphics, as rows of 32 pixels. */
  val blockRows: Array[Byte] =
    Bitplanes.decode(
      bytes,
      Assets.Blocks32x16Offset,
      2,
      (Assets.FontOffset - Assets.Blocks32x16Offset) / 16
    )

  /** Index of the first row of background block `n` (0-112) in [[blockRows]]. */
  def blockRow(n: Int): Int = n * 16

  /** Index in [[blockRows]] of the graphics at `byteOffset` from the "other graphics" pointer. */
  def otherRow(byteOffset: Int): Int =
    (Assets.OthersOffset - Assets.Blocks32x16Offset + byteOffset) / 16

  /** The 100 characters of the font (8x8 pixels each), after the masking done by `font_init`. */
  val font: Array[Array[Byte]] =
    val glyphs = new Array[Array[Byte]](100)
    val maskTable = Assets.FontMaskOffset
    for pair <- 0 until 50 do
      val src = Assets.FontOffset + 64 * pair
      val words = new Array[Byte](64)
      for w <- 0 until 32 do
        var v = u16(src + 2 * w)
        if 49 - pair >= 4 then v &= u16(maskTable + 2 * w)
        words(2 * w) = (v >> 8).toByte
        words(2 * w + 1) = v.toByte
      val px = Bitplanes.decode(words, 0, 1, 8) // 16x8: two characters side by side
      glyphs(2 * pair) = Array.tabulate(64)(i => px((i / 8) * 16 + i % 8))
      glyphs(2 * pair + 1) = Array.tabulate(64)(i => px((i / 8) * 16 + 8 + i % 8))
    glyphs

  /** Offset of the data of tomb `n` (0-4): the program stores addresses relative to its start,
    * which is 22994 bytes before the data segment.
    */
  def tombOffset(n: Int): Int = u32(Assets.TombsOffset + 4 * n) - Assets.CodeSize

object Assets:
  val Size = 96884
  val CodeSize = 22994
  val SoundsOffset = 1492
  val TuneOffsets: IndexedSeq[Int] = IndexedSeq(2206, 2236, 2266)
  val FontMaskOffset = 416
  val TombsOffset = 4768
  val DataSegmentSize = 31412
  val Blocks32x16Offset = 31412
  val OthersOffset = 60340
  val FontOffset = 92980

  lazy val default: Assets = new Assets(EmbeddedData.datasBin)

package entombed.game

/** Memory layout of the game, identical to the oracle's (the 68000 interpreter running the 1995
  * source), so that both memories can be compared byte for byte and that pointers stored in memory
  * by the game (tomb cells, linked lists of objects...) have the same values.
  *
  * The original game keeps all its state in its data segment (31412 bytes followed by the graphics)
  * and in its BSS (214102 bytes), plus two screens. The port does the same.
  */
object Layout:
  /** Start of the data segment (`debut_data`). */
  val Data = 0x2090

  /** The 113 background blocks of 32x16 (chunky 4 bpp). */
  val Blocks32x16 = 0x9b44

  /** The other graphics, 32 pixels wide. */
  val Others = 0x10c44

  /** The font: 100 characters of 8x8, 32 bytes each (chunky). */
  val Font = 0x18bc4

  /** Start of the BSS (`debut_bss`), 214102 bytes. */
  val Bss = 0x19c00

  /** Second screen buffer of the Smaky version (`other_scr`). */
  val OtherScreen = 0x4e056

  /** Variables added in 1995 after the BSS. */
  val SoundNumber = 0x56056
  val LoadFlag = 0x56058
  val SaveFlag = 0x5605a
  val End = 0x5605c
  val Size = 0x80000

  val DataSize = 31412
  val BssSize = 214102

/** Big-endian byte-addressed memory, as seen by the 68000. */
final class Ram(val bytes: Array[Byte]):
  def this() = this(new Array[Byte](Layout.Size))

  inline def b(a: Int): Int = bytes(a) & 0xff
  inline def sb(a: Int): Int = bytes(a).toInt
  inline def w(a: Int): Int = ((bytes(a) & 0xff) << 8) | (bytes(a + 1) & 0xff)
  inline def sw(a: Int): Int = w(a).toShort.toInt
  inline def l(a: Int): Int =
    ((bytes(a) & 0xff) << 24) | ((bytes(a + 1) & 0xff) << 16) | ((bytes(a + 2) & 0xff) << 8) |
      (bytes(a + 3) & 0xff)

  inline def setB(a: Int, v: Int): Unit = bytes(a) = v.toByte
  inline def setW(a: Int, v: Int): Unit =
    bytes(a) = (v >> 8).toByte
    bytes(a + 1) = v.toByte
  inline def setL(a: Int, v: Int): Unit =
    bytes(a) = (v >> 24).toByte
    bytes(a + 1) = (v >> 16).toByte
    bytes(a + 2) = (v >> 8).toByte
    bytes(a + 3) = v.toByte

  /** Pointers are 24-bit addresses on the 68000. */
  inline def ptr(a: Int): Int = l(a) & 0xffffff

  def clear(from: Int, length: Int): Unit =
    java.util.Arrays.fill(bytes, from, from + length, 0.toByte)

/** 68000 arithmetic helpers: 16-bit and 8-bit wrap-around, sign extension. */
object M68:
  inline def w(v: Int): Int = v & 0xffff
  inline def sw(v: Int): Int = v.toShort.toInt
  inline def b(v: Int): Int = v & 0xff
  inline def sb(v: Int): Int = v.toByte.toInt

  /** The N flag of a 16-bit result. */
  inline def neg16(v: Int): Boolean = (v & 0x8000) != 0

  /** `CMP.W a,b` followed by `BCS`: b < a, unsigned. */
  inline def lowerU16(b: Int, a: Int): Boolean = (b & 0xffff) < (a & 0xffff)
  inline def lowerU8(b: Int, a: Int): Boolean = (b & 0xff) < (a & 0xff)

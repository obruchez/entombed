package entombed.gfx

/** A 320x200 screen of 4-bit colour indices (one byte per pixel). The palette is applied by the
  * host when the frame is presented.
  */
final class FrameBuffer:
  val pixels: Array[Byte] = new Array[Byte](FrameBuffer.Width * FrameBuffer.Height)

  def clear(): Unit = java.util.Arrays.fill(pixels, 0.toByte)

  def copyFrom(other: FrameBuffer): Unit =
    System.arraycopy(other.pixels, 0, pixels, 0, pixels.length)

  inline def apply(x: Int, y: Int): Int = pixels(y * FrameBuffer.Width + x)

  inline def update(x: Int, y: Int, c: Int): Unit =
    pixels(y * FrameBuffer.Width + x) = c.toByte

  /** Converts the frame to 0xAARRGGBB pixels. */
  def toArgb(palette: Palette, out: Array[Int]): Unit =
    val lut = palette.argb
    var i = 0
    while i < pixels.length do
      out(i) = lut(pixels(i) & 0xf)
      i += 1

  /** A 32-bit FNV-1a hash of the pixels, for tests. */
  def hash: Int =
    var h = 0x811c9dc5
    var i = 0
    while i < pixels.length do
      h = (h ^ (pixels(i) & 0xff)) * 0x01000193
      i += 1
    h

object FrameBuffer:
  val Width = 320
  val Height = 200

/** A 16-colour Atari ST palette: words in the `xxxxxRRRxGGGxBBB` format (3 bits per channel). */
final class Palette(val words: Array[Int]):
  require(words.length == 16)

  /** Colours as 0xFFRRGGBB. A 3-bit level v becomes v * 255 / 7. */
  val argb: Array[Int] = words.map(Palette.toArgb)

  override def equals(other: Any): Boolean = other match
    case p: Palette => java.util.Arrays.equals(words, p.words)
    case _          => false

  override def hashCode: Int = java.util.Arrays.hashCode(words)

  override def toString: String = words.map(w => f"$w%03x").mkString("Palette(", " ", ")")

object Palette:
  val Black: Palette = new Palette(new Array[Int](16))

  def toArgb(word: Int): Int =
    def expand(v: Int) = (v & 7) * 255 / 7
    0xff000000 | (expand(word >> 8) << 16) | (expand(word >> 4) << 8) | expand(word)

  /** Reads 16 big-endian words. */
  def read(bytes: Array[Byte], offset: Int): Palette =
    new Palette(Array.tabulate(16)(i => ((bytes(offset + 2 * i) & 0xff) << 8) | (bytes(offset + 2 * i + 1) & 0xff)))

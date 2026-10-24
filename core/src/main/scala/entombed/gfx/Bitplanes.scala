package entombed.gfx

/** Conversions between the Atari ST low-resolution formats and pixel indices.
  *
  *   - Interleaved bitplanes: each group of 16 pixels is stored as 4 big-endian words, one per
  *     plane (plane 0 is the least significant bit of the colour index). A 32-pixel-wide block row
  *     is 16 bytes (2 groups).
  *   - "Chunky" 4 bpp, as used by the 1995 Smaky port: 2 pixels per byte, high nibble first.
  */
object Bitplanes:

  /** Decodes `rows` rows of `groups` interleaved 16-pixel groups into colour indices. */
  def decode(bytes: Array[Byte], offset: Int, groups: Int, rows: Int): Array[Byte] =
    val width = groups * 16
    val out = new Array[Byte](width * rows)
    var p = offset
    var r = 0
    while r < rows do
      var g = 0
      while g < groups do
        val w0 = word(bytes, p)
        val w1 = word(bytes, p + 2)
        val w2 = word(bytes, p + 4)
        val w3 = word(bytes, p + 6)
        p += 8
        var x = 0
        while x < 16 do
          val b = 15 - x
          val c = ((w0 >> b) & 1) | (((w1 >> b) & 1) << 1) | (((w2 >> b) & 1) << 2) |
            (((w3 >> b) & 1) << 3)
          out(r * width + g * 16 + x) = c.toByte
          x += 1
        g += 1
      r += 1
    out

  /** Converts pixel indices to chunky 4 bpp (2 pixels per byte, high nibble first). */
  def toChunky(pixels: Array[Byte]): Array[Byte] =
    Array.tabulate(pixels.length / 2)(i =>
      ((pixels(2 * i) << 4) | (pixels(2 * i + 1) & 0xf)).toByte
    )

  /** Converts chunky 4 bpp bytes to pixel indices. */
  def fromChunky(bytes: Array[Byte], offset: Int, length: Int): Array[Byte] =
    val out = new Array[Byte](length * 2)
    var i = 0
    while i < length do
      val b = bytes(offset + i)
      out(2 * i) = ((b >> 4) & 0xf).toByte
      out(2 * i + 1) = (b & 0xf).toByte
      i += 1
    out

  private inline def word(bytes: Array[Byte], p: Int): Int =
    ((bytes(p) & 0xff) << 8) | (bytes(p + 1) & 0xff)

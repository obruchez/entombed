package entombed.gfx

/** Display of 32-pixel-wide blocks, with the behaviour of the original Atari routines
  * (`aff_bloc32x24_main` and its variants in `reference/atari/CHANGED3.L`).
  *
  * Graphics and screens are stored as "chunky" 4 bpp in a byte array, as in the 1995 Smaky port:
  * a block row is 16 bytes, a screen row 160 bytes, the high nibble is the left pixel.
  *
  * Atari semantics, reproduced exactly:
  *   - the block is placed at pixel precision (the Smaky routines rounded to even coordinates);
  *   - it is not drawn at all if `x + 31 < 0`, `x >= 320`, `y + h - 1 < 0` or `y >= 192`,
  *     in which case the routine returns false;
  *   - otherwise the visible part, clipped to 320x192 (the information line below is never
  *     touched), is drawn and the routine returns true.
  */
object Blitter:
  /** Colour 0 is transparent. */
  inline val Transparent = 0

  /** All pixels are copied. */
  inline val Opaque = 1

  /** Non-transparent pixels are drawn in colour 15 (enemy hit by an arrow). */
  inline val White = 2

  val ScreenWidth = 320
  val PlayfieldHeight = 192
  val BytesPerScreenRow = 160
  val BytesPerBlockRow = 16

  /** Coordinates are 16-bit values as on the 68000 (`x` and `y` are sign-extended here). */
  def draw(mem: Array[Byte], src: Int, height: Int, x0: Int, y0: Int, screen: Int,
      mode: Int): Boolean =
    val x = x0.toShort.toInt
    val y = y0.toShort.toInt
    val last = (y + height - 1).toShort.toInt
    if (x + 31).toShort < 0 || ((x + 31) & 0xffff) >= 351 then false
    else if last < 0 || (last & 0xffff) >= (PlayfieldHeight + height - 1) then false
    else
      var row = if y < 0 then -y else 0
      while row < height && y + row < PlayfieldHeight do
        val sy = y + row
        val srcRow = src + row * BytesPerBlockRow
        val dstRow = screen + sy * BytesPerScreenRow
        var c = 0
        while c < 32 do
          val sx = x + c
          if sx >= 0 && sx < ScreenWidth then
            val b = mem(srcRow + (c >> 1))
            val p = if (c & 1) == 0 then (b >> 4) & 0xf else b & 0xf
            if mode == Opaque || p != 0 then
              val v = if mode == White then 15 else p
              val a = dstRow + (sx >> 1)
              val old = mem(a)
              mem(a) =
                if (sx & 1) == 0 then ((old & 0x0f) | (v << 4)).toByte
                else ((old & 0xf0) | v).toByte
          c += 1
        row += 1
      true

  /** Converts a chunky screen (32000 bytes) into a frame buffer. */
  def present(mem: Array[Byte], screen: Int, frame: FrameBuffer): Unit =
    val px = frame.pixels
    var i = 0
    while i < 32000 do
      val b = mem(screen + i)
      px(2 * i) = ((b >> 4) & 0xf).toByte
      px(2 * i + 1) = (b & 0xf).toByte
      i += 1

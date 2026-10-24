package entombed.audio

/** Renders the game's sound at the host sample rate.
  *
  * The 50 Hz `Dosound` clock is derived from the sample clock (882 samples per tick at 44.1 kHz,
  * 960 at 48 kHz), exactly like TOS runs `Dosound` from the vertical blank interrupt. A sequence
  * requested with `play` starts at the next tick boundary, like the real XBIOS call. When several
  * sequences are requested during the same tick, the last one wins, as on the hardware.
  *
  * Not thread-safe: hosts that render on a separate thread must hand requests over themselves.
  */
final class AudioEngine(val sampleRate: Int, val volume: Double = 0.8):
  val ym = new Ym2149(sampleRate)
  private val dosound = new Dosound(ym)
  private var pending: Array[Byte] = null
  private var samplesUntilTick = 0
  private var tickAccumulator = 0

  /** Requests a `Dosound` sequence. */
  def play(sequence: Array[Byte]): Unit = pending = sequence

  /** Silences the chip immediately (mixer all off, volumes 0). */
  def silence(): Unit =
    pending = null
    dosound.start(Array(7, 0x3f, 8, 0, 9, 0, 10, 0, 0x82, 0).map(_.toByte))

  private def nextTickLength(): Int =
    tickAccumulator += sampleRate
    val n = tickAccumulator / 50
    tickAccumulator -= n * 50
    n

  /** Writes `count` samples in [-1, 1] into `out`, starting at `offset`. */
  def render(out: Array[Float], offset: Int, count: Int): Unit =
    var i = offset
    val end = offset + count
    while i < end do
      if samplesUntilTick == 0 then
        if pending != null then
          dosound.start(pending)
          pending = null
        dosound.tick()
        samplesUntilTick = nextTickLength()
      val n = math.min(end - i, samplesUntilTick)
      var k = 0
      while k < n do
        ym.process()
        ym.removeDc()
        out(i) = (ym.output * volume).toFloat
        i += 1
        k += 1
      samplesUntilTick -= n

package entombed.audio

/** YM2149 sound chip emulator, as found in the Atari ST (clocked at 2 MHz).
  *
  * This is a mono Scala port of "ayumi" by Peter Sovietov (https://github.com/true-grue/ayumi, MIT
  * licence, see NOTICE.md). The chip is simulated at clock/8, the output is band-limited with a
  * cubic interpolator and a 192-tap FIR decimation filter, then a DC filter is applied. Only
  * arithmetic operations are used, so the output is bit-identical on the JVM and in JavaScript.
  *
  * On top of the ayumi API, `writeRegister` exposes the chip's 14 registers as the Atari's XBIOS
  * `Dosound` sees them.
  */
final class Ym2149(sampleRate: Int, clockRate: Double = 2_000_000.0):
  import Ym2149.*

  // Tone channels
  private val tonePeriod = Array.fill(ToneChannels)(1)
  private val toneCounter = new Array[Int](ToneChannels)
  private val tone = new Array[Int](ToneChannels)
  private val tOff = new Array[Int](ToneChannels)
  private val nOff = new Array[Int](ToneChannels)
  private val eOn = new Array[Boolean](ToneChannels)
  private val volume = new Array[Int](ToneChannels)

  private var noisePeriod = 1
  private var noiseCounter = 0
  private var noise = 1
  private var envelopeCounter = 0
  private var envelopePeriod = 1
  private var envelopeShape = 0
  private var envelopeSegment = 0
  private var envelope = 0

  private val step: Double = clockRate / (sampleRate * 8 * DecimateFactor)
  require(step < 1, s"Sample rate $sampleRate too low for clock $clockRate")
  private var x = 0.0

  private val interpC = new Array[Double](4)
  private val interpY = new Array[Double](4)
  private val fir = new Array[Double](FirSize * 2)
  private var firIndex = 0

  private val dcDelay = new Array[Double](DcFilterSize)
  private var dcSum = 0.0
  private var dcIndex = 0

  private var mixed = 0.0

  /** Last output sample, roughly in [-1, 1]. */
  var output: Double = 0.0

  private val registers = new Array[Int](16)

  /** Optional observer of register writes (tests, debugging). */
  var onWrite: (Int, Int) => Unit = null

  // ----- ayumi API -----

  def setTone(index: Int, period: Int): Unit =
    val p = period & 0xfff
    tonePeriod(index) = if p == 0 then 1 else p

  def setNoise(period: Int): Unit =
    val p = period & 0x1f
    noisePeriod = if p == 0 then 1 else p

  def setMixer(index: Int, toneOff: Int, noiseOff: Int, envelopeOn: Boolean): Unit =
    tOff(index) = toneOff & 1
    nOff(index) = noiseOff & 1
    eOn(index) = envelopeOn

  def setVolume(index: Int, v: Int): Unit = volume(index) = v & 0xf

  def setEnvelope(period: Int): Unit =
    val p = period & 0xffff
    envelopePeriod = if p == 0 then 1 else p

  def setEnvelopeShape(shape: Int): Unit =
    envelopeShape = shape & 0xf
    envelopeCounter = 0
    envelopeSegment = 0
    resetSegment()

  // ----- Register interface -----

  /** Writes one of the 14 sound registers (0-13), with the same effect as on the real chip. */
  def writeRegister(r: Int, value: Int): Unit =
    val v = value & 0xff
    if onWrite != null then onWrite(r, v)
    if r >= 0 && r < 16 then registers(r) = v
    r match
      case 0 | 1 => setTone(0, registers(0) | ((registers(1) & 0xf) << 8))
      case 2 | 3 => setTone(1, registers(2) | ((registers(3) & 0xf) << 8))
      case 4 | 5 => setTone(2, registers(4) | ((registers(5) & 0xf) << 8))
      case 6     => setNoise(v)
      case 7     =>
        for ch <- 0 until ToneChannels do setMixer(ch, (v >> ch) & 1, (v >> (3 + ch)) & 1, eOn(ch))
      case 8 | 9 | 10 =>
        val ch = r - 8
        setVolume(ch, v)
        eOn(ch) = (v & 0x10) != 0
      case 11 | 12 => setEnvelope(registers(11) | (registers(12) << 8))
      case 13      => setEnvelopeShape(v)
      case _       => ()

  def readRegister(r: Int): Int = registers(r & 0xf)

  // ----- Simulation -----

  private def resetSegment(): Unit =
    val f = Envelopes(envelopeShape)(envelopeSegment)
    envelope = if f == SlideDown || f == HoldTop then 31 else 0

  private def updateEnvelope(): Int =
    envelopeCounter += 1
    if envelopeCounter >= envelopePeriod then
      envelopeCounter = 0
      Envelopes(envelopeShape)(envelopeSegment) match
        case SlideUp =>
          envelope += 1
          if envelope > 31 then
            envelopeSegment ^= 1
            resetSegment()
        case SlideDown =>
          envelope -= 1
          if envelope < 0 then
            envelopeSegment ^= 1
            resetSegment()
        case _ => ()
    envelope

  private def updateNoise(): Int =
    noiseCounter += 1
    if noiseCounter >= (noisePeriod << 1) then
      noiseCounter = 0
      val bit0x3 = (noise ^ (noise >> 3)) & 1
      noise = (noise >> 1) | (bit0x3 << 16)
    noise & 1

  private def updateMixer(): Unit =
    val n = updateNoise()
    val e = updateEnvelope()
    var sum = 0.0
    var i = 0
    while i < ToneChannels do
      toneCounter(i) += 1
      if toneCounter(i) >= tonePeriod(i) then
        toneCounter(i) = 0
        tone(i) ^= 1
      var out = (tone(i) | tOff(i)) & (n | nOff(i))
      out *= (if eOn(i) then e else volume(i) * 2 + 1)
      sum += YmDacTable(out)
      i += 1
    mixed = sum / ToneChannels

  /** Computes the next output sample into `output`. */
  def process(): Unit =
    val base = FirSize - firIndex * DecimateFactor
    firIndex = (firIndex + 1) % (FirSize / DecimateFactor - 1)
    var i = DecimateFactor - 1
    while i >= 0 do
      x += step
      if x >= 1 then
        x -= 1
        interpY(0) = interpY(1)
        interpY(1) = interpY(2)
        interpY(2) = interpY(3)
        updateMixer()
        interpY(3) = mixed
        val y1 = interpY(2) - interpY(0)
        interpC(0) = 0.5 * interpY(1) + 0.25 * (interpY(0) + interpY(2))
        interpC(1) = 0.5 * y1
        interpC(2) = 0.25 * (interpY(3) - interpY(1) - y1)
      fir(base + i) = (interpC(2) * x + interpC(1)) * x + interpC(0)
      i -= 1
    output = decimate(base)

  private def decimate(base: Int): Double =
    var y = 0.125 * fir(base + 96)
    var k = 1
    while k < 96 do
      val c = FirCoefficients(k)
      if c != 0.0 then y += c * (fir(base + k) + fir(base + 192 - k))
      k += 1
    System.arraycopy(fir, base, fir, base + FirSize - DecimateFactor, DecimateFactor)
    y

  /** Removes the DC offset from `output`. */
  def removeDc(): Unit =
    dcSum += -dcDelay(dcIndex) + output
    dcDelay(dcIndex) = output
    output = output - dcSum / DcFilterSize
    dcIndex = (dcIndex + 1) & (DcFilterSize - 1)

object Ym2149:
  val ToneChannels = 3
  val DecimateFactor = 8
  val FirSize = 192
  val DcFilterSize = 1024

  private inline val SlideDown = 0
  private inline val SlideUp = 1
  private inline val HoldTop = 2
  private inline val HoldBottom = 3

  private val Envelopes: Array[Array[Int]] = Array(
    Array(SlideDown, HoldBottom),
    Array(SlideDown, HoldBottom),
    Array(SlideDown, HoldBottom),
    Array(SlideDown, HoldBottom),
    Array(SlideUp, HoldBottom),
    Array(SlideUp, HoldBottom),
    Array(SlideUp, HoldBottom),
    Array(SlideUp, HoldBottom),
    Array(SlideDown, SlideDown),
    Array(SlideDown, HoldBottom),
    Array(SlideDown, SlideUp),
    Array(SlideDown, HoldTop),
    Array(SlideUp, SlideUp),
    Array(SlideUp, HoldTop),
    Array(SlideUp, SlideDown),
    Array(SlideUp, HoldBottom)
  )

  private val YmDacTable: Array[Double] = Array(
    0.0, 0.0, 0.00465400167849, 0.00772106507973, 0.0109559777218, 0.0139620050355, 0.0169985503929,
    0.0200198367285, 0.024368657969, 0.029694056611, 0.0350652323186, 0.0403906309606,
    0.0485389486534, 0.0583352407111, 0.0680552376593, 0.0777752346075, 0.0925154497597,
    0.111085679408, 0.129747463188, 0.148485542077, 0.17666895552, 0.211551079576, 0.246387426566,
    0.281101701381, 0.333730067903, 0.400427252613, 0.467383840696, 0.53443198291, 0.635172045472,
    0.75800717174, 0.879926756695, 1.0
  )

  /** Coefficients of the symmetric decimation filter: `FirCoefficients(k)` multiplies
    * `x(k) + x(192 - k)`, the centre tap `x(96)` has weight 0.125.
    */
  private val FirCoefficients: Array[Double] =
    val c = new Array[Double](96)
    val values = Seq(
      1 -> -0.0000046183113992051936,
      2 -> -0.00001117761640887225,
      3 -> -0.000018610264502005432,
      4 -> -0.000025134586135631012,
      5 -> -0.000028494281690666197,
      6 -> -0.000026396828793275159,
      7 -> -0.000017094212558802156,
      9 -> 0.000023798193576966866,
      10 -> 0.000051281160242202183,
      11 -> 0.00007762197826243427,
      12 -> 0.000096759426664120416,
      13 -> 0.00010240229300393402,
      14 -> 0.000089344614218077106,
      15 -> 0.000054875700118949183,
      17 -> -0.000069839082210680165,
      18 -> -0.0001447966132360757,
      19 -> -0.00021158452917708308,
      20 -> -0.00025535069106550544,
      21 -> -0.00026228714374322104,
      22 -> -0.00022258805927027799,
      23 -> -0.00013323230495695704,
      25 -> 0.00016182578767055206,
      26 -> 0.00032846175385096581,
      27 -> 0.00047045611576184863,
      28 -> 0.00055713851457530944,
      29 -> 0.00056212565121518726,
      30 -> 0.00046901918553962478,
      31 -> 0.00027624866838952986,
      33 -> -0.00032564179486838622,
      34 -> -0.00065182310286710388,
      35 -> -0.00092127787309319298,
      36 -> -0.0010772534348943575,
      37 -> -0.0010737727700273478,
      38 -> -0.00088556645390392634,
      39 -> -0.00051581896090765534,
      41 -> 0.00059548767193795277,
      42 -> 0.0011803558710661009,
      43 -> 0.0016527320270369871,
      44 -> 0.0019152679330965555,
      45 -> 0.0018927324805381538,
      46 -> 0.0015481870327877937,
      47 -> 0.00089470695834941306,
      49 -> -0.0010178225878206125,
      50 -> -0.0020037400552054292,
      51 -> -0.0027874356824117317,
      52 -> -0.003210329988021943,
      53 -> -0.0031540624117984395,
      54 -> -0.0025657163651900345,
      55 -> -0.0014750752642111449,
      57 -> 0.0016624165446378462,
      58 -> 0.0032591192839069179,
      59 -> 0.0045165685815867747,
      60 -> 0.0051838984346123896,
      61 -> 0.0050774264697459933,
      62 -> 0.0041192521414141585,
      63 -> 0.0023628575417966491,
      65 -> -0.0026543507866759182,
      66 -> -0.0051990251084333425,
      67 -> -0.0072020238234656924,
      68 -> -0.0082672928192007358,
      69 -> -0.0081033739572956287,
      70 -> -0.006583111539570221,
      71 -> -0.0037839040415292386,
      73 -> 0.0042781252851152507,
      74 -> 0.0084176358598320178,
      75 -> 0.01172566057463055,
      76 -> 0.013550476647788672,
      77 -> 0.013388189369997496,
      78 -> 0.010979501242341259,
      79 -> 0.006381274941685413,
      81 -> -0.007421229604153888,
      82 -> -0.01486456304340213,
      83 -> -0.021143584622178104,
      84 -> -0.02504275058758609,
      85 -> -0.025473530942547201,
      86 -> -0.021627310017882196,
      87 -> -0.013104323383225543,
      89 -> 0.017065133989980476,
      90 -> 0.036978919264451952,
      91 -> 0.05823318062093958,
      92 -> 0.079072012081405949,
      93 -> 0.097675998716952317,
      94 -> 0.11236045936950932,
      95 -> 0.12176343577287731
    )
    values.foreach((k, v) => c(k) = v)
    c

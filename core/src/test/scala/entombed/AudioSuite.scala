package entombed

import entombed.audio.{AudioEngine, Dosound, Ym2149}
import entombed.data.Assets

class AudioSuite extends munit.FunSuite:

  private def zeroCrossings(samples: Array[Float]): Int =
    samples.indices.drop(1).count(i => samples(i - 1) < 0 && samples(i) >= 0)

  test("a tone of period p sounds at 2 MHz / (16 p)") {
    val sr = 44100
    val ym = new Ym2149(sr)
    ym.writeRegister(0, 0x00)
    ym.writeRegister(1, 0x01) // period 256 -> 488.28 Hz
    ym.writeRegister(7, 0x3e) // tone A only
    ym.writeRegister(8, 15)
    val out = Array.fill(sr) { ym.process(); ym.removeDc(); ym.output.toFloat }
    val f = zeroCrossings(out.drop(sr / 10)) * 10.0 / 9
    assert(math.abs(f - 2_000_000.0 / (16 * 256)) < 3, f)
  }

  test("the output is silent when the mixer is off") {
    val ym = new Ym2149(44100)
    ym.writeRegister(7, 0x3f)
    ym.writeRegister(8, 15)
    val out = Array.fill(44100) { ym.process(); ym.removeDc(); ym.output }
    assert(out.drop(2000).forall(v => math.abs(v) < 1e-6))
  }

  test("Dosound: register writes, sweeps with 8-bit wrap-around, waits and end") {
    val ym = new Ym2149(44100)
    val writes = scala.collection.mutable.ArrayBuffer.empty[(Int, Int)]
    ym.onWrite = (r, v) => writes += r -> v
    val ds = new Dosound(ym)
    // reg 8 = 15, temp = $59, sweep reg 0 by +1 up to 7 (wraps through $FF), wait 2, end
    ds.start(Array(8, 15, 0x80, 0x59, 0x81, 0, 1, 7, 0x82, 2, 0x82, 0).map(_.toByte))
    var ticks = 0
    while ds.playing do
      ds.tick()
      ticks += 1
    val sweep = writes.filter(_._1 == 0).map(_._2)
    assertEquals(sweep.length, (7 - 0x59) & 0xff) // 174 steps, one per tick
    assertEquals(sweep.head, 0x5a)
    assertEquals(sweep.last, 7)
    // 174 sweep ticks, the wait command, 2 skipped ticks, the end command
    assertEquals(ticks, 174 + 1 + 2 + 1)
  }

  test("the engine ticks at exactly 50 Hz") {
    for sr <- Seq(44100, 48000, 32000) do
      val engine = new AudioEngine(sr)
      val seqs = Array(Array(0x82, 1, 8, 0, 0x82, 0).map(_.toByte))
      engine.play(seqs(0))
      val buf = new Array[Float](sr)
      engine.render(buf, 0, sr) // one second
      assert(buf.forall(v => !v.isNaN))
  }

  test("all game sounds render without errors and are not silent") {
    val engine = new AudioEngine(44100)
    for (s, i) <- Assets.default.sounds.zipWithIndex do
      engine.play(s.sequence)
      val buf = new Array[Float](44100 / 2)
      engine.render(buf, 0, buf.length)
      val peak = buf.map(math.abs).max
      assert(peak > 0.01, s"sound $i peak $peak")
      engine.silence()
      engine.render(new Array[Float](4410), 0, 4410)
  }

  test("rendering is deterministic") {
    def render(): Seq[Float] =
      val engine = new AudioEngine(44100)
      engine.play(Assets.default.sounds(10).sequence)
      val buf = new Array[Float](44100)
      engine.render(buf, 0, buf.length)
      buf.toSeq
    assertEquals(render(), render())
  }

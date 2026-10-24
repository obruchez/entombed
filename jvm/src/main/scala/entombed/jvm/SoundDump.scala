package entombed.jvm

import entombed.audio.AudioEngine
import entombed.data.Assets

import java.io.{ByteArrayInputStream, File}
import javax.sound.sampled.{AudioFileFormat, AudioFormat, AudioInputStream, AudioSystem}

/** Writes the 20 sounds of the game as WAV files (and the "tomb completed" tune), and prints
  * their duration and an estimate of their pitch.
  */
object SoundDump:
  val SampleRate = 44100

  def render(sequences: Seq[(Array[Byte], Int)], seconds: Double): Array[Float] =
    val engine = new AudioEngine(SampleRate)
    val out = new Array[Float]((seconds * SampleRate).toInt)
    var pos = 0
    for (seq, waitTicks) <- sequences do
      engine.play(seq)
      val n = math.min(out.length - pos, waitTicks * SampleRate / 50)
      engine.render(out, pos, n)
      pos += n
    engine.render(out, pos, out.length - pos)
    out

  def writeWav(samples: Array[Float], file: File): Unit =
    val bytes = new Array[Byte](samples.length * 2)
    for i <- samples.indices do
      val v = (math.max(-1f, math.min(1f, samples(i))) * 32767).toInt
      bytes(2 * i) = v.toByte
      bytes(2 * i + 1) = (v >> 8).toByte
    val format = new AudioFormat(SampleRate.toFloat, 16, 1, true, false)
    val stream = new AudioInputStream(new ByteArrayInputStream(bytes), format, samples.length)
    file.getParentFile.mkdirs()
    AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file)

  def describe(samples: Array[Float]): String =
    val window = SampleRate / 100
    val loud = samples.grouped(window).map(w => w.map(math.abs).max).toVector
    val lastLoud = loud.lastIndexWhere(_ > 0.01)
    val duration = (lastLoud + 1) * 10
    val first = samples.take(math.min(samples.length, SampleRate / 10))
    val crossings = first.indices.drop(1).count(i => first(i - 1) < 0 && first(i) >= 0)
    f"duration ~${duration}%4d ms, first 100 ms ~${crossings * 10}%5d Hz, peak ${loud.max}%.2f"

  def main(args: Array[String]): Unit =
    val dir = new File(args.headOption.getOrElse("out/sounds"))
    val sounds = Assets.default.sounds
    for (s, i) <- sounds.zipWithIndex do
      val samples = render(Seq(s.sequence -> 1), 3.0)
      writeWav(samples, new File(dir, f"sound$i%02d.wav"))
      println(f"sound $i%2d prio ${s.priority}%3d pause ${s.pause}%3d: ${describe(samples)}")
    // play_big_sound: 17, wait 21 VBLs, 18, wait 14, 17, wait 7, 19, wait 9
    val tune = Seq(17 -> 21, 18 -> 14, 17 -> 7, 19 -> 9).map((n, w) => sounds(n).sequence -> w)
    val samples = render(tune, 3.0)
    writeWav(samples, new File(dir, "tomb-completed.wav"))
    println(s"tomb completed: ${describe(samples)}")

package entombed.jvm

import entombed.audio.AudioEngine

import java.util.concurrent.ConcurrentLinkedQueue
import javax.sound.sampled.{AudioFormat, AudioSystem, SourceDataLine}

/** Plays the game's sounds through javax.sound, on a daemon thread. */
final class JvmAudio(sampleRate: Int = 44100, bufferTicks: Int = 3):
  private val requests = new ConcurrentLinkedQueue[Array[Byte]]()
  @volatile private var running = true

  def play(sequence: Array[Byte]): Unit = requests.add(sequence)

  def close(): Unit = running = false

  private val thread = new Thread(() => loop(), "entombed-audio")
  thread.setDaemon(true)

  def start(): JvmAudio =
    thread.start()
    this

  private def loop(): Unit =
    val format = new AudioFormat(sampleRate.toFloat, 16, 1, true, false)
    val line: SourceDataLine =
      try AudioSystem.getSourceDataLine(format)
      catch
        case e: Exception =>
          System.err.println(s"No audio output: ${e.getMessage}")
          return
    val tick = sampleRate / 50
    line.open(format, tick * 2 * bufferTicks)
    line.start()
    val engine = new AudioEngine(sampleRate)
    val samples = new Array[Float](tick)
    val bytes = new Array[Byte](tick * 2)
    while running do
      var r = requests.poll()
      while r != null do
        engine.play(r)
        r = requests.poll()
      engine.render(samples, 0, tick)
      var i = 0
      while i < tick do
        val v = (math.max(-1f, math.min(1f, samples(i))) * 32767).toInt
        bytes(2 * i) = v.toByte
        bytes(2 * i + 1) = (v >> 8).toByte
        i += 1
      line.write(bytes, 0, bytes.length) // blocks when the buffer is full: paces the loop
    line.drain()
    line.close()

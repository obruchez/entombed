package entombed.js

import entombed.audio.AudioEngine

import scala.scalajs.js

/** Web Audio output. The audio context can only start after a user gesture (autoplay policy), so
  * sounds requested before are dropped.
  */
final class WebAudio:
  private var context: js.Dynamic = null
  private var engine: AudioEngine = null
  private var buffer: Array[Float] = null

  def resume(): Unit =
    if context == null then
      val ctor = js.Dynamic.global.AudioContext
      val ctx =
        if !js.isUndefined(ctor) then js.Dynamic.newInstance(ctor)()
        else js.Dynamic.newInstance(js.Dynamic.global.webkitAudioContext)()
      context = ctx
      engine = new AudioEngine(ctx.sampleRate.asInstanceOf[Double].toInt)
      val size = 2048
      buffer = new Array[Float](size)
      val node = ctx.createScriptProcessor(size, 0, 1)
      node.onaudioprocess = { (e: js.Dynamic) =>
        val out = e.outputBuffer.getChannelData(0).asInstanceOf[js.typedarray.Float32Array]
        val n = out.length
        if buffer.length < n then buffer = new Array[Float](n)
        engine.render(buffer, 0, n)
        var i = 0
        while i < n do
          out(i) = buffer(i)
          i += 1
      }: js.Function1[js.Dynamic, Unit]
      node.connect(ctx.destination)
    if context.state.asInstanceOf[String] == "suspended" then context.resume()

  def play(sequence: Array[Byte]): Unit = if engine != null then engine.play(sequence)

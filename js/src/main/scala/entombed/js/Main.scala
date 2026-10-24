package entombed.js

import entombed.core.{GameCore, Input}
import entombed.gfx.FrameBuffer
import entombed.oracle.OracleCore
import org.scalajs.dom

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.typedarray.Int32Array

/** Browser version: canvas, keyboard as joystick, Web Audio. */
object Main:
  private val TickMs = 20.0 // 50 Hz, the PAL vertical blank of the Atari ST

  private var joystick = 0
  private val typed = mutable.Queue.empty[Int]
  private val storage = new LocalStorage
  private var core: GameCore = newCore()
  private val audio = new WebAudio

  /** Diagnostics, readable from the browser console as `entombedStats`. */
  private val stats = js.Dynamic.literal(ticks = 0, stepMs = 0.0)
  private var ticks = 0
  private var stepMs = 0.0
  dom.window.asInstanceOf[js.Dynamic].entombedStats = stats

  private def newCore(): GameCore = new OracleCore(storage)

  def main(args: Array[String]): Unit =
    val canvas = dom.document.getElementById("screen").asInstanceOf[dom.html.Canvas]
    val screen = new CanvasScreen(canvas)
    installKeys()
    var last = 0.0
    var acc = 0.0
    def frame(t: Double): Unit =
      if last > 0 then acc += math.min(t - last, 250.0)
      last = t
      var n = 0
      while acc >= TickMs && n < 5 do
        val key = if typed.nonEmpty then typed.dequeue() else 0
        val t0 = dom.window.performance.now()
        core.step(Input(joystick, key))
        ticks += 1
        stepMs += dom.window.performance.now() - t0
        stats.ticks = ticks
        stats.stepMs = stepMs
        core.drainSounds(audio.play)
        if core.finished then core = newCore()
        acc -= TickMs
        n += 1
      screen.present(core.frame, core)
      dom.window.requestAnimationFrame(t => frame(t))
    dom.document.addEventListener("visibilitychange", (_: dom.Event) => { last = 0; acc = 0 })
    dom.window.requestAnimationFrame(t => frame(t))

  private def joystickBit(code: String): Int = code match
    case "ArrowUp" | "Numpad8"                     => Input.Up
    case "ArrowDown" | "Numpad2"                   => Input.Down
    case "ArrowLeft" | "Numpad4"                   => Input.Left
    case "ArrowRight" | "Numpad6"                  => Input.Right
    case "Space" | "ControlLeft" | "ControlRight" | "Numpad0" => Input.Fire
    case _                                         => 0

  private def installKeys(): Unit =
    dom.window.addEventListener("keydown", (e: dom.KeyboardEvent) => {
      audio.resume()
      val bit = joystickBit(e.code)
      if bit != 0 then
        joystick |= bit
        e.preventDefault()
      if !e.repeat && !e.ctrlKey && !e.metaKey then
        if e.key.length == 1 && e.key.charAt(0) < 127 then typed.enqueue(e.key.charAt(0).toInt)
        else if e.key == "Backspace" then
          typed.enqueue(8)
          e.preventDefault()
        else if e.key == "Enter" then typed.enqueue(13)
    })
    dom.window.addEventListener("keyup", (e: dom.KeyboardEvent) => {
      joystick &= ~joystickBit(e.code)
    })
    dom.window.addEventListener("blur", (_: dom.Event) => joystick = 0)
    dom.window.addEventListener("pointerdown", (_: dom.Event) => audio.resume())

/** Draws the 320x200 frame on a canvas, scaled without smoothing. */
final class CanvasScreen(canvas: dom.html.Canvas):
  private val offscreen = dom.document.createElement("canvas").asInstanceOf[dom.html.Canvas]
  offscreen.width = FrameBuffer.Width
  offscreen.height = FrameBuffer.Height
  private val offCtx = offscreen.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  private val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  private val image = offCtx.createImageData(FrameBuffer.Width, FrameBuffer.Height)
  private val pixels = new Int32Array(
    image.asInstanceOf[js.Dynamic].data.buffer.asInstanceOf[js.typedarray.ArrayBuffer]
  )
  private val argb = new Array[Int](FrameBuffer.Width * FrameBuffer.Height)

  def present(frame: FrameBuffer, core: GameCore): Unit =
    frame.toArgb(core.palette, argb)
    var i = 0
    while i < argb.length do
      val c = argb(i) // 0xAARRGGBB -> little-endian RGBA bytes: 0xAABBGGRR
      pixels(i) = (c & 0xff00ff00) | ((c >> 16) & 0xff) | ((c & 0xff) << 16)
      i += 1
    offCtx.putImageData(image, 0, 0)
    ctx.asInstanceOf[js.Dynamic].imageSmoothingEnabled = false
    ctx.drawImage(offscreen, 0, 0, canvas.width.toDouble, canvas.height.toDouble)

package entombed.js

import entombed.core.{GameCore, Input}
import entombed.driver.GameDriver
import entombed.gfx.FrameBuffer
import entombed.oracle.OracleCore
import entombed.save.Storage
import org.scalajs.dom

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.typedarray.Int32Array

/** Browser version: canvas, keyboard as joystick, Web Audio, local storage.
  *
  * The page may contain buttons with the ids `save`, `load`, `god`, `tomb5` and `engine`; the first
  * four are also on the keys F6, F7, F8 and F4. With `?oracle` in the URL, the game starts with the
  * original 68000 code instead of the Scala port.
  */
object Main:
  private val TickMs = 20.0 // 50 Hz, the PAL vertical blank of the Atari ST

  private var joystick = 0
  private val typed = mutable.Queue.empty[Int]
  private val storage = new LocalStorage
  private val oracle: Storage => GameCore = s => new OracleCore(s)
  private var isOracle = dom.window.location.search.contains("oracle")
  private val driver = new GameDriver(storage, if isOracle then oracle else GameDriver.port)
  private val audio = new WebAudio

  /** Diagnostics, readable from the browser console as `entombedStats`. */
  private val stats = js.Dynamic.literal(ticks = 0, stepMs = 0.0)
  dom.window.asInstanceOf[js.Dynamic].entombedStats = stats
  private var ticks = 0
  private var stepMs = 0.0

  def main(args: Array[String]): Unit =
    val canvas = dom.document.getElementById("screen").asInstanceOf[dom.html.Canvas]
    val screen = new CanvasScreen(canvas)
    installKeys()
    installButtons()
    var last = 0.0
    var acc = 0.0
    def frame(t: Double): Unit =
      if last > 0 then acc += math.min(t - last, 250.0)
      last = t
      var n = 0
      while acc >= TickMs && n < 5 do
        while typed.nonEmpty do driver.keyTyped(typed.dequeue())
        val t0 = dom.window.performance.now()
        driver.step(joystick | gamepad())
        ticks += 1
        stepMs += dom.window.performance.now() - t0
        stats.ticks = ticks
        stats.stepMs = stepMs
        driver.drainSounds(audio.play)
        acc -= TickMs
        n += 1
      screen.present(driver.frame, driver)
      updateStatus()
      dom.window.requestAnimationFrame(t => frame(t))
    dom.document.addEventListener("visibilitychange", (_: dom.Event) => { last = 0; acc = 0 })
    dom.window.requestAnimationFrame(t => frame(t))

  /** The first gamepad, if any: d-pad or left stick, and any of the first four buttons to fire. */
  private def gamepad(): Int =
    val nav = dom.window.navigator.asInstanceOf[js.Dynamic]
    if js.isUndefined(nav.getGamepads) then 0
    else
      val pads = nav.getGamepads().asInstanceOf[js.Array[js.Dynamic]]
      val pad = (0 until pads.length).map(pads(_)).find(p => p != null && !js.isUndefined(p))
      pad.fold(0) { p =>
        val buttons = p.buttons.asInstanceOf[js.Array[js.Dynamic]]
        val axes = p.axes.asInstanceOf[js.Array[Double]]
        def pressed(i: Int) = i < buttons.length && buttons(i).pressed.asInstanceOf[Boolean]
        def axis(i: Int) = if i < axes.length then axes(i) else 0.0
        var j = 0
        if pressed(12) || axis(1) < -0.5 then j |= Input.Up
        if pressed(13) || axis(1) > 0.5 then j |= Input.Down
        if pressed(14) || axis(0) < -0.5 then j |= Input.Left
        if pressed(15) || axis(0) > 0.5 then j |= Input.Right
        if (0 to 3).exists(pressed) then j |= Input.Fire
        j
      }

  private def switchEngine(): Unit =
    isOracle = !isOracle
    driver.restartWith(
      if isOracle then oracle else GameDriver.port,
      if isOracle then "original 68000 code" else "Scala port"
    )

  private def joystickBit(code: String): Int = code match
    case "ArrowUp" | "Numpad8"                                => Input.Up
    case "ArrowDown" | "Numpad2"                              => Input.Down
    case "ArrowLeft" | "Numpad4"                              => Input.Left
    case "ArrowRight" | "Numpad6"                             => Input.Right
    case "Space" | "ControlLeft" | "ControlRight" | "Numpad0" => Input.Fire
    case _                                                    => 0

  private def installKeys(): Unit =
    dom.window.addEventListener(
      "keydown",
      (e: dom.KeyboardEvent) => {
        audio.resume()
        val bit = joystickBit(e.code)
        if bit != 0 then
          joystick |= bit
          e.preventDefault()
        val command: Option[() => Unit] = e.key match
          case "F6"  => Some(() => driver.save())
          case "F7"  => Some(() => driver.load())
          case "F8"  => Some(() => driver.toggleGodMode())
          case "F4"  => Some(() => driver.unlockTomb5())
          case "F2"  => Some(() => driver.toggleSpeed())
          case "F12" => None // developer tools
          case _     => None
        command match
          case Some(c) =>
            c()
            e.preventDefault()
          case None =>
            if !e.repeat && !e.ctrlKey && !e.metaKey && !e.altKey then
              if e.key.length == 1 && e.key.charAt(0) < 127 then
                typed.enqueue(e.key.charAt(0).toInt)
              else if e.key == "Backspace" then
                typed.enqueue(8)
                e.preventDefault()
              else if e.key == "Enter" then typed.enqueue(13)
      }
    )
    dom.window.addEventListener(
      "keyup",
      (e: dom.KeyboardEvent) => {
        joystick &= ~joystickBit(e.code)
      }
    )
    dom.window.addEventListener("blur", (_: dom.Event) => joystick = 0)
    dom.window.addEventListener("pointerdown", (_: dom.Event) => audio.resume())

  private def installButtons(): Unit =
    def on(id: String)(f: => Unit): Unit =
      val el = dom.document.getElementById(id)
      if el != null then
        el.addEventListener(
          "click",
          (e: dom.Event) => {
            audio.resume()
            f
            e.preventDefault()
            el.asInstanceOf[dom.html.Element].blur()
          }
        )
    on("save")(driver.save())
    on("load")(driver.load())
    on("god")(driver.toggleGodMode())
    on("tomb5")(driver.unlockTomb5())
    on("engine")(switchEngine())
    on("speed")(driver.toggleSpeed())

  private var lastStatus = ""

  private def updateStatus(): Unit =
    val text = (if driver.messageTicks > 0 then driver.message else "") +
      (if driver.godMode then "  [god mode]" else "") +
      (if driver.originalSpeed then "" else "  [fast]") +
      (if isOracle then "  [original code]" else "")
    if text != lastStatus then
      lastStatus = text
      val el = dom.document.getElementById("status")
      if el != null then el.textContent = text

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

  def present(frame: FrameBuffer, driver: GameDriver): Unit =
    frame.toArgb(driver.palette, argb)
    var i = 0
    while i < argb.length do
      val c = argb(i) // 0xAARRGGBB -> little-endian RGBA bytes: 0xAABBGGRR
      pixels(i) = (c & 0xff00ff00) | ((c >> 16) & 0xff) | ((c & 0xff) << 16)
      i += 1
    offCtx.putImageData(image, 0, 0)
    ctx.asInstanceOf[js.Dynamic].imageSmoothingEnabled = false
    ctx.drawImage(offscreen, 0, 0, canvas.width.toDouble, canvas.height.toDouble)

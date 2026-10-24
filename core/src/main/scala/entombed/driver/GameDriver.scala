package entombed.driver

import entombed.core.{GameCore, Input}
import entombed.game.{Game, Vars}
import entombed.gfx.{FrameBuffer, Palette}
import entombed.save.Storage

import scala.collection.mutable

/** What the hosts run: the game (the Scala port, or the original code through the oracle) plus the
  * features added in 2026: saving and loading at any moment, god mode, the fifth tomb.
  *
  * @param makeCore
  *   creates a new game (also after the player quits with Q)
  */
final class GameDriver(storage: Storage, makeCore: Storage => GameCore):
  private var make = makeCore
  private var core: GameCore = make(storage)

  /** A short message for the host to display (for example after a save), with its duration. */
  var message: String = ""
  var messageTicks: Int = 0

  def game: GameCore = core
  def frame: FrameBuffer = core.frame
  def palette: Palette = core.palette

  /** Speed of the Atari ST: the game computed a frame in about 3 vertical blanks (16.7 frames per
    * second: the hourglass counts 16 frames per second), the other screens in about 2. The sound
    * always runs at 50 Hz. When false, a frame per vertical blank (50 Hz).
    */
  var originalSpeed: Boolean = true
  private var busyTicks = 0
  private val keys = mutable.Queue.empty[Int]

  /** Keys typed by the player, kept until the game reads the keyboard (as the Atari's keyboard
    * buffer did while the game was busy).
    */
  def keyTyped(c: Int): Unit = keys.enqueue(c & 0xff)

  /** One vertical blank (call 50 times per second). */
  def step(joystick: Int): Unit =
    if busyTicks > 0 then busyTicks -= 1
    else
      val key = if keys.nonEmpty then keys.dequeue() else 0
      core.step(Input(joystick, key))
      if core.finished then core = make(storage)
      else if originalSpeed && core.presentedFrame then
        val playing = core.peekWord(Vars.FirstPageDone) == 1 &&
          core.peekWord(Vars.TombChosenFlag) == 1
        busyTicks = if playing then 2 else 1
    if messageTicks > 0 then messageTicks -= 1

  def step(input: Input): Unit =
    if input.key != 0 then keyTyped(input.key)
    step(input.joystick)

  def toggleSpeed(): Unit =
    originalSpeed = !originalSpeed
    say(if originalSpeed then "Atari ST speed" else "Fast: 50 frames per second")

  def drainSounds(f: Array[Byte] => Unit): Unit = core.drainSounds(f)

  private def say(text: String): Unit =
    message = text
    messageTicks = 100

  private def slotName(slot: Int): String = s"snapshot-$slot"

  /** Saves the complete state of the game (not only at the moments the 1995 save allowed). */
  def save(slot: Int = 1): Unit =
    storage.write(slotName(slot), core.snapshot())
    say(s"Game saved (slot $slot)")

  def load(slot: Int = 1): Unit =
    storage.read(slotName(slot)) match
      case Some(bytes) =>
        try
          core.restore(bytes)
          say(s"Game loaded (slot $slot)")
        catch case e: Exception => say(s"Cannot load slot $slot: ${e.getMessage}")
      case None => say(s"Nothing saved in slot $slot")

  /** Toggles god mode (the Scala port only). */
  def toggleGodMode(): Unit = core match
    case g: Game =>
      g.godMode = !g.godMode
      say(if g.godMode then "God mode on" else "God mode off")
    case _ => say("God mode needs the Scala port")

  def godMode: Boolean = core match
    case g: Game => g.godMode
    case _       => false

  /** The fifth tomb is offered at the next tomb selection. */
  def unlockTomb5(): Unit = core match
    case g: Game =>
      g.unlockTomb5()
      say("Tomb 5 unlocked")
    case _ => say("Needs the Scala port")

  /** Switches between the Scala port and the original code (a new game starts). */
  def restartWith(newMake: Storage => GameCore, name: String): Unit =
    make = newMake
    core = make(storage)
    say(s"New game: $name")

object GameDriver:
  val port: Storage => GameCore = s => Game(s)

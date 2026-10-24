package entombed.game

import entombed.audio.SoundSequence
import entombed.core.Input
import entombed.data.Assets
import entombed.gfx.{Bitplanes, Blitter, FrameBuffer, Palette}
import entombed.save.{ByteReader, ByteWriter, Storage}

import scala.collection.mutable

import Vars.*

/** What the game produces, in order. A `Vbl` ends a vertical blank. */
sealed trait Event
object Event:
  final case class SetPalette(words: Array[Int]) extends Event
  final case class ShowScreen(chunky: Array[Byte]) extends Event
  final case class PlaySound(sequence: Array[Byte]) extends Event
  case object Vbl extends Event

/** The machine seen by the game: its memory and the 17 external routines of the 1995 version
  * (`sm_*`), implemented exactly like the oracle implements them.
  *
  * The game logic never waits: `sm_wait_vbl` queues a `Vbl` event and the logic continues until it
  * needs fresh input. [[Game.step]] then hands the events over one vertical blank at a time.
  */
abstract class Machine(val assets: Assets, val storage: Storage):
  val ram = new Ram()

  // ----- Input, as sent by the keyboard processor -----
  protected var input: Input = Input.Empty
  protected var keyPending = 0
  private var joystickCleared = false
  private var joystickAtClear = 0

  // ----- Output -----
  protected val events = mutable.ArrayDeque.empty[Event]

  /** Loads the data segment, the graphics converted to 4 bpp, and the font. */
  protected def loadMemory(): Unit =
    System.arraycopy(assets.bytes, 0, ram.bytes, Layout.Data, Layout.DataSize)
    val gfx = Bitplanes.toChunky(assets.blockRows)
    System.arraycopy(gfx, 0, ram.bytes, Layout.Blocks32x16, gfx.length)
    for c <- 0 until 100 do
      val glyph = Bitplanes.toChunky(assets.font(c))
      System.arraycopy(glyph, 0, ram.bytes, Layout.Font + 32 * c, 32)

  // ----- External routines -----

  /** `sm_get_joystick`: writes the joystick state to `joystick_state`. After `sm_clr_joystick`, the
    * keyboard processor only reports the joystick again when its state changes.
    */
  protected def getJoystick(): Unit =
    if joystickCleared && input.joystick != joystickAtClear then joystickCleared = false
    ram.setB(JoystickState, if joystickCleared then 0 else input.joystick & 0xff)

  protected def clrJoystick(): Unit =
    joystickCleared = true
    joystickAtClear = input.joystick

  /** `sm_get_keyboard`: the key typed during this vertical blank, or 0. */
  protected def getKeyboard(): Int =
    val k = keyPending
    keyPending = 0
    k

  protected def changePal(address: Int): Unit =
    events += Event.SetPalette(Array.tabulate(16)(i => ram.w(address + 2 * i)))

  protected def waitVbl(): Unit = events += Event.Vbl

  /** End of a vertical blank without display (busy-waiting loops of the original). */
  protected def idleVbl(): Unit = events += Event.Vbl

  protected def affScreen(): Unit =
    val screen = ram.ptr(PhysScreen1)
    events += Event.ShowScreen(java.util.Arrays.copyOfRange(ram.bytes, screen, screen + 32000))

  protected def playSound(address: Int): Unit =
    events += Event.PlaySound(SoundSequence.read(ram.bytes, address))

  /** `_sm_aff_transp` and its variants: (x, y) are tomb coordinates. Returns "visible". */
  protected def affTransp(block: Int, rows: Int, x: Int, y: Int): Boolean =
    blit(block, rows, x, y, Blitter.Transparent)
  protected def affNormal(block: Int, rows: Int, x: Int, y: Int): Boolean =
    blit(block, rows, x, y, Blitter.Opaque)
  protected def affWhite(block: Int, rows: Int, x: Int, y: Int): Boolean =
    blit(block, rows, x, y, Blitter.White)

  private def blit(block: Int, rows: Int, x: Int, y: Int, mode: Int): Boolean =
    Blitter.draw(
      ram.bytes,
      block,
      rows,
      x - ram.w(PosInTombX),
      y - ram.w(PosInTombY),
      ram.ptr(PhysScreen2),
      mode
    )

  // ----- Snapshots -----

  protected def writeMachine(w: ByteWriter): Unit =
    w.bytes(java.util.Arrays.copyOfRange(ram.bytes, 0, Layout.End))
    w.bool(joystickCleared).i32(joystickAtClear)
    w.i32(events.size)
    events.foreach {
      case Event.SetPalette(p) => w.u8(0).ints(p)
      case Event.ShowScreen(s) => w.u8(1).bytes(s)
      case Event.PlaySound(s)  => w.u8(2).bytes(s)
      case Event.Vbl           => w.u8(3)
    }

  protected def readMachine(r: ByteReader): Unit =
    val m = r.bytes()
    java.util.Arrays.fill(ram.bytes, 0.toByte)
    System.arraycopy(m, 0, ram.bytes, 0, m.length)
    joystickCleared = r.bool()
    joystickAtClear = r.i32()
    events.clear()
    for _ <- 0 until r.i32() do
      events += (r.u8() match
        case 0 => Event.SetPalette(r.ints())
        case 1 => Event.ShowScreen(r.bytes())
        case 2 => Event.PlaySound(r.bytes())
        case _ => Event.Vbl)

object Machine:
  def palette(words: Array[Int]): Palette = new Palette(words)
  def present(chunky: Array[Byte], frame: FrameBuffer): Unit = Blitter.present(chunky, 0, frame)

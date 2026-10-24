package entombed.core

import entombed.gfx.{FrameBuffer, Palette}

/** Input for one vertical blank: the joystick byte and at most one key (ASCII code, 0 if none).
  *
  * Joystick bits, as sent by the Atari keyboard processor: 0 up, 1 down, 2 left, 3 right, 7 fire.
  */
final case class Input(joystick: Int, key: Int)

object Input:
  val Up = 0x01
  val Down = 0x02
  val Left = 0x04
  val Right = 0x08
  val Fire = 0x80
  val Empty: Input = Input(0, 0)

/** A running game. The host calls `step` once per vertical blank (50 times per second), then
  * presents `frame` with `palette` and plays the requested sounds.
  *
  * Two implementations exist: the original 68000 code run by an interpreter (the "oracle") and the
  * Scala port.
  */
trait GameCore:
  /** Runs the game until its next vertical blank synchronisation. Never blocks. */
  def step(input: Input): Unit

  /** The frame currently displayed. */
  def frame: FrameBuffer

  /** The palette currently displayed. */
  def palette: Palette

  /** Hands over the XBIOS `Dosound` sequences started since the last call, in order. */
  def drainSounds(f: Array[Byte] => Unit): Unit

  /** True if the last `step` displayed a new screen (`swap_screens`). */
  def presentedFrame: Boolean

  /** A word of the game's memory (both implementations share the same memory layout). */
  def peekWord(address: Int): Int

  /** True when the game has quit (the original returned to the desktop). */
  def finished: Boolean

  /** Complete state, restorable with `restore` (save games, rewind, tests). */
  def snapshot(): Array[Byte]

  def restore(bytes: Array[Byte]): Unit

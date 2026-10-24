package entombed.game

import entombed.core.{GameCore, Input}
import entombed.data.Assets
import entombed.gfx.{FrameBuffer, Palette}
import entombed.save.{ByteReader, ByteWriter, InMemoryStorage, Storage}

import scala.collection.mutable

import M68.*
import Vars.*

/** The Scala port of Entombed.
  *
  * The code follows the machine-independent 68000 version written in 1995
  * (`reference/smaky/STOMBED1.L`): same memory layout, same variables, same algorithms; line
  * numbers of the source are given in comments. Only the control flow is restructured: the original
  * loops wait for the vertical blank, which a browser cannot do, so the game is a state machine
  * whose states are the places where the original reads the joystick or the keyboard after a
  * vertical blank (see [[Flow]]).
  */
final class Game(assets: Assets = Assets.default, storage: Storage = new InMemoryStorage)
    extends Machine(assets, storage)
    with GameCore
    with Display
    with Flow
    with TombLoader
    with ManLogic
    with Objects
    with Projectiles:

  private val currentFrame = new FrameBuffer
  private var currentPalette = Palette.Black
  private val sounds = mutable.ArrayBuffer.empty[Array[Byte]]
  private var halted = false

  /** Number of vertical blanks so far. */
  var ticks: Long = 0L

  /** State of the control flow (see [[Flow]]) and the 68000 registers it keeps between states. */
  protected var state: Int = Flow.Start
  protected val regs: Array[Int] = new Array[Int](8)

  loadMemory()

  /** True when no vertical blank requested by the logic is pending: the memory then corresponds to
    * the original's at the same moment.
    */
  def inSync: Boolean = events.isEmpty

  /** The state of the control flow (see [[Flow]]), for debugging. */
  def stateNumber: Int = state

  private var presented = false
  def presentedFrame: Boolean = presented
  def peekWord(address: Int): Int = ram.w(address)

  def frame: FrameBuffer = currentFrame
  def palette: Palette = currentPalette
  def finished: Boolean = halted

  protected def quit(): Unit =
    halted = true
    events += Event.Vbl

  def drainSounds(f: Array[Byte] => Unit): Unit =
    sounds.foreach(f)
    sounds.clear()

  def step(in: Input): Unit =
    if halted then return
    input = in
    keyPending = in.key & 0xff
    presented = false
    var guard = 0
    while !events.contains(Event.Vbl) do
      runState()
      guard += 1
      if guard > 1000 then throw new IllegalStateException(s"No vertical blank in state $state")
    var done = false
    while !done do
      events.removeHead() match
        case Event.SetPalette(p) => currentPalette = new Palette(p)
        case Event.ShowScreen(s) =>
          Machine.present(s, currentFrame)
          presented = true
        case Event.PlaySound(s) => sounds += s
        case Event.Vbl          => done = true
    ticks += 1

  // ----- Snapshots -----

  def snapshot(): Array[Byte] =
    val w = new ByteWriter
    w.string("ENTOMBED-PORT").i32(1)
    writeMachine(w)
    w.i32(state).ints(regs).bool(halted).i64(ticks).bool(godMode)
    w.bytes(currentFrame.pixels).ints(currentPalette.words)
    w.result

  def restore(bytes: Array[Byte]): Unit =
    val r = new ByteReader(bytes)
    r.expectMagic("ENTOMBED-PORT")
    r.i32()
    readMachine(r)
    state = r.i32()
    System.arraycopy(r.ints(), 0, regs, 0, regs.length)
    halted = r.bool()
    ticks = r.i64()
    godMode = r.bool()
    System.arraycopy(r.bytes(), 0, currentFrame.pixels, 0, currentFrame.pixels.length)
    currentPalette = new Palette(r.ints())
    sounds.clear()

  // ----- Cheats -----

  /** God mode: the man cannot die (not part of the original game). */
  var godMode: Boolean = false

  /** The man dies (fire, spikes, enemies, projectiles...), unless in god mode. */
  def killMan(): Boolean =
    if godMode then false
    else
      ram.setW(StateMan, 4)
      ram.setW(MoveManCounter, 0)
      ram.setW(TirEnCoursFlag, 0)
      true

  /** Marks the first four tombs as completed: the fifth one is offered at the next selection. */
  def unlockTomb5(): Unit = ram.setW(WichTombPlayed, 0xf)

  // ----- Shared helpers -----

  /** `add_points` (5587): adds BCD points; more than 999999 points gives 7 lives. */
  def addPoints(points: Int): Unit =
    var x = 0 // X flag, cleared by ADDI.B #0,D0
    var d0 = points
    var a = NbPoints + 4
    var d1 = 0
    for _ <- 0 to 3 do
      a -= 1
      val s = d0 & 0xff
      val d = ram.b(a)
      var r = (d & 0x0f) + (s & 0x0f) + x
      if r > 9 then r += 6
      r += (d & 0xf0) + (s & 0xf0)
      if r > 0x99 then
        r -= 0xa0
        x = 1
      else x = 0
      d1 = r & 0xff
      ram.setB(a, d1)
      d0 = Integer.rotateRight(d0, 8)
    if d1 != 0 then
      ram.setL(NbPoints, 0x999999)
      ram.setW(NbLifes, 7)

  /** `prepare_address_of_sound` (5920): requests sound `n` if its priority is high enough. */
  def prepareSound(n: Int): Unit =
    ram.setB(Layout.SoundNumber, n)
    val a0 = Sounds18 + 42 * n
    if lowerU16(ram.w(SoundPriority), ram.w(a0)) then
      ram.setW(SoundPriority, ram.w(a0))
      ram.setW(SoundPause, ram.w(a0 + 2))
      ram.setL(SoundAddress, a0 + 4)

  /** `play_yamaha` (5945), called once per frame. */
  def playYamaha(): Unit =
    if ram.l(SoundAddress) != 0 then
      playSound(ram.ptr(SoundAddress))
      ram.setL(SoundAddress, 0)
    else
      val p = w(ram.w(SoundPause) - 1)
      ram.setW(SoundPause, p)
      if p == 0 then ram.setW(SoundPriority, 0)

  /** `play_big_sound` (5962): the tune played when a tomb is completed. */
  def playBigSound(): Unit =
    for (address, wait) <- Seq(Sound17 -> 0x14, Sound18 -> 0xd, Sound17 -> 6, Sound19 -> 8) do
      playSound(address)
      for _ <- 0 to wait do waitVbl() // play_yamaha_wait

  protected def runState(): Unit = runFlow()

object Game:
  /** The file name used by the save routine added in 1995 (keys S and L). */
  val SaveSlot = "partie"

  def apply(storage: Storage): Game = new Game(Assets.default, storage)

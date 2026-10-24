package entombed.oracle

import entombed.core.{GameCore, Input}
import entombed.gfx.{FrameBuffer, Palette}
import entombed.save.{ByteReader, ByteWriter, InMemoryStorage, Storage}

import scala.collection.mutable

/** The original game, run instruction by instruction from the commented 68000 source written in
  * 1995 for the Smaky port (`reference/smaky/STOMBED1.L`), with the Smaky data files.
  *
  * The 1995 source reaches the machine only through a table of external routines (`sm_*`), which
  * are implemented here in Scala, exactly as `CALLER.L` did on TOS:
  *
  *   - `sm_wait_vbl` ends the current `step`,
  *   - `sm_aff_screen` converts the "chunky" 4 bpp screen into the frame buffer,
  *   - `sm_play_sound` hands the `Dosound` sequence over to the host,
  *   - the save routines (keys S and L during the game) use the host storage.
  *
  * The block display routines are the portable 68000 fallbacks included in the source, as on the
  * Smaky.
  */
final class OracleCore(storage: Storage = new InMemoryStorage) extends GameCore:
  import OracleCore.*

  private val program = OracleCore.program
  val cpu = new Cpu(program, hooks)

  private val currentFrame = new FrameBuffer
  private var currentPalette = Palette.Black
  private val sounds = mutable.ArrayBuffer.empty[Array[Byte]]
  private var input = Input.Empty
  private var keyPending = 0
  private var joystickCleared = false
  private var joystickAtClear = 0
  private var pollsSinceVbl = 0
  private var saveBuffer: java.io.ByteArrayOutputStream = null
  private var loadData: Array[Byte] = null
  private var loadPos = 0

  /** Number of vertical blanks so far. */
  var ticks: Long = 0L

  private val joystickStateAddress = program.address("joystick_state")

  // Install the external routines, like CALLER.L did
  for (name, n) <- HookNames.zipWithIndex do cpu.wl(program.address(name), Memory.hookAddress(n))

  def frame: FrameBuffer = currentFrame
  def palette: Palette = currentPalette
  def finished: Boolean = cpu.halted

  def drainSounds(f: Array[Byte] => Unit): Unit =
    sounds.foreach(f)
    sounds.clear()

  def step(in: Input): Unit =
    if cpu.halted then return
    input = in
    keyPending = in.key & 0xff
    cpu.run(MaxInstructionsPerStep)
    if !cpu.yieldRequested && !cpu.halted then
      throw new CpuError(s"No vertical blank after $MaxInstructionsPerStep instructions " +
        s"(line ${cpu.currentLine})")

  /** The joystick as read by the game: after `sm_clr_joystick`, the keyboard processor only
    * reports the joystick again when its state changes.
    */
  private def joystick: Int =
    if joystickCleared then
      if input.joystick != joystickAtClear then joystickCleared = false
    if joystickCleared then 0 else input.joystick & 0xff

  private def poll(): Unit =
    // Busy-waiting loops (pause, "press fire") poll without waiting for the VBL: treat many polls
    // in a row as a vertical blank, so that the host stays responsive.
    pollsSinceVbl += 1
    if pollsSinceVbl > MaxPollsPerVbl then vbl()

  private def vbl(): Unit =
    pollsSinceVbl = 0
    ticks += 1
    cpu.yieldRequested = true

  private object hooks extends Hooks:
    def call(c: Cpu, n: Int): Unit = n match
      case GetJoystick =>
        c.wb(joystickStateAddress, joystick)
        poll()
      case ClrJoystick =>
        joystickCleared = true
        joystickAtClear = input.joystick
      case GetKeyboard =>
        c.d(0) = keyPending
        keyPending = 0
        poll()
      case ChangePal =>
        val base = c.a(0)
        currentPalette = new Palette(Array.tabulate(16)(i => c.rw(base + 2 * i)))
      case WaitVbl => vbl()
      case AffScreen =>
        val base = c.a(0)
        val px = currentFrame.pixels
        var i = 0
        while i < 32000 do
          val b = c.mem(base + i)
          px(2 * i) = ((b >> 4) & 0xf).toByte
          px(2 * i + 1) = (b & 0xf).toByte
          i += 1
      case PlaySound =>
        // _sm_play_sound pushed A0 (the sequence address) before calling us
        val seq = c.rl(c.a(7) + 4)
        sounds += soundSequence(c, seq)
      case EndPrg => c.halted = true
      case OpenForSave => saveBuffer = new java.io.ByteArrayOutputStream()
      case Save =>
        if saveBuffer != null then
          saveBuffer.write(c.mem, c.a(0) & 0xffffff, c.d(0))
      case OpenForLoad =>
        loadData = storage.read(SaveSlot).orNull
        loadPos = 0
      case Load =>
        if loadData != null then
          val n = math.min(c.d(0), loadData.length - loadPos)
          System.arraycopy(loadData, loadPos, c.mem, c.a(0) & 0xffffff, n)
          loadPos += n
      case Close =>
        if saveBuffer != null then storage.write(SaveSlot, saveBuffer.toByteArray)
        saveBuffer = null
        loadData = null
      case _ => throw new CpuError(s"Unknown hook $n")

  // ----- Snapshots -----

  def snapshot(): Array[Byte] =
    val w = new ByteWriter
    w.string("ENTOMBED-ORACLE").i32(1)
    w.bytes(cpu.mem).ints(cpu.d).ints(cpu.a).i32(cpu.pc)
    w.bool(cpu.fn).bool(cpu.fz).bool(cpu.fv).bool(cpu.fc).bool(cpu.fx).bool(cpu.halted)
    w.bytes(currentFrame.pixels).ints(currentPalette.words)
    w.bool(joystickCleared).i32(joystickAtClear).i32(pollsSinceVbl).i64(ticks)
    w.result

  def restore(bytes: Array[Byte]): Unit =
    val r = new ByteReader(bytes)
    r.expectMagic("ENTOMBED-ORACLE")
    r.i32()
    System.arraycopy(r.bytes(), 0, cpu.mem, 0, cpu.mem.length)
    System.arraycopy(r.ints(), 0, cpu.d, 0, 8)
    System.arraycopy(r.ints(), 0, cpu.a, 0, 8)
    cpu.pc = r.i32()
    cpu.fn = r.bool(); cpu.fz = r.bool(); cpu.fv = r.bool(); cpu.fc = r.bool(); cpu.fx = r.bool()
    cpu.halted = r.bool()
    System.arraycopy(r.bytes(), 0, currentFrame.pixels, 0, currentFrame.pixels.length)
    currentPalette = new Palette(r.ints())
    joystickCleared = r.bool()
    joystickAtClear = r.i32()
    pollsSinceVbl = r.i32()
    ticks = r.i64()
    sounds.clear()

  // ----- Debugging helpers -----

  /** Reads a word variable of the game by symbol name (e.g. "nb_lifes"). */
  def word(symbol: String): Int = cpu.rw(program.address(symbol))

  def setWord(symbol: String, v: Int): Unit = cpu.ww(program.address(symbol), v)

object OracleCore:
  val MaxInstructionsPerStep = 50_000_000L
  val MaxPollsPerVbl = 32
  val SaveSlot = "partie"

  val HookNames: Vector[String] = Vector(
    "sm_get_joystick", "sm_clr_joystick", "sm_get_keyboard", "sm_change_pal", "sm_wait_vbl",
    "sm_aff_screen", "sm_play_sound", "sm_end_prg", "sm_open_for_save", "sm_save",
    "sm_open_for_load", "sm_load", "sm_close"
  )
  private inline val GetJoystick = 0
  private inline val ClrJoystick = 1
  private inline val GetKeyboard = 2
  private inline val ChangePal = 3
  private inline val WaitVbl = 4
  private inline val AffScreen = 5
  private inline val PlaySound = 6
  private inline val EndPrg = 7
  private inline val OpenForSave = 8
  private inline val Save = 9
  private inline val OpenForLoad = 10
  private inline val Load = 11
  private inline val Close = 12

  /** Corrections applied to the 1995 source before assembling it, each checked against the
    * original line: (line number, expected original text, replacement).
    */
  val Patches: Vector[(Int, String, String)] = Vector(
    // Debugging leftover of 1995: without this branch, tomb selection always offers tomb 5.
    (598, ";BNE L0009C", " BNE L0009C ; (restored, as in the Atari version)"),
    // The Smaky routine received a sound number which was sometimes stale (the title bounce
    // sets sound_address directly). Pass the sequence address instead, as the Atari did.
    (5948, "move.b _sm_sound_number,d0", " move.l sound_address,a0 ; (patched)")
  )

  def patchedSource: String =
    val lines = new String(EmbeddedSource.stombed1, "UTF-8").split("\n", -1)
    for (line, expected, replacement) <- Patches do
      val original = lines(line - 1)
      if !original.trim.startsWith(expected) then
        throw new IllegalStateException(s"Unexpected source at line $line: $original")
      lines(line - 1) = replacement
    lines.mkString("\n")

  lazy val program: Program =
    new Assembler(
      patchedSource,
      Map(
        "DATASA.BIN" -> EmbeddedSource.datasA,
        "DATASB.BIN" -> EmbeddedSource.datasB,
        "DATASC.BIN" -> EmbeddedSource.datasC,
        "DATASD.BIN" -> EmbeddedSource.datasD
      )
    ).assemble()

  /** Copies a `Dosound` sequence out of memory, up to its end marker (`$82 00`). */
  def soundSequence(c: Cpu, address: Int): Array[Byte] =
    val out = mutable.ArrayBuffer.empty[Byte]
    var p = address
    var done = false
    while !done && out.length < 256 do
      val op = c.rb(p)
      out += op.toByte
      p += 1
      if op == 0x81 then
        (0 until 3).foreach(_ => { out += c.rb(p).toByte; p += 1 })
      else
        val arg = c.rb(p)
        out += arg.toByte
        p += 1
        if op >= 0x82 && arg == 0 then done = true
    out.toArray

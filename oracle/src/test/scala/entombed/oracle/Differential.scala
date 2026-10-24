package entombed.oracle

import entombed.core.Input
import entombed.game.{Game, Layout}

import scala.collection.mutable

/** Runs the oracle and the port side by side and compares them after every vertical blank. */
object Differential:

  /** Regions not compared: the font masks, which were de-interleaved for the Smaky in 1995, and the
    * font, which the port loads in its final form (the oracle converts it at start-up).
    */
  val Ignored: Seq[(Int, Int)] = Seq(
    (Layout.Data + 416, Layout.Data + 480),
    (Layout.Font, Layout.Font + 3904)
  )

  private def ignored(a: Int): Boolean = Ignored.exists((s, e) => a >= s && a < e)

  def describe(address: Int): String =
    if address >= Layout.Bss then f"debut_bss+${address - Layout.Bss}%d ($address%x)"
    else if address >= Layout.Data then f"debut_data+${address - Layout.Data}%d ($address%x)"
    else f"$address%x"

  final case class Divergence(tick: Int, what: String)

  /** Inputs, one per vertical blank. */
  def run(
      inputs: Iterator[Input],
      maxTicks: Int,
      compareMemory: Boolean = true
  ): Option[Divergence] =
    val oracle = new OracleCore()
    val port = new Game()
    var tick = 0
    var result: Option[Divergence] = None
    val oSounds = mutable.ArrayBuffer.empty[Seq[Byte]]
    val pSounds = mutable.ArrayBuffer.empty[Seq[Byte]]
    while result.isEmpty && tick < maxTicks && inputs.hasNext do
      val in = inputs.next()
      oracle.step(in)
      port.step(in)
      oracle.drainSounds(s => oSounds += s.toSeq)
      port.drainSounds(s => pSounds += s.toSeq)
      result = compare(oracle, port, tick, oSounds.toSeq, pSounds.toSeq, compareMemory)
      oSounds.clear()
      pSounds.clear()
      tick += 1
    result

  private def compare(
      oracle: OracleCore,
      port: Game,
      tick: Int,
      oSounds: Seq[Seq[Byte]],
      pSounds: Seq[Seq[Byte]],
      compareMemory: Boolean
  ): Option[Divergence] =
    def d(what: String) = Some(Divergence(tick, what))
    if oracle.finished != port.finished then d(s"finished ${oracle.finished} ${port.finished}")
    else if oracle.palette != port.palette then d(s"palette ${oracle.palette} vs ${port.palette}")
    else if oSounds != pSounds then
      d(
        s"sounds ${oSounds.map(_.take(6).map(b => f"${b & 0xff}%02x").mkString)} vs " +
          s"${pSounds.map(_.take(6).map(b => f"${b & 0xff}%02x").mkString)}"
      )
    else
      val op = oracle.frame.pixels
      val pp = port.frame.pixels
      val firstPixel = op.indices.find(i => op(i) != pp(i))
      if firstPixel.isDefined then
        val i = firstPixel.get
        val count = op.indices.count(i => op(i) != pp(i))
        d(s"frame: $count pixels differ, first at (${i % 320}, ${i / 320})")
      else if compareMemory && port.inSync then
        val om = oracle.cpu.mem
        val pm = port.ram.bytes
        val diffs = (Layout.Data until Layout.End).filter(a => om(a) != pm(a) && !ignored(a))
        if diffs.nonEmpty then
          val shown =
            diffs.take(12).map(a => f"${describe(a)}: ${om(a) & 0xff}%02x/${pm(a) & 0xff}%02x")
          d(s"memory: ${diffs.size} bytes differ (oracle/port): ${shown.mkString(", ")}")
        else None
      else None

  /** A script: (number of ticks, joystick, key typed at the first tick). */
  def script(parts: (Int, Int, Int)*): Iterator[Input] =
    parts.iterator.flatMap((n, joy, key) =>
      Iterator.tabulate(n)(i => Input(joy, if i == 0 then key else 0))
    )

  /** Random play: holds random joystick states for random durations. */
  def random(seed: Long, ticks: Int, keys: Boolean = false): Iterator[Input] =
    val rnd = new scala.util.Random(seed)
    val choices = Array(
      0,
      Input.Left,
      Input.Right,
      Input.Up,
      Input.Down,
      Input.Fire,
      Input.Left | Input.Up,
      Input.Right | Input.Up,
      Input.Left | Input.Fire,
      Input.Right | Input.Fire,
      Input.Right,
      Input.Left,
      Input.Right
    )
    Iterator
      .continually {
        val joy = choices(rnd.nextInt(choices.length))
        val n = 1 + rnd.nextInt(40)
        val key = if keys && rnd.nextInt(30) == 0 then "0123456789p".charAt(rnd.nextInt(11)).toInt
        else 0
        Iterator.tabulate(n)(i => Input(joy, if i == 0 then key else 0))
      }
      .flatten
      .take(ticks)

  def main(args: Array[String]): Unit =
    val r = run(random(args.headOption.map(_.toLong).getOrElse(1L), 20000), 20000)
    println(r.getOrElse("no divergence"))

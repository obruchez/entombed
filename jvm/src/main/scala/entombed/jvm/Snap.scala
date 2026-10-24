package entombed.jvm

import entombed.core.{GameCore, Input}
import entombed.game.Game
import entombed.oracle.OracleCore

import java.io.File

/** Runs a scripted session and writes PNG snapshots.
  *
  * Usage: Snap <outDir> <script> where the script is a sequence of commands separated by spaces:
  * `w<n>` waits n ticks, `f` presses fire for 5 ticks, `u/d/l/r<n>` hold a direction n ticks,
  * `k<c>` types a key, `s<name>` writes a snapshot. The Scala port is used, or the original code
  * with `--oracle` as the second argument.
  */
object Snap:
  def main(args: Array[String]): Unit =
    val out = new File(args(0))
    val useOracle = args.lift(1).contains("--oracle")
    val core: GameCore = if useOracle then new OracleCore() else new Game()
    var tick = 0
    def run(n: Int, in: Input): Unit =
      for i <- 0 until n do
        core.step(if i == 0 then in else in.copy(key = 0))
        tick += 1
    for cmd <- args.drop(if useOracle then 2 else 1).flatMap(_.split("\\s+")).filter(_.nonEmpty) do
      def n = cmd.drop(1).toIntOption.getOrElse(1)
      cmd.head match
        case 'w' => run(n, Input.Empty)
        case 'f' => run(5, Input(Input.Fire, 0)); run(5, Input.Empty)
        case 'u' => run(n, Input(Input.Up, 0))
        case 'd' => run(n, Input(Input.Down, 0))
        case 'l' => run(n, Input(Input.Left, 0))
        case 'r' => run(n, Input(Input.Right, 0))
        case 'k' => run(1, Input(0, cmd.charAt(1).toInt))
        case 's' =>
          val f = new File(out, cmd.drop(1) + ".png")
          Png.write(core.frame, core.palette, f)
          println(s"tick $tick: wrote $f palette=${core.palette}")
        case other => throw new IllegalArgumentException(s"Unknown command $other")

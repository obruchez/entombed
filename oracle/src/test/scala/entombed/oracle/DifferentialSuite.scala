package entombed.oracle

import entombed.core.Input
import Differential.*

/** The Scala port must behave exactly like the original code: same frames, palettes, sounds and
  * memory after every vertical blank.
  */
class DifferentialSuite extends munit.FunSuite:
  override val munitTimeout = scala.concurrent.duration.Duration(600, "s")

  private def check(inputs: Iterator[Input], ticks: Int): Unit =
    run(inputs, ticks) match
      case Some(Divergence(tick, what)) => fail(s"tick $tick: $what")
      case None                         => ()

  test("title pages, best scores, instructions, back to the title") {
    check(script((3000, 0, 0)), 3000)
  }

  test("start a game, choose each tomb in turn") {
    check(
      script(
        (100, 0, 0),
        (5, Input.Fire, 0),
        (60, 0, 0),
        (3, Input.Down, 0),
        (20, 0, 0),
        (3, Input.Down, 0),
        (20, 0, 0),
        (3, Input.Up, 0),
        (20, 0, 0),
        (5, Input.Fire, 0),
        (300, 0, 0)
      ),
      600
    )
  }

  test("tomb 1: walk, jump, fall, climb") {
    check(
      script(
        (100, 0, 0),
        (5, Input.Fire, 0),
        (60, 0, 0),
        (5, Input.Fire, 0),
        (80, 0, 0),
        (100, Input.Right, 0),
        (30, Input.Up, 0),
        (100, Input.Left, 0),
        (50, Input.Right | Input.Up, 0),
        (100, Input.Right, 0),
        (60, Input.Down, 0),
        (200, Input.Right, 0)
      ),
      900
    )
  }

  for seed <- 1 to 4 do
    test(s"random play, seed $seed") {
      val prefix = script(
        (100, 0, 0),
        (5, Input.Fire, 0),
        (60, 0, 0),
        (3 * (seed % 4), Input.Down, 0),
        (20, 0, 0),
        (5, Input.Fire, 0),
        (80, 0, 0)
      )
      check(prefix ++ random(seed, 6000, keys = true), 6300)
    }

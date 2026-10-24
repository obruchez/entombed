package entombed.oracle

import entombed.game.{Game, Vars}

/** Prints the objects of each tomb (debugging). */
object TombStats:
  def main(args: Array[String]): Unit =
    val g = new Game()
    g.step(entombed.core.Input.Empty) // initialisation
    for t <- 0 to 4 do
      g.ram.setW(Vars.TombNumber, t)
      g.readTombDatas()
      val counts = scala.collection.mutable.Map.empty[Int, Int].withDefaultValue(0)
      var a = Vars.B205312
      val end = g.ram.ptr(Vars.D542)
      val sizes = Map(
        0 -> 0x1e,
        1 -> 0x18,
        2 -> 0x1e,
        3 -> 0x26,
        4 -> 0x22,
        5 -> 0x24,
        6 -> 0x28,
        7 -> 0x26,
        8 -> 0x1a
      )
      while a < end do
        val tag = g.ram.l(a + 6) - Vars.Raft
        counts(tag) += 1
        a += sizes(tag)
      val names = Vector(
        "raft",
        "vase",
        "shooter",
        "dragon",
        "stone",
        "pharaoh",
        "snake",
        "killing stones",
        "sphere"
      )
      println(
        s"tomb ${t + 1}: " + counts.toSeq.sorted.map((k, n) => s"$n ${names(k)}").mkString(", ")
      )

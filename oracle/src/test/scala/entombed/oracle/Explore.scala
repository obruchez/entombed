package entombed.oracle

import entombed.game.{Game, Layout, Vars}

import scala.collection.mutable

/** Long differential runs with coverage statistics (not a unit test: run with
  * `oracleJVM/Test/runMain entombed.oracle.Explore <firstSeed> <seeds> <ticks>`).
  */
object Explore:
  def main(args: Array[String]): Unit =
    val first = args.lift(0).map(_.toInt).getOrElse(1)
    val count = args.lift(1).map(_.toInt).getOrElse(10)
    val ticks = args.lift(2).map(_.toInt).getOrElse(20000)
    val sounds = mutable.Set.empty[Int]
    val tags = mutable.Set.empty[Int]
    var deaths = 0
    var balls = 0
    var gameOvers = 0
    var maxPoints = 0
    val tombs = mutable.Set.empty[Int]
    for seed <- first until first + count do
      val oracle = new OracleCore()
      val port = new Game()
      val inputs = Differential.random(seed, ticks, keys = true)
      val rnd = new scala.util.Random(seed * 7919L)
      // Same changes in both memories, at synchronisation points, to reach more of the game
      def write(addr: Int, size: Int, v: Int): Unit =
        for (mem <- Seq(oracle.cpu.mem, port.ram.bytes); i <- 0 until size) do
          mem(addr + i) = (v >> (8 * (size - 1 - i))).toByte
      if seed % 3 == 0 || args.contains("tomb5") then write(Vars.WichTombPlayed, 2, 0xf)
      var tick = 0
      var lives = -1
      var failed = false
      while !failed && inputs.hasNext do
        val in = inputs.next()
        oracle.step(in)
        port.step(in)
        oracle.drainSounds(_ => ())
        port.drainSounds(_ => ())
        val m = port.ram
        val traceFrom =
          args.find(_.startsWith("trace=")).map(_.drop(6).toInt).getOrElse(Int.MaxValue)
        if tick >= traceFrom then
          println(
            f"tick $tick in=${in.joystick}%02x/${in.key}%02x oracle frame ${oracle.frame.hash}%08x " +
              f"pal ${oracle.palette.words(15)}%03x line ${oracle.cpu.currentLine} | port frame " +
              f"${port.frame.hash}%08x pal ${port.palette.words(15)}%03x state ${port.stateNumber} sync ${port.inSync}"
          )
        val playing = port.stateNumber == entombed.game.Flow.PostSwap && m.w(Vars.StateMan) != 4
        if port.inSync && playing && tick % 50 == 0 then
          write(Vars.NbArc, 2, 1 + rnd.nextInt(3))
          write(Vars.NbFleches, 2, 12)
          write(Vars.NbCle, 2, 4)
          write(Vars.NbSeconds, 2, 20)
          if rnd.nextInt(4) == 0 then write(Vars.NbLifes, 2, 7)
          if rnd.nextInt(3) == 0 then
            // Teleport, as the spheres do
            val x = rnd.nextInt(m.w(Vars.TombWord2) + 0x120) & ~1
            val y = rnd.nextInt(m.w(Vars.TombWord3) + 0x90) & ~3
            write(Vars.TombWord0, 2, x)
            write(Vars.TombWord1, 2, y)
            val px = port.clampScroll(x, 0x90, m.w(Vars.TombWord2))
            val py = port.clampScroll(y, 0x54, m.w(Vars.TombWord3))
            write(Vars.PosInTombX, 2, px)
            write(Vars.PosInTombY, 2, py)
            write(Vars.D510, 2, px + 0x80)
            write(Vars.D512, 2, px + 0xa0)
            write(Vars.D514, 2, py + 0x48)
            write(Vars.D516, 2, py + 0x60)
          if rnd.nextInt(40) == 0 then write(Vars.NbPoints, 4, 0x123456)
          if args.contains("switches") then for i <- 0 until 256 do write(Vars.B213760 + i, 1, 1)
          if rnd.nextInt(60) == 0 then write(Vars.ExitFound, 2, 1)
        if port.inSync then
          val om = oracle.cpu.mem
          val diff = (Layout.Data until Layout.End).find(a =>
            om(a) != m.bytes(a) && !Differential.Ignored.exists((s, e) => a >= s && a < e)
          )
          if diff.isDefined then
            println(s"seed $seed tick $tick: memory differs at ${Differential.describe(diff.get)}")
            failed = true
        if !java.util.Arrays.equals(oracle.frame.pixels, port.frame.pixels) then
          val op = oracle.frame.pixels
          val pp = port.frame.pixels
          val idx = op.indices.filter(i => op(i) != pp(i))
          val xs = idx.map(_ % 320)
          val ys = idx.map(_ / 320)
          println(
            s"seed $seed tick $tick: frame differs, ${idx.size} pixels in " +
              s"x ${xs.min}-${xs.max}, y ${ys.min}-${ys.max}; state ${m.w(Vars.StateMan)}, " +
              s"man (${m.w(Vars.TombWord0)}, ${m.w(Vars.TombWord1)}), " +
              s"pos (${m.w(Vars.PosInTombX)}, ${m.w(Vars.PosInTombY)}), inSync ${port.inSync}"
          )
          for (name, f) <- Seq("oracle" -> oracle.frame, "port" -> port.frame) do
            val img =
              new java.awt.image.BufferedImage(320, 200, java.awt.image.BufferedImage.TYPE_INT_RGB)
            val argb = new Array[Int](64000)
            f.toArgb(if name == "oracle" then oracle.palette else port.palette, argb)
            img.setRGB(0, 0, 320, 200, argb, 0, 320)
            javax.imageio.ImageIO.write(
              img,
              "png",
              new java.io.File(
                s"/private/tmp/claude-501/-Users-olivierbruchez-Github-entombed/73b40780-5b1d-4492-8f44-248113a1e9ed/scratchpad/explore-$seed-$name.png"
              )
            )
          failed = true
        val l = m.w(Vars.NbLifes)
        if lives >= 0 && l < lives then deaths += 1
        if lives > 1 && l == 7 && m.w(Vars.FirstPageDone) == 0 then gameOvers += 1
        lives = l
        sounds += m.b(Layout.SoundNumber)
        tombs += m.w(Vars.TombNumber)
        maxPoints = math.max(maxPoints, m.l(Vars.NbPoints))
        var a = m.l(Vars.D4810)
        var guard = 0
        while a != -1 && a != 0 && guard < 200 do
          tags += m.l(a + 6) - Vars.Raft
          a = m.l(a)
          guard += 1
        var p = m.l(Vars.D552)
        while p != -1 && p != 0 do
          if m.w(p + 0x14) == 3 then balls += 1
          p = m.l(p)
        tick += 1
      if !failed then println(s"seed $seed: $tick ticks identical (ball frames so far: $balls)")
    println(
      s"deaths $deaths, game overs ~$gameOvers, tombs ${tombs.toSeq.sorted}, " +
        f"max points $maxPoints%x, ball frames $balls, sounds ${sounds.toSeq.sorted}, object types ${tags.toSeq.sorted}"
    )

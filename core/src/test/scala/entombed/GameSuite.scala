package entombed

import entombed.core.Input
import entombed.driver.GameDriver
import entombed.game.{Game, Vars}
import entombed.save.InMemoryStorage

class GameSuite extends munit.FunSuite:

  /** Title, fire, tomb 1, then the man walks right and jumps. */
  private val script: Vector[Input] =
    Vector.fill(100)(Input.Empty) ++ Vector.fill(5)(Input(Input.Fire, 0)) ++
      Vector.fill(60)(Input.Empty) ++ Vector.fill(5)(Input(Input.Fire, 0)) ++
      Vector.fill(80)(Input.Empty) ++ Vector.fill(150)(Input(Input.Right, 0)) ++
      Vector.fill(30)(Input(Input.Up | Input.Right, 0)) ++ Vector.fill(100)(Input(Input.Left, 0))

  private def run(g: Game, inputs: Seq[Input]): Unit = inputs.foreach(g.step)

  test("the port is deterministic") {
    val a = new Game()
    val b = new Game()
    run(a, script)
    run(b, script)
    assertEquals(a.frame.hash, b.frame.hash)
    assertEquals(a.ram.bytes.toSeq, b.ram.bytes.toSeq)
  }

  test("a snapshot taken at any moment restores exactly") {
    for cut <- Seq(50, 180, 250, 300) do
      val reference = new Game()
      run(reference, script)
      val first = new Game()
      run(first, script.take(cut))
      val snap = first.snapshot()
      val second = new Game()
      second.restore(snap)
      run(second, script.drop(cut))
      assertEquals(second.frame.hash, reference.frame.hash, s"cut at $cut")
      assertEquals(second.ram.bytes.toSeq, reference.ram.bytes.toSeq, s"cut at $cut")
  }

  test("the game reaches tomb 1 and the man moves") {
    val g = new Game()
    run(g, script.take(420))
    assertEquals(g.ram.w(Vars.TombChosenFlag), 1)
    assertEquals(g.ram.w(Vars.TombNumber), 0)
    assertNotEquals(g.ram.w(Vars.TombWord0), 416)
  }

  test("god mode: the man never dies") {
    val g = new Game()
    g.godMode = true
    run(g, script.take(250))
    val rnd = new scala.util.Random(3)
    for _ <- 0 until 5000 do
      g.step(Input(Seq(0, 1, 4, 8, 5, 9, 0x80)(rnd.nextInt(7)), 0))
      assertNotEquals(g.ram.w(Vars.StateMan), 4)
    assertEquals(g.ram.w(Vars.NbLifes), 7)
  }

  test("the driver saves and loads the complete state") {
    val storage = new InMemoryStorage
    val d = new GameDriver(storage, GameDriver.port)
    d.originalSpeed = false
    script.take(300).foreach(d.step)
    d.save(1)
    val saved = d.frame.hash
    script.drop(300).foreach(d.step)
    assertNotEquals(d.frame.hash, saved)
    d.load(1)
    assertEquals(d.game.asInstanceOf[Game].ram.w(Vars.TombChosenFlag), 1)
    val again = new GameDriver(storage, GameDriver.port)
    again.originalSpeed = false
    again.load(1)
    script.drop(300).foreach(again.step)
    script.drop(300).foreach(d.step)
    assertEquals(again.frame.hash, d.frame.hash)
  }

  test("Atari ST speed: a game frame every 3 vertical blanks") {
    val d = new GameDriver(new InMemoryStorage, GameDriver.port)
    script.take(300).foreach(d.step)
    val g = d.game.asInstanceOf[Game]
    assertEquals(g.ram.w(Vars.TombChosenFlag), 1)
    val before = g.ticks
    for _ <- 0 until 300 do d.step(Input.Up)
    assertEquals(g.ticks - before, 100L)
  }

  test("the 1995 save game (keys S and L) restores the tomb") {
    val storage = new InMemoryStorage
    val g = Game(storage)
    run(g, script.take(300))
    g.step(Input(0, 's'))
    g.step(Input.Empty)
    assert(storage.read(Game.SaveSlot).isDefined)
    val x = g.ram.w(Vars.TombWord0)
    run(g, Vector.fill(60)(Input(Input.Left, 0)))
    assertNotEquals(g.ram.w(Vars.TombWord0), x)
    g.step(Input(0, 'l'))
    g.step(Input.Empty)
    g.step(Input.Empty)
    assertEquals(g.ram.w(Vars.TombWord0), x)
  }

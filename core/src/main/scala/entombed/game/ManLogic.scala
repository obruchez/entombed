package entombed.game

import M68.*
import Vars.*

/** The man: movements, collisions and objects picked up (`reference/smaky/STOMBED1.L`, lines
  * 1512-2562).
  *
  * `joy` is the joystick byte read at the beginning of the frame (register D7 of the original).
  */
trait ManLogic:
  this: Game =>

  private def attr(block: Int): Int = ram.b(AttributesTable + block)

  /** `L00054` (1512): the man, once per frame. */
  def l00054(): Unit =
    if ram.w(StateMan) == 4 then
      // Dying: goes up, then falls out of the screen
      ram.setW(AnimManCounter, w(ram.w(AnimManCounter) + 1))
      val c = w(ram.w(MoveManCounter) + 1)
      ram.setW(MoveManCounter, c)
      if c < 4 then ram.setW(TombWord1, w(ram.w(TombWord1) - 4))
      else ram.setW(TombWord1, w(ram.w(TombWord1) + 4))
    else
      getJoystick()
      var joy = ram.b(JoystickState)
      if (joy & 4) != 0 then ram.setW(DirectionMan, 0)
      else if (joy & 8) != 0 then ram.setW(DirectionMan, 1)
      var checkFire = true
      if ram.w(TirEnCoursFlag) != 0 then
        if ram.w(AnimManCounter) != 3 then
          ram.setW(AnimManCounter, w(ram.w(AnimManCounter) + 1))
          joy = 0
          checkFire = false
        else
          // The arrow leaves the bow
          doProject(ram.w(DirectionMan), 1, ram.w(TombWord0), ram.w(TombWord1))
          prepareSound(0)
          ram.setW(TirEnCoursFlag, 0)
          ram.setW(NbFlechesTirees, w(ram.w(NbFlechesTirees) + 1))
          ram.setW(NbFleches, w(ram.w(NbFleches) - 1))
      if checkFire && ram.w(NbFlechesTirees) != 3 && ram.w(NbFleches) != 0 &&
        ram.w(NbArc) != 0 && (joy & 0xf0) != 0 && ram.w(StateMan) != 3
      then
        ram.setW(TirEnCoursFlag, 1)
        ram.setW(AnimManCounter, 0)
        joy = 0
      ram.w(StateMan) match
        case 0 => gereMouvements(joy)
        case 2 => jumpingMan(joy)
        case 1 => fallingMan(joy)
        case 3 => ladderingMan(joy)
        case _ => ()

  /** `gere_mouvements` (1597): the man on the ground. */
  private def gereMouvements(joy: Int): Unit =
    if ram.w(StateMan) == 4 then return
    if ram.w(TirEnCoursFlag) == 0 then
      getJoystick()
      if (ram.b(JoystickState) & 0xf) == 0 then ram.setW(AnimManCounter, 0)
    val raft = ram.ptr(D4988)
    var onGround = false
    if raft != 0 && onRaft(raft) then
      ram.setW(D4992, ram.w(raft + 0x16))
      onGround = true
    else
      ram.setL(D4988, 0)
      ram.setW(D4992, 0)
      val w0 = ram.w(TombWord0)
      val w1 = ram.w(TombWord1)
      if (attr(ram.b(cellAt(w(w0 + 6), w(w1 + 0x18)))) & 0x20) != 0 ||
        (attr(ram.b(cellAt(w(w0 + 0x13), w(w1 + 0x18)))) & 0x20) != 0
      then onGround = true
      else
        // No ground: is there a raft under the feet?
        var a0 = ram.l(D4810)
        while !onGround && a0 != -1 do
          if ram.l(a0 + 6) == Raft && onRaft(a0) then
            ram.setL(D4988, a0)
            ram.setW(D4992, ram.w(a0 + 0x16))
            onGround = true
          else a0 = ram.l(a0)
    if !onGround then
      ram.setW(StateMan, 1)
      fallingMan(joy)
    else if (joy & 1) != 0 then
      // Up: climb a ladder or jump
      if ram.b(cellAt(w(ram.w(TombWord0) + 0xc), w(ram.w(TombWord1) + 0xf))) == 0x1f then
        ram.setW(StateMan, 3)
        ladderingMan(joy)
      else
        ram.setW(StateMan, 2)
        ram.setW(NbToJump, 7)
        prepareSound(0xe)
        jumpingMan(joy)
    else
      var d5 = ram.w(TombWord0)
      val d6 = ram.w(TombWord1)
      if (joy & 4) != 0 then
        d5 = w(d5 - 2)
        walkStep()
      else if (joy & 8) != 0 then
        d5 = w(d5 + 2)
        walkStep()
      d5 = w(d5 + ram.w(D4992))
      moveMan(d5, d6, 1)

  private def walkStep(): Unit =
    if (ram.b(ManSynchro + 1) & 1) != 0 then
      ram.setW(AnimManCounter, w(ram.w(AnimManCounter) + 1))
      if (ram.w(ManSynchro) & 7) == 7 then prepareSound(0xc) // footsteps

  /** `L001E7` (1754): is the man standing on the raft `a0`? */
  private def onRaft(a0: Int): Boolean =
    val rx = ram.w(a0 + 0x1a)
    if w(ram.w(TombWord1) + 0x18) != ram.w(a0 + 0x1c) then false
    else
      val l = w(ram.w(TombWord0) + 6)
      if l >= rx then w(l - 0x20) < rx
      else
        val r = w(ram.w(TombWord0) + 0x13)
        r >= rx && w(r - 0x20) < rx

  /** `jumping_man` (1784). */
  private def jumpingMan(joy: Int): Unit =
    if ram.w(StateMan) == 4 then return
    ram.setL(D4988, 0)
    ram.setW(D4992, 0)
    val n = w(ram.w(NbToJump) - 1)
    if n == 0 then
      ram.setW(StateMan, 1)
      fallingMan(joy)
    else
      ram.setW(NbToJump, n)
      if (joy & 3) != 0 && ladderHere() then
        ram.setW(StateMan, 3)
        ladderingMan(joy)
      else
        var d5 = ram.w(TombWord0)
        var d6 = ram.w(TombWord1)
        var diagonal = true
        if (joy & 4) != 0 then d5 = w(d5 - 2)
        else if (joy & 8) != 0 then d5 = w(d5 + 2)
        else diagonal = false
        var finished = false
        if diagonal then
          d6 = w(d6 - 4)
          val moved = moveMan(d5, d6, 1)
          if ram.w(StateMan) == 4 || moved then finished = true
          else
            d5 = ram.w(TombWord0)
            d6 = ram.w(TombWord1)
        if !finished then
          d6 = w(d6 - 4)
          val moved = moveMan(d5, d6, 1)
          if ram.w(StateMan) != 4 && !moved then
            ram.setW(StateMan, 1)
            fallingMan(joy)

  private def ladderHere(): Boolean =
    ram.b(cellAt(w(ram.w(TombWord0) + 0xc), w(ram.w(TombWord1) + 0xf))) == 0x1f

  /** `falling_man` (1857). */
  private def fallingMan(joy: Int): Unit =
    if ram.w(StateMan) == 4 then return
    ram.setL(D4988, 0)
    ram.setW(D4992, 0)
    val d3 = if (w(ram.w(TombWord1) + 0x17 + 4) & 0xf) + 1 == 4 then 5 else 1
    if (joy & 3) != 0 && ladderHere() then
      ram.setW(StateMan, 3)
      ladderingMan(joy)
    else
      var d5 = ram.w(TombWord0)
      var d6 = ram.w(TombWord1)
      var diagonal = true
      if (joy & 4) != 0 then d5 = w(d5 - 2)
      else if (joy & 8) != 0 then d5 = w(d5 + 2)
      else diagonal = false
      var result = -1 // -1: continue, 0: landed, 1: still falling
      if diagonal then
        d6 = w(d6 + 4)
        val moved = moveMan(d5, d6, d3)
        if ram.w(StateMan) == 4 then result = 2
        else if moved then result = 1
        else
          d5 = ram.w(TombWord0)
          d6 = ram.w(TombWord1)
      if result == -1 then
        d6 = w(d6 + 4)
        val moved = moveMan(d5, d6, d3)
        result = if ram.w(StateMan) == 4 then 2 else if moved then 1 else 0
      if result == 0 then
        ram.setW(StateMan, 0)
        gereMouvements(joy)
      else if result == 1 then
        // L0020B: falling onto a raft?
        var a0 = ram.l(D4810)
        var landed = false
        while !landed && a0 != -1 do
          if ram.l(a0 + 6) == Raft && onRaft(a0) then
            ram.setL(D4988, a0)
            ram.setW(D4992, ram.w(a0 + 0x16))
            ram.setW(StateMan, 0)
            landed = true
          else a0 = ram.l(a0)
        if landed then gereMouvements(joy)

  /** `laddering_man` (1934). */
  private def ladderingMan(joy: Int): Unit =
    if ram.w(StateMan) == 4 then return
    ram.setL(D4988, 0)
    ram.setW(D4992, 0)
    var d3 = 5
    if !ladderHere() then
      ram.setW(StateMan, 1)
      fallingMan(joy)
    else
      var d5 = ram.w(TombWord0)
      var d6 = ram.w(TombWord1)
      if (joy & 1) != 0 then
        if ram.b(cellAt(w(d5 + 0xc), w(d6 + 0xb))) == 0x1f then
          d6 = w(d6 - 4)
          d3 = 1
      else if (joy & 2) != 0 then
        d6 = w(d6 + 4)
        d3 = 1
        if (w(d6 + 0x17) & 0xf) + 1 == 4 then d3 = 5
      if (joy & 4) != 0 then d5 = w(d5 - 2)
      else if (joy & 8) != 0 then d5 = w(d5 + 2)
      if (joy & 0xf) != 0 then
        ram.setW(AnimManCounter, w(ram.w(AnimManCounter) + 1))
        if (ram.b(ManSynchro + 1) & 1) != 0 then prepareSound(0xd) // footsteps on the ladder
      val moved = moveMan(d5, d6, d3)
      if ram.w(StateMan) != 4 && !moved then moveMan(ram.w(TombWord0), d6, d3)

  /** `move_man` (2016): moves the man to (d5, d6) if possible. The 6 cells touched (at x+6 and
    * x+19, y, y+15 and y+23) are recorded at D4842; doors are opened with keys; the screen scrolls
    * when needed. Returns true if the man has moved.
    */
  def moveMan(d5start: Int, d6start: Int, d3: Int): Boolean =
    var d5 = d5start
    var d6 = d6start
    var result = false
    var again = true
    while again do
      again = false
      var flags = 0
      var a2 = D4842
      for (dx, dy, k) <- MoveManProbes do
        val x = w(d5 + dx)
        val a0 = cellAt(x, w(d6 + dy))
        var d4 = ram.b(a0)
        if d4 == 0x20 && (x & 0x1f) >= 0xa then d4 = 0x18 // an open door's frame
        ram.setW(a2, d4)
        ram.setL(a2 + 2, a0)
        a2 += 6
        if k < 4 then flags |= attr(d4)
        else if ((attr(d4) >> (d3 & 7)) & 1) != 0 then flags = 0xff
      // The same cell must not be counted twice
      val p = D4842
      def copy(to: Int, from: Int): Unit =
        ram.setW(p + 6 * to, ram.w(p + 6 * from))
        ram.setL(p + 6 * to + 2, ram.l(p + 6 * from + 2))
      def addr(i: Int): Int = ram.l(p + 6 * i + 2)
      if addr(0) == addr(1) then
        copy(1, 2)
        copy(2, 4)
        ram.setW(D4878, 2)
        if addr(1) == addr(0) then
          copy(1, 2)
          ram.setW(D4878, 1)
        else if addr(1) == addr(2) then ram.setW(D4878, 1)
      else
        ram.setW(D4878, 5)
        if addr(2) == addr(0) then
          copy(2, 4)
          copy(3, 5)
          ram.setW(D4878, 3)
        else if addr(2) == addr(4) then ram.setW(D4878, 3)
      // Doors
      if (flags & 2) == 0 then
        var i = 0
        val n = ram.w(D4878)
        var done = false
        while !done && i <= n do
          if ram.w(p + 6 * i) == 0x20 then
            done = true
            if ram.w(NbCle) == 0 then flags = 0xff
            else
              prepareSound(2) // door
              ram.setW(NbCle, w(ram.w(NbCle) - 1))
              val a0 = addr(i) & 0xffffff
              ram.setB(a0, 0x21)
              if ram.b(a0 + 0x50) == 0x20 then ram.setB(a0 + 0x50, 0x21)
              else ram.setB(a0 - 0x50, 0x21)
          i += 1
      if (flags & 2) != 0 && ram.w(MoveManFlag) == 0 then
        // Blocked: stay where he is (and touch what is there)
        d5 = ram.w(TombWord0)
        d6 = ram.w(TombWord1)
        ram.setW(MoveManFlag, 1)
        again = true
      else
        if (flags & 2) == 0 then scrollAndMove(d5, d6)
        testTouchedObjects()
        result = (flags & 2) == 0 && ram.w(MoveManFlag) == 0
        ram.setW(MoveManFlag, 0)
    result

  /** (dx, dy, kind) of the 6 cells tested by move_man; kinds 4-5 test the bit d3 (ground). */
  private val MoveManProbes =
    Seq((6, 0, 0), (0x13, 0, 1), (6, 0xf, 2), (0x13, 0xf, 3), (6, 0x17, 4), (0x13, 0x17, 5))

  /** `L0022C` (2207): the man moves to (d5, d6); the screen scrolls if he leaves the central area.
    */
  private def scrollAndMove(d5: Int, d6: Int): Unit =
    val dx = w(d5 - ram.w(TombWord0))
    val dy = w(d6 - ram.w(TombWord1))
    val px = ram.w(PosInTombX)
    val py = ram.w(PosInTombY)
    if d5 < ram.w(D510) then
      if px >= 2 then ram.setW(D518, w(px + dx))
    else if d5 >= ram.w(D512) then if px < ram.w(TombWord2) then ram.setW(D518, w(px + dx))
    if d6 < ram.w(D514) then
      if py >= 2 then ram.setW(D520, w(py + dy))
    else if d6 >= ram.w(D516) then if py < ram.w(TombWord3) then ram.setW(D520, w(py + dy))
    ram.setW(TombWord0, d5)
    ram.setW(TombWord1, d6)
    val nx = ram.w(D518)
    ram.setW(D522, w(nx + 0x80))
    ram.setW(D524, w(nx + 0xa0))
    val ny = ram.w(D520)
    ram.setW(D526, w(ny + 0x48))
    ram.setW(D528, w(ny + 0x60))

  /** Entry of tomb_table3 for the cell at `a2`: a pointer into the tomb data or a record. */
  private def table3(a2: Int): Int = TombTable3 + sw(w((a2 - TombTable1) << 2))

  /** `test_touched_objects` (2284). */
  def testTouchedObjects(): Unit =
    var a0 = D4842
    var n = ram.w(D4878)
    var dead = false
    while !dead && n != 0xffff do
      val d0 = ram.w(a0)
      val a2 = ram.l(a0 + 2) & 0xffffff
      a0 += 6
      val at = attr(d0 & 0xff)
      if (at & 8) != 0 then
        // Fire, water, spikes...
        if killMan() then dead = true
      else if (at & 0x40) != 0 then touch(d0 & 0xff, a2)
      n = w(n - 1)

  private def touch(code: Int, a2: Int): Unit = code match
    case 0x2c => // Chest: opened with a key, gives its next object
      if ram.w(NbCle) != 0 then
        ram.setW(NbCle, w(ram.w(NbCle) - 1))
        val a3 = table3(a2)
        val a4 = ram.ptr(a3)
        val c = ram.b(a4)
        ram.setL(a3, a4 + 1)
        var t = D880
        var d1 = ram.w(D878)
        var found = false
        while !found do
          val v = ram.b(t)
          t += 1
          if v == c then found = true
          else
            t += 1
            d1 = w(d1 - 1)
            if d1 == 0xffff then found = true
        ram.setB(a2, ram.b(t))
    case 0x3f => // Extra life
      if ram.w(NbLifes) != 7 then
        ram.setW(NbLifes, w(ram.w(NbLifes) + 1))
        ram.setB(a2, 0x18)
        prepareSound(6)
    case 0x1b => // Key
      if ram.w(NbCle) != 4 then
        prepareSound(5)
        ram.setB(a2, 0x18)
        ram.setW(NbCle, w(ram.w(NbCle) + 1))
    case 0x28 => // Bow
      if ram.w(NbArc) != 3 then
        prepareSound(7)
        ram.setB(a2, 0x18)
        ram.setW(NbArc, w(ram.w(NbArc) + 1))
    case 0x36 => // Gold coin
      prepareSound(4)
      ram.setB(a2, 0x18)
      val v = ram.b(ram.ptr(table3(a2))) & 3
      ram.setW(PointsToAdd, v)
      addPoints(ram.l(D5032 + 4 * v))
      val index = a2 - TombTable1
      val row = index / 0x50
      val y = w(row << 4)
      ram.setW(D5094, y)
      ram.setW(D5092, w(w(index - w(y * 5)) << 5))
      ram.setW(D5096, 0x10)
    case 0x2e => // Hourglass
      prepareSound(6)
      ram.setB(a2, 0x18)
      ram.setW(NbSeconds, w(ram.w(NbSeconds) + ram.b(ram.ptr(table3(a2)))))
    case 0x2f => // Checkpoint, multiple-action point or switch
      val a3 = ram.ptr(table3(a2))
      val d0 = ram.b(a3)
      if d0 == 0xff then
        ram.setB(a2, 0x18)
        prepareSound(4)
        ram.setL(NonRetourAdr, a2)
        addPoints(ram.b(a3 + 1 + 0xd) << 12)
      else if d0 == 0xfe then
        val d1 = w(ram.w(FixationCounter) + 1)
        if (d1 & 0xff) == ram.b(a3 + 1) && ram.w(ram.ptr(D874)) == 0xffff then
          ram.setW(FixationCounter, ram.b(a3 + 1))
          ram.setL(D874, ram.l(D874) + 2)
          ram.setW(FixationSynchro, 0x8000)
      else ram.setB(B213760 + d0, ram.b(a3 + 1))
    case 0x29 => // Arrows
      if ram.w(NbFleches) != 0xc then
        prepareSound(7)
        ram.setB(a2, 0x18)
        val n = w(ram.w(NbFleches) + ram.b(ram.ptr(table3(a2))))
        ram.setW(NbFleches, if n >= 0xd then 0xc else n)
    case 0x33 | 0x34 => ram.setW(ExitFound, 1) // The exit
    case _           => ()

package entombed.game

import M68.*
import Vars.*

/** The objects of the tombs (`reference/smaky/STOMBED1.L`, lines 4064-4879).
  *
  * Objects are records in the BSS (see [[TombLoader]]), linked in a list of active objects (pointer
  * at D4810, -1 terminated). A record starts with the link (long), a "visible" flag (word at 4), a
  * type tag (long at 6: $ABCDEFF0 + type) and the address of its cell (long at $A); its position is
  * at $E (x) and $10 (y).
  *
  * While the list is walked, `a5` is the previous record (-1 at the start) and `a6` the current
  * one, as in the original; each routine moves to the next record or unlinks the current one.
  */
trait Objects:
  this: Game =>

  protected var a5: Int = -1
  protected var a6: Int = -1

  private inline def fw(off: Int): Int = ram.w(a6 + off)
  private inline def setFw(off: Int, v: Int): Unit = ram.setW(a6 + off, v)

  /** `L00055` (4064): objects whose cell is visible become active (inserted in the list). */
  def l00055(): Unit =
    var a0 = ram.ptr(D4806)
    for _ <- 0 to 0xc do
      for _ <- 0 to 0xa do
        val c = ram.b(a0)
        a0 += 1
        if (ram.b(AttributesTable + c) & 4) != 0 then
          val entry = TombTable3 + w((a0 - (Layout.Bss + 174081)) << 2)
          val a2 = ram.ptr(entry)
          if a2 != 0 then
            if ram.l(a2) == 0 then
              ram.setL(a2, ram.l(D4810))
              ram.setL(D4810, a2)
            ram.setW(a2 + 4, 1)
      a0 += 0x45

  /** `L00056` (4093): runs the routine of each active object. */
  def l00056(): Unit =
    a5 = -1
    a6 = ram.l(D4810)
    while a6 != -1 && !finished do
      val tag = ram.l(a6 + 6)
      if (tag & 0xfffffff0) != Raft then quit() // the 1995 safety check
      else
        (tag - Raft) match
          case 0 => doRadeau()
          case 1 => breakVase()
          case 2 => projectFromWall(0)
          case 3 => projectFromWall(3)
          case 4 => l002C9()
          case 5 => enemy(isSnake = false)
          case 6 => enemy(isSnake = true)
          case 7 => affKillingStones()
          case 8 => affSphere()
          case _ => quit()

  /** `L0033F` (4132): removes the current object from the list. */
  protected def unlink(): Unit =
    val a4 = a6
    a6 = ram.l(a6)
    ram.setL(a4, 0)
    if a5 == -1 then ram.setL(D4810, a6) else ram.setL(a5, a6)

  /** Keeps the current object and goes to the next one. */
  private def keep(): Unit =
    setFw(4, 0)
    a5 = a6
    a6 = ram.l(a6)

  private def switchOn(index: Int): Boolean = (index & 0xff) == 0xff || ram.b(B213760 + index) != 0

  /** `project_from_wall` (4146) and `project_from_dragoon` (4196): shooters. `kind` is the
    * projectile type (0 from a wall, 3 a ball falling from a dragon).
    */
  private def projectFromWall(kind: Int): Unit =
    if fw(4) == 0 then unlink()
    else
      if switchOn(fw(0x12)) then
        val t = fw(0x18)
        if t != 0 then setFw(0x18, (t & 0xff00) | b(t - 1))
        else
          setFw(0x18, fw(0x14))
          val shots = ram.b(a6 + 0x1d)
          if shots != 0 then
            if (shots & 0x80) == 0 then setFw(0x1c, w(fw(0x1c) - 1))
            doProject(fw(0x16), kind, fw(0xe), fw(0x10))
            prepareSound(8)
      val d1 = fw(0x1a)
      if (d1 & 0xff) != 0xff then ram.setB(B213760 + fw(0x12), d1)
      keep()

  /** `do_radeau` (4245): a raft going back and forth. */
  private def doRadeau(): Unit =
    val visible = affRadeauBille(0, fw(0x1a), fw(0x1c))
    if !visible && fw(4) == 0 then
      setFw(0x1a, fw(0xe))
      setFw(0x1c, fw(0x10))
      unlink()
    else
      var d0 = w(fw(0x1a) + fw(0x16))
      if neg16(d0) || d0 < fw(0x12) || d0 >= fw(0x14) then
        val s = fw(0x18)
        setFw(0x18, fw(0x16))
        setFw(0x16, s)
        d0 = w(s + fw(0x1a))
      setFw(0x1a, d0)
      keep()

  /** Is the man inside the box [x0, x1] x [y0, y1] (relative to the man: x+8 to x+19, y to y+23)?
    * The tests of the original, all unsigned.
    */
  private def touchesMan(xMin: Int, xMax: Int, yMin: Int, yMax: Int): Boolean =
    val mx = w(ram.w(TombWord0) + 8)
    val my = ram.w(TombWord1)
    !(w(xMax) < mx || w(mx + 0xb) < w(xMin) || w(yMax) < my || w(my + 0x17) < w(yMin))

  private def kill(): Unit = killMan()

  /** `pharaon_killed` (4285) / `serpent_killed` (4639): pharaohs and snakes. */
  private def enemy(isSnake: Boolean): Unit =
    val hitField = if isSnake then 0x24 else 0x20
    val healthField = if isSnake then 0x26 else 0x22
    var d0 = w(w(fw(0x1a) << 2) + fw(0x1e) + (if isSnake then 0x24 else 0x1c))
    if (ram.b(ManSynchro + 1) & 1) != 0 then
      setFw(0x1e, (fw(0x1e) + 1) & (if isSnake then 1 else 3))
    val d1 = fw(0x12)
    val d2 = fw(0x14)
    var visible = false
    var continue = true
    if fw(hitField) == 0 then visible = affBloc32x24(d0, d1, d2)
    else
      visible = affBloc32x24White(d0, d1, d2)
      val hit = fw(hitField)
      setFw(hitField, 0)
      val health = fw(healthField)
      setFw(healthField, w(health - hit))
      if lowerU16(health, hit) then
        // Dead
        prepareSound(0xb)
        ram.setB(ram.ptr(a6 + 0xa), 0x18)
        if isSnake then
          ram.setW(SerpentKilledX, fw(0x12))
          ram.setW(SerpentKilledY, fw(0x14))
          ram.setW(SerpentKilledCounter, 1)
        else
          ram.setW(PharaonKilledX, fw(0x12))
          ram.setW(PharaonKilledY, fw(0x14))
          ram.setW(PharaonKilledCounter, 1)
        unlink()
        addPoints(if isSnake then 0x1500 else 0x1000)
        continue = false
      else
        prepareSound(9)
        visible = true // the original tests D0 after prepare_address_of_sound, which set it to 9
    if continue then
      if !visible && fw(4) == 0 then
        setFw(0x12, fw(0xe))
        setFw(0x14, w(fw(0x10) - 8))
        unlink()
      else
        if ram.w(StateMan) != 4 && touchesMan(
            w(fw(0x12) + 8),
            w(fw(0x12) + 0x18),
            fw(0x14),
            w(fw(0x14) + 0x17)
          )
        then kill()
        // Movement: towards the man if chasing ($1C), else back and forth
        if fw(0x1c) != 0 then
          d0 = w(fw(0x12) - ram.w(TombWord0)) >>> 15
          setFw(0x1a, d0)
        else d0 = fw(0x1a)
        d0 = w(w(d0 << 2) - 2)
        var x = w(fw(0x12) + d0)
        if neg16(x) || x < fw(0x16) || x >= fw(0x18) then
          if fw(0x1c) != 0 then x = fw(0x12)
          else
            d0 = w(-d0)
            setFw(0x1a, fw(0x1a) ^ 1)
            x = w(fw(0x12) + d0)
        setFw(0x12, x)
        if isSnake then
          // The snake spits
          if fw(0x22) != 0 then setFw(0x22, w(fw(0x22) - 1))
          else
            setFw(0x22, fw(0x20))
            val man = ram.w(TombWord1)
            if w(fw(0x14) + 0x18) >= man && w(man + 0x18) >= fw(0x14) then
              doProject(fw(0x1a), 2, fw(0x12), fw(0x14))
              prepareSound(8)
        keep()

  /** `aff_sphere` (4406): a sphere teleporting the man. */
  private def affSphere(): Unit =
    val n = (fw(0x12) + 1) & 7
    setFw(0x12, n)
    if !affWallSphere((n >>> 1) + 3, w(fw(0xe) + 8), fw(0x10)) then unlink()
    else if ram.w(StateMan) != 4 && touchesMan(
        w(fw(0xe) + 8),
        w(fw(0xe) + 0x17),
        fw(0x10),
        w(fw(0x10) + 0xf)
      )
    then
      ram.setW(TombWord0, fw(0x14))
      ram.setW(TombWord1, fw(0x16))
      ram.setW(PosInTombX, clampScroll(ram.w(TombWord0), 0x90, ram.w(TombWord2)))
      ram.setW(PosInTombY, clampScroll(ram.w(TombWord1), 0x54, ram.w(TombWord3)))
      setScrollThresholds()
      ram.setW(D518, ram.w(PosInTombX))
      ram.setW(D520, ram.w(PosInTombY))
      ram.setW(D522, ram.w(D510))
      ram.setW(D524, ram.w(D512))
      ram.setW(D526, ram.w(D514))
      ram.setW(D528, ram.w(D516))
      if fw(0x18) == 0 then
        // Used once only
        ram.setB(ram.ptr(a6 + 0xa), 0x18)
        unlink()
      prepareSound(0x10)
      playYamaha()
      paletteDecrescendo(2)
      ram.setW(ManSynchro, 0x7fff)
    else keep()

  /** `aff_killing_stones` (4511): stones falling and rolling, killing the man. */
  private def affKillingStones(): Unit =
    if fw(0x12) == 0 then
      ram.setB(ram.ptr(a6 + 0xa), ram.b(a6 + 0x14))
      setFw(0x12, w(fw(0x12) + 1))
    affWallSphere(fw(0x16), fw(0x18), fw(0x1a))
    if ram.w(StateMan) != 4 && touchesMan(fw(0x18), w(fw(0x18) + 0x1f), fw(0x1a), w(fw(0x1a) + 0xf))
    then kill()
    var destroy = false
    if (fw(0x18) & 0x1f) == 0 && (fw(0x1a) & 0xf) == 0 then
      // On a cell: transformations, then falls or rolls (table at debut_data+258)
      val a0 = TombTable1 + sw(w(w(fw(0x1a) * 5) + (fw(0x18) >>> 5)))
      val a1 = D258 + sw(w(fw(0x16) * 0x34))
      var a2 = a1 + 0x1c
      val c = ram.b(a0)
      var support = true
      var searching = true
      while searching do
        if ram.b(a2) == 0xff then searching = false
        else if ram.b(a2) == c then
          ram.setB(a0, ram.b(a2 + 1))
          a2 += 2
          searching = false
          if ram.b(a2) != 1 then
            destroy = true
            support = false
        else a2 += 3
      if support then
        // Something below to stand on?
        a2 = a1 + 0x10
        val below = ram.b(a0 + 0x50)
        var found = false
        while !found && ram.b(a2) != 0xff do
          if ram.b(a2) == below then found = true
          a2 += 1
        if !found then
          setFw(0x20, 2) // falls
          if fw(0x1c) != 0 then
            setFw(0x1e, fw(0x1c))
            setFw(0x1c, 0)
        else
          def side: Int = if neg16(fw(0x1e)) then ram.b(a0 - 1) else ram.b(a0 + 1)
          def blocked(v: Int): Boolean =
            var p = a1
            var hit = false
            while !hit && ram.b(p) != 0xff do
              if ram.b(p) == v then hit = true
              p += 1
            hit
          if blocked(side) then
            setFw(0x1e, w(-fw(0x1e)))
            if blocked(side) then
              setFw(0x1e, w(-fw(0x1e)))
              setFw(0x1c, 0)
              setFw(0x20, 0)
            else
              setFw(0x20, 0)
              setFw(0x1c, fw(0x1e))
          else
            setFw(0x20, 0)
            setFw(0x1c, fw(0x1e))
    if !destroy then
      val dx = fw(0x1c)
      setFw(0x18, w(fw(0x18) + dx))
      val dy = fw(0x20)
      setFw(0x1a, w(fw(0x1a) + dy))
      val moved = w(fw(0x24) | dy | dx)
      setFw(0x24, moved)
      if moved != 0 then
        val life = w(fw(0x22) - 1)
        setFw(0x22, life)
        if life == 0 then destroy = true
    if destroy then
      ram.setB(ram.ptr(a6 + 0xa), 0x18)
      unlink()
    else
      a5 = a6
      a6 = ram.l(a6)

  /** `L002C9` (4793): a stone appearing and disappearing. */
  private def l002C9(): Unit =
    if fw(4) == 0 then unlink()
    else
      if switchOn(fw(0x1a)) then
        val t = fw(0x1e)
        if t != 0 then
          if (t & 0xff) != 0xff then setFw(0x1e, (t & 0xff00) | b(t - 1))
        else
          val d0 = fw(0x18)
          setFw(0x18, fw(0x16))
          setFw(0x16, d0)
          setFw(0x1e, d0)
          val block = fw(0x14)
          setFw(0x14, fw(0x12))
          setFw(0x12, block)
          val count = fw(0x20)
          if (count & 0xff) != 0 then
            if (count & 0x80) == 0 then setFw(0x20, w(count - 1))
            ram.setB(ram.ptr(a6 + 0xa), block)
      val s = ram.b(a6 + 0x1c)
      if s != 0xff then ram.setB(B213760 + fw(0x1a), s)
      keep()

  /** `break_vase` (4842): a vase hit by an arrow. */
  private def breakVase(): Unit =
    val strength = fw(0x12)
    val arcs = ram.w(NbArc)
    val a0 = ram.ptr(a6 + 0xa)
    if !lowerU16(strength, arcs) then
      setFw(0x12, w(strength - arcs))
      ram.setB(a0, 0x2a)
      ram.setB(a0 - 0x50, 0x2b)
      prepareSound(1)
    else
      prepareSound(3) // the vase breaks
      ram.setW(VaseBrokenX, fw(0xe))
      ram.setW(VaseBrokenY, w(fw(0x10) - 8))
      ram.setW(VaseBrokenCounter, 1)
      ram.setB(a0, 0x18)
      ram.setB(a0 - 0x50, 0x18)
      // Its content appears
      val (a1, _) = element(a0, ram.ptr(D542), ram.ptr(a6 + 0x14))
      ram.setL(D542, a1)
    unlink()

package entombed.game

import M68.*
import Vars.*

/** Projectiles: arrows, projectiles from walls, snake spit, balls from dragons
  * (`reference/smaky/STOMBED1.L`, lines 4895-5497).
  *
  * 16 records of 22 bytes at B214016, in a free list (D548) and an active list (D552): link, x (4),
  * y (6), dx (8), dy ($A), image ($C), offsets of the tested point ($E, $10), duration ($12), type
  * ($14: 0 from a wall, 1 arrow, 2 snake, 3 ball). Balls use fixed point positions (fractions in
  * the bytes $E and $F).
  */
trait Projectiles:
  this: Game =>

  /** `L00259` (5487): all the projectiles are free. */
  def l00259(): Unit =
    var a0 = B214016
    ram.setL(D548, a0)
    ram.setL(D552, -1)
    var a1 = a0
    for _ <- 0 to 15 do
      a1 = a0
      a0 += 0x16
      ram.setL(a1, a0)
    ram.setL(a1, -1)

  /** `do_project` (4895): a new projectile of type `kind` going in `direction` (0 left, 1 right)
    * from (x, y). For balls, the parameters come from the dragon `a6`.
    */
  def doProject(direction: Int, kind: Int, x: Int, y: Int): Unit =
    val a0 = ram.l(D548)
    if a0 != -1 then
      ram.setL(D548, ram.l(a0))
      ram.setL(a0, ram.l(D552))
      ram.setL(D552, a0)
      ram.setW(a0 + 0x14, kind)
      var a1 = ListProjects + sw(w(kind << 5)) + sw(w(direction << 4))
      ram.setW(a0 + 4, w(x + ram.w(a1)))
      ram.setW(a0 + 6, w(y + ram.w(a1 + 2)))
      a1 += 4
      if kind == 3 then
        ram.setW(a0 + 8, ram.w(a6 + 0x1e))
        ram.setW(a0 + 0xa, 0)
        ram.setW(a0 + 0xe, 0)
        ram.setW(a0 + 0x10, ram.w(a6 + 0x22))
        ram.setW(a0 + 0xc, ram.w(a6 + 0x24))
        ram.setW(a0 + 0x12, ram.w(a6 + 0x20))
      else
        for off <- Seq(8, 0xa, 0xc, 0xe, 0x10, 0x12) do
          ram.setW(a0 + off, ram.w(a1))
          a1 += 2

  /** `gere_project` (4934): moves the projectiles and checks what they hit. */
  def gereProject(): Unit =
    a5 = -1
    a6 = ram.l(D552)
    while a6 != -1 do
      if ram.w(a6 + 0x14) == 3 then doBille()
      else
        val t = w(ram.w(a6 + 0x12) - 1)
        ram.setW(a6 + 0x12, t)
        if t == 0 || !affProj(ram.w(a6 + 0xc), ram.w(a6 + 4), ram.w(a6 + 6)) then removeProjectile()
        else projectileHits()

  /** `L00394` (4951): the projectile disappears. */
  private def removeProjectile(): Unit =
    if ram.w(a6 + 0x14) == 1 then ram.setW(NbFlechesTirees, w(ram.w(NbFlechesTirees) - 1))
    freeProjectile()

  /** `L00397` (4954). */
  private def freeProjectile(): Unit =
    val a4 = a6
    a6 = ram.l(a6)
    ram.setL(a4, ram.l(D548))
    ram.setL(D548, a4)
    if a5 == -1 then ram.setL(D552, a6) else ram.setL(a5, a6)

  /** `L00396` (4964). */
  private def projectileHits(): Unit =
    val px = w(ram.w(a6 + 4) + ram.w(a6 + 0xe))
    val py = w(ram.w(a6 + 6) + ram.w(a6 + 0x10))
    val ry = w(py & 0xfff0)
    val a0 = TombTable1 + (px >>> 5) + sw(ry) + sw(w(ry << 2))
    val d0 = ram.b(a0)
    var removed = false
    if ram.w(a6 + 0x14) == 1 then
      // An arrow: vases and enemies
      if d0 == 0x2b then
        ram.setB(a0, 0x32)
        ram.setB(a0 + 0x50, 0x31)
        removed = true
      else if d0 == 0x2a then
        ram.setB(a0, 0x31)
        ram.setB(a0 - 0x50, 0x32)
        removed = true
      else
        var e = ram.l(D4810)
        while !removed && e != -1 do
          val tag = ram.l(e + 6)
          if (tag == Snake || tag == Pharaoh) &&
            !(px < w(ram.w(e + 0x12) + 8) || w(ram.w(e + 0x12) + 0x18) < px ||
              py < ram.w(e + 0x14) || w(ram.w(e + 0x14) + 0x17) < py)
          then
            ram.setW(e + (if tag == Snake then 0x24 else 0x20), ram.w(NbArc))
            removed = true
          else e = ram.l(e)
    else if ram.w(StateMan) != 4 then
      val mx = w(ram.w(TombWord0) + 8)
      val my = ram.w(TombWord1)
      if !(px < mx || w(mx + 0xb) < px || py < my || w(my + 0x17) < py) then
        killMan()
        removed = true
    if removed then removeProjectile()
    else if (ram.b(AttributesTable + d0) & 1) != 0 then removeProjectile()
    else
      ram.setW(a6 + 4, w(ram.w(a6 + 4) + ram.w(a6 + 8)))
      ram.setW(a6 + 6, w(ram.w(a6 + 6) + ram.w(a6 + 0xa)))
      a5 = a6
      a6 = ram.l(a6)

  // ----- 68000 register operations, for the fixed point code below -----

  private inline def addW(x: Int, v: Int): Int = (x & 0xffff0000) | ((x + v) & 0xffff)
  private inline def lsrW(x: Int, n: Int): Int = (x & 0xffff0000) | ((x & 0xffff) >>> n)
  private inline def aslW(x: Int, n: Int): Int = (x & 0xffff0000) | ((x << n) & 0xffff)
  private inline def andW(x: Int, m: Int): Int = (x & 0xffff0000) | (x & m & 0xffff)
  private inline def orW(x: Int, m: Int): Int = x | (m & 0xffff)
  private inline def negW(x: Int): Int = (x & 0xffff0000) | (-x & 0xffff)
  private inline def setW(x: Int, v: Int): Int = (x & 0xffff0000) | (v & 0xffff)
  private inline def setB(x: Int, v: Int): Int = (x & 0xffffff00) | (v & 0xff)
  private inline def muls(a: Int, b: Int): Int = a.toShort.toInt * b.toShort.toInt
  private inline def mulu(a: Int, b: Int): Int = (a & 0xffff) * (b & 0xffff)

  /** Cell of the tomb at fixed point coordinates (x + dx, y + dy) (pixel offsets). */
  private def cellAtFixed(d0: Int, d1: Int, dx: Int, dy: Int): Int =
    var d4 = d0 >>> 8
    if dx != 0 then d4 = addW(d4, dx)
    d4 = lsrW(d4, 5)
    var d5 = d1 >>> 8
    if dy != 0 then d5 = addW(d5, dy)
    d5 = lsrW(d5, 4)
    d5 = mulu(d5, 0x50)
    TombTable1 + d5 + d4

  private def attrAt(a0: Int): Int = ram.b(AttributesTable + ram.b(a0))

  /** `L003B5` (5080): square root of a 16-bit value, as an 8.8 fixed point number. */
  private def l003B5(input: Int): Int =
    var d0 = input & 0xffff
    d0 = Integer.rotateRight(d0, 8)
    var d1 = 0x40
    for _ <- 0 to 7 do
      val low = (d0 & 0xffff) - (d1 & 0xffff)
      if low < 0 then
        // SUB.W with borrow, then ADD.W back
        d1 = setW(d1, d1 + d1)
        d1 = setB(d1, 0x40)
      else
        d0 = setW(d0, low)
        d1 = setW(d1, d1 + d1)
        d1 = addW(d1, 0xc0)
      d0 = Integer.rotateLeft(d0, 2)
    (d1 & 0xffff) >>> 8

  /** `do_bille` (5106): a ball, bouncing on walls and floors. */
  private def doBille(): Unit =
    val t = w(ram.w(a6 + 0x12) - 1)
    ram.setW(a6 + 0x12, t)
    if t == 0 || !affRadeauBille(1, ram.w(a6 + 4), ram.w(a6 + 6)) then freeProjectile()
    else
      val bx = ram.w(a6 + 4)
      val by = ram.w(a6 + 6)
      if ram.w(StateMan) != 4 && {
          val mx = w(ram.w(TombWord0) + 8)
          val my = ram.w(TombWord1)
          !(w(bx + 7) < mx || w(mx + 0xb) < bx || w(by + 7) < my || w(my + 0x17) < by)
        }
      then
        killMan()
        removeProjectile()
      else moveBall()

  // scalastyle:off method.length
  private def moveBall(): Unit =
    val elasticity = ram.w(a6 + 0x10)
    var d0 = (ram.w(a6 + 4) << 8) | ram.b(a6 + 0xe)
    var d1 = (ram.w(a6 + 6) << 8) | ram.b(a6 + 0xf)
    var d2 = ram.sw(a6 + 8)
    var d3 = ram.sw(a6 + 0xa)
    var d4 = 0
    var d5 = 0
    var d6 = 0
    // Horizontal move and walls
    d0 += d2
    val goingRight = d2 >= 0
    if (attrAt(cellAtFixed(d0, d1, if goingRight then 7 else 0, 3)) & 1) != 0 then
      d4 = d0
      d0 -= d2
      d0 = andW(d0, 0xe000)
      if goingRight then d0 = addW(d0, 0x1800)
      d4 -= d0
      d4 = muls(d4, elasticity) >> 8
      d0 -= d4
      d2 = -d2
      d2 = muls(d2, elasticity) >> 8
      prepareSound(1)
    // Gravity
    d1 += d3
    d1 += 0x100
    d4 = setW(d4, d3)
    d3 += 0x200
    if !neg16(d4) then
      if w(d3) >= 0xf00 then d3 = setW(d3, 0xf00)
      if (attrAt(cellAtFixed(d0, d1, 3, 7)) & 0x20) != 0 then
        // Bounce on a floor
        d3 -= 0x200
        d1 -= 0x100
        d1 -= d3
        d4 = setW(d4, d1)
        d4 = addW(d4, 0x700)
        d4 = andW(d4, 0xfff)
        d4 = negW(d4)
        d4 = addW(d4, 0xfff)
        d4 = muls(d4, 0x400) >> 8
        d5 = setW(d5, d3)
        d5 = muls(d3, d5) >> 8
        val sum = (d4 & 0xffff) + (d5 & 0xffff)
        d4 = setW(d4, sum)
        if sum > 0xffff then d4 = setW(d4, 0xffff)
        d4 = setW(d4, l003B5(d4))
        d4 = aslW(d4, 4)
        if w(d4) >= 0xf00 then d4 = setW(d4, 0xf00)
        d5 = setW(d5, d4)
        d5 = setW(d5, d5 - d3)
        d5 &= 0xffff
        d5 = setW(d5, d5 / 2)
        d5 = negW(d5)
        d5 = addW(d5, 0x100)
        d4 = negW(d4)
        d4 = muls(d4, ram.w(a6 + 0xc)) >> 8
        d6 = setW(d6, d5)
        d6 = muls(d6, 0x200) >> 8
        d6 = setW(d6, d6 + d4)
        d3 = setW(d3, d6)
        if !neg16(d3) then d3 = setW(d3, 0)
        d6 = setW(d6, d5)
        d6 = muls(d6, 0x100) >> 8
        d6 = setW(d6, d6 + d4)
        d6 = muls(d5, d6) >> 8
        d1 += 0x700
        d1 = orW(d1, 0xfff)
        if neg16(d6) then d1 = setW(d1, d1 + d6)
        d1 -= 0x700
        d4 = ram.sw(a6 + 6) << 8
        d4 = setB(d4, ram.b(a6 + 0xf))
        if d4 != d1 then prepareSound(1)
    else if (attrAt(cellAtFixed(d0, d1, 3, 0)) & 2) != 0 then
      // Bounce on a ceiling
      d3 -= 0x200
      d1 -= 0x100
      d1 -= d3
      d4 = setW(d4, d1)
      d4 = andW(d4, 0xfff)
      d4 = negW(d4)
      d4 = muls(d4, 0x400) >> 8
      d5 = setW(d5, d3)
      d5 = muls(d3, d5) >> 8
      d4 = setW(d4, d4 + d5)
      d4 = setW(d4, l003B5(d4))
      d4 = aslW(d4, 4)
      d4 = negW(d4)
      d5 = setW(d5, d4)
      d5 = setW(d5, d5 - d3)
      d5 &= 0xffff
      d5 = setW(d5, d5 / 2)
      d5 = negW(d5)
      d5 = addW(d5, 0x100)
      d4 = negW(d4)
      d4 = muls(d4, ram.w(a6 + 0xc)) >> 8
      d6 = setW(d6, d5)
      d6 = muls(d6, 0x200) >> 8
      d6 = setW(d6, d6 + d4)
      d3 = setW(d3, d6)
      if w(d3) >= 0xf00 then d3 = setW(d3, 0xf00)
      d6 = setW(d6, d5)
      d6 = muls(d6, 0x100) >> 8
      d6 = setW(d6, d6 + d4)
      d6 = muls(d5, d6) >> 8
      d1 = andW(d1, 0xf000)
      d1 = setW(d1, d1 + d6)
      prepareSound(1)
    // Getting out of walls
    if (attrAt(cellAtFixed(d0, d1, 3, 0)) & 2) != 0 then
      d1 += 0x1000
      d1 &= -0x1000
    if (attrAt(cellAtFixed(d0, d1, 3, 7)) & 2) != 0 then
      d1 -= 0x900
      d1 &= -0x1000
      d1 += 0x800
    if (attrAt(cellAtFixed(d0, d1, 0, 3)) & 2) != 0 then
      d0 += 0x2000
      d0 &= -0x2000
    if (attrAt(cellAtFixed(d0, d1, 7, 3)) & 2) != 0 then
      d0 -= 0x1900
      d0 &= -0x2000
      d0 += 0x1800
    ram.setB(a6 + 0xe, d0)
    ram.setW(a6 + 4, d0 >>> 8)
    ram.setB(a6 + 0xf, d1)
    ram.setW(a6 + 6, d1 >>> 8)
    ram.setW(a6 + 8, d2)
    ram.setW(a6 + 0xa, d3)
    a5 = a6
    a6 = ram.l(a6)
  // scalastyle:on

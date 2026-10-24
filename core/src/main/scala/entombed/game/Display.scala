package entombed.game

import M68.*
import Vars.*

/** Display routines (`reference/smaky/STOMBED1.L`, line numbers in comments). */
trait Display:
  this: Game =>

  // ----- Palettes and screens -----

  /** `select_palette` (1232): shows palette `n` and waits for the vertical blank. */
  def selectPalette(n: Int): Unit =
    changePal(Palettes + w(n << 5))
    waitVbl()

  /** `swap_screens` (1257). */
  def swapScreens(): Unit =
    val s2 = ram.l(PhysScreen2)
    ram.setL(PhysScreen2, ram.l(PhysScreen1))
    ram.setL(PhysScreen1, s2)
    affScreen()
    waitVbl()

  /** Clears `longs` longs of the screen at `PhysScreen1` or `PhysScreen2`. */
  def clearScreen(screenVar: Int, bytes: Int = 32000): Unit =
    ram.clear(ram.ptr(screenVar), bytes)

  /** Computes palette 0 from palette `n` at intensity `i`/256 (`palette_decrescendo` and co). */
  private def scaledPalette(n: Int, intensity: Int): Unit =
    var a0 = Palettes + w(n << 5)
    var a1 = Palettes
    for _ <- 0 to 15 do
      val c = ram.w(a0)
      var d2 = w((c & 0xf) * intensity) >>> 8
      d2 |= (w((c & 0xf0) * intensity) >>> 8) & 0xf0
      d2 |= (((c & 0xf00) * intensity) >>> 8) & 0xf00
      ram.setW(a1, d2)
      a0 += 2
      a1 += 2

  /** `palette_decrescendo` (5799): from palette `n` to black in 9 steps of 2 vertical blanks. */
  def paletteDecrescendo(n: Int): Unit =
    var i = 0x100
    for _ <- 0 to 8 do
      scaledPalette(n, i)
      selectPalette(0)
      waitVbl()
      i -= 0x20

  /** `palette_crescendo` (5879): from black to palette `n`. */
  def paletteCrescendo(n: Int): Unit =
    var i = 0
    for _ <- 0 to 8 do
      scaledPalette(n, i)
      selectPalette(0)
      waitVbl()
      i += 0x20

  /** `create_palette` (5840): palette `n` at intensity (debut_data+142)*32/256 in palette 0. */
  def createPalette(n: Int): Unit =
    scaledPalette(n, ram.w(D142) * 0x20)
    selectPalette(0)

  // ----- Blocks -----

  private def others: Int = ram.ptr(BlocsOthers)

  /** `aff_bloc32x24` (3930): block `n` of 32x24 among the "other" graphics. */
  def affBloc32x24(n: Int, x: Int, y: Int): Boolean =
    val d0 = w(n << 7)
    affTransp(others + sw(d0) + sw(w(d0 * 2)), 24, x, y)

  /** `L00352` (3943): the same block, in white (enemy hit). */
  def affBloc32x24White(n: Int, x: Int, y: Int): Boolean =
    val d0 = w(n << 7)
    affWhite(others + sw(d0) + sw(w(d0 * 2)), 24, x, y)

  /** `aff_09_sablier` (3956): big digits (hourglass time). */
  def aff09Sablier(n: Int, x: Int, y: Int): Boolean =
    val d0 = w(n << 6)
    affTransp(others + 0x300 + sw(d0) + sw(w(d0 * 2)), 12, x, y)

  /** `aff_wall_sphere` (3970): killing stones or teleport sphere. */
  def affWallSphere(n: Int, x: Int, y: Int): Boolean =
    affTransp(others + 0x6480 + sw(w(n << 8)), 16, x, y)

  /** `aff_proj` (3981): projectiles. */
  def affProj(n: Int, x: Int, y: Int): Boolean =
    affTransp(others + sw(w(n << 6)), 4, x, y)

  /** `aff_points_gotten` (3992). */
  def affPointsGotten(n: Int, x: Int, y: Int): Boolean =
    val d0 = w(n << 5)
    affTransp(others + 0x180 + sw(d0) + sw(w(d0 * 2)), 6, x, y)

  /** `aff_radeau_bille` (4006): raft (0) or ball (1). */
  def affRadeauBille(n: Int, x: Int, y: Int): Boolean =
    affTransp(others + 0x2880 + sw(w(n << 7)), 8, x, y)

  private def block32x16(n: Int): Int = (n & 0xffff) * 0x100 + ram.l(Blocs32x16)

  /** `change_stones` (1389): swaps the 11 blocks that differ between tombs. */
  def changeStones(): Unit =
    var a0 = StonesTable + (ram.w(Zero1) * 0x16)
    var a1 = StonesTable + (ram.w(TombNumber2) * 0x16)
    for _ <- 0 to 10 do
      val a2 = block32x16(ram.w(a0)) & 0xffffff
      val a3 = block32x16(ram.w(a1)) & 0xffffff
      a0 += 2
      a1 += 2
      for i <- 0 until 256 do
        val t = ram.bytes(a2 + i)
        ram.bytes(a2 + i) = ram.bytes(a3 + i)
        ram.bytes(a3 + i) = t

  /** `blocs32x16_init` (1423): draws the 110 background blocks over their background and keeps them
    * in slots of $500 bytes, pointed to by the table at B173568.
    */
  def blocs32x16Init(): Unit =
    var a0 = D1154
    val count = ram.w(D1152)
    var a4 = B32768
    var a5 = B173568
    val a1 = ram.ptr(PhysScreen2)
    for _ <- 0 to count do
      val block = ram.b(a0)
      val mode = ram.b(a0 + 1)
      a0 += 2
      val a2 = block32x16(block) & 0xffffff
      val px = ram.w(PosInTombX)
      val py = ram.w(PosInTombY)
      if mode == 0 then affNormal(a2, 16, px, py)
      else
        val bg = if mode == 1 then 0x18 else 0x36
        affNormal(block32x16(bg) & 0xffffff, 16, px, py)
        affTransp(a2, 16, px, py)
      ram.setL(a5, a4)
      a5 += 4
      for row <- 0 until 16 do
        System.arraycopy(ram.bytes, a1 + 160 * row, ram.bytes, a4 + 16 * row, 16)
      a4 += 0x500
    ram.setL(B173568 + 80, B121088)
    ram.setL(B173568 + 84, B123648)

  /** `L00042` (3385): every other frame, the next image of the animated blocks (fire, water,
    * torches...).
    */
  def animateBlocks(): Unit =
    val counter = w(ram.w(AMainCounter) + 1)
    ram.setW(AMainCounter, counter)
    if (counter & 1) != 0 then
      val frame = (ram.w(D530) + 1) & 3
      ram.setW(D530, frame)
      var a0 = B32768 + sw(w(frame * 0x500))
      val a1 = B173568
      ram.setL(a1 + 0xd8, a0 + 0x10e00)
      ram.setL(a1 + 0x154, a0 + 0x1a900)
      for i <- 0 to 5 do
        ram.setL(a1 + 4 * i, a0)
        a0 += 0x1400
      ram.setL(B173568 + 424, ram.l(B173568) + 0x21200) // debut_bss+173992
      ram.setL(B173568 + 292, ram.l(B173568 + 8) + 0x14500) // 173860
      ram.setL(B173568 + 308, ram.l(B173568 + 16) + 0x13100) // 173876
      ram.setL(B173568 + 324, ram.l(B173568 + 20) + 0x13100) // 173892

  /** `get_adr_for_test` (2269): address of the cell at tomb coordinates (x, y). */
  def cellAt(x: Int, y: Int): Int =
    val r = w(y & 0xfff0)
    TombTable1 + sw(r) + sw(w(r << 2)) + (w(x) >>> 5)

  /** `aff_all_screen` (1272): the visible part of the tomb. */
  def affAllScreen(): Unit =
    val px = ram.w(PosInTombX)
    val py = ram.w(PosInTombY)
    val d1 = w(py & 0xfff0)
    var a6 = TombTable1 + sw(w(w(d1 * 5) + (px >>> 5)))
    ram.setL(D4806, a6)
    val attr = AttributesTable
    for _ <- 0 to 12 do
      for _ <- 0 to 10 do
        val c = ram.b(a6)
        if c == 0x20 && ram.b(a6 - 0x50) == 0x20 then ram.setB(a6, 0x41)
        else if c == 0x21 && ram.b(a6 - 0x50) == 0x21 then ram.setB(a6, 0x42)
        val d = ram.b(attr + ram.b(a6 - 1)) | ram.b(attr + ram.b(a6 - 0x50)) |
          ram.b(attr + ram.b(a6 - 0x51))
        if (d & 0x80) != 0 then ram.setB(a6, ram.b(D1374 + ram.b(a6)))
        a6 += 1
      a6 += 0x45
    // Blocks
    val cols = if (px & 0x1f) == 0 then 9 else 10
    val rows = if (py & 0xf) == 0 then 11 else 12
    var y = w(py & 0xfff0)
    for _ <- 0 to rows do
      var x = w(px & 0xffe0)
      var a = cellAt(x, y)
      for _ <- 0 to cols do
        val block = ram.ptr(B173568 + 4 * ram.b(a))
        a += 1
        affNormal(block, 16, x, y)
        x = w(x + 32)
      y = w(y + 16)
    // Restores the cells changed above
    a6 = ram.ptr(D4806)
    for _ <- 0 to 12 do
      for _ <- 0 to 10 do
        val c = ram.b(a6)
        if c >= 0x40 then ram.setB(a6, ram.b(D1374 + c))
        a6 += 1
      a6 += 0x45

  // ----- Text -----

  /** `aff_car` (5503): character `c` at byte offset `offset` of the screen. */
  def affCar(c: Int, offset: Int): Unit =
    val a0 = ram.ptr(Fonts8x8) + w(b(c - 0x20) << 5)
    var a1 = ram.ptr(PhysScreen2) + sw(offset)
    for row <- 0 to 7 do
      System.arraycopy(ram.bytes, a0 + 4 * row, ram.bytes, a1, 4)
      a1 += 0xa0

  /** `aff_text2` (5520): the string at `a0` at screen offset `offset`. Returns the address after
    * the string (or `a0 + 1` if the offset is outside the screen).
    */
  def affText2(a0: Int, offset: Int): Int =
    if neg16(offset) || w(offset) >= 0x78a0 then a0 + 1
    else
      var a2 = a0
      var d2 = offset
      var c = ram.b(a2)
      a2 += 1
      while c != 0 do
        affCar(c, d2)
        d2 = w(d2 + 4)
        c = ram.b(a2)
        a2 += 1
      a2

  /** `aff_text` (5553): lines of text at (x, y) of the tomb, until a $FF byte. */
  def affText(a0start: Int, x: Int, y: Int): Unit =
    var d1 = w(y - ram.w(PosInTombY))
    var ok = true
    if neg16(d1) then
      d1 = w(-d1)
      if d1 >= 0x40 then ok = false else d1 = w(-(d1 * 0xa0))
    else if d1 >= 0xc1 then ok = false
    else d1 = w(d1 * 0xa0)
    if ok then
      // Chunky version of the Atari offset (see OracleCore.Patches)
      var d0 = w(w((w(x) >>> 3) << 2) + d1)
      var a0 = a0start
      var continue = true
      while continue do
        a0 = affText2(a0, d0)
        if ram.b(a0 - 1) != 0 then while { val z = ram.b(a0) == 0; a0 += 1; !z } do ()
        d0 = w(d0 + 0x640)
        continue = ram.b(a0) != 0xff

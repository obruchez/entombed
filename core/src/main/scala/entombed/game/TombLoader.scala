package entombed.game

import M68.*
import Vars.*

/** Reading of the tomb data, timed changes ("fixations") and attributes
  * (`reference/smaky/STOMBED1.L`, lines 2586-3512 and 4055).
  *
  * A tomb starts with 5 words (man x and y, maximum scroll x and y, bonus in thousands of points),
  * then 32 characters '0'/'1' telling which of the 8x4 screens exist. Each screen is 12 lines of 20
  * characters (10 for the front table, 10 for the alternative one), followed by the list of the
  * elements marked by letters in the screen. The fixations (timed changes) come last.
  */
trait TombLoader:
  this: Game =>

  /** `attributes_table_init` (4055). */
  def attributesTableInit(): Unit =
    var a0 = AttrTblData
    for _ <- 0 to ram.w(D1150) do
      ram.setB(AttributesTable + ram.b(a0), ram.b(a0 + 1))
      a0 += 2

  /** `read_tomb_datas` (2586). */
  def readTombDatas(): Unit =
    ram.setW(FixationCounter, 0)
    ram.setL(D4810, -1)
    l00259()
    ram.setL(D542, B205312)
    // Default content: walls ($19/$1A alternating lines) and nothing behind
    var d6 = 0
    for i <- 0 until 0xf00 do
      if d6 < 0x50 then ram.setB(TombTable1 + i, 0x19)
      else ram.setB(TombTable1 + i, 0x1a)
      d6 += 1
      if d6 == 0xa0 then d6 = 0
      ram.setB(TombTable2 + i, 0x18)
    ram.clear(TombTable3, 0xf00 * 4)
    // Header
    var a6 = ram.ptr(Tombs5 + w(ram.w(TombNumber) << 2))
    ram.setW(TombWord0, ram.w(a6))
    ram.setW(TombWord1, ram.w(a6 + 2))
    ram.setW(TombWord2, ram.w(a6 + 4))
    ram.setW(TombWord3, ram.w(a6 + 6))
    ram.setL(TombWord4, ram.w(a6 + 8) << 12)
    a6 += 10
    ram.setL(TombDatas, a6)
    // The screens present
    var a0 = TombTable1
    var a1 = D746
    var screens = 0xffff
    for _ <- 0 to 3 do
      for _ <- 0 to 7 do
        val c = ram.b(a6)
        a6 += 1
        if c == 0x31 then
          screens = w(screens + 1)
          ram.setL(a1, a0)
          a1 += 4
        a0 += 0xa
      a0 += 0x370
    // Each screen
    var screen = D746
    var remaining = screens
    while remaining != 0xffff do
      var markers = 0xffff // number of element markers - 1 (upper word of D5)
      var cell = ram.ptr(screen)
      screen += 4
      for _ <- 0 to 0xb do
        for _ <- 0 to 9 do
          val (c1, m1) = l0026A(a6, cell)
          ram.setB(cell, c1)
          val (c2, m2) = l0026A(a6 + 10, cell + 0xf00)
          ram.setB(cell + 0xf00, c2)
          markers = w(markers + m1 + m2)
          cell += 1
          a6 += 1
        cell += 0x46
        a6 += 0xa
      a6 = l00270(a6, markers)
      remaining = w(remaining - 1)
    // Initial scrolling
    ram.setW(PosInTombX, clampScroll(ram.w(TombWord0), 0x90, ram.w(TombWord2)))
    ram.setW(PosInTombY, clampScroll(ram.w(TombWord1), 0x54, ram.w(TombWord3)))
    setScrollThresholds()
    l00276(a6)
    // Fixations already triggered before the last checkpoint
    val done = ram.w(FixationCounter)
    if done != 0 then
      for _ <- 0 until done do
        ram.setW(FixationSynchro, 0xfffe)
        doFixeElements()
        ram.setL(D874, ram.l(D874) + 2)
      ram.setW(FixationSynchro, 0xfffe)
      doFixeElements()
      ram.setW(FixationSynchro, 0x8000)
    deriveWallEdges()

  /** Scroll position for the man at `v`: `v - offset`, between 0 and `max`. */
  def clampScroll(v: Int, offset: Int, max: Int): Int =
    val d0 = if v < offset then 0 else v - offset
    if d0 >= max then max else d0

  /** The man scrolls the screen when he leaves the area between these limits. */
  def setScrollThresholds(): Unit =
    val px = ram.w(PosInTombX)
    val py = ram.w(PosInTombY)
    ram.setW(D510, w(px + 0x80))
    ram.setW(D512, w(px + 0xa0))
    ram.setW(D514, w(py + 0x48))
    ram.setW(D516, w(py + 0x60))

  /** `L0026A` (2848): the block for the character at `a6`. Letters A-Z except B and K mark
    * elements: their cell (`cell`) is recorded and the block is empty ($18). Returns the block and
    * 1 if it was a marker.
    */
  private def l0026A(a6: Int, cell: Int): (Int, Int) =
    val c = ram.b(a6)
    if c != 0x42 && c != 0x4b && c >= 0x41 && c < 0x5b then
      ram.setL(D954 + 4 * (c - 0x41), cell)
      (0x18, 1)
    else
      var a4 = D880
      var result = 0x18
      var n = ram.w(D878)
      var found = false
      while !found && n != 0xffff do
        if ram.b(a4) == c then
          result = ram.b(a4 + 1)
          found = true
        else
          a4 += 2
          n = w(n - 1)
      (result, 0)

  /** `L00270` (2880): the elements of a screen. Returns the address after them. */
  private def l00270(start: Int, markers: Int): Int =
    var a6 = start
    if markers != 0xffff then
      var a1 = ram.ptr(D542)
      var n = markers
      while n != 0xffff do
        val letter = ram.b(a6)
        a6 += 1
        val a2 = ram.ptr(D954 + 4 * b(letter - 0x41))
        ram.setL(D4830, a2)
        val r = element(a2, a1, a6)
        a1 = r._1
        a6 = r._2
        n = w(n - 1)
      ram.setL(D542, a1)
    a6

  /** Writes the cell coordinates of `a3` (an entry of tomb_table3) to the record `a1`: y at $10, x
    * at $E. Returns (y, x).
    */
  private def recordPosition(a3: Int, a1: Int): (Int, Int) =
    val index = w((a3 - TombTable3) >>> 2)
    val row = index / 0x50
    val y = w(row << 4)
    val x = w(w(index - w(y * 5)) << 5)
    ram.setW(a1 + 0x10, y)
    ram.setW(a1 + 0xe, x)
    (y, x)

  private def newRecord(a1: Int, a3: Int, tag: Int): Unit =
    ram.setL(a3, a1)
    ram.setL(a1, 0)
    ram.setW(a1 + 4, 0)
    ram.setL(a1 + 6, tag)
    ram.setL(a1 + 0xa, ram.l(D4830))

  /** `L0038C` (2890): one element at cell `a2`, read at `a6`; new records at `a1`. Returns the next
    * free record and the address after the element. Also used for the content of vases.
    */
  def element(a2start: Int, a1start: Int, a6start: Int): (Int, Int) =
    var a1 = a1start
    var a6 = a6start
    val a2 = a2start
    var d2 = w(a2 - TombTable1)
    if d2 >= 0xf00 then
      d2 = w(d2 - 0xf00)
      ram.setL(D4830, ram.l(D4830) - 0xf00)
    val a3 = TombTable3 + sw(w(d2 << 2))
    def next(): Int =
      val v = ram.b(a6)
      a6 += 1
      v
    def nextWord(): Int =
      val h = next()
      (h << 8) | next()
    next() match
      case 0x41 => // 'A': arrows
        ram.setL(a3, a6)
        a6 += 1
        ram.setB(a2, 0x29)
      case 0x44 => // 'D': a block
        ram.setB(a2, next())
      case 0x50 => // 'P': gold coin
        ram.setL(a3, a6)
        a6 += 1
        ram.setB(a2, 0x36)
      case 0x4b => ram.setB(a2, 0x1b) // 'K': key
      case 0x42 => ram.setB(a2, 0x28) // 'B': bow
      case 0x48 => // 'H': hourglass
        ram.setL(a3, a6)
        a6 += 1
        ram.setB(a2, 0x2e)
      case 0x55 => // 'U': vase (strength, then content up to $FE)
        ram.setL(a3, a1)
        ram.setW(a1 + 0x12, next())
        ram.setL(a1 + 0x14, a6)
        ram.setL(a1 + 0xa, ram.l(D4830))
        recordPosition(a3, a1)
        ram.setL(a1 + 6, Vase)
        ram.setL(a1, 0)
        ram.setW(a1 + 4, 0)
        a1 += 0x18
        while next() != 0xfe do ()
        ram.setB(a2, 0x2a)
        ram.setB(a2 - 0x50, 0x2b)
      case 0x43 => // 'C': chest (content up to $FE)
        ram.setL(a3, a6)
        while next() != 0xfe do ()
        ram.setB(a2, 0x2c)
      case t @ (0x7b | 0x7d) => // '{' '}': projectile shooter in a wall
        ram.setB(a2, if t == 0x7b then 0x22 else 0x23)
        ram.setW(a1 + 0x16, if t == 0x7b then 0 else 1)
        newRecord(a1, a3, WallShooter)
        recordPosition(a3, a1)
        ram.setW(a1 + 0x14, next())
        ram.setW(a1 + 0x18, next())
        ram.setW(a1 + 0x1c, next())
        val sw12 = next()
        ram.setW(a1 + 0x12, sw12)
        ram.setB(B213760 + sw12, next())
        ram.setW(a1 + 0x1a, next())
        a1 += 0x1e
      case t @ (0x29 | 0x28) => // ')' '(': dragon
        ram.setB(a2, if t == 0x29 then 0x14 else 0x15)
        ram.setW(a1 + 0x16, if t == 0x29 then 0 else 1)
        newRecord(a1, a3, Dragon)
        recordPosition(a3, a1)
        ram.setW(a1 + 0x14, next())
        ram.setW(a1 + 0x18, next())
        ram.setW(a1 + 0x1c, next())
        ram.setW(a1 + 0x1e, nextWord())
        ram.setW(a1 + 0x20, nextWord())
        ram.setW(a1 + 0x22, next() << 1)
        ram.setW(a1 + 0x24, next() << 1)
        val sw12 = next()
        ram.setW(a1 + 0x12, sw12)
        ram.setB(B213760 + sw12, next())
        ram.setW(a1 + 0x1a, next())
        a1 += 0x26
      case 0x54 => // 'T': checkpoint ("point of no return") or multiple-action point
        if ram.b(a6) == 0xff then
          if a2 == ram.ptr(NonRetourAdr) then
            // The man restarts here: state saved when he went through
            a6 += 1
            ram.setB(a2, next())
            ram.setB(a2 - 0x50, next())
            ram.setW(TombWord0, nextWord())
            ram.setW(TombWord1, nextWord())
            ram.setW(NbArc, next())
            ram.setW(NbFleches, next())
            ram.setW(NbCle, next())
            ram.setW(NbSeconds, next())
            ram.setW(FixationSynchro, nextWord())
            ram.setW(FixationCounter, 0)
            ram.setW(FixationCounter, next())
            a6 += 1
          else
            ram.setL(a3, a6)
            a6 += 0xf
            ram.setB(a2, 0x2f)
        else
          ram.setL(a3, a6)
          a6 += 2
          ram.setB(a2, 0x2f)
      case 0x2d => // '-': raft
        ram.setB(a2, 0x30)
        newRecord(a1, a3, Raft)
        val (y, x) = recordPosition(a3, a1)
        ram.setW(a1 + 0x1c, y)
        ram.setW(a1 + 0x1a, x)
        ram.setW(a1 + 0x12, w(x - w(next() << 4)))
        ram.setW(a1 + 0x14, w(w(next() << 4) + x + 1))
        ram.setW(a1 + 0x16, w(sb(next())))
        ram.setW(a1 + 0x18, w(sb(next())))
        a1 += 0x1e
      case 0x2b => // '+': stone appearing and disappearing
        val block = next()
        ram.setB(a2, block)
        ram.setW(a1 + 0x12, block)
        newRecord(a1, a3, ToggleStone)
        recordPosition(a3, a1)
        ram.setW(a1 + 0x14, next())
        ram.setW(a1 + 0x16, next())
        ram.setW(a1 + 0x18, next())
        ram.setW(a1 + 0x1e, next())
        ram.setW(a1 + 0x20, next())
        val sw = next()
        ram.setW(a1 + 0x1a, sw)
        ram.setB(B213760 + sw, next())
        ram.setB(a1 + 0x1c, next())
        a1 += 0x22
      case 0x4d => // 'M': pharaoh (mummy)
        ram.setB(a2, 0x30)
        newRecord(a1, a3, Pharaoh)
        val (y, x) = recordPosition(a3, a1)
        ram.setW(a1 + 0x14, w(y - 8))
        ram.setW(a1 + 0x12, x)
        ram.setW(a1 + 0x16, w(x - w(next() << 4)))
        ram.setW(a1 + 0x18, w(w(next() << 4) + x + 1))
        ram.setW(a1 + 0x1a, next())
        ram.setW(a1 + 0x1c, next())
        ram.setW(a1 + 0x22, next())
        ram.setW(a1 + 0x20, 0)
        ram.setW(a1 + 0x1e, 0)
        a1 += 0x24
      case 0x51 => // 'Q': killing stones
        ram.setB(a2, 0x30)
        newRecord(a1, a3, KillingStones)
        val (y, x) = recordPosition(a3, a1)
        ram.setW(a1 + 0x1a, y)
        ram.setW(a1 + 0x18, x)
        ram.setW(a1 + 0x16, next())
        ram.setB(a1 + 0x14, next())
        ram.setW(a1 + 0x1e, w(sb(next())))
        ram.setW(a1 + 0x22, nextWord())
        ram.setW(a1 + 0x12, 0)
        ram.setW(a1 + 0x1c, 0)
        ram.setW(a1 + 0x20, 0)
        ram.setW(a1 + 0x24, 0)
        a1 += 0x26
      case 0x58 => // 'X': sphere (teleport)
        ram.setB(a2, 0x30)
        newRecord(a1, a3, Sphere)
        recordPosition(a3, a1)
        ram.setW(a1 + 0x14, nextWord())
        ram.setW(a1 + 0x16, nextWord())
        ram.setW(a1 + 0x18, next())
        ram.setW(a1 + 0x12, 0)
        a1 += 0x1a
      case 0x53 => // 'S': snake
        ram.setB(a2, 0x30)
        newRecord(a1, a3, Snake)
        val (y, x) = recordPosition(a3, a1)
        ram.setW(a1 + 0x14, w(y - 8))
        ram.setW(a1 + 0x12, x)
        ram.setW(a1 + 0x16, w(x - w(next() << 4)))
        ram.setW(a1 + 0x18, w(w(next() << 4) + x + 1))
        ram.setW(a1 + 0x1a, next())
        ram.setW(a1 + 0x1c, next())
        ram.setW(a1 + 0x20, next())
        ram.setW(a1 + 0x26, next())
        ram.setW(a1 + 0x24, 0)
        ram.setW(a1 + 0x1e, 0)
        ram.setW(a1 + 0x22, 0)
        a1 += 0x28
      case _ => ()
    (a1, a6)

  /** `L00276` (3430): the fixations, decoded into B197120. */
  private def l00276(start: Int): Unit =
    var a6 = start
    var a0 = B197120
    ram.setL(D874, a0)
    def next(): Int =
      val v = ram.b(a6)
      a6 += 1
      v
    var done = false
    while !done do
      val t = next()
      if t == 0xff then done = true
      else
        val time = w(((t << 8) | next()) + 0x8000)
        ram.setW(a0, time)
        a0 += 2
        if time != 0xffff then
          var d0 = next()
          var inScreen = true
          while inScreen do
            // A screen: column in the high nibble (bit 7 set), line in the 2 low bits
            val line = d0 & 3
            val base = w(b((d0 >>> 4) - 8) * 0xa + line * 0x3c0)
            var group = true
            var d1 = next()
            while group do
              ram.setW(a0, d1)
              a0 += 2
              val c = next()
              ram.setW(a0, w((c & 0xf) * 0x50 + (c >>> 4) + base))
              a0 += 2
              d1 = next()
              if d1 == 0xff then
                group = false
                inScreen = false
              else if (d1 & 0x80) != 0 then
                group = false
                d0 = d1
    ram.setW(a0, 0xffff)

  /** `do_fixe_elements` (3476): the parts of the tomb about to change flicker (cells swapped
    * between the front and the alternative table) for 15 frames, then the change is final.
    */
  def doFixeElements(): Unit =
    var a0 = ram.ptr(D874)
    var done = false
    while !done do
      if ram.w(a0) == 0xffff then done = true
      else
        val synchro = ram.w(FixationSynchro)
        val time = ram.w(a0)
        a0 += 2
        if synchro < time then done = true
        else if w(synchro - time) < 0xf then
          // Flicker: swap the cells
          var group = true
          while group do
            val count = ram.w(a0)
            val offset = ram.w(a0 + 2)
            a0 += 4
            val a1 = TombTable1 + sw(offset)
            val a2 = TombTable2 + sw(offset)
            for i <- 0 to count do
              val t = ram.bytes(a1 + i)
              ram.bytes(a1 + i) = ram.bytes(a2 + i)
              ram.bytes(a2 + i) = t
            group = (ram.w(a0) & 0x8000) == 0
        else
          // The change is final: go to the next one
          while (ram.w(a0) & 0x8000) == 0 do a0 += 2
          prepareSound(0xf)
          ram.setL(D874, a0)

  /** Derivation of the edges of the walls ($3A-$3E), using the back screen as a buffer (2724-2842).
    */
  private def deriveWallEdges(): Unit =
    def isWall(c: Int) = c == 0x19 || c == 0x1a
    val buf = ram.ptr(PhysScreen2)
    var a0 = TombTable1
    var a2 = buf
    for _ <- 0 to 0x4f do
      ram.setB(a2, if isWall(ram.b(a0)) then ram.b(a0) else 0xff)
      a2 += 1
      a0 += 1
    for _ <- 0 to 0x2e; _ <- 0 to 0x4f do
      val c = ram.b(a0)
      if !isWall(c) then ram.setB(a2, 0xff)
      else
        // Mask of the 8 neighbours which are walls
        var d1 = 0
        for k <- 0 to 7 do
          if isWall(ram.b(a0 + ram.sw(D730 + 2 * k))) then d1 = (d1 & 0xff00) | b(d1 + 1) // ADDQ.B
          d1 = w(d1 + d1)
        d1 = d1 >>> 1
        var a3 = D684
        var result = -1
        while result < 0 do
          if ram.b(a3) == 0xff then result = ram.b(a0)
          else if (d1 & ram.b(a3)) == ram.b(a3 + 1) then result = ram.b(a3 + 2)
          else a3 += 3
        ram.setB(a2, result)
      a2 += 1
      a0 += 1
    var p = buf + 0x50
    for _ <- 0 to 0xeaf do
      if isWall(ram.b(p)) then
        val above = ram.b(p - 0x50)
        if above == 0x3d || above == 0x3b then
          if ram.b(p - 1) == 0xff then ram.setB(p, 0x3d)
        else if above == 0x3e || above == 0x3c then if ram.b(p + 1) == 0xff then ram.setB(p, 0x3e)
      p += 1
    p -= 0x51
    for _ <- 0 to 0xe5f do
      val c = ram.b(p)
      if c == 0x3a || isWall(c) then
        val below = ram.b(p + 0x50)
        if below == 0x3e then
          if ram.b(p + 1) == 0xff then ram.setB(p, if ram.b(p - 0x50) == 0xff then 0x3c else 0x3e)
        else if below == 0x3d then
          if ram.b(p - 1) == 0xff then ram.setB(p, if ram.b(p - 0x50) == 0xff then 0x3b else 0x3d)
      p -= 1
    var src = buf
    for i <- 0 until 0xf00 do
      val c = ram.b(src)
      src += 1
      if c != 0xff then ram.setB(TombTable1 + i, c)

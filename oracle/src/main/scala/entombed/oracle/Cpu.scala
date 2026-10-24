package entombed.oracle

import Operand.*

/** Native routines reached by `jsr`/`jmp` to a hook address (see [[Memory.hookAddress]]). */
trait Hooks:
  def call(cpu: Cpu, n: Int): Unit

final class CpuError(message: String) extends Exception(message)

/** Interpreter for the subset of the 68000 instruction set used by Entombed.
  *
  * Condition codes N, Z, C, X are maintained exactly; V is computed for the arithmetic
  * instructions but never tested by the game.
  */
final class Cpu(val program: Program, hooks: Hooks):
  val mem: Array[Byte] = program.memory.clone()
  val d: Array[Int] = new Array[Int](8)
  val a: Array[Int] = new Array[Int](8)
  var pc: Int = program.entry
  var fn, fz, fv, fc, fx = false

  /** Set by hooks to make [[run]] return after the current instruction. */
  var yieldRequested = false
  var halted = false
  var executed: Long = 0L

  a(7) = Memory.StackTop

  private val code = program.code

  // ----- Memory -----

  private inline def check(addr: Int): Int =
    val ad = addr & 0xffffff
    if ad >= Memory.Size then
      throw new CpuError(f"Bad address $addr%08x at ${code(pc - 1)}")
    ad

  def rb(addr: Int): Int = mem(check(addr)) & 0xff
  def rw(addr: Int): Int =
    val ad = check(addr)
    ((mem(ad) & 0xff) << 8) | (mem(ad + 1) & 0xff)
  def rl(addr: Int): Int =
    val ad = check(addr)
    ((mem(ad) & 0xff) << 24) | ((mem(ad + 1) & 0xff) << 16) | ((mem(ad + 2) & 0xff) << 8) |
      (mem(ad + 3) & 0xff)
  def wb(addr: Int, v: Int): Unit = mem(check(addr)) = v.toByte
  def ww(addr: Int, v: Int): Unit =
    val ad = check(addr)
    mem(ad) = (v >> 8).toByte
    mem(ad + 1) = v.toByte
  def wl(addr: Int, v: Int): Unit =
    val ad = check(addr)
    mem(ad) = (v >> 24).toByte
    mem(ad + 1) = (v >> 16).toByte
    mem(ad + 2) = (v >> 8).toByte
    mem(ad + 3) = v.toByte

  private def readMem(addr: Int, size: Int): Int = size match
    case 1 => rb(addr)
    case 2 => rw(addr)
    case _ => rl(addr)

  private def writeMem(addr: Int, size: Int, v: Int): Unit = size match
    case 1 => wb(addr, v)
    case 2 => ww(addr, v)
    case _ => wl(addr, v)

  def push(v: Int): Unit =
    a(7) -= 4
    wl(a(7), v)

  def pop(): Int =
    val v = rl(a(7))
    a(7) += 4
    v

  // ----- Sizes and flags -----

  private inline def mask(size: Int): Int = size match
    case 1 => 0xff
    case 2 => 0xffff
    case _ => -1

  private inline def msb(size: Int): Int = size match
    case 1 => 0x80
    case 2 => 0x8000
    case _ => 0x80000000

  private inline def sext(v: Int, size: Int): Int = size match
    case 1 => v.toByte.toInt
    case 2 => v.toShort.toInt
    case _ => v

  private inline def setNZ(v: Int, size: Int): Unit =
    fn = (v & msb(size)) != 0
    fz = (v & mask(size)) == 0

  private inline def logicFlags(v: Int, size: Int): Unit =
    setNZ(v, size)
    fv = false
    fc = false

  private def addFlags(s: Int, dv: Int, r: Int, size: Int): Unit =
    val m = mask(size)
    setNZ(r, size)
    fc = (java.lang.Integer.toUnsignedLong(s & m) + java.lang.Integer.toUnsignedLong(dv & m)) >
      java.lang.Integer.toUnsignedLong(m)
    fv = ((s ^ r) & (dv ^ r) & msb(size)) != 0
    fx = fc

  private def subFlags(s: Int, dv: Int, r: Int, size: Int, setX: Boolean): Unit =
    val m = mask(size)
    setNZ(r, size)
    fc = Integer.compareUnsigned(dv & m, s & m) < 0
    fv = ((s ^ dv) & (r ^ dv) & msb(size)) != 0
    if setX then fx = fc

  // ----- Effective addresses -----

  private inline def incr(reg: Int, size: Int): Int = if size == 1 && reg == 7 then 2 else size

  private def indexValue(o: Operand): Int =
    val r = if o.indexReg < 8 then d(o.indexReg) else a(o.indexReg - 8)
    if o.indexLong then r else r.toShort.toInt

  /** Address of a memory operand, applying (An)+ and -(An) side effects. */
  private def ea(o: Operand, size: Int): Int = o.kind match
    case Ind => a(o.reg)
    case PostInc =>
      val ad = a(o.reg)
      a(o.reg) += incr(o.reg, size)
      ad
    case PreDec =>
      a(o.reg) -= incr(o.reg, size)
      a(o.reg)
    case Disp  => a(o.reg) + o.value
    case Index => a(o.reg) + o.value + indexValue(o)
    case Abs   => o.value
    case _     => throw new CpuError(s"Not a memory operand at ${code(pc - 1)}")

  private def controlAddress(o: Operand): Int = o.kind match
    case Ind   => a(o.reg)
    case Disp  => a(o.reg) + o.value
    case Index => a(o.reg) + o.value + indexValue(o)
    case Abs   => o.value
    case _     => throw new CpuError(s"Not a control operand at ${code(pc - 1)}")

  /** Reads an operand, returning the value masked to `size` (unsigned). */
  private def read(o: Operand, size: Int): Int = o.kind match
    case DReg => d(o.reg) & mask(size)
    case AReg => a(o.reg) & mask(size)
    case Imm  => o.value & mask(size)
    case _    => readMem(ea(o, size), size)

  private def writeD(reg: Int, size: Int, v: Int): Unit = size match
    case 1 => d(reg) = (d(reg) & 0xffffff00) | (v & 0xff)
    case 2 => d(reg) = (d(reg) & 0xffff0000) | (v & 0xffff)
    case _ => d(reg) = v

  private def write(o: Operand, size: Int, v: Int): Unit = o.kind match
    case DReg => writeD(o.reg, size, v)
    case AReg => a(o.reg) = v
    case _    => writeMem(ea(o, size), size, v)

  /** For read-modify-write instructions: the address (or -1 for a register). */
  private inline def rmwAddress(o: Operand, size: Int): Int =
    if o.kind == DReg || o.kind == AReg then -1 else ea(o, size)

  private inline def readAt(o: Operand, addr: Int, size: Int): Int =
    if addr == -1 then (if o.kind == DReg then d(o.reg) else a(o.reg)) & mask(size)
    else readMem(addr, size)

  private inline def writeAt(o: Operand, addr: Int, size: Int, v: Int): Unit =
    if addr == -1 then
      if o.kind == DReg then writeD(o.reg, size, v) else a(o.reg) = v
    else writeMem(addr, size, v)

  private def reg(r: Int): Int = if r < 8 then d(r) else a(r - 8)
  private def setReg(r: Int, v: Int): Unit = if r < 8 then d(r) = v else a(r - 8) = v

  // ----- Control flow -----

  private def jumpTo(address: Int, isCall: Boolean): Unit =
    if Memory.isHook(address) then
      hooks.call(this, Memory.hookNumber(address))
      if isCall && !halted then pc = Memory.codeIndex(pop())
    else if Memory.isCode(address) then pc = Memory.codeIndex(address)
    else throw new CpuError(f"Jump to non-code address $address%08x at ${code(pc - 1)}")

  /** Current source line (for diagnostics). */
  def currentLine: Int = if pc >= 0 && pc < code.length then code(pc).line else -1

  /** Executes instructions until a hook requests a yield, the program halts, or `limit`
    * instructions have been executed. Returns the number of executed instructions.
    */
  def run(limit: Long): Long =
    yieldRequested = false
    var n = 0L
    while !yieldRequested && !halted && n < limit do
      step()
      n += 1
    executed += n
    n

  // scalastyle:off cyclomatic.complexity method.length
  def step(): Unit =
    val ins = code(pc)
    pc += 1
    val size = ins.size
    val src = ins.src
    val dst = ins.dst
    val op = ins.op
    if op == Op.MOVE then
      val v = read(src, size)
      write(dst, size, v)
      logicFlags(v, size)
    else if op == Op.MOVEA then
      a(dst.reg) = sext(read(src, size), size)
    else if op == Op.ADD || op == Op.ADDI || (op == Op.ADDQ && dst.kind != AReg) then
      val s = read(src, size)
      val addr = rmwAddress(dst, size)
      val dv = readAt(dst, addr, size)
      val r = (dv + s) & mask(size)
      writeAt(dst, addr, size, r)
      addFlags(s, dv, r, size)
    else if op == Op.SUB || op == Op.SUBI || (op == Op.SUBQ && dst.kind != AReg) then
      val s = read(src, size)
      val addr = rmwAddress(dst, size)
      val dv = readAt(dst, addr, size)
      val r = (dv - s) & mask(size)
      writeAt(dst, addr, size, r)
      subFlags(s, dv, r, size, setX = true)
    else if op == Op.ADDA || op == Op.ADDQ then
      a(dst.reg) += (if op == Op.ADDQ then src.value else sext(read(src, size), size))
    else if op == Op.SUBA || op == Op.SUBQ then
      a(dst.reg) -= (if op == Op.SUBQ then src.value else sext(read(src, size), size))
    else if op == Op.CMP || op == Op.CMPI then
      val s = read(src, size)
      val dv = read(dst, size)
      subFlags(s, dv, (dv - s) & mask(size), size, setX = false)
    else if op == Op.CMPA then
      val s = sext(read(src, size), size)
      val dv = a(dst.reg)
      subFlags(s, dv, dv - s, 4, setX = false)
    else if op == Op.AND || op == Op.ANDI || op == Op.OR || op == Op.ORI || op == Op.EOR ||
      op == Op.EORI
    then
      val s = read(src, size)
      val addr = rmwAddress(dst, size)
      val dv = readAt(dst, addr, size)
      val r =
        if op == Op.AND || op == Op.ANDI then dv & s
        else if op == Op.OR || op == Op.ORI then dv | s
        else dv ^ s
      writeAt(dst, addr, size, r)
      logicFlags(r, size)
    else if op == Op.CLR then
      write(src, size, 0)
      fn = false; fz = true; fv = false; fc = false
    else if op == Op.TST then
      logicFlags(read(src, size), size)
    else if op == Op.NEG then
      val addr = rmwAddress(src, size)
      val dv = readAt(src, addr, size)
      val r = (0 - dv) & mask(size)
      writeAt(src, addr, size, r)
      subFlags(dv, 0, r, size, setX = true)
    else if op == Op.EXT then
      val r = src.reg
      if size == 2 then writeD(r, 2, d(r).toByte.toInt) else d(r) = d(r).toShort.toInt
      logicFlags(d(r), size)
    else if op == Op.SWAP then
      val r = src.reg
      d(r) = (d(r) >>> 16) | (d(r) << 16)
      logicFlags(d(r), 4)
    else if op >= Op.ASL && op <= Op.ROR then shift(op, size, src, dst)
    else if op == Op.MULU then
      val r = (read(src, 2) & 0xffff) * (d(dst.reg) & 0xffff)
      d(dst.reg) = r
      logicFlags(r, 4)
    else if op == Op.MULS then
      val r = read(src, 2).toShort.toInt * d(dst.reg).toShort.toInt
      d(dst.reg) = r
      logicFlags(r, 4)
    else if op == Op.DIVU then
      val divisor = read(src, 2) & 0xffff
      if divisor == 0 then throw new CpuError(s"Division by zero at $ins")
      val dividend = java.lang.Integer.toUnsignedLong(d(dst.reg))
      val q = dividend / divisor
      fc = false
      if q > 0xffff then fv = true
      else
        val rem = dividend % divisor
        d(dst.reg) = ((rem.toInt & 0xffff) << 16) | (q.toInt & 0xffff)
        fv = false
        fn = (q & 0x8000) != 0
        fz = q == 0
    else if op == Op.DIVS then
      val divisor = read(src, 2).toShort.toInt
      if divisor == 0 then throw new CpuError(s"Division by zero at $ins")
      val dividend = d(dst.reg)
      val q = dividend / divisor
      fc = false
      if q < -32768 || q > 32767 then fv = true
      else
        val rem = dividend % divisor
        d(dst.reg) = ((rem & 0xffff) << 16) | (q & 0xffff)
        fv = false
        fn = q < 0
        fz = q == 0
    else if op == Op.ABCD then
      val s = d(src.reg) & 0xff
      val dv = d(dst.reg) & 0xff
      var r = (dv & 0x0f) + (s & 0x0f) + (if fx then 1 else 0)
      if r > 9 then r += 6
      r += (dv & 0xf0) + (s & 0xf0)
      if r > 0x99 then
        r -= 0xa0
        fc = true
      else fc = false
      fx = fc
      r &= 0xff
      if r != 0 then fz = false
      writeD(dst.reg, 1, r)
    else if op == Op.BTST || op == Op.BSET || op == Op.BCLR then
      val bitNum = if src.kind == Imm then src.value else d(src.reg)
      if dst.kind == DReg then
        val b = bitNum & 31
        fz = ((d(dst.reg) >> b) & 1) == 0
        if op == Op.BSET then d(dst.reg) |= 1 << b
        else if op == Op.BCLR then d(dst.reg) &= ~(1 << b)
      else
        val b = bitNum & 7
        val addr = ea(dst, 1)
        val v = rb(addr)
        fz = ((v >> b) & 1) == 0
        if op == Op.BSET then wb(addr, v | (1 << b))
        else if op == Op.BCLR then wb(addr, v & ~(1 << b))
    else if op == Op.DBRA then
      val r = src.reg
      val w = (d(r) - 1) & 0xffff
      writeD(r, 2, w)
      if w != 0xffff then pc = ins.target
    else if op == Op.BRA then pc = ins.target
    else if op == Op.BEQ then { if fz then pc = ins.target }
    else if op == Op.BNE then { if !fz then pc = ins.target }
    else if op == Op.BCS then { if fc then pc = ins.target }
    else if op == Op.BCC then { if !fc then pc = ins.target }
    else if op == Op.BMI then { if fn then pc = ins.target }
    else if op == Op.BPL then { if !fn then pc = ins.target }
    else if op == Op.BSR then
      push(Memory.codeAddress(pc))
      pc = ins.target
    else if op == Op.JSR then
      val target = controlAddress(src)
      push(Memory.codeAddress(pc))
      jumpTo(target, isCall = true)
    else if op == Op.JMP then jumpTo(controlAddress(src), isCall = false)
    else if op == Op.RTS then jumpTo(pop(), isCall = false)
    else if op == Op.LEA then a(dst.reg) = controlAddress(src)
    else if op == Op.MOVEM then movem(ins)
    else if op == Op.NOP then ()
    else throw new CpuError(s"Unimplemented instruction $ins")
  // scalastyle:on

  private def movem(ins: Instruction): Unit =
    val size = ins.size
    if ins.src.kind == RegList then
      val m = ins.src.value
      val dst = ins.dst
      if dst.kind == PreDec then
        var addr = a(dst.reg)
        var r = 15
        while r >= 0 do
          if (m & (1 << r)) != 0 then
            addr -= size
            writeMem(addr, size, reg(r))
          r -= 1
        a(dst.reg) = addr
      else
        var addr = controlAddress(dst)
        var r = 0
        while r < 16 do
          if (m & (1 << r)) != 0 then
            writeMem(addr, size, reg(r))
            addr += size
          r += 1
    else
      val m = ins.dst.value
      val src = ins.src
      var addr = if src.kind == PostInc then a(src.reg) else controlAddress(src)
      var r = 0
      while r < 16 do
        if (m & (1 << r)) != 0 then
          setReg(r, sext(readMem(addr, size), size))
          addr += size
        r += 1
      if src.kind == PostInc then a(src.reg) = addr

  private def shift(op: Int, size: Int, src: Operand, dst: Operand): Unit =
    if dst.kind != DReg then throw new CpuError(s"Memory shift not supported at ${code(pc - 1)}")
    val count = if src.kind == Imm then src.value else d(src.reg) & 63
    val m = mask(size)
    val top = msb(size)
    var v = d(dst.reg) & m
    var c = false
    var i = 0
    while i < count do
      if op == Op.ASL || op == Op.LSL then
        c = (v & top) != 0
        v = (v << 1) & m
      else if op == Op.ASR then
        c = (v & 1) != 0
        v = ((v >>> 1) | (v & top)) & m
      else if op == Op.LSR then
        c = (v & 1) != 0
        v = (v >>> 1) & m
      else if op == Op.ROL then
        c = (v & top) != 0
        v = ((v << 1) | (if c then 1 else 0)) & m
      else
        c = (v & 1) != 0
        v = ((v >>> 1) | (if c then top else 0)) & m
      i += 1
    writeD(dst.reg, size, v)
    setNZ(v, size)
    fv = false
    fc = c
    if count > 0 && op != Op.ROL && op != Op.ROR then fx = c

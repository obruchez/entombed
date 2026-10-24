package entombed.oracle

import scala.collection.mutable

/** Operand of a decoded 68000 instruction. */
final class Operand(
    val kind: Int,
    val reg: Int = 0,
    val value: Int = 0,
    val indexReg: Int = 0,
    val indexLong: Boolean = false
):
  override def toString: String = s"Operand($kind,$reg,$value,$indexReg,$indexLong)"

object Operand:
  inline val DReg = 0
  inline val AReg = 1
  inline val Ind = 2 // (An)
  inline val PostInc = 3 // (An)+
  inline val PreDec = 4 // -(An)
  inline val Disp = 5 // d(An)
  inline val Index = 6 // d(An,Xn.s), indexReg 0-7 = D0-D7, 8-15 = A0-A7
  inline val Abs = 7 // absolute address
  inline val Imm = 8 // #value
  inline val RegList = 9 // MOVEM register mask, bit 0 = D0 ... bit 15 = A7

  val None: Operand = new Operand(-1)

/** A decoded instruction. `target` is the instruction index of a branch destination. */
final class Instruction(
    val op: Int,
    val size: Int,
    val src: Operand,
    val dst: Operand,
    val target: Int,
    val line: Int,
    val text: String
):
  override def toString: String = s"$line: $text"

/** Result of assembling the source: decoded code plus the initial memory image. */
final class Program(
    val code: Array[Instruction],
    val memory: Array[Byte],
    val symbols: Map[String, Int],
    val entry: Int
):
  /** Address of a symbol (labels are case-insensitive). */
  def address(name: String): Int =
    symbols.getOrElse(name.toLowerCase, throw new NoSuchElementException(s"Unknown symbol $name"))

  /** Index of the first instruction of a code label. */
  def codeIndex(name: String): Int = Memory.codeIndex(address(name))

  /** Instruction index of the first instruction at or after a source line. */
  def indexOfLine(line: Int): Int = code.indexWhere(_.line >= line)

object Op:
  val Names: Vector[String] = Vector(
    "MOVE", "MOVEA", "MOVEM", "ADD", "ADDA", "ADDI", "ADDQ", "SUB", "SUBA", "SUBI", "SUBQ",
    "CMP", "CMPA", "CMPI", "AND", "ANDI", "OR", "ORI", "EOR", "EORI", "CLR", "TST", "NEG",
    "EXT", "SWAP", "ASL", "ASR", "LSL", "LSR", "ROL", "ROR", "MULU", "MULS", "DIVU", "DIVS",
    "ABCD", "BTST", "BSET", "BCLR", "DBRA", "BRA", "BSR", "BEQ", "BNE", "BCS", "BCC", "BMI",
    "BPL", "JSR", "JMP", "RTS", "LEA", "NOP"
  )
  private val byName: Map[String, Int] = Names.zipWithIndex.toMap
  def apply(name: String): Int = byName(name)

  val MOVE = apply("MOVE"); val MOVEA = apply("MOVEA"); val MOVEM = apply("MOVEM")
  val ADD = apply("ADD"); val ADDA = apply("ADDA"); val ADDI = apply("ADDI")
  val ADDQ = apply("ADDQ"); val SUB = apply("SUB"); val SUBA = apply("SUBA")
  val SUBI = apply("SUBI"); val SUBQ = apply("SUBQ"); val CMP = apply("CMP")
  val CMPA = apply("CMPA"); val CMPI = apply("CMPI"); val AND = apply("AND")
  val ANDI = apply("ANDI"); val OR = apply("OR"); val ORI = apply("ORI"); val EOR = apply("EOR")
  val EORI = apply("EORI"); val CLR = apply("CLR"); val TST = apply("TST"); val NEG = apply("NEG")
  val EXT = apply("EXT"); val SWAP = apply("SWAP"); val ASL = apply("ASL"); val ASR = apply("ASR")
  val LSL = apply("LSL"); val LSR = apply("LSR"); val ROL = apply("ROL"); val ROR = apply("ROR")
  val MULU = apply("MULU"); val MULS = apply("MULS"); val DIVU = apply("DIVU")
  val DIVS = apply("DIVS"); val ABCD = apply("ABCD"); val BTST = apply("BTST")
  val BSET = apply("BSET"); val BCLR = apply("BCLR"); val DBRA = apply("DBRA")
  val BRA = apply("BRA"); val BSR = apply("BSR"); val BEQ = apply("BEQ"); val BNE = apply("BNE")
  val BCS = apply("BCS"); val BCC = apply("BCC"); val BMI = apply("BMI"); val BPL = apply("BPL")
  val JSR = apply("JSR"); val JMP = apply("JMP"); val RTS = apply("RTS"); val LEA = apply("LEA")
  val NOP = apply("NOP")

  val Branches: Set[Int] = Set(BRA, BSR, BEQ, BNE, BCS, BCC, BMI, BPL)

/** Memory map used by the interpreter. Code is not stored in memory: each instruction gets a
  * pseudo address in a separate region so that return addresses and routine pointers (`dc.l
  * do_radeau`, `move.l #aff_bloc32x24_main,...`) work as on the real machine.
  */
object Memory:
  val Size = 0x80000
  val TextBase = 0x1000
  val DataBase = 0x2000
  val StackTop = 0x7fff0
  val CodeBase = 0x00e00000
  val HookBase = 0x00f00000

  def codeAddress(index: Int): Int = CodeBase + 4 * index
  def codeIndex(address: Int): Int = (address - CodeBase) >> 2
  def isCode(address: Int): Boolean = address >= CodeBase && address < HookBase
  def isHook(address: Int): Boolean = address >= HookBase && address < HookBase + 0x1000
  def hookAddress(n: Int): Int = HookBase + 4 * n
  def hookNumber(address: Int): Int = (address - HookBase) >> 2

/** Assembler for the dialect of the 1995 Atari/Smaky sources (Profimat-style syntax).
  *
  * Supported: `equ`, `TEXT`/`DATA`/`BSS`, `dc.b/w/l` (numbers, strings, expressions), `ds.b/w/l`,
  * `align.w`, `ibytes "file"`, `END`, global labels (case-insensitive, optional colon) and local
  * labels (`\name`, scoped to the previous global label), expressions with `+ - * /` and
  * parentheses, `$hex`, `%binary`, decimal and `"c"` character constants.
  *
  * @param files contents of the files included with `ibytes`, keyed by upper-case base name
  */
final class Assembler(source: String, files: Map[String, Array[Byte]]):
  import Assembler.*

  private sealed trait Section
  private case object Text extends Section
  private case object Data extends Section
  private case object Bss extends Section

  private final case class Stmt(line: Int, mnemonic: String, operands: String, text: String)

  private val symbols = mutable.HashMap.empty[String, Int]
  private val equs = mutable.LinkedHashMap.empty[String, (String, String)] // name -> (expr, scope)

  def assemble(): Program =
    val lines = source.split("\n", -1).toVector
    var section: Section = Text
    var scope = ""
    val pending = mutable.ArrayBuffer.empty[String]
    // Pass 1: layout
    var textData = Memory.TextBase
    var dataPtr = 0 // relative to the start of the DATA section
    var bssPtr = 0
    var codeCount = 0
    val dataBytes = mutable.ArrayBuffer.empty[(Int, Stmt, String)] // DATA directives, with offset
    val textBytes = mutable.ArrayBuffer.empty[(Int, Stmt, String)]
    val dataLabels = mutable.ArrayBuffer.empty[(String, Int)] // relative to the DATA section
    val bssLabels = mutable.ArrayBuffer.empty[(String, Int)]
    val codeStmts = mutable.ArrayBuffer.empty[(Stmt, String)]
    var ended = false

    def bindPending(f: String => Unit): Unit =
      pending.foreach(f)
      pending.clear()

    def define(name: String, address: Int): Unit =
      if symbols.contains(name) then throw AsmError(s"Duplicate symbol $name")
      symbols(name) = address

    for (raw, i) <- lines.zipWithIndex if !ended do
      val lineNo = i + 1
      val noComment = stripComment(raw.replace("\r", ""))
      if noComment.trim.nonEmpty then
        var rest = noComment
        var label: String = null
        if !rest.head.isWhitespace then
          val m = LabelRe.findPrefixMatchOf(rest).getOrElse(throw AsmError(s"Bad label at $lineNo"))
          label = m.group(1)
          rest = rest.substring(m.end)
        val trimmed = rest.trim
        val (mn, ops) =
          val k = trimmed.indexWhere(_.isWhitespace)
          if k < 0 then (trimmed, "") else (trimmed.substring(0, k), trimmed.substring(k).trim)
        val mnU = mn.toUpperCase
        // A label alone on a line which is a section keyword
        val (labelName, mnemonic, operands) =
          if label != null && mn.isEmpty && Set("TEXT", "DATA", "BSS", "END")(label.toUpperCase) then
            (null, label.toUpperCase, "")
          else (label, mnU, ops)
        val stmt = Stmt(lineNo, mnemonic, operands, raw.trim)
        if mnemonic == "EQU" then
          equs(labelName.toLowerCase) = (operands, scope)
        else
          if labelName != null then
            // A global label opens a new scope for the local (\name) labels that follow it
            if !labelName.startsWith("\\") then scope = labelName.toLowerCase
            pending += qualify(labelName, scope)
          mnemonic match
            case "" => ()
            case "TEXT" | "DATA" | "BSS" =>
              bindPending(n =>
                section match
                  case Text => define(n, Memory.codeAddress(codeCount))
                  case Data => dataLabels += n -> dataPtr
                  case Bss  => bssLabels += n -> bssPtr
              )
              section = mnemonic match
                case "TEXT" => Text
                case "DATA" => Data
                case _      => Bss
            case "END" => ended = true
            case m if m.startsWith("ALIGN") =>
              section match
                case Text => textData = (textData + 1) & ~1
                case Data => dataPtr = (dataPtr + 1) & ~1
                case Bss  => bssPtr = (bssPtr + 1) & ~1
            case m if m.startsWith("DC.") || m.startsWith("DS.") || m == "IBYTES" =>
              val size = directiveSize(m, operands, lineNo)
              section match
                case Text =>
                  bindPending(n => define(n, textData))
                  textBytes += ((textData, stmt, scope))
                  textData += size
                case Data =>
                  bindPending(n => dataLabels += n -> dataPtr)
                  dataBytes += ((dataPtr, stmt, scope))
                  dataPtr += size
                case Bss =>
                  bindPending(n => bssLabels += n -> bssPtr)
                  bssPtr += size
            case _ =>
              if section != Text then throw AsmError(s"Instruction outside TEXT at line $lineNo")
              bindPending(n => define(n, Memory.codeAddress(codeCount)))
              codeStmts += ((stmt, scope))
              codeCount += 1

    if textData > Memory.DataBase then throw AsmError("TEXT data overflow")
    val dataBase = Memory.DataBase
    val bssBase = (dataBase + dataPtr + 0xff) & ~0xff
    if bssBase + bssPtr > Memory.StackTop - 0x4000 then throw AsmError("Memory overflow")
    dataLabels.foreach((n, off) => define(n, dataBase + off))
    bssLabels.foreach((n, off) => define(n, bssBase + off))

    // Resolve equates (may reference labels defined anywhere)
    def resolveEqu(name: String, depth: Int): Int =
      symbols.get(name) match
        case Some(v) => v
        case None =>
          if depth > 50 then throw AsmError(s"Recursive equate $name")
          val (expr, sc) = equs.getOrElse(name, throw AsmError(s"Unknown symbol $name"))
          val v = evaluate(expr, sc, n => resolveEqu(n, depth + 1))
          symbols(name) = v
          v
    equs.keys.toList.foreach(n => resolveEqu(n, 0))

    // Pass 2: memory image
    val memory = new Array[Byte](Memory.Size)
    def emit(at: Int, stmt: Stmt, scope: String): Unit =
      var p = at
      val m = stmt.mnemonic
      if m == "IBYTES" then
        val name = unquote(stmt.operands).split("[\\\\/:]").last.toUpperCase
        val bytes = files.getOrElse(name, throw AsmError(s"Missing included file $name"))
        System.arraycopy(bytes, 0, memory, p, bytes.length)
      else if m.startsWith("DC.") then
        val size = sizeOf(m.charAt(3))
        for item <- splitOperands(stmt.operands) do
          if item.startsWith("\"") then
            for c <- unquote(item) do
              memory(p) = c.toByte
              p += 1
          else
            val v = evaluate(item, scope, n => symbol(n, scope))
            writeBig(memory, p, size, v)
            p += size
    textBytes.foreach((a, s, sc) => emit(a, s, sc))
    dataBytes.foreach((off, s, sc) => emit(dataBase + off, s, sc))

    // Pass 3: decode instructions
    val code = codeStmts.map((s, sc) => decode(s, sc)).toArray
    new Program(code, memory, symbols.toMap, Memory.codeIndex(symbol("debut_entombed", "")))

  private def symbol(name: String, scope: String): Int =
    val n = qualify(name, scope)
    symbols.getOrElse(n, throw AsmError(s"Unknown symbol $name"))

  private def directiveSize(m: String, operands: String, line: Int): Int =
    if m == "IBYTES" then
      val name = unquote(operands).split("[\\\\/:]").last.toUpperCase
      files.getOrElse(name, throw AsmError(s"Missing included file $name at line $line")).length
    else
      val size = sizeOf(m.charAt(3))
      if m.startsWith("DS.") then size * evaluate(operands, "", _ => 0)
      else
        splitOperands(operands).map { item =>
          if item.startsWith("\"") then unquote(item).length else size
        }.sum

  // ----- Instruction decoding -----

  private def decode(s: Stmt, scope: String): Instruction =
    try decodeUnsafe(s, scope)
    catch case e: AsmError => throw AsmError(s"Line ${s.line}: ${e.getMessage} in '${s.text}'")

  private def decodeUnsafe(s: Stmt, scope: String): Instruction =
    val parts = s.mnemonic.split('.')
    var name = parts(0)
    val sizeChar = if parts.length > 1 then parts(1).charAt(0) else ' '
    val ops = splitOperands(s.operands).map(o => parseOperand(o, scope, name))
    val src = if ops.nonEmpty then ops(0) else Operand.None
    val dst = if ops.length > 1 then ops(1) else Operand.None
    var size = sizeChar match
      case 'B' => 1
      case 'W' => 2
      case 'L' => 4
      case 'S' => 0
      case _   => 0
    // Normalisations done by the original assembler
    name match
      case "MOVE" if dst.kind == Operand.AReg => name = "MOVEA"
      case "ADD" if dst.kind == Operand.AReg  => name = "ADDA"
      case "SUB" if dst.kind == Operand.AReg  => name = "SUBA"
      case "CMP" if dst.kind == Operand.AReg  => name = "CMPA"
      case "ADD" if src.kind == Operand.Imm && dst.kind != Operand.AReg => name = "ADDI"
      case "SUB" if src.kind == Operand.Imm && dst.kind != Operand.AReg => name = "SUBI"
      case "CMP" if src.kind == Operand.Imm => name = "CMPI"
      case "AND" if src.kind == Operand.Imm => name = "ANDI"
      case "OR" if src.kind == Operand.Imm  => name = "ORI"
      case "EOR" if src.kind == Operand.Imm => name = "EORI"
      case _ => ()
    if name == "ADDI" && dst.kind == Operand.AReg then name = "ADDA"
    if name == "SUBI" && dst.kind == Operand.AReg then name = "SUBA"
    val op = Op(name)
    if size == 0 then
      size = name match
        case "MULU" | "MULS" | "DIVU" | "DIVS" | "SWAP" | "EXT" => 2
        case "BTST" | "BSET" | "BCLR" => if dst.kind == Operand.DReg then 4 else 1
        case "LEA" | "JSR" | "JMP" => 4
        case "ABCD" => 1
        case _ => 2
    var target = -1
    var source = src
    if Op.Branches(op) || op == Op.DBRA then
      val labelText = if op == Op.DBRA then splitOperands(s.operands)(1) else s.operands.trim
      val address = evaluate(labelText, scope, n => symbol(n, scope))
      if !Memory.isCode(address) then throw AsmError(s"Branch to non-code $labelText")
      target = Memory.codeIndex(address)
      if op != Op.DBRA then source = Operand.None
    new Instruction(op, size, source, if op == Op.DBRA then Operand.None else dst, target, s.line,
      s.text)

  private val RegRe = "(?i)(d[0-7]|a[0-7]|sp)".r
  private val IndRe = "(?i)\\((a[0-7]|sp)\\)".r
  private val PostRe = "(?i)\\((a[0-7]|sp)\\)\\+".r
  private val PreRe = "(?i)-\\((a[0-7]|sp)\\)".r
  private val IndexRe = "(?i)(.*)\\((a[0-7]|sp),\\s*(d[0-7]|a[0-7])(\\.[wl])?\\)".r
  private val DispRe = "(?i)(.*)\\((a[0-7]|sp)\\)".r

  private def regNum(r: String): Int = r.toLowerCase match
    case "sp" => 15
    case x    => (if x.charAt(0) == 'd' then 0 else 8) + (x.charAt(1) - '0')

  private def parseOperand(text: String, scope: String, mnemonic: String): Operand =
    val t = text.trim
    def ev(e: String): Int = evaluate(e, scope, n => symbol(n, scope))
    t match
      case _ if t.startsWith("#") => new Operand(Operand.Imm, value = ev(t.substring(1)))
      case RegRe(r) =>
        val n = regNum(r)
        if mnemonic == "MOVEM" then new Operand(Operand.RegList, value = 1 << n)
        else if n < 8 then new Operand(Operand.DReg, n)
        else new Operand(Operand.AReg, n - 8)
      case _ if mnemonic == "MOVEM" && isRegList(t) =>
        new Operand(Operand.RegList, value = regListMask(t))
      case PostRe(r) => new Operand(Operand.PostInc, regNum(r) - 8)
      case PreRe(r)  => new Operand(Operand.PreDec, regNum(r) - 8)
      case IndRe(r)  => new Operand(Operand.Ind, regNum(r) - 8)
      case IndexRe(d, a, x, sz) =>
        new Operand(Operand.Index, regNum(a) - 8, if d.trim.isEmpty then 0 else ev(d), regNum(x),
          sz != null && sz.equalsIgnoreCase(".l"))
      case DispRe(d, a) if d.trim.nonEmpty => new Operand(Operand.Disp, regNum(a) - 8, ev(d))
      case _ => new Operand(Operand.Abs, value = ev(t))

  private def isRegList(t: String): Boolean =
    t.split("/").forall(p => p.trim.matches("(?i)(d[0-7]|a[0-7]|sp)(-(d[0-7]|a[0-7]|sp))?"))

  private def regListMask(t: String): Int =
    t.split("/").foldLeft(0) { (mask, p) =>
      val rs = p.trim.split("-")
      val from = regNum(rs(0))
      val to = if rs.length > 1 then regNum(rs(1)) else from
      (from to to).foldLeft(mask)((m, r) => m | (1 << r))
    }

  // ----- Expressions -----

  private def evaluate(expr: String, scope: String, lookup: String => Int): Int =
    val p = new ExprParser(expr.trim, name => lookup(qualify(name, scope)))
    val v = p.parseExpr()
    p.expectEnd()
    v

object Assembler:
  final case class AsmError(message: String) extends Exception(message)

  private val LabelRe = "^([A-Za-z_\\\\][A-Za-z0-9_\\\\]*):?".r

  def sizeOf(c: Char): Int = c.toUpper match
    case 'B' => 1
    case 'W' => 2
    case 'L' => 4
    case _   => throw AsmError(s"Bad size $c")

  /** Local labels (`\name`) are scoped to the last global label. */
  def qualify(name: String, scope: String): String =
    val n = name.toLowerCase
    if n.startsWith("\\") then scope + n else n

  def stripComment(s: String): String =
    val sb = new StringBuilder
    var inQuote = false
    var i = 0
    while i < s.length do
      val c = s.charAt(i)
      if c == '"' then inQuote = !inQuote
      if c == ';' && !inQuote then i = s.length
      else
        sb += c
        i += 1
    sb.toString

  def unquote(s: String): String =
    val t = s.trim
    if t.length >= 2 && t.startsWith("\"") && t.endsWith("\"") then t.substring(1, t.length - 1)
    else t

  /** Splits operands on commas that are outside parentheses and quotes. */
  def splitOperands(s: String): Vector[String] =
    if s.trim.isEmpty then Vector.empty
    else
      val out = Vector.newBuilder[String]
      val sb = new StringBuilder
      var depth = 0
      var inQuote = false
      for c <- s do
        if c == '"' then inQuote = !inQuote
        if !inQuote && c == '(' then depth += 1
        if !inQuote && c == ')' then depth -= 1
        if c == ',' && depth == 0 && !inQuote then
          out += sb.toString.trim
          sb.clear()
        else sb += c
      out += sb.toString.trim
      out.result()

  def writeBig(mem: Array[Byte], at: Int, size: Int, v: Int): Unit =
    var i = 0
    while i < size do
      mem(at + i) = (v >> (8 * (size - 1 - i))).toByte
      i += 1

/** Recursive-descent parser for assembler expressions. */
final class ExprParser(s: String, lookup: String => Int):
  private var pos = 0

  private def peek: Char = if pos < s.length then s.charAt(pos) else '\u0000'
  private def skip(): Unit = while pos < s.length && s.charAt(pos).isWhitespace do pos += 1

  def expectEnd(): Unit =
    skip()
    if pos != s.length then throw Assembler.AsmError(s"Unexpected '${s.substring(pos)}' in $s")

  def parseExpr(): Int =
    var v = parseTerm()
    var continue = true
    while continue do
      skip()
      peek match
        case '+' => pos += 1; v += parseTerm()
        case '-' => pos += 1; v -= parseTerm()
        case _   => continue = false
    v

  private def parseTerm(): Int =
    var v = parseUnary()
    var continue = true
    while continue do
      skip()
      peek match
        case '*' => pos += 1; v *= parseUnary()
        case '/' => pos += 1; v /= parseUnary()
        case _   => continue = false
    v

  private def parseUnary(): Int =
    skip()
    peek match
      case '-' => pos += 1; -parseUnary()
      case '+' => pos += 1; parseUnary()
      case _   => parsePrimary()

  private def parsePrimary(): Int =
    skip()
    val c = peek
    if c == '(' then
      pos += 1
      val v = parseExpr()
      skip()
      if peek != ')' then throw Assembler.AsmError(s"Missing ) in $s")
      pos += 1
      v
    else if c == '$' then
      pos += 1
      val start = pos
      while pos < s.length && Character.digit(s.charAt(pos), 16) >= 0 do pos += 1
      java.lang.Long.parseLong(s.substring(start, pos), 16).toInt
    else if c == '%' then
      pos += 1
      val start = pos
      while pos < s.length && (s.charAt(pos) == '0' || s.charAt(pos) == '1') do pos += 1
      java.lang.Long.parseLong(s.substring(start, pos), 2).toInt
    else if c.isDigit then
      val start = pos
      while pos < s.length && s.charAt(pos).isDigit do pos += 1
      java.lang.Long.parseLong(s.substring(start, pos)).toInt
    else if c == '"' then
      pos += 1
      var v = 0
      while pos < s.length && s.charAt(pos) != '"' do
        v = (v << 8) | (s.charAt(pos) & 0xff)
        pos += 1
      pos += 1
      v
    else if c.isLetter || c == '_' || c == '\\' then
      val start = pos
      while pos < s.length && (s.charAt(pos).isLetterOrDigit || s.charAt(pos) == '_' ||
          s.charAt(pos) == '\\')
      do pos += 1
      lookup(s.substring(start, pos))
    else throw Assembler.AsmError(s"Bad expression '$s'")

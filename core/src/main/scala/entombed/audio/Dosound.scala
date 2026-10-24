package entombed.audio

/** Interpreter for Atari ST XBIOS `Dosound` sequences (XBIOS function 32).
  *
  * TOS executes one step of the current sequence at every vertical blank (50 Hz on a PAL ST):
  *
  *   - `$00-$7F rr vv`: writes `vv` to sound register `rr` (only 0-15 are meaningful),
  *   - `$80 vv`: loads `vv` into a temporary register,
  *   - `$81 rr ii ee`: adds `ii` to the temporary register (8 bits, wrapping), writes it to
  *     register `rr`, and repeats this command at the next tick until the value equals `ee`,
  *   - `$82-$FF nn`: skips the next `nn` ticks; `nn = 0` ends the sequence.
  *
  * This follows the EmuTOS implementation, which mirrors the original TOS behaviour.
  */
final class Dosound(ym: Ym2149):
  private var sequence: Array[Byte] = null
  private var pointer = 0
  private var delay = 0
  private var temp = 0

  /** Starts a new sequence. As with the real XBIOS call, it begins at the next tick. */
  def start(seq: Array[Byte]): Unit =
    sequence = seq
    pointer = 0
    delay = 0

  def playing: Boolean = sequence != null

  private def byteAt(i: Int): Int = if i < sequence.length then sequence(i) & 0xff else 0

  /** Executes one 50 Hz tick. */
  def tick(): Unit =
    if sequence == null then return
    if delay > 0 then
      delay -= 1
      return
    var continue = true
    while continue do
      val op = byteAt(pointer)
      pointer += 1
      if op <= 0x80 then
        val data = byteAt(pointer)
        pointer += 1
        if op == 0x80 then temp = data
        else ym.writeRegister(op & 0x0f, data)
      else if op == 0x81 then
        val reg = byteAt(pointer)
        temp = (temp + byteAt(pointer + 1)) & 0xff
        ym.writeRegister(reg & 0x0f, temp)
        if temp != byteAt(pointer + 2) then pointer -= 1 // repeat the $81 command next tick
        else pointer += 3
        continue = false
      else
        val n = byteAt(pointer)
        pointer += 1
        if n == 0 then sequence = null
        else delay = n // the next n ticks are skipped
        continue = false

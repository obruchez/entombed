package entombed.audio

import scala.collection.mutable

object SoundSequence:
  /** Copies a `Dosound` sequence out of a memory image, up to its end marker (`$82 00`). */
  def read(mem: Array[Byte], address: Int): Array[Byte] =
    val out = mutable.ArrayBuffer.empty[Byte]
    def at(p: Int): Int = mem(p & 0xffffff) & 0xff
    var p = address
    var done = false
    while !done && out.length < 256 do
      val op = at(p)
      out += op.toByte
      p += 1
      if op == 0x81 then
        for _ <- 0 until 3 do
          out += at(p).toByte
          p += 1
      else
        val arg = at(p)
        out += arg.toByte
        p += 1
        if op >= 0x82 && arg == 0 then done = true
    out.toArray

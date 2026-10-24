package entombed.save

import java.io.ByteArrayOutputStream

/** Minimal big-endian binary writer, identical on the JVM and in JavaScript. */
final class ByteWriter:
  private val out = new ByteArrayOutputStream()

  def u8(v: Int): ByteWriter =
    out.write(v & 0xff)
    this

  def bool(v: Boolean): ByteWriter = u8(if v then 1 else 0)

  def i32(v: Int): ByteWriter =
    out.write(v >>> 24)
    out.write(v >>> 16)
    out.write(v >>> 8)
    out.write(v)
    this

  def i64(v: Long): ByteWriter = i32((v >>> 32).toInt).i32(v.toInt)

  def bytes(b: Array[Byte]): ByteWriter =
    i32(b.length)
    out.write(b, 0, b.length)
    this

  def ints(a: Array[Int]): ByteWriter =
    i32(a.length)
    a.foreach(i32)
    this

  def string(s: String): ByteWriter = bytes(s.getBytes("UTF-8"))

  def result: Array[Byte] = out.toByteArray

final class ByteReader(data: Array[Byte]):
  private var pos = 0

  def u8(): Int =
    val v = data(pos) & 0xff
    pos += 1
    v

  def bool(): Boolean = u8() != 0

  def i32(): Int = (u8() << 24) | (u8() << 16) | (u8() << 8) | u8()

  def i64(): Long = (i32().toLong << 32) | (i32().toLong & 0xffffffffL)

  def bytes(): Array[Byte] =
    val n = i32()
    val b = java.util.Arrays.copyOfRange(data, pos, pos + n)
    pos += n
    b

  def ints(): Array[Int] = Array.fill(i32())(i32())

  def string(): String = new String(bytes(), "UTF-8")

  def expectMagic(magic: String): Unit =
    val m = string()
    if m != magic then throw new IllegalArgumentException(s"Bad save data: $m instead of $magic")

package entombed.save

import scala.collection.mutable

/** Persistent storage for saved games, provided by the host (files, browser storage...). */
trait Storage:
  def write(slot: String, bytes: Array[Byte]): Unit
  def read(slot: String): Option[Array[Byte]]
  def list(): Seq[String]

/** Storage kept in memory only, for tests and as a fallback. */
final class InMemoryStorage extends Storage:
  private val slots = mutable.LinkedHashMap.empty[String, Array[Byte]]
  def write(slot: String, bytes: Array[Byte]): Unit = slots(slot) = bytes.clone()
  def read(slot: String): Option[Array[Byte]] = slots.get(slot).map(_.clone())
  def list(): Seq[String] = slots.keys.toSeq

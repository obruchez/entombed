package entombed.js

import entombed.save.Storage
import org.scalajs.dom

import java.util.Base64

/** Saved games in the browser's local storage. */
final class LocalStorage(prefix: String = "entombed.") extends Storage:
  def write(slot: String, bytes: Array[Byte]): Unit =
    try dom.window.localStorage.setItem(prefix + slot, Base64.getEncoder.encodeToString(bytes))
    catch case e: Throwable => dom.console.warn(s"Could not save $slot: $e")

  def read(slot: String): Option[Array[Byte]] =
    try Option(dom.window.localStorage.getItem(prefix + slot)).map(Base64.getDecoder.decode)
    catch case _: Throwable => None

  def list(): Seq[String] =
    try
      val ls = dom.window.localStorage
      (0 until ls.length).map(ls.key).filter(_.startsWith(prefix)).map(_.stripPrefix(prefix))
    catch case _: Throwable => Seq.empty

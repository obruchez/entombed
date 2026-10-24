package entombed.jvm

import entombed.save.Storage

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters.*

/** Saved games as files in `~/.entombed`. */
final class FileStorage(dir: Path = Paths.get(System.getProperty("user.home"), ".entombed"))
    extends Storage:
  private def file(slot: String): Path = dir.resolve(s"$slot.sav")

  def write(slot: String, bytes: Array[Byte]): Unit =
    Files.createDirectories(dir)
    Files.write(file(slot), bytes)

  def read(slot: String): Option[Array[Byte]] =
    val f = file(slot)
    if Files.exists(f) then Some(Files.readAllBytes(f)) else None

  def list(): Seq[String] =
    if !Files.isDirectory(dir) then Seq.empty
    else
      Files.list(dir).iterator.asScala.map(_.getFileName.toString)
        .filter(_.endsWith(".sav")).map(_.stripSuffix(".sav")).toSeq.sorted

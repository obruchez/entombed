package entombed.jvm

import entombed.oracle.OracleCore

/** Prints addresses of the oracle's symbols (debugging). */
object Symbols:
  def main(args: Array[String]): Unit =
    val p = OracleCore.program
    for n <- args do println(f"$n%-24s ${p.address(n)}%08x")

# Reference material

Files recovered on 24 October 2026 from floppy disk backups on the NAS
(`Disquettes/Anciennes copies de disquettes Atari à comparer et archiver/`), mostly from
`Entombed/TOMBED2.ZIP` (July 1995). The same archives exist in the MSA images
`Olivier (Entombed PC, etc.).msa` and `Olivier (Archives sources, Entombed & Prince).msa`.

| Directory | Content |
|---|---|
| `original/` | `ENTOMBED.PRG` (packed with Pompey Pirates 1.5) and `ENTOMBED.DOC`, as distributed by Nick Concannon in 1991 (freeware). |
| `atari/` | `DATAS.BIN`, the 96884-byte data segment of the unpacked program; raw disassemblies (`TOMBED1-3.L`, `TOMBED_X.L`) and commented Atari versions (`CHANGED1-3.L`), June-July 1995. |
| `smaky/` | Machine-independent version for the Smaky port (`STOMBED1.L`, July 1995, with save games; `STOMBED2.L` is an earlier 640x480 variant), the test caller for TOS (`CALLER.L`, `TOMB_LIB.L`), and the data converted to "chunky" 4 bits per pixel (`DATASA-D.BIN`, `DATAS2.BIN`). |
| `notes-1995/` | Study notes (`ENTOMBED.CAL`, Calligrapher; text extracted in `ENTOMBED-notes.md`) and GFA Basic helper programs. |
| `entombed-pc-1996/` | Unfinished 68000-to-C translator written in April 1996 for a PC port. |

Encoding: the `.L` sources are CP437 with CRLF line endings. Every `.L` file has a `.utf8.L`
twin (UTF-8, LF) with the same line numbers; line numbers quoted in the Scala code refer to them.
`Main.utf8.cpp` and the `.txt` export of the Word document are conversions as well.

# Entombed

A Scala 3 port of *Entombed*, a platform game for the Atari ST written by Nick Concannon in 1991.

Four Egyptian tombs, in any order, then a fifth one. The tombs change while you play (they
flicker before they do), and hourglasses let you delay those changes. Seven lives for the whole
game, no saving: it was *very* hard.

## History

- 1991: Nick Concannon releases Entombed as freeware (ST Format cover disk 30, PD libraries).
- 1995: Olivier and Erik Bruchez disassemble and comment it, then port it to the Smaky, a Swiss
  68000-based computer. The game logic is separated from the machine through 17 external
  routines, and a save game feature is added.
- 1996: an attempt at a PC port through a 68000-to-C translator, never finished.
- 2026: this port.

The recovered material is in [`reference/`](reference/README.md).

## How the port works

- `oracle`: an interpreter for the 68000 subset used by the game runs the commented 1995 source
  (`reference/smaky/STOMBED1.L`) directly. It is the reference implementation.
- `core`: the platform-independent code: assets, YM2149 sound chip emulation (a port of ayumi)
  with an XBIOS `Dosound` interpreter, and the Scala translation of the game.
- `jvm`: desktop version (Swing, javax.sound).
- `js`: browser version (Scala.js, canvas, Web Audio).

## Building and running

```sh
sbt test              # unit tests on the JVM and with Node.js
sbt jvm/run           # desktop version
sbt js/fastLinkJS     # then open js/index.html
```

## Licences

See [`assets/NOTICE.md`](assets/NOTICE.md).

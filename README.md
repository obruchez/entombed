# Entombed

A Scala 3 port of *Entombed*, a platform game for the Atari ST written by Nick Concannon in 1991,
for the browser (Scala.js) and the desktop (JVM).

Four Egyptian tombs, in any order, then a fifth one. The tombs change while you play (they
flicker before they do), and hourglasses let you delay those changes. Seven lives for the whole
game and no saving: it was *very* hard. This port adds saving at any moment and a god mode.

## History

- **1991**: Nick Concannon releases Entombed as freeware (ST Format cover disk 30, PD libraries).
- **1995**: Olivier and Erik Bruchez disassemble and comment it, then port it to the Smaky, a
  Swiss computer also based on the 68000. The game logic is separated from the machine through 17
  external routines, the graphics are converted to 4 bits per pixel, and a save game feature is
  added (keys S and L).
- **1996**: an attempt at a PC port through a 68000-to-C translator, never finished.
- **2026**: this port.

The recovered material (original program, data, disassemblies, 1995 notes) is in
[`reference/`](reference/README.md).

## Playing

| Keys | |
|---|---|
| Arrows | walk, jump (up), climb ladders |
| Space or Ctrl | fire: start, select a tomb, shoot arrows |
| P | pause, any key to resume |
| 1 to 9, 0 | use 1 to 10 seconds of hourglass time |
| S, L | the save game of 1995 (during play) |
| Q | quit (a new game starts) |

Added in 2026 (buttons in the browser):

| Desktop | Browser | |
|---|---|---|
| F5 | F6 | save the game at any moment |
| F9 | F7 | load it |
| F8 | F8 | god mode: the man cannot die |
| F4 | F4 | unlock the fifth tomb (offered at the next tomb selection) |
| F2 | F2 | Atari ST speed (default) or 50 frames per second |
| F12 | button | switch between the Scala port and the original code (new game) |

The game runs at the speed of the Atari ST: it computed a frame in 3 vertical blanks (16.7
frames per second; the hourglass counts 16 frames per second), the title pages in 2. The sound
runs at 50 Hz, as the XBIOS played it.

## Building and running

Requirements: a JDK (17 or later), sbt.

```sh
sbt test              # unit and differential tests, on the JVM and with Node.js
sbt jvm/run           # desktop version (add --oracle to start with the original code)
sbt js/fastLinkJS     # browser version, then open js/index.html
```

## Docker

The browser version is also published as a Docker image, `obruchez/entombed` (static files
served by an unprivileged nginx on port 8080, `/healthz` for health checks):

```sh
docker run -d --name entombed -p 8080:8080 obruchez/entombed
```

The page only uses relative URLs, so a reverse proxy may serve it under a path such as
`/entombed/`. To build and publish a new version (`ThisBuild / version` in `build.sbt`):

```sh
sbt site
docker buildx build --platform linux/amd64,linux/arm64 \
  -t obruchez/entombed:<version> -t obruchez/entombed:latest --push .
```

## How the port was made

The port has two implementations of the game, which must behave identically:

- **The oracle** (`oracle/`): an interpreter for the subset of the 68000 instruction set used by
  the game. It assembles the commented source of 1995 (`reference/smaky/STOMBED1.L`) and runs it,
  with the 17 external routines written in Scala. A few lines are patched so that it looks and
  behaves like the Atari original (see `OracleCore.Patches`): the Smaky version rounded sprite
  positions to even pixels, displayed text at slightly different places, and kept a debugging
  shortcut to the fifth tomb.
- **The Scala port** (`core/.../game`): the same game written in Scala, routine by routine, with
  the line numbers of the 1995 source in comments. It keeps the memory layout of the original
  (data segment, BSS, screens), which makes it possible to compare it with the oracle byte for
  byte, to reproduce the original's behaviour exactly (including its reads outside tables), and to
  save a game by copying the memory, as the 1995 save routine did. Only the control flow is
  different: the original waits for the vertical blank in many loops, which a browser cannot do,
  so the port is a state machine whose states are the places where the original reads the
  joystick or the keyboard.

The differential tests (`oracle/src/test/.../DifferentialSuite.scala`) run both implementations
with the same inputs and compare, after every vertical blank, the displayed frame, the palette,
the sounds and the whole memory of the game. Long random sessions, with teleports, extra weapons
and keys to reach every kind of object, have been run on all five tombs
(`oracleJVM/Test/runMain entombed.oracle.Explore`).

Other parts:

- `core/.../audio`: the YM2149 sound chip (a Scala port of ayumi by Peter Sovietov) and an
  interpreter for the XBIOS `Dosound` sequences in which the game's sounds are written.
- `core/.../gfx`: Atari bitplanes, palettes, and the block display routine with the exact
  clipping of the original.
- `core/.../driver`: what the hosts run: save and load, god mode, Atari ST speed.
- `jvm`, `js`: the two hosts (Swing and javax.sound; canvas, Web Audio and local storage).

## Licences

See [`assets/NOTICE.md`](assets/NOTICE.md). The game itself remains the work of Nick Concannon.

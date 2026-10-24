package entombed.game

import M68.*
import Vars.*

/** States of the control flow: the places where the original reads the joystick or the keyboard
  * after a vertical blank. Between two such places, the logic runs without waiting; the vertical
  * blanks it requests (fades, music) are queued and played by [[Game.step]].
  *
  * A state also starts after the last vertical blank of a sequence when the original changes memory
  * after it, so that the memory is the original's whenever no vertical blank is pending (this is
  * what the differential tests compare).
  */
object Flow:
  inline val Start = 0
  inline val TitleBounce = 1
  inline val TitleLogoWait = 2
  inline val TitleScoresWait = 3
  inline val TitleInstrAfterSwap = 4
  inline val TitleInstrCheck = 5
  inline val ChooseTombInput = 6
  inline val Tomb5Wait = 7
  inline val MainFrame = 8
  inline val PostSwap = 9
  inline val PostSwapAfterFade = 10
  inline val Paused = 11
  inline val End5Wait = 12
  inline val NameEntry = 13
  inline val AfterLifeFade = 14
  inline val TombChosenFade = 15
  inline val GameOverFade = 16
  inline val End5Start = 17
  inline val End5Fade = 18
  inline val NameEntryAfterFade = 19

/** The control flow of the game: start, title pages, tomb selection, main loop, end of the game.
  * Line numbers refer to `reference/smaky/STOMBED1.L`.
  */
trait Flow:
  this: Game =>
  import Flow.*

  protected def runFlow(): Unit = state match
    case Start               => start()
    case TitleBounce         => titleBounce()
    case TitleLogoWait       => titleLogoWait()
    case TitleScoresWait     => titleScoresWait()
    case TitleInstrAfterSwap => titleInstrAfterSwap()
    case TitleInstrCheck     => titleInstrCheck()
    case ChooseTombInput     => chooseTombInput()
    case Tomb5Wait           => tomb5Wait()
    case MainFrame           => mainLoop()
    case PostSwap            => postSwap()
    case PostSwapAfterFade   => postSwapAfterFade()
    case Paused              => paused()
    case End5Wait            => end5Wait()
    case NameEntry           => nameEntry()
    case AfterLifeFade       => l00087()
    case TombChosenFade      => tombLoaded()
    case GameOverFade        => gameOverScores()
    case End5Start           => end5Start()
    case End5Fade            => bestScoresTable()
    case NameEntryAfterFade  =>
      ram.setW(TombWord0, 1)
      nameEntry()
    case other => throw new IllegalStateException(s"Unknown state $other")

  private def fire: Boolean =
    getJoystick()
    (ram.b(JoystickState) & 0xf0) != 0

  // ----- Start (debut_entombed, 141) -----

  private def start(): Unit =
    // Relocation of the addresses of the 5 tombs
    for i <- 0 until 5 do ram.setL(Tombs5 + 4 * i, ram.l(Tombs5 + 4 * i) + Layout.Data - 22994)
    ram.setL(PhysScreen1, (Layout.OtherScreen & ~0xff) + 0x100)
    ram.setL(PhysScreen2, (Layout.Bss & ~0xff) + 0x100)
    ram.setL(Blocs32x16, Layout.Blocks32x16)
    ram.setL(BlocsOthers, Layout.Others)
    ram.setL(Fonts8x8, Layout.Font)
    ram.setW(JoystickState, 0)
    clrJoystick()
    selectPalette(1)
    attributesTableInit()
    blocs32x16Init()
    // blocsothers_init and font_init: the graphics and the font are already in their final form.
    // font_init used B214038 as a buffer (later part of the projectiles): same content.
    for r <- 0 to 7 do
      System.arraycopy(ram.bytes, Layout.Font + 32 * 98 + 4 * r, ram.bytes, B214038 + 8 * r, 4)
      System.arraycopy(ram.bytes, Layout.Font + 32 * 99 + 4 * r, ram.bytes, B214038 + 8 * r + 4, 4)
    ram.setW(TombNumber, 0)
    ram.setW(Zero1, 0)
    ram.setW(TombNumber2, 0)
    ram.setW(FirstPageDone, 0)
    outerLoop()

  /** `L0001A` (204): the loop around everything. */
  private def outerLoop(): Unit =
    if ram.w(FirstPageDone) == 0 then titleStart()
    else if ram.w(FirstPageDone) == 1 then newGame()
    else throw new IllegalStateException("firstpage_done")

  // ----- Title pages (title_and_instr, 934) -----

  private def titleStart(): Unit =
    ram.setW(PosInTombX, 0)
    ram.setW(PosInTombY, 0)
    clearScreen(PhysScreen1)
    affEntombed(-0x180000)
    swapScreens()
    paletteCrescendo(2)
    regs(0) = -0x180000
    regs(1) = 0x10000
    affEntombed(regs(0))
    swapScreens()
    state = TitleBounce

  /** `aff_entombed` (880): the word ENTOMBED at height `d0` (16.16 fixed point), underlined. */
  private def affEntombed(d0: Int): Unit =
    clearScreen(PhysScreen2)
    affBloc32x24(0x52, 0x20, 0x40)
    affBloc32x24(0x54, 0x100, 0x40)
    for i <- 0 to 5 do affBloc32x24(0x53, 0x40 + 0x20 * i, 0x40)
    val y = w(d0 >>> 16)
    for i <- 0 to 7 do affBloc32x24(0x48 + i, 0x20 + 0x20 * i, y)

  private def titleBounce(): Unit =
    if fire then titleEnd()
    else
      var d0 = regs(0) + regs(1)
      var d1 = regs(1)
      var logo = false
      if d0 - 0x280000 < 0 then d1 += 0x8000
      else
        ram.setL(SoundAddress, Sound1) // the title bounces
        playYamaha()
        d0 = 0x280000
        d1 = d1 >>> 1
        d1 += d1 >>> 1
        if d1 - 0x20000 < 0 then logo = true else d1 = -d1
      regs(0) = d0
      regs(1) = d1
      if logo then titleLogo()
      else
        affEntombed(d0)
        swapScreens()

  /** `L000FF` (983): the logo and the copyright. */
  private def titleLogo(): Unit =
    affEntombed(0x280000)
    affBloc32x24(0x50, 0x90, 0x60)
    affBloc32x24(0x51, 0x90, 0x78)
    affText(TxtCopyright, 0, 0xb0)
    swapScreens()
    regs(7) = 0x96
    waitVbl()
    state = TitleLogoWait

  private def titleLogoWait(): Unit =
    if fire then titleEnd()
    else
      regs(7) = w(regs(7) - 1)
      if regs(7) != 0xffff then waitVbl()
      else titleScores()

  /** Best scores page (1017). */
  private def titleScores(): Unit =
    paletteDecrescendo(2)
    clearScreen(PhysScreen2)
    showScoresTable(0xf38)
    swapScreens()
    paletteCrescendo(2)
    regs(7) = 0xfa
    waitVbl()
    state = TitleScoresWait

  /** The title and the 10 best scores, from screen offset `titleOffset` (1026, 5723). */
  private def showScoresTable(titleOffset: Int): Unit =
    affText2(BestScores, titleOffset)
    var a0 = D3028
    var d0 = 0x1ba0
    for _ <- 0 to 9 do
      var d1 = ram.l(a0)
      var a = D5054
      for _ <- 0 to 5 do
        a -= 1
        ram.setB(a, (d1 & 0xf) + 0x30)
        d1 = Integer.rotateRight(d1, 4)
      ram.setB(a + 6, 0)
      affText2(LineInfo, d0)
      affText2(a0 + 4, w(d0 + 0x18))
      d0 = w(d0 + 0x780)
      a0 += 0x18

  private def titleScoresWait(): Unit =
    if fire then titleEnd()
    else
      regs(7) = w(regs(7) - 1)
      if regs(7) != 0xffff then waitVbl()
      else
        paletteDecrescendo(2)
        ram.setW(PosInTombX, 0)
        ram.setW(PosInTombY, 0)
        ram.setW(D142, 8)
        instructionsPage()

  /** `L00136` (1078): the instructions, scrolling. */
  private def instructionsPage(): Unit =
    clearScreen(PhysScreen2)
    for (block, y) <- Seq(
        0x2c -> 0x64,
        0x2d -> 0xae,
        0x2e -> 0xda,
        0x2f -> 0x110,
        0x30 -> 0x13c,
        0x31 -> 0x17c,
        0x32 -> 0x1b2,
        0x33 -> 0x1ca,
        0x3f -> 0x1fc
      )
    do affBloc32x24(block, 0x20, y)
    affText(InstrTitle, 0x48, 0x32)
    for (text, y) <- Seq(
        InstrArc -> 0x64,
        InstrFleches -> 0xae,
        InstrCle -> 0xda,
        InstrOr -> 0x110,
        InstrSablier -> 0x13c,
        InstrCoffre -> 0x17c,
        InstrVase -> 0x1b2,
        InstrVie -> 0x1fc
      )
    do affText(text, 0x50, y)
    val s = ram.ptr(PhysScreen2)
    ram.clear(s, 0x500)
    ram.clear(s + 0x7800, 0x500)
    swapScreens()
    state = TitleInstrAfterSwap

  private def titleInstrAfterSwap(): Unit =
    if ram.w(PosInTombY) == 0 then
      paletteCrescendo(2)
      state = TitleInstrCheck
    else titleInstrCheck()

  private def titleInstrCheck(): Unit =
    if fire then titleEnd()
    else
      val y = w(ram.w(PosInTombY) + 1)
      ram.setW(PosInTombY, y)
      if neg16(y - 0x1a8) then instructionsPage()
      else
        val n = w(ram.w(D142) - 1)
        ram.setW(D142, n)
        if neg16(n) then titleStart()
        else
          createPalette(2)
          instructionsPage()

  /** `L000FA` (1225). */
  private def titleEnd(): Unit =
    ram.setW(FirstPageDone, 1)
    outerLoop()

  // ----- A new game (L0001B, 210) and the game itself (play_game, 229) -----

  private def newGame(): Unit =
    ram.setW(NbLifes, 7)
    ram.setL(NbPoints, 0)
    ram.setW(TombChosenFlag, 0)
    ram.setW(TombNumber, 0)
    ram.setL(NonRetourAdr, 0)
    playGame()

  private def playGame(): Unit =
    paletteDecrescendo(2)
    state = AfterLifeFade

  /** `L00087` (233): a life starts, in the chosen tomb or after choosing one. */
  private def l00087(): Unit =
    ram.setW(TirEnCoursFlag, 0)
    ram.setW(AnimManCounter, 0)
    ram.setW(GameOverYOffset, w(-0xf))
    ram.setW(NbMaxGameOver, 0x20)
    ram.setW(ExitFound, 0)
    ram.setW(NbCle, 0)
    ram.setW(NbFleches, 0)
    ram.setW(NbArc, 0)
    ram.setW(ManSynchro, 0x8000)
    ram.setW(FixationSynchro, 0x8000)
    ram.setW(FixationCounter, 0)
    ram.setW(StateMan, 0)
    ram.setW(PointsToAdd, 0xffff)
    ram.setW(NbFlechesTirees, 0)
    ram.setW(NbSecsRemain, 0)
    ram.setW(DirectionMan, 1)
    ram.setW(VaseBrokenCounter, 0)
    ram.setW(PharaonKilledCounter, 0)
    ram.setW(SerpentKilledCounter, 0)
    ram.setW(SoundPriority, 0)
    ram.setL(SoundAddress, 0)
    ram.setW(NbSeconds, 0)
    if ram.w(TombChosenFlag) == 0 then chooseTomb()
    else l0003E()

  /** `L0003E` (264), then the main loop. */
  private def l0003E(): Unit =
    readTombDatas()
    ram.setW(AMainCounter, 0)
    mainLoop()

  // ----- Tomb selection (choose_tomb, 584) -----

  private def chooseTomb(): Unit =
    ram.setW(PosInTombX, 0)
    ram.setW(PosInTombY, 0)
    ram.setW(TombNumber, 0)
    ram.setW(TombWord0, 0)
    ram.setW(TombWord1, 0)
    chooseTombPage()

  /** `L000BE` (590). */
  private def chooseTombPage(): Unit =
    clearScreen(PhysScreen2)
    val played = ram.w(WichTombPlayed)
    if played == 0xf then
      // The first four tombs are completed: the fifth one
      affText(TxtConfirme, 0, 8)
      affText(TxtTomb5, 0, 0x24)
      ram.setW(TombNumber, 4)
      ram.setW(NbLifes, 7)
      swapScreens()
      paletteCrescendo(2)
      state = Tomb5Wait
    else
      affText(TxtTombSelect, 0, 8)
      affText(TxtTomb1, 0, 0x28)
      affText(TxtTomb2, 0, 0x50)
      affText(TxtTomb3, 0, 0x78)
      affText(TxtTomb4, 0, 0xa0)
      // The tombs already completed are greyed out
      var d7 = played
      var d6 = 0x1900
      while d7 != 0 do
        if (d7 & 1) != 0 then
          var a0 = ram.ptr(PhysScreen2) + sw(d6)
          for _ <- 0 to 0x13 do
            for _ <- 0 to 0x27 do
              ram.setL(a0 + 0xa0, ram.l(a0 + 0xa0) & 0xffff0000)
              ram.setL(a0, ram.l(a0) & 0x0000ffff)
              a0 += 4
            a0 += 0xa0
        d7 = d7 >>> 1
        d6 = w(d6 + 0x1900)
      // The man in front of the selected tomb (skipping completed tombs)
      var d1 = ram.w(TombNumber)
      while ((played >> (d1 & 31)) & 1) != 0 do d1 = (d1 + 1) & 3
      ram.setW(TombNumber, d1)
      affBloc32x24(0xb, ram.w(TombWord0), w(d1 * 0x28 + 0x28))
      swapScreens()
      if ram.w(TombWord1) == 0 then
        ram.setW(TombWord1, 1)
        paletteCrescendo(2)
      state = ChooseTombInput

  /** `L000B8` (702). */
  private def chooseTombInput(): Unit =
    getKeyboard()
    getJoystick()
    val d0 = ram.b(JoystickState)
    if (d0 & 1) != 0 then
      // Up
      val played = ram.w(WichTombPlayed)
      var d1 = ram.w(TombNumber)
      d1 = (d1 - 1) & 3
      while ((played >> d1) & 1) != 0 do d1 = (d1 - 1) & 3
      ram.setW(TombNumber, d1)
      chooseTombAgain()
    else if (d0 & 2) != 0 then
      // Down
      ram.setW(TombNumber, (ram.w(TombNumber) + 1) & 3)
      chooseTombAgain()
    else if (d0 & 0xf0) != 0 then tombChosen()
    else chooseTombAgain()

  /** `L000BB` (734). */
  private def chooseTombAgain(): Unit =
    ram.setB(JoystickState, 0)
    clrJoystick()
    chooseTombPage()

  /** `L000A3` (618): busy wait for the fire button. */
  private def tomb5Wait(): Unit =
    if fire then tombChosen() else idleVbl()

  /** `L000A2` (739), then the rest of play_game (235). */
  private def tombChosen(): Unit =
    changeStones()
    ram.setW(TombNumber2, ram.w(TombNumber))
    changeStones()
    blocs32x16Init()
    paletteDecrescendo(2)
    state = TombChosenFade

  private def tombLoaded(): Unit =
    ram.setW(TombChosenFlag, 1)
    l0003E()

  // ----- Main loop (main_loop, 276) -----

  private def mainLoop(): Unit =
    if ram.w(Layout.LoadFlag) != 0 then
      changeStones()
      loadGame()
      ram.setW(TombNumber2, ram.w(TombNumber))
      changeStones()
      blocs32x16Init()
      ram.setW(Layout.LoadFlag, 0)
    if ram.w(Layout.SaveFlag) != 0 then
      saveGame()
      ram.setW(Layout.SaveFlag, 0)
    animateBlocks()
    doFixeElements()
    affAllScreen()
    if (ram.b(FixationSynchro + 1) & 1) == 0 then doFixeElements()
    affVaseOrDead()
    ram.setW(D518, ram.w(PosInTombX))
    ram.setW(D520, ram.w(PosInTombY))
    ram.setW(D522, ram.w(D510))
    ram.setW(D524, ram.w(D512))
    ram.setW(D526, ram.w(D514))
    ram.setW(D528, ram.w(D516))
    l00054()
    l00055()
    l00056()
    gereProject()
    // The man
    var d0 = if ram.w(TirEnCoursFlag) != 0 then 5 else ram.w(StateMan)
    d0 = w(d0 << 4)
    if ram.w(NbArc) != 0 then d0 = w(d0 + 8)
    d0 = w(d0 + w(ram.w(DirectionMan) << 2))
    d0 = w(d0 + (ram.w(AnimManCounter) & 3))
    val sprite = ram.b(D4892 + sw(d0))
    if affBloc32x24(sprite, ram.w(TombWord0), ram.w(TombWord1)) || godMode then frameTail()
    else if ram.w(NbLifes) == 1 then affGameOver()
    else
      ram.setW(NbLifes, w(ram.w(NbLifes) - 1))
      playGame()

  /** The 1995 save game: the variables and tables of the game, as three blocks of memory. */
  private val SavedRanges = Seq(
    (Layout.Data + 146, 4768 - 146),
    (Layout.Data + 4788, 31348 - 4788),
    (Layout.Bss + 32768, 214102 - 32768)
  )

  private def saveGame(): Unit =
    val out = new java.io.ByteArrayOutputStream()
    for (a, n) <- SavedRanges do out.write(ram.bytes, a, n)
    storage.write(Game.SaveSlot, out.toByteArray)

  private def loadGame(): Unit =
    storage.read(Game.SaveSlot).foreach { data =>
      var pos = 0
      for (a, n) <- SavedRanges do
        val k = math.min(n, data.length - pos)
        if k > 0 then System.arraycopy(data, pos, ram.bytes, a, k)
        pos += math.max(k, 0)
    }

  /** `aff_game_over` (387): "Game Over" comes down. */
  private def affGameOver(): Unit =
    val n = w(ram.w(NbMaxGameOver) - 1)
    ram.setW(NbMaxGameOver, n)
    if n == 0 then
      // L00065
      ram.setW(WichTombPlayed, 0)
      ram.setW(FirstPageDone, 0)
      paletteDecrescendo(2)
      state = GameOverFade
    else
      val x = w(ram.w(PosInTombX) + 0x7a)
      val y = w(ram.w(PosInTombY) + ram.w(GameOverYOffset))
      for i <- 0 to 2 do affBloc32x24(0x40 + i, w(x + 0x20 * i), y)
      if ram.w(GameOverYOffset) != 0x55 then
        ram.setW(GameOverYOffset, w(ram.w(GameOverYOffset) + 5))
      frameTail()

  /** `L00063` (420): end of a frame. */
  private def frameTail(): Unit =
    l0006D()
    if (ram.b(FixationSynchro + 1) & 1) == 0 then doFixeElements()
    ram.setW(ManSynchro, w(ram.w(ManSynchro) + 1))
    val remain = ram.w(NbSecsRemain)
    if remain != 0 then
      // The hourglass time remaining, in big digits
      val d0 = remain + 0xc
      val tens = d0 / 0xa0
      val units = w(d0 - tens * 0xa0) >>> 4
      val px = ram.w(PosInTombX)
      val py = ram.w(PosInTombY)
      aff09Sablier(tens, w(px + 8), w(py + 0xac))
      aff09Sablier(units, w(px + 0x14), w(py + 0xac))
      ram.setW(NbSecsRemain, w(remain - 1))
      ram.setW(FixationSynchro, ram.w(FixationSynchro) ^ 1)
    else ram.setW(FixationSynchro, w(ram.w(FixationSynchro) + 1))
    // L00075: the scrolling decided by move_man
    if !neg16(ram.w(D518)) then
      ram.setW(PosInTombX, ram.w(D518))
      ram.setW(PosInTombY, ram.w(D520))
      ram.setW(D510, ram.w(D522))
      ram.setW(D512, ram.w(D524))
      ram.setW(D514, ram.w(D526))
      ram.setW(D516, ram.w(D528))
    affLineInfo()
    if ram.w(StateMan) == 4 then prepareSound(0xa) // the man dies
    playYamaha()
    swapScreens()
    state = PostSwap

  /** After the end of a frame (473). */
  private def postSwap(): Unit =
    if ram.w(ManSynchro) == 0x8001 then
      paletteCrescendo(2)
      state = PostSwapAfterFade
    else postSwapAfterFade()

  /** `L0007E` (477). */
  private def postSwapAfterFade(): Unit =
    if ram.w(StateMan) != 4 && ram.w(ExitFound) != 0 then
      // The exit has been found
      addPoints(ram.l(TombWord4))
      playBigSound()
      paletteDecrescendo(2)
      val n = ram.w(TombNumber)
      if n == ram.w(D4788) then
        ram.setW(FirstPageDone, 0)
        ram.setW(WichTombPlayed, 0)
        showBestScores()
      else
        ram.setB(WichTombPlayed + 1, ram.b(WichTombPlayed + 1) | (1 << (n & 7)))
        ram.setW(TombChosenFlag, 0)
        ram.setL(NonRetourAdr, 0)
        l00087()
    else testKeyboard()

  /** `test_keyboard` (502). */
  private def testKeyboard(): Unit =
    val k = getKeyboard() & 0xff
    if k == 'q' || k == 'Q' then quit()
    else
      if (k == 's' || k == 'S') && ram.w(StateMan) != 4 then ram.setW(Layout.SaveFlag, 0xffff)
      if k == 'l' || k == 'L' then ram.setW(Layout.LoadFlag, 0xffff)
      if k == 'p' || k == 'P' then paused()
      else if k < 0x30 || k >= 0x3a then mainLoop()
      else
        // Keys 1-9 and 0 (10): use hourglass time
        val wanted = if k == 0x30 then 0xa else k - 0x30
        val available = ram.w(NbSeconds)
        val d0 = if available < wanted then available else wanted
        ram.setW(NbSeconds, w(available - d0))
        ram.setW(NbSecsRemain, w(ram.w(NbSecsRemain) + w(d0 << 4)))
        mainLoop()

  /** `L0008B` (571): pause until a key is pressed. */
  private def paused(): Unit =
    if getKeyboard() != 0 then mainLoop()
    else
      idleVbl()
      state = Paused

  // ----- Information display -----

  /** `aff_vase_or_dead` (749). */
  private def affVaseOrDead(): Unit =
    val vc = ram.w(VaseBrokenCounter)
    if vc != 0 then
      affBloc32x24(w((w(vc - 1) >>> 1) + 0x3c), ram.w(VaseBrokenX), ram.w(VaseBrokenY))
      val n = w(vc + 1)
      ram.setW(VaseBrokenCounter, if n >= 7 then 0 else n)
    killedAnimation(PharaonKilledCounter, PharaonKilledX, PharaonKilledY, 0x38)
    killedAnimation(SerpentKilledCounter, SerpentKilledX, SerpentKilledY, 0x3a)

  private def killedAnimation(counter: Int, xVar: Int, yVar: Int, block: Int): Unit =
    val c = ram.w(counter)
    if c != 0 then
      val n = ((w(c - 1) >>> 1) & 1) + block
      if !affBloc32x24(n, ram.w(xVar), ram.w(yVar)) then ram.setW(counter, 0)
      else
        if c >= 5 then ram.setW(yVar, w(ram.w(yVar) + 4))
        else ram.setW(yVar, w(ram.w(yVar) - 4))
        ram.setW(counter, w(c + 1))

  /** `aff_line_info` (819): lives, score, bows, arrows, keys and hourglass seconds. */
  private def affLineInfo(): Unit =
    for i <- 0 to 0x27 do ram.setB(LineInfo + i, 0x20)
    var d7 = w(ram.w(NbLifes) - 2)
    if !neg16(d7) then for i <- 0 to d7 do ram.setB(LineInfo + 1 + i, 0x80)
    var d6 = ram.l(NbPoints)
    var a1 = LineInfo + 0xe
    for _ <- 0 to 5 do
      a1 -= 1
      ram.setB(a1, (d6 & 0xf) + 0x30)
      d6 = d6 >>> 4
    d7 = w(ram.w(NbArc) - 1)
    if !neg16(d7) then for i <- 0 to d7 do ram.setB(LineInfo + 0xf + i, 0x81)
    d7 = w(ram.w(NbFleches) - 1)
    if !neg16(d7) then for i <- 0 to d7 do ram.setB(LineInfo + 0x13 + i, 0x82)
    d7 = w(ram.w(NbCle) - 1)
    if !neg16(d7) then for i <- 0 to d7 do ram.setB(LineInfo + 0x20 + i, 0x83)
    val s = ram.w(NbSeconds)
    ram.setB(LineInfo + 0x25, s / 10 + 0x30)
    ram.setB(LineInfo + 0x26, w(s - (s / 10) * 10) + 0x30)
    affText2(LineInfo, 0x7800)

  /** `L0006D` (2568): the points just won, going up. */
  private def l0006D(): Unit =
    val d0 = ram.w(PointsToAdd)
    if d0 != 0xffff then
      val d1 = w(ram.w(D5092) + 7)
      val d2 = w(ram.w(D5094) + 5)
      ram.setW(D5094, w(ram.w(D5094) - 1))
      val n = w(ram.w(D5096) - 1)
      ram.setW(D5096, n)
      if n == 0 then ram.setW(PointsToAdd, 0xffff)
      affPointsGotten(d0, d1, d2)

  // ----- Best scores (show_best_scores, 5617) -----

  private def gameOverScores(): Unit =
    ram.setW(TombNumber, 0)
    showBestScores()

  private def showBestScores(): Unit =
    if ram.w(TombNumber) == 4 then
      clearScreen(PhysScreen2)
      ram.setW(PosInTombY, 0)
      affText(End5Levels, 0, 0)
      swapScreens()
      paletteCrescendo(2)
      state = End5Start
    else bestScoresTable()

  private def end5Start(): Unit =
    ram.setW(JoystickState, 0)
    clrJoystick()
    end5Wait()

  private def end5Wait(): Unit =
    getJoystick()
    if (ram.b(JoystickState) & 0xf0) == 0 then
      idleVbl()
      state = End5Wait
    else
      paletteDecrescendo(2)
      state = End5Fade

  /** `L003D2` (5654). */
  private def bestScoresTable(): Unit =
    val points = ram.l(NbPoints)
    var a0 = D3244
    var d7 = 9
    var found = false
    while !found && d7 != 0xffff do
      if Integer.compareUnsigned(points, ram.l(a0)) < 0 then found = true
      else
        a0 -= 0x18
        d7 = w(d7 - 1)
    if d7 == 9 then bestScoresDone()
    else
      ram.setW(TombWord0, 0)
      d7 = w(d7 + 1)
      if d7 != 9 then
        var a = D3220
        for _ <- 0 to w(8 - d7) do
          ram.setL(a + 0x18, ram.l(a))
          ram.setL(a + 0x22, ram.l(a + 0xa))
          ram.setL(a + 0x26, ram.l(a + 0xe))
          ram.setL(a + 0x2a, ram.l(a + 0x12))
          a -= 0x18
      val entry = D3028 + 24 * d7
      ram.setL(entry, points)
      for i <- 0 to 2 do ram.setL(entry + 0xa + 4 * i, 0x5f5f5f5f)
      clearScreen(PhysScreen2)
      clearScreen(PhysScreen1)
      regs(7) = 0
      regs(6) = entry + 0xa
      nameEntry()

  /** `L003EF` (5696): name entry. */
  private def nameEntry(): Unit =
    clearScreen(PhysScreen2)
    val anim = ram.w(AnimManCounter)
    ram.setW(PosInTombX, 0)
    ram.setW(PosInTombY, 0)
    ram.setW(AnimManCounter, w(anim + 1))
    val d0 = anim & 3
    for i <- 0 to 9 do
      affBloc32x24(d0 + 0xb, 0x20 * i, 0)
      affBloc32x24(d0 + 7, 0x20 * i, 0xa8)
    showScoresTable(0x1438)
    val k = getKeyboard() & 0xff
    var d7 = regs(7)
    var a0 = regs(6)
    var done = false
    if k == 8 then
      if (d7 & 0xff) != 0 then
        d7 = (d7 & 0xff00) | b(d7 - 1)
        a0 -= 1
        ram.setB(a0, 0x5f)
    else if k == 0xd then
      ram.setB(a0, 0)
      done = true
    else if k >= 0x20 && k < 0x7f then
      val c = if k >= 0x61 && k < 0x7b then k - 0x20 else k
      if d7 == 0xc then ram.setB(a0 - 1, c)
      else
        ram.setB(a0, c)
        a0 += 1
        d7 = w(d7 + 1)
    regs(7) = d7
    regs(6) = a0
    if done then
      paletteDecrescendo(2)
      bestScoresDone()
    else
      swapScreens()
      if ram.w(TombWord0) == 0 then
        paletteCrescendo(2)
        state = NameEntryAfterFade
      else state = NameEntry

  /** `L003D9` (5792), then back to the outer loop. */
  private def bestScoresDone(): Unit =
    ram.setW(FirstPageDone, 0)
    outerLoop()

package entombed.game

/** Addresses of the game's variables and tables, with the names given in 1995
  * (`reference/smaky/STOMBED1.L`, lines 15-112). Unnamed locations keep their offset in the name
  * (`D510` is `debut_data+510`, `B32768` is `debut_bss+32768`).
  */
object Vars:
  private inline val D = 0x2090 // Layout.Data
  private inline val B = 0x19c00 // Layout.Bss

  // ----- BSS -----
  /** 32 slots of $500 bytes: background blocks pre-drawn over their background (32x16). */
  val B32768 = B + 32768
  val B121088 = B + 121088
  val B123648 = B + 123648

  /** Table of 128 pointers: the block (in B32768) displayed for each cell value. */
  val B173568 = B + 173568

  /** 80x48 cells: the tomb as displayed. */
  val TombTable1 = B + 174080

  /** 80x48 cells: the alternative content, swapped in by "fixations" (timed changes). */
  val TombTable2 = B + 177920

  /** 80x48 longs: for each cell, a pointer into the tomb data or to an object record. */
  val TombTable3 = B + 181760

  /** Fixations (timed changes), decoded from the tomb data. */
  val B197120 = B + 197120

  /** Object records (rafts, enemies, dragons...), allocated sequentially. */
  val B205312 = B + 205312
  val AttributesTable = B + 213504

  /** Switches set by multiple-action points and read by objects. */
  val B213760 = B + 213760

  /** Pool of 16 projectiles of 22 bytes. */
  val B214016 = B + 214016

  /** Scratch buffer used by font_init. */
  val B214038 = B + 214038

  // ----- Data segment -----
  val Palettes = D + 14
  val D142 = D + 142 // intensity for create_palette
  val Zero1 = D + 144
  val TombNumber2 = D + 146
  val StonesTable = D + 148
  val D258 = D + 258 // patterns of the "killing stones"
  val NbDoubleCar = D + 414
  val FontMaskTable = D + 416
  val GameOverYOffset = D + 480
  val NbMaxGameOver = D + 482
  val NbBlocsOthers85 = D + 486
  val PhysScreen1 = D + 488
  val PhysScreen2 = D + 492
  val IkbdBuffer = D + 496
  val JoystickState = D + 498
  val PosInTombX = D + 506
  val PosInTombY = D + 508

  /** Scrolling thresholds (left, right, top, bottom) and their pending values. */
  val D510 = D + 510
  val D512 = D + 512
  val D514 = D + 514
  val D516 = D + 516
  val D518 = D + 518
  val D520 = D + 520
  val D522 = D + 522
  val D524 = D + 524
  val D526 = D + 526
  val D528 = D + 528
  val D530 = D + 530 // animation frame of the animated blocks (0-3)
  val AMainCounter = D + 532
  val ManSynchro = D + 534
  val FixationSynchro = D + 536
  val FixationCounter = D + 538
  val NbSecsRemain = D + 540
  val D542 = D + 542 // next free object record
  val FirstPageDone = D + 546
  val D548 = D + 548 // free projectiles
  val D552 = D + 552 // active projectiles
  val ListProjects = D + 556
  val D684 = D + 684 // rules for the edges of walls
  val D730 = D + 730 // offsets of the 8 neighbours of a cell
  val D746 = D + 746 // cells of the screens present in the tomb
  val D874 = D + 874 // current position in the fixations
  val D878 = D + 878 // number - 1 of character to block pairs
  val D880 = D + 880 // character to block pairs
  val D954 = D + 954 // cell of each element marker letter (A-Z)
  val AttrTblData = D + 1058
  val D1150 = D + 1150 // number - 1 of attribute pairs
  val D1152 = D + 1152 // number - 1 of pre-drawn blocks
  val D1154 = D + 1154 // pre-drawn blocks: (block, background) pairs
  val D1374 = D + 1374 // block replacements for overlapping backgrounds
  val SoundPriority = D + 1484
  val SoundPause = D + 1486
  val SoundAddress = D + 1488
  val Sounds18 = D + 1492
  val Sound1 = D + 1534
  val Sound17 = D + 2206
  val Sound18 = D + 2236
  val Sound19 = D + 2266
  val End5Levels = D + 2296
  val BestScores = D + 3014
  val D3028 = D + 3028 // best scores: 10 entries of 24 bytes (BCD score, name)
  val D3220 = D + 3220
  val D3244 = D + 3244
  val InstrTitle = D + 3268
  val InstrArc = D + 3292
  val InstrFleches = D + 3395
  val InstrCle = D + 3422
  val InstrOr = D + 3462
  val InstrSablier = D + 3501
  val InstrCoffre = D + 3593
  val InstrVase = D + 3635
  val InstrVie = D + 3733
  val TxtCopyright = D + 4734
  val Tombs5 = D + 4768
  val D4788 = D + 4788 // number of the last tomb (4)
  val TombNumber = D + 4790
  val TombDatas = D + 4792
  val TombWord2 = D + 4800 // maximum scroll x
  val TombWord3 = D + 4802 // maximum scroll y
  val TombChosenFlag = D + 4804
  val D4806 = D + 4806 // cell at the top left of the screen
  val D4810 = D + 4810 // list of active objects
  val NonRetourAdr = D + 4814
  val D4830 = D + 4830
  val TombWord0 = D + 4834 // man x
  val TombWord1 = D + 4836 // man y
  val StateMan = D + 4838 // 0 on the ground, 1 falling, 2 jumping, 3 on a ladder, 4 dying
  val NbToJump = D + 4840
  val D4842 = D + 4842 // the 6 cells touched by the man (word, pointer)
  val D4878 = D + 4878
  val NbCle = D + 4880
  val NbArc = D + 4882
  val NbFleches = D + 4884
  val MoveManCounter = D + 4886
  val DirectionMan = D + 4888
  val AnimManCounter = D + 4890
  val D4892 = D + 4892 // man sprites by state, bow, direction and animation
  val D4988 = D + 4988 // raft carrying the man
  val D4992 = D + 4992 // its speed
  val TirEnCoursFlag = D + 4994
  val NbFlechesTirees = D + 4996
  val NbLifes = D + 4998
  val NbPoints = D + 5000 // BCD
  val ExitFound = D + 5004
  val MoveManFlag = D + 5006
  val NbSeconds = D + 5008
  val TombWord4 = D + 5010 // bonus, BCD (long)
  val VaseBrokenX = D + 5014
  val VaseBrokenY = D + 5016
  val VaseBrokenCounter = D + 5018
  val PharaonKilledX = D + 5020
  val PharaonKilledY = D + 5022
  val PharaonKilledCounter = D + 5024
  val SerpentKilledX = D + 5026
  val SerpentKilledY = D + 5028
  val SerpentKilledCounter = D + 5030
  val D5032 = D + 5032 // values of the gold coins (BCD)
  val LineInfo = D + 5048
  val D5054 = D + 5054
  val PointsToAdd = D + 5090
  val D5092 = D + 5092 // points display: x, y, duration
  val D5094 = D + 5094
  val D5096 = D + 5096
  val WichTombPlayed = D + 5098
  val TxtTombSelect = D + 5100
  val TxtConfirme = D + 5140
  val TxtTomb1 = D + 5196
  val TxtTomb2 = D + 7980
  val TxtTomb3 = D + 11724
  val TxtTomb4 = D + 16166
  val TxtTomb5 = D + 20992
  val Blocs32x16 = D + 31348
  val BlocsOthers = D + 31352
  val Fonts8x8 = D + 31356

  /** Object record type tags (long at offset 6 of a record). */
  val Raft = 0xabcdeff0
  val Vase = 0xabcdeff1
  val WallShooter = 0xabcdeff2
  val Dragon = 0xabcdeff3
  val ToggleStone = 0xabcdeff4
  val Pharaoh = 0xabcdeff5
  val Snake = 0xabcdeff6
  val KillingStones = 0xabcdeff7
  val Sphere = 0xabcdeff8

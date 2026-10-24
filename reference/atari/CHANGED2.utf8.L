; --------------------------------------------
; Entombed désassemblé et adapté à mes besoins
; --------------------------------------------
;
; Mises à jour : 5, 6, 7, 9, 10, 11, 13 juin 1995, 1er juillet 1995
;                2, 3, 4, 5, 6 juillet 1995 (compréhension générale)
;
; (C) 1995 Olivier Bruchez
;
; A enlever dans la version définitive : - mshrink
;                                        - chgt. de frq. 68030
;                                        - chgt. résol.
;                                        - "option quitter"
; A faire pour adapation Smaky : - vérifier problème du scroll/format du
;                                  graphisme (entrelacement)

tomb_table1      equ (debut_bss+174080) ; Table 80*48 : définition de la tombe premier plan
tomb_table2      equ (debut_bss+177920) ; Table 80*48 : définition de la tombe second plan
tomb_table3      equ (debut_bss+181760) ; Table 80*48 : définition de la tombe
attributes_table equ (debut_bss+213504) ; Table des attributs pour les éléments du "paysage"

palettes         equ (debut_data+14)    ; Adresse des palettes
zero1            equ (debut_data+144)   ; Constante (vaut tjs zéro) pour la routine de "changement de pierres"
tomb_number2     equ (debut_data+146)   ; Numéro de la tombe choisie
stones_table     equ (debut_data+148)   ; Table de correspondance tombe-pierres
nb_doublecar     equ (debut_data+414)   ; Nombre de caractères dans la fonte/2 - 1
font_mask_table  equ (debut_data+416)   ; Table de masques pour la fonte de caractères
gameover_yoffset equ (debut_data+480)   ; Position du bloc "Game Over"
nb_max_gameover  equ (debut_data+482)   ; Nombre de "Game Over" maximum
nb_blocsothers85 equ (debut_data+486)   ; Nombre de blocs 32x24 à initialiser * 24 (nombre de lignes)
physscreen1      equ (debut_data+488)   ; Pointeur sur l'adresse de l'écran 1
physscreen2      equ (debut_data+492)   ; Pointeur sur l'adresse de l'écran 2
ikbd_buffer      equ (debut_data+496)   ; Buffer pour l'envoi de messages au processeur clavier
joystick_state   equ (debut_data+498)   ; Etat du joystick
pos_in_tomb_x    equ (debut_data+506)   ; Position relative de l'écran dans la tombe
pos_in_tomb_y    equ (debut_data+508)
a_main_counter   equ (debut_data+532)   ; *Compteur servant à un rout. devant s'exéc. 1x/2
man_synchro      equ (debut_data+534)   ; Compteur pour la synchronisation des mouvements de l'homme
fixation_synchro equ (debut_data+536)   ; Compteur pour la synchronisation des éléments qui se fixent
fixation_counter equ (debut_data+538)   ; Compteur contenant le nombre d'actions multiples déjà déclanchées
nb_secs_remain   equ (debut_data+540)   ; Nombre de secondes restantes pour retarder une action multiple
; + 542 : buffer
firstpage_done   equ (debut_data+546)   ; Flag : 0 --> présentation non faite
list_projects    equ (debut_data+556)
attr_tbl_data    equ (debut_data+1058)  ; Table des attributs (copié en BSS)
sound_priority   equ (debut_data+1484)  ; Priorité d'un son (interruption possible ou non d'un son)
sound_pause      equ (debut_data+1486)  ; Temps à attendre après le début d'un son
sound_address    equ (debut_data+1488)  ; Pointeur sur l'adresse du son
sounds18         equ (debut_data+1492)  ; Début des 18 sons principaux
sound_1          equ (debut_data+1534)  ; Adresse du son 1
sound_17         equ (debut_data+2206)  ; Adresse du son 17
sound_18         equ (debut_data+2236)  ; Adresse du son 18
sound_19         equ (debut_data+2266)  ; Adresse du son 19
end5levels       equ (debut_data+2296)  ; Texte de fin des 5 niveaux
bestscores       equ (debut_data+3014)  ; Texte des meilleurs scores
instr_title      equ (debut_data+3268)  ; Textes de la page d'instructions
instr_arc        equ (debut_data+3292)
instr_fleches    equ (debut_data+3395)
instr_cle        equ (debut_data+3422)
instr_or         equ (debut_data+3462)
instr_sablier    equ (debut_data+3501)
instr_coffre     equ (debut_data+3593)     
instr_vase       equ (debut_data+3635)    
instr_vie        equ (debut_data+3733)   
graph160routs    equ (debut_data+3754)  ; Début des 160 adresses de routines d'affichage
txt_copyright    equ (debut_data+4734)  ; Texte de copyright
tombs5           equ (debut_data+4768)  ; Début des adresses des 5 tombes
tomb_number      equ (debut_data+4790)  ; Numéro de la tombe choisie
tomb_datas       equ (debut_data+4792)  ; Pointeur sur les données de la tombe
tomb_word2       equ (debut_data+4800)  ; Mot 2 du header d'une tombe
tomb_word3       equ (debut_data+4802)  ; Mot 3 du header d'une tombe
tomb_chosen_flag equ (debut_data+4804)  ; Flag pour indiquer si la tombe a été choisie
non_retour_adr   equ (debut_data+4814)  ; Adresse du dernier point de non-retour
tomb_word0       equ (debut_data+4834)  ; Mot 0 du header d'une tombe
tomb_word1       equ (debut_data+4836)  ; Mot 1 du header d'une tombe
state_man        equ (debut_data+4838)  ; Etat de l'homme (tombant, sur échelle...)
nb_to_jump       equ (debut_data+4840)  ; Nombre de pixels à "monter" lors d'un saut
nb_cle           equ (debut_data+4880)  ; Nombre de clés
nb_arc           equ (debut_data+4882)  ; Nombre d'arc
nb_fleches       equ (debut_data+4884)  ; Nombre de flèches
move_man_counter equ (debut_data+4886)  ; Compteur pour le déplacement de l'homme
direction_man    equ (debut_data+4888)  ; Direction de l'homme
anim_man_counter equ (debut_data+4890)  ; Compteur pour l'animation de l'homme
tirencours_flag  equ (debut_data+4994)  ; Flag indiquant que l'homme est en train de tirer
nb_flechestirees equ (debut_data+4996)  ; Nombre de flèches "en l'air"
nb_lifes         equ (debut_data+4998)  ; Nombre de vie
nb_points        equ (debut_data+5000)  ; Nombre de points
exit_found       equ (debut_data+5004)  ; Flag indiquant que la sortie a été trouvée
move_man_flag    equ (debut_data+5006)  ; Flag pour la "réentrance" de la routine 'move_man'
nb_seconds       equ (debut_data+5008)  ; Nombre de seconde allouées par un sablier
tomb_word4       equ (debut_data+5010)  ; Mot 4 du header d'une tombe
vase_broken_x    equ (debut_data+5014)  ; Position d'un vase qui se casse
vase_broken_y    equ (debut_data+5016)
vase_broken_counter equ (debut_data+5018) ; Compteur pour l'animation d'un vase qui se casse
pharaon_killed_x equ (debut_data+5020) ; Position d'un pharaon mort
pharaon_killed_y equ (debut_data+5022)
pharaon_killed_counter equ (debut_data+5024) ; Compteur pour l'animation d'un pharaon mort
serpent_killed_x equ (debut_data+5026) ; Position d'un serpent mort
serpent_killed_y equ (debut_data+5028)
serpent_killed_counter equ (debut_data+5030) ; Compteur pour l'animation d'un serpent mort
line_info        equ (debut_data+5048)  ; Texte d'information de bas d'écran
points_to_add    equ (debut_data+5090)  ; Points à ajouter au compteur (-1 si aucun)
wich_tomb_played equ (debut_data+5098)  ; Inidique quelle tombe a déjà été finie
txt_tombselect   equ (debut_data+5100)  ; Texte de sélection d'une tombe
txt_confirme     equ (debut_data+5140)  ; Texte de confirmation de la sélection
txt_tomb1        equ (debut_data+5196)  ; Textes de commentaire des tombes
txt_tomb2        equ (debut_data+7980)
txt_tomb3        equ (debut_data+11724)
txt_tomb4        equ (debut_data+16166)
txt_tomb5        equ (debut_data+20992)
blocs32x16       equ (debut_data+31348) ; Pointeur sur adresse des blocs 32x16
blocsothers      equ (debut_data+31352) ; Pointeur sur adresse des autres blocs
fonts8x8         equ (debut_data+31356) ; Pointeur sur fonte de 100 caractères

              TEXT         

              ; Calcule la taille du programme
              move.l    a7,a5
              move.l    4(a5),a5
              move.l    12(a5),d7  ; TEXT
              add.l     20(a5),d7  ; + DATA
              add.l     28(a5),d7  ; + BSS
              add.l     #$100,d7   ; + Basepage

              ; Mshrink
              move.l    d7,-(sp)
              move.l    a5,-(sp)
              clr.w     -(sp)
              move.w    #$4a,-(sp)
              trap      #1
              add.l     #12,sp

; ----------------------------------
; Relocation de différentes adresses
; ----------------------------------

 ; Inscrit les 160 adresses des routines d'affichage de bloc
 ; dans le bloc de donnée
 LEA       relocation,A0
 LEA       graph160routs,A1
 MOVE.W    #160-1,D0
\bcl:
 MOVE.L    (A0)+,(A1)+
 DBRA      D0,\bcl

 ; Reloge les adresses des cinq tombes
 LEA       debut_data,A0
 MOVE.L    A0,D0
 ADD.L     D0,tombs5+0*4
 ADD.L     D0,tombs5+1*4
 ADD.L     D0,tombs5+2*4
 ADD.L     D0,tombs5+3*4
 ADD.L     D0,tombs5+4*4
 SUB.L     #22994,tombs5+0*4
 SUB.L     #22994,tombs5+1*4
 SUB.L     #22994,tombs5+2*4
 SUB.L     #22994,tombs5+3*4
 SUB.L     #22994,tombs5+4*4

 ; Adresse écran
 move.w #2,-(sp)
 trap #14
 addq.l #2,sp
 move.l d0,save_scr
 
 bss
 
save_scr:ds.l 1 
 
 text

 ; Basse résolution
 MOVE.W #%110110010,-(SP)
 MOVE.W #88,-(SP)
 TRAP #14
 ADDQ.L #4,SP
 
; ---------------
; Initialisations
; ---------------

 ; Désactive le curseur
 MOVE.W #0,-(A7) 
 MOVE.W #$15,-(A7)
 TRAP #$E
 ADDQ.L #4,A7

 ; Cherche l'adresse de l'écran physique
 MOVE.W #2,-(A7)
 TRAP #$E
 ADDQ.L #2,A7
 MOVE.L D0,physscreen1
 
 ; Adresse du nouvel écran
 MOVE.L #(debut_bss+0),D0
 CLR.B D0
 ADDI.L #$100,D0
 MOVE.L D0,physscreen2
 
 ; Relocation de 8 adresses ayant rapport avec les blocs graphiques
 ; 1) Début des 113 blocs de 32x16
 ; 2) Fin des 113 blocs de 32x16, début des autres blocs (toutes tailles, surtout 32x24)
 ; 3) Fin des blocs de toutes tailles, début de la fonte 8x8 (100 caractères)
 ; 4-8) Inutilisé ?
 MOVEA.L #blocs32x16,A0
 MOVE.L A0,D0
 MOVE.W #7,D7
L0000A:ADD.L D0,(A0)+
 DBRA D7,L0000A
 
 ; Cherche adresse de la table des adresses des routines clavier, joystick...
 MOVE.W #$22,-(A7)
 TRAP #$E
 ADDQ.L #2,A7
 MOVEA.L D0,A0
 MOVE.L #get_joystick,$18(A0) ; Nouvelle routine joystick
 
 JSR joystick_header ; Indique au processeur clavier le format désiré pour le joystick

 CLR.W joystick_state
 MOVE.W #1,D0
 JSR select_palette        ; Active la palette 1
 JSR attributes_table_init ; Initalise la table des attributs
 JSR blocs32x16_init       ; Initialise les blocs 32x16
 JSR blocsothers_init      ; Initialise les blocs 32x24 (et autres)
 JSR font_init             ; Initalise la fonte
 
 MOVE.W #0,tomb_number     ; Initalise numéro de tombe
 MOVE.W #0,zero1           ; Constante pour la routine de "changement de pierres"
 MOVE.W #0,tomb_number2    ; Initalise le numéro de tombe
 MOVE.W #0,firstpage_done  ; Page de présentation non-affichée
 
 JMP L0001A 
 
 ; Quitte (inutilisé)
 CLR.W -(A7)
 TRAP #1
  
; ----------------------------------
; Boucle englobant toutes les autres
; ----------------------------------   
  
L0001A:
 CMPI.W #0,firstpage_done ; Est-ce que la page de présentation a déjà été affichée ?
 BNE L0001B ; Oui
 
 JSR title_and_instr ; Affiche page de présentation
 
L0001B:
 CMPI.W #1,firstpage_done ; L'affichage de la page de présentation a été faite
 BNE L0001D
 
 ; Initialise quelques variables
 MOVE.W #7,nb_lifes     ; Sept vies
 MOVE.L #0,nb_points    ; Zéro point
 CLR.W tomb_chosen_flag ; Tombe pas encore choisie
 CLR.W tomb_number      ; Numéro de tombe à zéro
 CLR.L non_retour_adr   ; Point de retour au début de la tombe
 
 JSR play_game ; Phase du jeu à proprement parler

L0001D:BRA L0001A

; -------------------------------------------------------------
; Débute le jeu (choix de la tombe + jeu entier) : autre boucle
; -------------------------------------------------------------

play_game:
 MOVE.W #2,D0
 JSR palette_decrescendo
 
L00087:

 ; Initialisation de certaines variables
 CLR.W tirencours_flag           ; L'homme n'est pas en train de tirer
 CLR.W anim_man_counter          ; Initialise le compteur pour l'animation de l'homme
 MOVE.W #-$F,gameover_yoffset    ; Position initiale du "Game Over" descendant
 MOVE.W #$20,nb_max_gameover     ; 32 "Game Over" au maximum
 CLR.W exit_found                ; Sortie pas encore découverte
 MOVE.W #0,nb_cle                ; Pas de clés
 MOVE.W #0,nb_fleches            ; Pas de flèches
 MOVE.W #0,nb_arc                ; Pas d'arcs
 MOVE.W #-$8000,man_synchro      ; = $8000
 MOVE.W #-$8000,fixation_synchro ; = $8000
 CLR.W fixation_counter          ; Pas encore d'action multiple déclanchée
 MOVE.W #0,state_man             ; Homme sur le sol
 MOVE.W #-1,points_to_add        ; Pas de points à ajouter au compteur
 CLR.W nb_flechestirees          ; Pas encore de flèches tirées
 CLR.W nb_secs_remain            ; Pas de retardement d'action multiple
 MOVE.W #1,direction_man         ; Direction : homme vers la droite
 CLR.W vase_broken_counter       ; Pas de vase se cassant (compteur animation)
 CLR.W pharaon_killed_counter    ; Pas de pharaon tué (compteur animation)
 CLR.W serpent_killed_counter    ; Pas de serpent tué (compteur animation)
 CLR.W sound_priority             ; Priorité du "son actuel" nulle
 CLR.L sound_address             ; Pas d'adresse de son
 CLR.W nb_seconds                ; Pas de temps alloué par le sablier
 
 TST.W tomb_chosen_flag ; Tombe choisie ?
 BNE L0003E ; Oui
 
 JSR choose_tomb ; Choix de la tombe
 
 MOVE.W #2,D0
 JSR palette_decrescendo
 MOVE.W #1,tomb_chosen_flag ; Tombe choisie
 
L0003E:
 JSR read_tomb_datas ; Lit les données de la tombe
 
 CLR.W a_main_counter
 
 ; ----------------------------------
 ; Noyau principal de la phase de jeu
 ; ----------------------------------
 
main_loop:
 JSR L00042
 JSR do_fixe_elements
 JSR aff_all_screen
 BTST #0,(fixation_synchro+1)
 BNE L00046
 JSR do_fixe_elements
L00046:
 JSR aff_vase_or_dead
 MOVE.W pos_in_tomb_x,(debut_data+518)
 MOVE.W pos_in_tomb_y,(debut_data+520)
 MOVE.W (debut_data+510),(debut_data+522)
 MOVE.W (debut_data+512),(debut_data+524)
 MOVE.W (debut_data+514),(debut_data+526)
 MOVE.W (debut_data+516),(debut_data+528)
 JSR L00054
 JSR L00055
 JSR L00056
 JSR gere_project
 
 TST.W tirencours_flag ; L'homme est-il en train de tirer ?
 BEQ L00058 ; Non
 
 MOVE.W #5,D0
 BRA L0005A 
L00058:MOVE.W state_man,D0
L0005A:ASL.W #4,D0
 CMPI.W #0,nb_arc
 BEQ L0005C
 ADDI.W #8,D0
L0005C:MOVE.W direction_man,D1
 ASL.W #2,D1
 ADD.W D1,D0
 MOVE.W anim_man_counter,D1
 ANDI.W #3,D1
 ADD.W D1,D0
 MOVEA.L #(debut_data+4892),A0
 ADDA.W D0,A0
 CLR.W D0
 
 ; Affiche le bloc (A0) en D1, D2 (homme)
 MOVE.B (A0),D0
 MOVE.W tomb_word0,D1
 MOVE.W tomb_word1,D2
 JSR aff_bloc32x24
 
 TST.W D0
 BNE L00063
 
 CMPI.W #1,nb_lifes ; A-t-on fini la partie (perte des 6 vies) ?
 BEQ aff_game_over
 SUBI.W #1,nb_lifes ; Une vie en moins
 BRA play_game

; ----------------------------------
; Affiche le "Game Over" qui descend
; ----------------------------------

aff_game_over:
 SUBQ.W #1,nb_max_gameover
 BEQ L00065
 MOVE.W #$40,D0 ; Blocs 64, 65 et 66 
 MOVE.W pos_in_tomb_x,D1
 ADDI.W #$7A,D1
 MOVE.W pos_in_tomb_y,D2
 MOVE.W gameover_yoffset,D3
 ADD.W D3,D2
 MOVE.W #2,D3
L00068:MOVEM.W D0-D3,-(A7)
 JSR aff_bloc32x24 ; Affiche les blocs
 MOVEM.W (A7)+,D0-D3
 ADDQ.W #1,D0
 ADDI.W #$20,D1
 DBRA D3,L00068
 CMPI.W #$55,gameover_yoffset
 BEQ L00063
 ADDQ.W #5,gameover_yoffset
 BRA L00063
 
L00065:CLR.W wich_tomb_played
 MOVE.W #0,firstpage_done
 MOVE.W #2,D0
 JSR palette_decrescendo
 CLR.W tomb_number
 JSR show_best_scores
 BRA L0006C

;
; homme mort
;
 
L00063:

 JSR L0006D
 BTST #0,(fixation_synchro+1)
 BNE L0006E
 JSR do_fixe_elements
L0006E:ADDI.W #1,man_synchro
 CLR.L D0
 MOVE.W nb_secs_remain,D0
 BEQ L0006F
 ADDI.W #$C,D0
 MOVE.L D0,D1
 DIVU #$A0,D1
 MOVE.W D1,D2
 MULU #$A0,D2
 SUB.W D2,D0
 LSR.W #4,D0
 MOVE.W D0,-(A7)
 MOVE.W D1,D0
 MOVE.W pos_in_tomb_x,D1
 MOVE.W pos_in_tomb_y,D2
 ADDI.W #8,D1
 ADDI.W #$AC,D2
 JSR aff_09_sablier
 MOVE.W (A7)+,D0
 MOVE.W pos_in_tomb_x,D1
 MOVE.W pos_in_tomb_y,D2
 ADDI.W #$14,D1
 ADDI.W #$AC,D2
 JSR aff_09_sablier
 SUBI.W #1,nb_secs_remain
 EORI.W #1,fixation_synchro
 BRA L00075
L0006F:ADDI.W #1,fixation_synchro
L00075:TST.W (debut_data+518)
 BMI L00076
 MOVE.W (debut_data+518),pos_in_tomb_x
 MOVE.W (debut_data+520),pos_in_tomb_y
 MOVE.W (debut_data+522),(debut_data+510)
 MOVE.W (debut_data+524),(debut_data+512)
 MOVE.W (debut_data+526),(debut_data+514)
 MOVE.W (debut_data+528),(debut_data+516)
L00076:JSR aff_line_info
 CMPI.W #4,state_man
 BNE L00078
 
 ; Son : homme mort
 MOVE.W #$A,D0
 JSR prepare_address_of_sound

L00078:
 JSR play_yamaha
 JSR swap_screens
 CMPI.W #-$7FFF,man_synchro ; = $8001
 BNE L0007E
 MOVE.W #2,D0
 JSR palette_crescendo
L0007E:CMPI.W #4,state_man
 BEQ test_keyboard
 TST.W exit_found
 BEQ test_keyboard
 MOVE.L tomb_word4,D0
 JSR add_points
 JSR play_big_sound ; Joue un son (multiple)
 MOVE.W #2,D0
 JSR palette_decrescendo
 MOVE.W tomb_number,D0
 CMP.W (debut_data+4788),D0
 BNE L00085
 MOVE.W #0,firstpage_done
 CLR.W wich_tomb_played
 JSR show_best_scores
 RTS 
L00085:BSET D0,(wich_tomb_played+1)
 CLR.W tomb_chosen_flag  ; Il faut choisir une tombe
 CLR.L non_retour_adr
 BRA L00087 ; Recommence
 
; ------------------------------
; Teste le clavier durant le jeu
; ------------------------------
 
test_keyboard:
 ; Teste si une touche a été pressée
 MOVE.W #$FF,-(A7)
 MOVE.W #6,-(A7)
 TRAP #1
 ADDQ.L #4,A7
 
 ; ----- quitte -------
 cmp.b #"q",d0
 bne dont_quit

quit_sisi:
 ; adresse écran
 move.w #-1,-(sp)
 move.l save_scr,-(sp)
 move.l save_scr,-(sp)
 move.w #5,-(sp)
 trap #14
 add.l #12,sp
 
 ; haute résolution
 MOVE.W #%010111000,-(SP)
 MOVE.W #88,-(SP)
 TRAP #14
 ADDQ.L #4,SP

 ; réactive souris
 MOVE.B #$8,ikbd_buffer
 MOVE.L #ikbd_buffer,-(A7)
 MOVE.W #0,-(A7)
 MOVE.W #$19,-(A7)
 TRAP #$E
 ADDQ.L #8,A7
 
 ; quitte
 clr.w -(sp)
 trap #1
 
dont_quit:
 cmp.b #"Q",d0
 beq quit_sisi

 CMP.B #$70,D0 ; Touche 'p' pour pause
 BEQ L0008B
 CMP.B #$50,D0 ; Touche 'P' pour pause
 BEQ L0008B
 CMP.B #$30,D0 ; Touches en dessous de '0' à ignorer
 BCS main_loop
 CMP.B #$3A,D0 ; Touches en dessus de '9' à ignorer
 BCC main_loop
 CMP.B #$30,D0 ; Touches  '1' à '9'
 BNE L00090
 MOVE.W #$A,D0 ; La touche '0' a la valeur 10
 BRA L00091
L00090:ANDI.W #$FF,D0
 SUBI.B #$30,D0
L00091:MOVE.W nb_seconds,D1
 CMP.W D0,D1
 BCC L00092
 MOVE.W D1,D0
L00092:SUB.W D0,D1
 MOVE.W D1,nb_seconds
 ASL.W #4,D0
 ADD.W D0,nb_secs_remain
 BRA main_loop
 
L0008B:
 ; Teste le clavier
 MOVE.W #$FF,-(A7)
 MOVE.W #6,-(A7)
 TRAP #1
 ADDQ.L #4,A7
 TST.W D0
 BEQ L0008B
 
 BRA main_loop
L0006C:RTS

; -------------------------------------------------------------
; Pour le debugging (pas utilisé) : affiche D0.W en hexadécimal
; -------------------------------------------------------------

 MOVEM.W D0-D1,-(A7)
 LSR.W #8,D0
 LSR.W #4,D0
 JSR L00093
 MOVEM.W (A7)+,D0-D1
 MOVEM.W D0-D1,-(A7)
 LSR.W #8,D0
 ANDI.W #$F,D0
 ADDI.W #1,D1
 JSR L00093
 MOVEM.W (A7)+,D0-D1
 MOVEM.W D0-D1,-(A7)
 LSR.W #4,D0
 ANDI.W #$F,D0
 ADDI.W #8,D1
 JSR L00093
 MOVEM.W (A7)+,D0-D1
 ANDI.W #$F,D0
 ADDI.W #9,D1
 JSR L00093
 RTS
L00093:CMP.W #$A,D0
 BCS L00096
 ADDI.W #$37,D0
 BRA L00098
L00096:ADDI.W #$30,D0
L00098:JSR aff_car
 RTS

; -------------------------------------------------
; Choix de la tombe dans laquelle on souhaite jouer
; -------------------------------------------------
 
choose_tomb:MOVE.W #0,pos_in_tomb_x
 MOVE.W #0,pos_in_tomb_y
 MOVE.W #0,tomb_number
 MOVE.W #0,tomb_word0
 MOVE.W #0,tomb_word1

L000BE:
 ; Efface l'écran
 MOVEA.L physscreen2,A0
 MOVE.W #$1F3F,D7
L0009B:CLR.L (A0)+
 DBRA D7,L0009B
 MOVE.W wich_tomb_played,D0
 CMP.W #$F,D0
 BNE L0009C ; On a pas réussi les 4 premières tombes

 ; Ici, on a réussi les 4 premières tombes 
 MOVEA.L #txt_confirme,A0
 MOVE.W #0,D0
 MOVE.W #8,D1
 JSR aff_text
 
 MOVEA.L #txt_tomb5,A0
 MOVE.W #0,D0
 MOVE.W #$24,D1
 JSR aff_text
 
 MOVE.W #4,tomb_number
 MOVE.W #7,nb_lifes
 JSR swap_screens
 MOVE.W #2,D0
 JSR palette_crescendo

; Attend qu'on appuie sur le bouton feu
L000A3:MOVE.B joystick_state,D0
 ANDI.B #-$10,D0
 BNE L000A2
 BRA L000A3

 ; Choix de la tombe à proprement parler
L0009C:
 ; Affiche le titre de la page de sélection de la tombe
 MOVE.W #0,D0
 MOVE.W #8,D1
 MOVEA.L #txt_tombselect,A0
 JSR aff_text
 
 ; Commentaire de la tombe 1 
 MOVE.W #0,D0
 MOVE.W #$28,D1
 MOVEA.L #txt_tomb1,A0
 JSR aff_text

 ; Commentaire de la tombe 2 
 MOVE.W #0,D0
 MOVE.W #$50,D1
 MOVEA.L #txt_tomb2,A0
 JSR aff_text

 ; Commentaire de la tombe 3 
 MOVE.W #0,D0
 MOVE.W #$78,D1
 MOVEA.L #txt_tomb3,A0
 JSR aff_text

 ; Commentaire de la tombe 4 
 MOVE.W #0,D0
 MOVE.W #$A0,D1
 MOVEA.L #txt_tomb4,A0
 JSR aff_text
 
 ; Met en grisé les tombes déjà jouées
 MOVE.W wich_tomb_played,D7
 MOVE.W #$1900,D6
L000B4:TST.W D7
 BEQ L000AC
 BTST #0,D7
 BEQ L000AD
 MOVEA.L physscreen2,A0
 ADDA.W D6,A0
 MOVE.W #$13,D0
L000B3:MOVE.W #$27,D1
L000B2:ANDI.L #$AAAAAAAA,$A0(A0)
 ANDI.L #$55555555,(A0)+
 DBRA D1,L000B2
 ADDA.W #$A0,A0
 DBRA D0,L000B3
L000AD:LSR.W #1,D7
 ADDI.W #$1900,D6
 BRA L000B4

 ; Affichage du bonhomme à la bonne position 
L000AC:MOVE.W wich_tomb_played,D0
 MOVE.W tomb_number,D1
L000B6:BTST D1,D0
 BEQ L000B5
 ADDQ.W #1,D1
 ANDI.W #3,D1
 BRA L000B6
L000B5:

 ; Affiche l'homme devant la tombe sélectionnée
 MOVE.W D1,tomb_number
 MOVE.W #$B,D0
 MOVE.W tomb_word0,D1
 MOVE.W tomb_number,D2
 MULU #$28,D2
 ADDI.W #$28,D2
 JSR aff_bloc32x24
 
 JSR swap_screens
 TST.W tomb_word1
 BNE L000B8
 MOVE.W #1,tomb_word1
 MOVE.W #2,D0
 JSR palette_crescendo 
L000B8:

 ; Teste le clavier
 MOVE.W #$FF,-(A7)
 MOVE.W #6,-(A7)
 TRAP #1
 ADDQ.L #4,A7
 
 ; Teste si le joystick est UP
 MOVE.B joystick_state,D0
 BTST #0,D0
 BEQ L000B9
 
 ; Le joystick est UP
 MOVE.W wich_tomb_played,D0
 MOVE.W tomb_number,D1
L000BA:SUBQ.W #1,D1
 ANDI.W #3,D1
 BTST D1,D0
 BNE L000BA
 MOVE.W D1,tomb_number
 BRA L000BB
 
L000B9:BTST #1,D0
 BEQ L000BC
 
 ; Le joystick est DOWN
 MOVE.W tomb_number,D1
 ADDQ.W #1,D1
 ANDI.W #3,D1
 MOVE.W D1,tomb_number
 BRA L000BB
 
L000BC:ANDI.W #$F0,D0
 BNE L000A2 
L000BB:CLR.B joystick_state
 BRA L000BE

; Le choix de la tombe est fait
L000A2:JSR change_stones
 MOVE.W tomb_number,tomb_number2
 JSR change_stones
 JSR blocs32x16_init
 RTS
 
; -------------------------------------------------------
; Affiche vase qui se casse, pharaon ou serpent qui meurt
; -------------------------------------------------------
 
aff_vase_or_dead:
 ; Vase qui se casse
 TST.W vase_broken_counter
 BEQ L000C0
 
 ; Affiche vase qui se casse
 MOVE.W vase_broken_counter,D0
 SUBI.W #1,D0
 LSR.W #1,D0
 ADDI.W #$3C,D0
 MOVE.W vase_broken_x,D1
 MOVE.W vase_broken_y,D2
 JSR aff_bloc32x24
 ADDI.W #1,vase_broken_counter
 CMPI.W #7,vase_broken_counter
 BCS L000C0
 CLR.W vase_broken_counter
 
L000C0:TST.W pharaon_killed_counter
 BEQ L000C4
 
 ; Affiche pharaon mort
 MOVE.W pharaon_killed_counter,D0
 SUBI.W #1,D0
 LSR.W #1,D0
 ANDI.W #1,D0
 ADDI.W #$38,D0
 MOVE.W pharaon_killed_x,D1
 MOVE.W pharaon_killed_y,D2
 JSR aff_bloc32x24
 
 TST.W D0
 BNE L000C8
 CLR.W pharaon_killed_counter
 BRA L000C4
L000C8:CMPI.W #5,pharaon_killed_counter
 BCC L000C9
 SUBI.W #4,pharaon_killed_y
 BRA L000CA
L000C9:ADDI.W #4,pharaon_killed_y
L000CA:ADDI.W #1,pharaon_killed_counter
L000C4:TST.W serpent_killed_counter
 BEQ L000CB
 
 ; Affiche serpent mort
 MOVE.W serpent_killed_counter,D0
 SUBI.W #1,D0
 LSR.W #1,D0
 ANDI.W #1,D0
 ADDI.W #$3A,D0
 MOVE.W serpent_killed_x,D1
 MOVE.W serpent_killed_y,D2
 JSR aff_bloc32x24
 
 TST.W D0
 BNE L000CE
 CLR.W serpent_killed_counter
 BRA L000CB
L000CE:CMPI.W #5,serpent_killed_counter
 BCC L000CF
 SUBI.W #4,serpent_killed_y
 BRA L000D0
L000CF:ADDI.W #4,serpent_killed_y
L000D0:ADDI.W #1,serpent_killed_counter
L000CB:RTS

; -----------------------------------------------------
; Affiche la ligne d'information dans le bas de l'écran
; -----------------------------------------------------

aff_line_info:MOVEA.L #line_info,A0
 MOVE.W #$27,D7
L000D2:MOVE.B #$20,(A0)+
 DBRA D7,L000D2
 MOVEA.L #line_info,A0
 LEA 1(A0),A1
 MOVE.W nb_lifes,D7
 SUBQ.W #2,D7
 BMI L000D3
L000D5:MOVE.B #-$80,(A1)+
 DBRA D7,L000D5
L000D3:LEA $E(A0),A1
 MOVE.L nb_points,D6
 MOVE.W #5,D7
L000D6:MOVE.W D6,D0
 ANDI.W #$F,D0
 ADDI.W #$30,D0
 MOVE.B D0,-(A1)
 LSR.L #4,D6
 DBRA D7,L000D6
 LEA $F(A0),A1
 MOVE.W nb_arc,D7
 SUBQ.W #1,D7
 BMI L000D7
L000D9:MOVE.B #-$7F,(A1)+
 DBRA D7,L000D9
L000D7:MOVE.W nb_fleches,D7
 SUBQ.W #1,D7
 BMI L000DA
 LEA $13(A0),A1
L000DC:MOVE.B #-$7E,(A1)+
 DBRA D7,L000DC
L000DA:MOVE.W nb_cle,D7
 SUBQ.W #1,D7
 BMI L000DD
 LEA $20(A0),A1
L000DF:MOVE.B #-$7D,(A1)+
 DBRA D7,L000DF
L000DD:CLR.L D0
 MOVE.W nb_seconds,D0
 MOVE.W D0,D1
 DIVU #$A,D0
 MOVE.W D0,D2
 ADDI.W #$30,D0
 MOVE.B D0,$25(A0)
 ADD.W D2,D2
 MOVE.W D2,D0
 ASL.W #2,D2
 ADD.W D2,D0
 SUB.W D0,D1
 ADDI.W #$30,D1
 MOVE.B D1,$26(A0)
 MOVEA.L #line_info,A0
 MOVE.W #$7800,D0
 JSR aff_text2
 RTS

; ----------------------------------
; Affiche le mot "ENTOMBED" souligné
; ----------------------------------
 
aff_entombed:
 MOVEM.L D0-A6,-(A7)
 
 ; Efface l'écran
 MOVEA.L physscreen2,A0
 MOVE.W #$3E7,D7
L000E5:CLR.L (A0)+
 CLR.L (A0)+
 CLR.L (A0)+
 CLR.L (A0)+
 CLR.L (A0)+
 CLR.L (A0)+
 CLR.L (A0)+
 CLR.L (A0)+
 DBRA D7,L000E5
 
 ; Affiche le graphisme
 MOVEM.L D0-D1,-(A7)
 MOVE.W #$52,D0
 MOVE.W #$20,D1
 MOVE.W #$40,D2
 JSR aff_bloc32x24 ; Début du soulignement
 MOVE.W #$54,D0
 MOVE.W #$100,D1
 MOVE.W #$40,D2
 JSR aff_bloc32x24 ; Fin du soulignement
 MOVE.W #$53,D0
 MOVE.W #$40,D1
 MOVE.W #$40,D2
 MOVE.W #5,D3
L000E9:MOVEM.W D0-D3,-(A7)
 JSR aff_bloc32x24 ; Milieu du soulignement
 MOVEM.W (A7)+,D0-D3
 ADDI.W #$20,D1
 DBRA D3,L000E9
 MOVEM.L (A7)+,D0-D1
 SWAP D0
 MOVE.W D0,D2
 MOVE.W #$20,D1
 MOVE.W #$48,D0
 MOVE.W #7,D3
L000EB:MOVEM.W D0-D3,-(A7)
 JSR aff_bloc32x24 ; Affiche les lettres du mot "ENTOMBED"
 MOVEM.W (A7)+,D0-D3
 ADDI.W #$20,D1
 ADDQ.W #1,D0
 DBRA D3,L000EB
 MOVEM.L (A7)+,D0-A6
 RTS
 
; --------------------------------------
; Test 1 pour le debugging (pas utilisé)
; -------------------------------------- 
 
test1:
 MOVEM.L D0-A6,-(A7)
L000ED:CMPI.W #-1,(A6)
 BEQ L000EC
 MOVE.W (A6)+,D0
 ADDI.W #$48,D0
 MOVE.W (A6)+,D1
 MOVE.W (A6)+,D2
 ADD.W tomb_word1,D2
 MOVE.L A6,-(A7)
 JSR aff_bloc32x24
 MOVEA.L (A7)+,A6
 BRA L000ED
L000EC:MOVEM.L (A7)+,D0-A6
 RTS
 
; --------------------------------------
; Test 2 pour le debugging (pas utilisé)
; -------------------------------------- 
 
test2:
 MOVEA.L #tomb_table1,A0
 MOVEA.L #tomb_table2,A1
 MOVE.W #$EFF,D7
L000F3:CMPI.B #$18,(A0)
 BNE L000F1
 CMPI.B #$18,(A1)
 BNE L000F1
 MOVE.B #$1F,(A0)
 MOVE.B #-1,(A1)
L000F1:ADDQ.L #1,A0
 ADDQ.L #1,A1
 DBRA D7,L000F3
 RTS
 
; --------------------------------------
; Test 3 pour le debugging (pas utilisé)
; -------------------------------------- 
 
test3:
 MOVEA.L #tomb_table1,A0
 MOVEA.L #tomb_table2,A1
 MOVE.W #$EFF,D7
L000F5:CMPI.B #-1,(A1)
 BNE L000F4
 MOVE.B #$18,(A0)
 MOVE.B #$18,(A1)
L000F4:ADDQ.L #1,A0
 ADDQ.L #1,A1
 DBRA D7,L000F5
 RTS

; ------------------------------------------------------------
; Affiche la page de présentation (titre du jeu, instructions)
; ------------------------------------------------------------
 
title_and_instr:CLR.W pos_in_tomb_x
 CLR.W pos_in_tomb_y
 
 ; Efface l'écran
 MOVEA.L physscreen1,A0
 MOVE.W #$1F3F,D7
L000F6:CLR.L (A0)+
 DBRA D7,L000F6
 
 CLR.L D0
 SUBI.L #$180000,D0
 MOVE.L #$10000,D1
 JSR aff_entombed
 JSR swap_screens
 MOVE.W #2,D0
 JSR palette_crescendo
 CLR.L D0
 SUBI.L #$180000,D0
 MOVE.L #$10000,D1
L00100:JSR aff_entombed
 JSR swap_screens
 
 ; Bouton feu ?
 MOVE.B joystick_state,D2
 ANDI.B #-$10,D2
 BNE L000FA
 
 ADD.L D1,D0
 CMP.L #$280000,D0
 BMI L000FC
 MOVEM.L D0-D1,-(A7)
 
 ; Joue le son 1 : titre qui rebondit
 MOVE.L #sound_1,sound_address
 JSR play_yamaha
 
 MOVEM.L (A7)+,D0-D1
 MOVE.L #$280000,D0
 LSR.L #1,D1
 MOVE.L D1,D2
 LSR.L #1,D2
 ADD.L D2,D1
 CMP.L #$20000,D1
 BMI L000FF
 NEG.L D1
 BRA L00100
L000FC:ADDI.L #$8000,D1
 BRA L00100
L000FF:MOVE.L #$280000,D0
 JSR aff_entombed
 
 ; Affiche la moitié 1 du logo
 MOVE.W #$50,D0
 MOVE.W #$90,D1
 MOVE.W #$60,D2
 JSR aff_bloc32x24
 
 ; Affiche la moitié 2 du logo
 MOVE.W #$51,D0
 MOVE.W #$90,D1
 MOVE.W #$78,D2
 JSR aff_bloc32x24
 
 ; Affiche le texte de copyright
 MOVE.W #0,D0
 MOVE.W #$B0,D1
 MOVEA.L #txt_copyright,A0
 JSR aff_text
 
 JSR swap_screens
 MOVE.W #$96,D7
L00109:JSR wait_vbl

 ; Bouton feu ?
 MOVE.B joystick_state,D0
 ANDI.B #-$10,D0
 BNE L000FA

 DBRA D7,L00109
 MOVE.W #2,D0
 
 JSR palette_decrescendo
 
 ; Efface l'écran
 MOVEA.L physscreen2,A0
 MOVE.W #$1F3F,D7
L0010A:CLR.L (A0)+
 DBRA D7,L0010A
 
 ; Affiche les meilleurs scores
 MOVEA.L #bestscores,A0
 MOVE.W #$F38,D0
 JSR aff_text2
 
 MOVEA.L #(debut_data+3028),A0
 MOVE.W #$1BA0,D0
 MOVE.W #9,D7
L00112:MOVE.L (A0),D1
 MOVEM.L D0/D7-A0,-(A7)
 MOVEA.L #(debut_data+5054),A0
 MOVE.W #5,D7
L00110:MOVE.W D1,D0
 ANDI.W #$F,D0
 ADDI.W #$30,D0
 MOVE.B D0,-(A0)
 ROR.L #4,D1
 DBRA D7,L00110
 
 MOVE.B #0,6(A0)
 MOVEM.L (A7)+,D0/D7-A0
 MOVEM.L D0/D7-A0,-(A7)
 MOVEA.L #line_info,A0
 JSR aff_text2
 MOVEM.L (A7)+,D0/D7-A0
 MOVEM.L D0/D7-A0,-(A7)
 ADDQ.L #4,A0
 ADDI.W #$18,D0
 JSR aff_text2 
 MOVEM.L (A7)+,D0/D7-A0
 ADDI.W #$780,D0
 ADDA.W #$18,A0
 DBRA D7,L00112
 
 JSR swap_screens
 MOVE.W #2,D0
 JSR palette_crescendo
 MOVE.W #$FA,D7
L00114:JSR wait_vbl

 ; Bouton feu ?
 MOVE.B joystick_state,D0
 ANDI.B #-$10,D0
 BNE L000FA
 
 DBRA D7,L00114
 MOVE.W #2,D0
 JSR palette_decrescendo
 MOVE.W #0,pos_in_tomb_x
 MOVE.W #0,pos_in_tomb_y
 MOVE.W #8,(debut_data+142)
 
L00136:

 ; Efface l'écran
 MOVEA.L physscreen2,A0
 MOVE.W #$7CF,D7
L00117:CLR.L (A0)+
 CLR.L (A0)+
 CLR.L (A0)+
 CLR.L (A0)+
 DBRA D7,L00117
  
 ; Affiche arc
 MOVE.W #$2C,D0
 MOVE.W #$20,D1
 MOVE.W #$64,D2
 JSR aff_bloc32x24
 
 ; Affiche flèches
 MOVE.W #$2D,D0
 MOVE.W #$20,D1
 MOVE.W #$AE,D2
 JSR aff_bloc32x24
 
 ; Affiche clé
 MOVE.W #$2E,D0
 MOVE.W #$20,D1
 MOVE.W #$DA,D2
 JSR aff_bloc32x24
 
 ; Affiche pièce d'or
 MOVE.W #$2F,D0
 MOVE.W #$20,D1
 MOVE.W #$110,D2
 JSR aff_bloc32x24
 
 ; Affiche sablier
 MOVE.W #$30,D0
 MOVE.W #$20,D1
 MOVE.W #$13C,D2
 JSR aff_bloc32x24
 
 ; Affiche coffre
 MOVE.W #$31,D0
 MOVE.W #$20,D1
 MOVE.W #$17C,D2
 JSR aff_bloc32x24
 
 ; Affiche vase
 MOVE.W #$32,D0
 MOVE.W #$20,D1
 MOVE.W #$1B2,D2
 JSR aff_bloc32x24
 
 ; Affiche bas du vase?
 MOVE.W #$33,D0
 MOVE.W #$20,D1
 MOVE.W #$1CA,D2
 JSR aff_bloc32x24
 
 ; Affiche vie supplémentaire
 MOVE.W #$3F,D0
 MOVE.W #$20,D1
 MOVE.W #$1FC,D2
 JSR aff_bloc32x24
 
 ; Affiche titre de la page d'instruction
 MOVE.W #$48,D0
 MOVE.W #$32,D1
 MOVEA.L #instr_title,A0
 JSR aff_text
 
 ; Instruction pour l'arc
 MOVE.W #$50,D0
 MOVE.W #$64,D1
 MOVEA.L #instr_arc,A0
 JSR aff_text
 
 ; Instruction pour les flèches
 MOVE.W #$50,D0
 MOVE.W #$AE,D1
 MOVEA.L #instr_fleches,A0
 JSR aff_text
 
 ; Instruction pour la clé
 MOVE.W #$50,D0
 MOVE.W #$DA,D1
 MOVEA.L #instr_cle,A0
 JSR aff_text
 
 ; Instruction pour la pièce d'or
 MOVE.W #$50,D0
 MOVE.W #$110,D1
 MOVEA.L #instr_or,A0
 JSR aff_text
 
 ; Instruction pour le sablier
 MOVE.W #$50,D0
 MOVE.W #$13C,D1
 MOVEA.L #instr_sablier,A0
 JSR aff_text
 
 ; Instruction pour le coffre
 MOVE.W #$50,D0
 MOVE.W #$17C,D1
 MOVEA.L #instr_coffre,A0
 JSR aff_text
 
 ; Instruction pour le vase
 MOVE.W #$50,D0
 MOVE.W #$1B2,D1
 MOVEA.L #instr_vase,A0
 JSR aff_text
 
 ; Instruction pour la vie supplémentaire
 MOVE.W #$50,D0
 MOVE.W #$1FC,D1
 MOVEA.L #instr_vie,A0
 JSR aff_text
 
 ; Efface l'écran
 MOVEA.L physscreen2,A0
 MOVE.W #$13F,D7
L00133:CLR.L $7800(A0)
 CLR.L (A0)+
 DBRA D7,L00133
 
 JSR swap_screens
 CMPI.W #0,pos_in_tomb_y
 BNE L00134
 MOVE.W #2,D0
 JSR palette_crescendo
L00134:

 ; Bouton feu ?
 MOVE.B joystick_state,D1
 ANDI.B #-$10,D1
 BNE L000FA
 
 ADDI.W #1,pos_in_tomb_y
 CMPI.W #$1A8,pos_in_tomb_y
 BMI L00136
 MOVE.W #2,D0
 SUBQ.W #1,(debut_data+142)
 BMI title_and_instr
 JSR create_palette
 BRA L00136
L000FA:MOVE.W #1,firstpage_done
 RTS

; ---------------------------
; Charge la palette numéro D0
; ---------------------------
 
select_palette:ASL.W #5,D0
 ANDI.L #$FFFF,D0
 ADDI.L #palettes,D0
 MOVE.L D0,-(A7)
 MOVE.W #6,-(A7)
 TRAP #$E
 ADDQ.L #6,A7
 JSR wait_vbl
 RTS
 
; --------------------------
; Synchronisation sur la VBL
; -------------------------- 
 
wait_vbl:MOVE.W #$25,-(A7)
 TRAP #$E
 ADDQ.L #2,A7
 RTS

; ------------------------
; Swapping des deux écrans
; ------------------------
 
swap_screens:
 MOVEM.L D0/A0,-(A7)
 MOVE.L physscreen2,D0
 MOVE.L physscreen1,physscreen2
 MOVE.L D0,physscreen1 
 MOVE.W #-1,-(A7)
 MOVE.L D0,-(A7)
 MOVE.L D0,-(A7)
 MOVE.W #5,-(A7)
 TRAP #$E
 ADDA.L #$C,A7
 JSR wait_vbl
 MOVEM.L (A7)+,D0/A0
 RTS

; -----------------------------------------
; Routine de gestion "hardware" du joystick
; -----------------------------------------
 
get_joystick:MOVE.B 2(A0),joystick_state
 RTS

; ---------------------------------------------------------------
; Indique au processeur clavier le format désiré pour le joystick
; ---------------------------------------------------------------
 
joystick_header:MOVE.B #$14,ikbd_buffer
 MOVE.L #ikbd_buffer,-(A7)
 MOVE.W #0,-(A7)
 MOVE.W #$19,-(A7)
 TRAP #$E
 ADDQ.L #8,A7
 RTS
 
 ; Cherche l'adresse de l'écran (pas utilisé)
 MOVE.L D0,-(A7)
 CLR.L D0
 MOVE.B $FF8201,D0
 ASL.W #8,D0
 MOVE.B $FF8203,D0
 ASL.L #8,D0
 MOVEA.L D0,A2
 MOVE.L (A7)+,D0
 RTS

; -----------------------------------------------------
; Affiche tout l'arrière-fond de l'écran (le principal)
; ----------------------------------------------------- 
 
aff_all_screen:
 MOVE.W pos_in_tomb_x,D0
 LSR.W #5,D0
 MOVE.W pos_in_tomb_y,D1
 ANDI.W #-$10,D1
 MOVE.W D1,D2
 ADD.W D1,D1
 ADD.W D1,D1
 ADD.W D2,D1
 ADD.W D1,D0
 MOVEA.L #tomb_table1,A6
 ADDA.W D0,A6
 MOVE.L A6,(debut_data+4806)
 MOVE.W #$C,D7
 MOVEA.L #attributes_table,A0
 MOVEA.L #(debut_data+1374),A1
 CLR.W D0
L0014B: 
 MOVE.W #$A,D6
L00149:CMPI.B #$20,(A6)
 BNE L00141
 CMPI.B #$20,-$50(A6)
 BNE L00141
 MOVE.B #$41,(A6)
 BRA L00144
L00141:CMPI.B #$21,(A6)
 BNE L00144
 CMPI.B #$21,-$50(A6)
 BNE L00144
 MOVE.B #$42,(A6)
L00144:MOVE.B -1(A6),D0
 MOVE.B 0(A0,D0.W),D1
 MOVE.B -$50(A6),D0
 OR.B 0(A0,D0.W),D1
 MOVE.B -$51(A6),D0
 OR.B 0(A0,D0.W),D1
 BTST #7,D1
 BEQ L00148
 MOVE.B (A6),D0
 MOVE.B 0(A1,D0.W),(A6)
L00148:ADDQ.L #1,A6
 DBRA D6,L00149
 LEA $45(A6),A6
 DBRA D7,L0014B
 
 MOVEA.L (debut_data+4806),A6
 MOVEA.L #(debut_bss+173568),A5
 MOVE.W pos_in_tomb_x,D0
 ANDI.W #$1F,D0
 LSR.W #3,D0
 MOVE.L #rout_aff0,D1
 CMP.W #0,D0
 BEQ L0014E
 MOVE.L #rout_aff1,D1
 CMP.W #1,D0
 BEQ L0014E
 MOVE.L #rout_aff2,D1 
 CMP.W #2,D0
 BEQ L0014E
 MOVE.L #rout_aff3,D1
L0014E:
 MOVE.L D1,(debut_data+500)
 MOVE.W pos_in_tomb_x,D0
 ANDI.W #6,D0
 ASL.W #5,D0
 MOVE.W D0,D1
 ADD.W D0,D0
 ADD.W D0,D0
 ADD.W D1,D0
 MOVE.W D0,(debut_data+504)
 MOVE.W pos_in_tomb_y,D0
 ANDI.W #$F,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L #graph160routs,A4 ; Adresses des routines d'affichage
 ADDA.W D0,A4
 MOVE.W (debut_data+504),D6
 MOVE.W D6,D5
 ADD.W D0,D5
 ADDI.W #$40,D6
 ADD.W D0,D0
 ADD.W D0,D0
 ADD.W D0,D6 
 MOVEA.L physscreen2,A2
 ADD.W D0,D0
 MOVE.W D0,D1
 ADD.W D0,D0
 ADD.W D0,D0
 ADD.W D1,D0
 SUBA.W D0,A2
 MOVEA.L (debut_data+500),A0 
 JSR (A0) ; Affiche la première ligne de blocs
 
 MOVEA.L #graph160routs,A4 ; Adresses des routines d'affichage
 MOVE.W (debut_data+504),D5
 MOVE.W D5,D6
 ADDI.W #$40,D6
 
 ; Affiche les 10 lignes de blocs suivantes
 MOVE.W #$A,D7
L00155:SWAP D7
 MOVEA.L (debut_data+500),A0
 JSR (A0)
 SWAP D7
 DBRA D7,L00155
 
 ; Affiche la dernière ligne de blocs si celle-ci est tronquée
 MOVE.W pos_in_tomb_y,D0
 ANDI.W #$F,D0
 BEQ L00156
 NEG.W D0
 ADDI.W #$10,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L #graph160routs,A4 ; Adresses des routines d'affichage
 ADDA.W D0,A4
 ASL.W #3,D0
 MOVE.W D0,D1
 ADD.W D0,D0
 ADD.W D0,D0
 ADD.W D1,D0
 SUBA.W D0,A2
 MOVEA.L (debut_data+500),A0
 JSR (A0) 
L00156:

 MOVEA.L (debut_data+4806),A6
 MOVEA.L #(debut_data+1374),A1
 MOVE.W #$C,D7
 CLR.W D0
L0015A:MOVE.W #$A,D6
L00159:MOVE.B (A6),D0
 CMP.B #$40,D0
 BCS L00158
 MOVE.B 0(A1,D0.W),(A6)
L00158:ADDQ.L #1,A6
 DBRA D6,L00159
 LEA $45(A6),A6
 DBRA D7,L0015A
 RTS
 
; ----------------------------
; Routines d'affichage 0 à 159
; ---------------------------- 

 ; 0 à 15  
rout_aff0:
 MOVEA.L (A4),A3
 MOVE.W #9,D7
 CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A0
 ADDA.W D6,A0
L00195:CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A1
 MOVE.L A1,D4
 ADDA.W D5,A1
 JMP (A3)
A000:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,0(A2)
 MOVEP.L D1,1(A2)
 MOVEP.L D2,8(A2)
 MOVEP.L D3,9(A2)
A001:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$A0(A2)
 MOVEP.L D1,$A1(A2)
 MOVEP.L D2,$A8(A2)
 MOVEP.L D3,$A9(A2)
A002:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$140(A2)
 MOVEP.L D1,$141(A2)
 MOVEP.L D2,$148(A2)
 MOVEP.L D3,$149(A2)
A003:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$1E0(A2)
 MOVEP.L D1,$1E1(A2)
 MOVEP.L D2,$1E8(A2)
 MOVEP.L D3,$1E9(A2)
A004:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$280(A2)
 MOVEP.L D1,$281(A2)
 MOVEP.L D2,$288(A2)
 MOVEP.L D3,$289(A2)
A005:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$320(A2)
 MOVEP.L D1,$321(A2)
 MOVEP.L D2,$328(A2)
 MOVEP.L D3,$329(A2)
A006:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$3C0(A2)
 MOVEP.L D1,$3C1(A2)
 MOVEP.L D2,$3C8(A2)
 MOVEP.L D3,$3C9(A2)
A007:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$460(A2)
 MOVEP.L D1,$461(A2)
 MOVEP.L D2,$468(A2)
 MOVEP.L D3,$469(A2)
A008:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$500(A2)
 MOVEP.L D1,$501(A2)
 MOVEP.L D2,$508(A2)
 MOVEP.L D3,$509(A2)
A009:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$5A0(A2)
 MOVEP.L D1,$5A1(A2)
 MOVEP.L D2,$5A8(A2)
 MOVEP.L D3,$5A9(A2)
A010:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$640(A2)
 MOVEP.L D1,$641(A2)
 MOVEP.L D2,$648(A2)
 MOVEP.L D3,$649(A2)
A011:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$6E0(A2)
 MOVEP.L D1,$6E1(A2)
 MOVEP.L D2,$6E8(A2)
 MOVEP.L D3,$6E9(A2)
A012:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$780(A2)
 MOVEP.L D1,$781(A2)
 MOVEP.L D2,$788(A2)
 MOVEP.L D3,$789(A2)
A013:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$820(A2)
 MOVEP.L D1,$821(A2)
 MOVEP.L D2,$828(A2)
 MOVEP.L D3,$829(A2)
A014:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$8C0(A2)
 MOVEP.L D1,$8C1(A2)
 MOVEP.L D2,$8C8(A2)
 MOVEP.L D3,$8C9(A2)
A015:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$960(A2)
 MOVEP.L D1,$961(A2)
 MOVEP.L D2,$968(A2)
 MOVEP.L D3,$969(A2)
 LEA $10(A2),A2
 MOVEA.L D4,A0
 ADDA.W D6,A0
 
 DBRA D7,L00195
 LEA $45(A6),A6
 LEA $960(A2),A2
 RTS

 ; 16 à 63  
rout_aff1:
 CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A0
 ADDA.W D6,A0
 CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A1
 MOVE.L A1,D4
 ADDA.W D5,A1
 MOVEA.L $40(A4),A3
 JMP (A3)
A016:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,0(A2)
 MOVEP.L D2,1(A2)
 MOVEP.L D3,8(A2)
A017:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$A0(A2)
 MOVEP.L D2,$A1(A2)
 MOVEP.L D3,$A8(A2)
A018:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$140(A2)
 MOVEP.L D2,$141(A2)
 MOVEP.L D3,$148(A2)
A019:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$1E0(A2)
 MOVEP.L D2,$1E1(A2)
 MOVEP.L D3,$1E8(A2)
A020:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$280(A2)
 MOVEP.L D2,$281(A2)
 MOVEP.L D3,$288(A2)
A021:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$320(A2)
 MOVEP.L D2,$321(A2)
 MOVEP.L D3,$328(A2)
A022:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$3C0(A2)
 MOVEP.L D2,$3C1(A2)
 MOVEP.L D3,$3C8(A2)
A023:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$460(A2)
 MOVEP.L D2,$461(A2)
 MOVEP.L D3,$468(A2)
A024:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$500(A2)
 MOVEP.L D2,$501(A2)
 MOVEP.L D3,$508(A2)
A025:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$5A0(A2)
 MOVEP.L D2,$5A1(A2)
 MOVEP.L D3,$5A8(A2)
A026:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$640(A2)
 MOVEP.L D2,$641(A2)
 MOVEP.L D3,$648(A2)
A027:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$6E0(A2)
 MOVEP.L D2,$6E1(A2)
 MOVEP.L D3,$6E8(A2)
A028:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$780(A2)
 MOVEP.L D2,$781(A2)
 MOVEP.L D3,$788(A2)
A029:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$820(A2)
 MOVEP.L D2,$821(A2)
 MOVEP.L D3,$828(A2)
A030:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$8C0(A2)
 MOVEP.L D2,$8C1(A2)
 MOVEP.L D3,$8C8(A2)
A031:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D1,$960(A2)
 MOVEP.L D2,$961(A2)
 MOVEP.L D3,$968(A2)
 LEA 9(A2),A2
 MOVE.W #8,D7
 MOVEA.L $80(A4),A3
L001B5:MOVEA.L D4,A0
 ADDA.W D6,A0
 CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A1
 MOVE.L A1,D4
 ADDA.W D5,A1
 JMP (A3)
A032:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,0(A2)
 MOVEP.L D1,7(A2)
 MOVEP.L D2,8(A2)
 MOVEP.L D3,$F(A2)
A033:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$A0(A2)
 MOVEP.L D1,$A7(A2)
 MOVEP.L D2,$A8(A2)
 MOVEP.L D3,$AF(A2)
A034:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$140(A2)
 MOVEP.L D1,$147(A2)
 MOVEP.L D2,$148(A2)
 MOVEP.L D3,$14F(A2)
A035:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$1E0(A2)
 MOVEP.L D1,$1E7(A2)
 MOVEP.L D2,$1E8(A2)
 MOVEP.L D3,$1EF(A2)
A036:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$280(A2)
 MOVEP.L D1,$287(A2)
 MOVEP.L D2,$288(A2)
 MOVEP.L D3,$28F(A2)
A037:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$320(A2)
 MOVEP.L D1,$327(A2)
 MOVEP.L D2,$328(A2)
 MOVEP.L D3,$32F(A2)
A038:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$3C0(A2)
 MOVEP.L D1,$3C7(A2)
 MOVEP.L D2,$3C8(A2)
 MOVEP.L D3,$3CF(A2)
A039:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$460(A2)
 MOVEP.L D1,$467(A2)
 MOVEP.L D2,$468(A2)
 MOVEP.L D3,$46F(A2)
A040:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$500(A2)
 MOVEP.L D1,$507(A2)
 MOVEP.L D2,$508(A2)
 MOVEP.L D3,$50F(A2)
A041:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$5A0(A2)
 MOVEP.L D1,$5A7(A2)
 MOVEP.L D2,$5A8(A2)
 MOVEP.L D3,$5AF(A2)
A042:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$640(A2)
 MOVEP.L D1,$647(A2)
 MOVEP.L D2,$648(A2)
 MOVEP.L D3,$64F(A2)
A043:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$6E0(A2)
 MOVEP.L D1,$6E7(A2)
 MOVEP.L D2,$6E8(A2)
 MOVEP.L D3,$6EF(A2)
A044:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$780(A2)
 MOVEP.L D1,$787(A2)
 MOVEP.L D2,$788(A2)
 MOVEP.L D3,$78F(A2)
A045:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$820(A2)
 MOVEP.L D1,$827(A2)
 MOVEP.L D2,$828(A2)
 MOVEP.L D3,$82F(A2)
A046:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$8C0(A2)
 MOVEP.L D1,$8C7(A2)
 MOVEP.L D2,$8C8(A2)
 MOVEP.L D3,$8CF(A2)
A047:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$960(A2)
 MOVEP.L D1,$967(A2)
 MOVEP.L D2,$968(A2)
 MOVEP.L D3,$96F(A2)
 LEA $10(A2),A2
 DBRA D7,L001B5
 
 MOVEA.L D4,A0
 ADDA.W D6,A0
 CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A1
 MOVE.L A1,D4
 ADDA.W D5,A1
 MOVEA.L $C0(A4),A3
 JMP (A3)
A048:
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,0(A2)
A049: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$A0(A2)
A050: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$140(A2)
A051: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$1E0(A2)
A052: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$280(A2)
A053: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$320(A2)
A054: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$3C0(A2)
A055: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$460(A2)
A056: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$500(A2)
A057: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$5A0(A2)
A058: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$640(A2)
A059: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$6E0(A2)
A060: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$780(A2)
A061: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$820(A2)
A062: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$8C0(A2)
A063: 
 MOVE.L (A0)+,D0
 LEA $C(A0),A0
 MOVEP.L D0,$960(A2)
 LEA $44(A6),A6
 LEA $967(A2),A2
 RTS

 ; 64 à 111 
rout_aff2:CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A0
 ADDA.W D6,A0
 CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A1
 MOVE.L A1,D4
 ADDA.W D5,A1
 MOVEA.L $100(A4),A3
 JMP (A3)
A064:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,0(A2)
 MOVEP.L D1,1(A2)
A065: 
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$A0(A2)
 MOVEP.L D1,$A1(A2)
A066:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$140(A2)
 MOVEP.L D1,$141(A2)
A067:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$1E0(A2)
 MOVEP.L D1,$1E1(A2)
A068:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$280(A2)
 MOVEP.L D1,$281(A2)
A069:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$320(A2)
 MOVEP.L D1,$321(A2)
A070:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$3C0(A2)
 MOVEP.L D1,$3C1(A2)
A071:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$460(A2)
 MOVEP.L D1,$461(A2)
A072:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$500(A2)
 MOVEP.L D1,$501(A2)
A073:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$5A0(A2)
 MOVEP.L D1,$5A1(A2)
A074:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$640(A2)
 MOVEP.L D1,$641(A2)
A075:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$6E0(A2)
 MOVEP.L D1,$6E1(A2)
A076:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$780(A2)
 MOVEP.L D1,$781(A2)
A077:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$820(A2)
 MOVEP.L D1,$821(A2)
A078:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$8C0(A2)
 MOVEP.L D1,$8C1(A2)
A079:
 LEA 8(A0),A0
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 OR.L (A1)+,D1
 MOVEP.L D0,$960(A2)
 MOVEP.L D1,$961(A2)
 LEA 8(A2),A2
 MOVE.W #8,D7
 MOVEA.L $140(A4),A3
L001B8:MOVEA.L D4,A0
 ADDA.W D6,A0
 CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A1
 MOVE.L A1,D4
 ADDA.W D5,A1
 JMP (A3)
A080:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,0(A2)
 MOVEP.L D1,1(A2)
 MOVEP.L D2,8(A2)
 MOVEP.L D3,9(A2)
A081: 
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$A0(A2)
 MOVEP.L D1,$A1(A2)
 MOVEP.L D2,$A8(A2)
 MOVEP.L D3,$A9(A2)
A082:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$140(A2)
 MOVEP.L D1,$141(A2)
 MOVEP.L D2,$148(A2)
 MOVEP.L D3,$149(A2)
A083:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$1E0(A2)
 MOVEP.L D1,$1E1(A2)
 MOVEP.L D2,$1E8(A2)
 MOVEP.L D3,$1E9(A2)
A084:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$280(A2)
 MOVEP.L D1,$281(A2)
 MOVEP.L D2,$288(A2)
 MOVEP.L D3,$289(A2)
A085:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$320(A2)
 MOVEP.L D1,$321(A2)
 MOVEP.L D2,$328(A2)
 MOVEP.L D3,$329(A2)
A086:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$3C0(A2)
 MOVEP.L D1,$3C1(A2)
 MOVEP.L D2,$3C8(A2)
 MOVEP.L D3,$3C9(A2)
A087:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$460(A2)
 MOVEP.L D1,$461(A2)
 MOVEP.L D2,$468(A2)
 MOVEP.L D3,$469(A2)
A088:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$500(A2)
 MOVEP.L D1,$501(A2)
 MOVEP.L D2,$508(A2)
 MOVEP.L D3,$509(A2)
A089:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$5A0(A2)
 MOVEP.L D1,$5A1(A2)
 MOVEP.L D2,$5A8(A2)
 MOVEP.L D3,$5A9(A2)
A090:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$640(A2)
 MOVEP.L D1,$641(A2)
 MOVEP.L D2,$648(A2)
 MOVEP.L D3,$649(A2)
A091:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$6E0(A2)
 MOVEP.L D1,$6E1(A2)
 MOVEP.L D2,$6E8(A2)
 MOVEP.L D3,$6E9(A2)
A092:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$780(A2)
 MOVEP.L D1,$781(A2)
 MOVEP.L D2,$788(A2)
 MOVEP.L D3,$789(A2)
A093:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$820(A2)
 MOVEP.L D1,$821(A2)
 MOVEP.L D2,$828(A2)
 MOVEP.L D3,$829(A2)
A094:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$8C0(A2)
 MOVEP.L D1,$8C1(A2)
 MOVEP.L D2,$8C8(A2)
 MOVEP.L D3,$8C9(A2)
A095:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$960(A2)
 MOVEP.L D1,$961(A2)
 MOVEP.L D2,$968(A2)
 MOVEP.L D3,$969(A2)
 LEA $10(A2),A2
 DBRA D7,L001B8
 MOVEA.L D4,A0
 ADDA.W D6,A0
 CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A1
 MOVE.L A1,D4
 ADDA.W D5,A1
 MOVEA.L $180(A4),A3
 JMP (A3)
A096:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,0(A2)
 MOVEP.L D1,1(A2)
A097:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$A0(A2)
 MOVEP.L D1,$A1(A2)
A098:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$140(A2)
 MOVEP.L D1,$141(A2)
A099:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$1E0(A2)
 MOVEP.L D1,$1E1(A2)
A100:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$280(A2)
 MOVEP.L D1,$281(A2)
A101:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$320(A2)
 MOVEP.L D1,$321(A2)
A102:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$3C0(A2)
 MOVEP.L D1,$3C1(A2)
A103:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$460(A2)
 MOVEP.L D1,$461(A2)
A104:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$500(A2)
 MOVEP.L D1,$501(A2)
A105:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$5A0(A2)
 MOVEP.L D1,$5A1(A2)
A106:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$640(A2)
 MOVEP.L D1,$641(A2)
A107:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$6E0(A2)
 MOVEP.L D1,$6E1(A2)
A108:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$780(A2)
 MOVEP.L D1,$781(A2)
A109:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$820(A2)
 MOVEP.L D1,$821(A2)
A110:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$8C0(A2)
 MOVEP.L D1,$8C1(A2)
A111:
 MOVE.L (A0)+,D0
 MOVE.L (A0)+,D1
 LEA 8(A0),A0
 MOVEP.L D0,$960(A2)
 MOVEP.L D1,$961(A2)
 LEA $44(A6),A6
 LEA $968(A2),A2
 RTS
 
 ; 112 à 160 
rout_aff3:CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A0
 ADDA.W D6,A0
 CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A1
 MOVE.L A1,D4
 ADDA.W D5,A1
 MOVEA.L $1C0(A4),A3
 JMP (A3)
A112:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,0(A2)
A113:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$A0(A2)
A114:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$140(A2)
A115:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$1E0(A2)
A116:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$280(A2)
A117:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$320(A2)
A118:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$3C0(A2)
A119:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$460(A2)
A120:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$500(A2)
A121:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$5A0(A2)
A122:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$640(A2)
A123:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$6E0(A2)
A124:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$780(A2)
A125:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$820(A2)
A126:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$8C0(A2)
A127:
 LEA $C(A0),A0
 MOVE.L (A0)+,D0
 OR.L (A1)+,D0
 MOVEP.L D0,$960(A2)
 LEA 1(A2),A2
 MOVE.W #8,D7
 MOVEA.L $200(A4),A3
L001BC:MOVEA.L D4,A0
 ADDA.W D6,A0
 CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A1
 MOVE.L A1,D4
 ADDA.W D5,A1
 JMP (A3)
A128:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,0(A2)
 MOVEP.L D1,7(A2)
 MOVEP.L D2,8(A2)
 MOVEP.L D3,$F(A2)
A129:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$A0(A2)
 MOVEP.L D1,$A7(A2)
 MOVEP.L D2,$A8(A2)
 MOVEP.L D3,$AF(A2)
A130:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$140(A2)
 MOVEP.L D1,$147(A2)
 MOVEP.L D2,$148(A2)
 MOVEP.L D3,$14F(A2)
A131:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$1E0(A2)
 MOVEP.L D1,$1E7(A2)
 MOVEP.L D2,$1E8(A2)
 MOVEP.L D3,$1EF(A2)
A132:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$280(A2)
 MOVEP.L D1,$287(A2)
 MOVEP.L D2,$288(A2)
 MOVEP.L D3,$28F(A2)
A133:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$320(A2)
 MOVEP.L D1,$327(A2)
 MOVEP.L D2,$328(A2)
 MOVEP.L D3,$32F(A2)
A134:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$3C0(A2)
 MOVEP.L D1,$3C7(A2)
 MOVEP.L D2,$3C8(A2)
 MOVEP.L D3,$3CF(A2)
A135:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$460(A2)
 MOVEP.L D1,$467(A2)
 MOVEP.L D2,$468(A2)
 MOVEP.L D3,$46F(A2)
A136:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$500(A2)
 MOVEP.L D1,$507(A2)
 MOVEP.L D2,$508(A2)
 MOVEP.L D3,$50F(A2)
A137:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$5A0(A2)
 MOVEP.L D1,$5A7(A2)
 MOVEP.L D2,$5A8(A2)
 MOVEP.L D3,$5AF(A2)
A138:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$640(A2)
 MOVEP.L D1,$647(A2)
 MOVEP.L D2,$648(A2)
 MOVEP.L D3,$64F(A2)
A139:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$6E0(A2)
 MOVEP.L D1,$6E7(A2)
 MOVEP.L D2,$6E8(A2)
 MOVEP.L D3,$6EF(A2)
A140:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$780(A2)
 MOVEP.L D1,$787(A2)
 MOVEP.L D2,$788(A2)
 MOVEP.L D3,$78F(A2)
A141:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$820(A2)
 MOVEP.L D1,$827(A2)
 MOVEP.L D2,$828(A2)
 MOVEP.L D3,$82F(A2)
A142:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$8C0(A2)
 MOVEP.L D1,$8C7(A2)
 MOVEP.L D2,$8C8(A2)
 MOVEP.L D3,$8CF(A2)
A143:
 MOVEM.L (A0)+,D0-D3
 OR.L (A1)+,D3
 MOVEP.L D0,$960(A2)
 MOVEP.L D1,$967(A2)
 MOVEP.L D2,$968(A2)
 MOVEP.L D3,$96F(A2)
 LEA $10(A2),A2
 DBRA D7,L001BC
 MOVEA.L D4,A0
 ADDA.W D6,A0
 CLR.W D0
 MOVE.B (A6)+,D0
 ADD.W D0,D0
 ADD.W D0,D0
 MOVEA.L 0(A5,D0.W),A1
 MOVE.L A1,D4
 ADDA.W D5,A1
 MOVEA.L $240(A4),A3
 JMP (A3)
A144:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,0(A2)
 MOVEP.L D1,7(A2)
 MOVEP.L D2,8(A2)
A145:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$A0(A2)
 MOVEP.L D1,$A7(A2)
 MOVEP.L D2,$A8(A2)
A146:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$140(A2)
 MOVEP.L D1,$147(A2)
 MOVEP.L D2,$148(A2)
A147:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$1E0(A2)
 MOVEP.L D1,$1E7(A2)
 MOVEP.L D2,$1E8(A2)
A148:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$280(A2)
 MOVEP.L D1,$287(A2)
 MOVEP.L D2,$288(A2)
A149:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$320(A2)
 MOVEP.L D1,$327(A2)
 MOVEP.L D2,$328(A2)
A150:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$3C0(A2)
 MOVEP.L D1,$3C7(A2)
 MOVEP.L D2,$3C8(A2)
A151:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$460(A2)
 MOVEP.L D1,$467(A2)
 MOVEP.L D2,$468(A2)
A152:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$500(A2)
 MOVEP.L D1,$507(A2)
 MOVEP.L D2,$508(A2)
A153:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$5A0(A2)
 MOVEP.L D1,$5A7(A2)
 MOVEP.L D2,$5A8(A2)
A154:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$640(A2)
 MOVEP.L D1,$647(A2)
 MOVEP.L D2,$648(A2)
A155:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$6E0(A2)
 MOVEP.L D1,$6E7(A2)
 MOVEP.L D2,$6E8(A2)
A156:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$780(A2)
 MOVEP.L D1,$787(A2)
 MOVEP.L D2,$788(A2)
A157:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$820(A2)
 MOVEP.L D1,$827(A2)
 MOVEP.L D2,$828(A2)
A158:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$8C0(A2)
 MOVEP.L D1,$8C7(A2)
 MOVEP.L D2,$8C8(A2)
A159:
 MOVEM.L (A0)+,D0-D3
 MOVEP.L D0,$960(A2)
 MOVEP.L D1,$967(A2)
 MOVEP.L D2,$968(A2)
 LEA $44(A6),A6
 LEA $96F(A2),A2
 RTS
 
; ----------------------------------------------------
; Change l'apparence de certaines pierres (11 pierres)
; ----------------------------------------------------
 
change_stones:
 MOVE.W zero1,D0
 MULU #$16,D0
 ADDI.L #stones_table,D0
 MOVEA.L D0,A0
 
 MOVE.W tomb_number2,D0
 MULU #$16,D0
 ADDI.L #stones_table,D0
 MOVEA.L D0,A1
 
 MOVE.W #$A,D7 
L001C1:MOVE.W (A0)+,D0 ; Numéro de bloc (0-112)
 MULU #$100,D0
 ADD.L blocs32x16,D0
 MOVEA.L D0,A2 ; Pointe sur le bloc (256 octets)
 
 MOVE.W (A1)+,D0 ; Numéro de bloc (0-112)
 MULU #$100,D0
 ADD.L blocs32x16,D0
 MOVEA.L D0,A3  ; Pointe sur le bloc (256 octets)
 
 MOVE.W #$3F,D6
L001C0:MOVE.L (A2),D0 ; Echange le contenu des blocs
 MOVE.L (A3),(A2)+
 MOVE.L D0,(A3)+
 DBRA D6,L001C0
 DBRA D7,L001C1
 RTS

; --------------------------------------------------------------
; Affiche les blocs 32x16 pour leur effectuer une transformation
; --------------------------------------------------------------
 
blocs32x16_init:
 MOVEA.L #(debut_data+1154),A0
 MOVE.W (debut_data+1152),D7
 
 MOVEA.L physscreen2,A1
L001CA:CLR.W D0
 MOVE.B (A0)+,D0 ; Numéro du bloc (0-112)
 MOVE.B (A0)+,D1
 MULU #$100,D0
 ADD.L blocs32x16,D0
 MOVEA.L D0,A2 ; Adresse du bloc (256 octets)
 
 CMP.B #0,D1
 BNE L001C4
 MOVE.W #$3F,D0
L001C5:MOVE.L (A2)+,(A1)+
 DBRA D0,L001C5
 BRA L001C6
L001C4:MOVE.W #$18,D0
 CMP.B #1,D1
 BEQ L001C7
 
 MOVE.W #$36,D0 ; Bloc N°54 (mur de fond)
L001C7:MULU #$100,D0
 ADD.L blocs32x16,D0
 MOVEA.L D0,A3 ; Adresse du bloc N°54
 
 MOVE.W #$1F,D0
L001C9:MOVE.W (A2),D1
 OR.W 2(A2),D1
 OR.W 4(A2),D1
 OR.W 6(A2),D1
 NOT.W D1
 MOVE.W D1,D2
 SWAP D1
 MOVE.W D2,D1
 MOVE.L D1,D2
 AND.L (A3)+,D1
 AND.L (A3)+,D2
 OR.L (A2)+,D1
 OR.L (A2)+,D2
 MOVE.L D1,(A1)+
 MOVE.L D2,(A1)+
 DBRA D0,L001C9
L001C6:DBRA D7,L001CA

 MOVE.W (debut_data+1152),D7
 MOVEA.L physscreen2,A0
 MOVEA.L #(debut_bss+32768),A1
 MOVEA.L #(debut_bss+173568),A3
L001D0:MOVE.L A1,(A3)+
 LEA $40(A1),A2
 MOVE.L A1,-(A7)
 SWAP D7
 MOVE.W #$F,D7
L001CF:MOVE.W #3,D6
L001CE:MOVE.W #3,D5
 CLR.B D0
 MOVE.B (A0),D1
 MOVE.B 1(A0),D2
 MOVE.B 8(A0),D3
 MOVE.B 9(A0),D4
L001CC:MOVE.B D0,(A1)
 MOVE.B D1,(A2)
 MOVE.B D2,4(A2)
 MOVE.B D3,8(A2)
 MOVE.B D4,$C(A2)
 LEA $140(A1),A1
 LEA $140(A2),A2
 ADD.B D4,D4
 ADDX.B D3,D3
 ADDX.B D2,D2
 ADDX.B D1,D1
 ADDX.B D0,D0
 ADD.B D4,D4
 ADDX.B D3,D3
 ADDX.B D2,D2
 ADDX.B D1,D1
 ADDX.B D0,D0
 DBRA D5,L001CC
 LEA -$4FF(A1),A1
 LEA -$4FF(A2),A2
 LEA 2(A0),A0
 DBRA D6,L001CE
 LEA 8(A0),A0
 LEA $C(A2),A2
 DBRA D7,L001CF
 MOVEA.L (A7)+,A1
 LEA $500(A1),A1
 SWAP D7
 DBRA D7,L001D0
 MOVE.L #(debut_bss+121088),(debut_bss+173648)
 MOVE.L #(debut_bss+123648),(debut_bss+173652)
 RTS

; -
; gestion de 
; -
 
L00054: 
 CMPI.W #4,state_man
 BNE L001D5
 ADDI.W #1,anim_man_counter
 ADDI.W #1,move_man_counter
 CMPI.W #4,move_man_counter
 BCS L001D7
 ADDI.W #4,tomb_word1
 BRA L001D8
L001D7:SUBI.W #4,tomb_word1
 BRA L001D8
L001D5:
 
 ; Joystick à gauche ?
 MOVE.B joystick_state,D7
 BTST #2,D7
 BEQ L001D9
 
 MOVE.W #0,direction_man
 BRA L001DA
L001D9:

 ; Joystick à droite ?
 BTST #3,D7
 BEQ L001DA
 
 MOVE.W #1,direction_man
L001DA:

 TST.W tirencours_flag ; L'homme est-il en train de tirer ?
 BEQ L001DB ; Non
 
 CMPI.W #3,anim_man_counter
 BEQ L001DC
 ADDI.W #1,anim_man_counter
 CLR.W D7
 BRA L001DD
L001DC:

 ; Affiche la flèche et s'occupe de sa trajectoire
 MOVE.W direction_man,D0
 MOVE.W #1,D1
 MOVE.W tomb_word0,D2
 MOVE.W tomb_word1,D3
 JSR do_project
 
 ; Arc qui tire
 MOVE.W #0,D0
 JSR prepare_address_of_sound
 
 CLR.W tirencours_flag ; L'homme n'est plus en train de tirer
 ADDI.W #1,nb_flechestirees
 SUBI.W #1,nb_fleches
L001DB:CMPI.W #3,nb_flechestirees
 BEQ L001DD
 TST.W nb_fleches
 BEQ L001DD
 TST.W nb_arc
 BEQ L001DD
 MOVE.W D7,D0
 ANDI.W #$F0,D0
 BEQ L001DD
 CMPI.W #3,state_man
 BEQ L001DD
 
 MOVE.W #1,tirencours_flag ; L'homme commence à tirer
 MOVE.W #0,anim_man_counter
 CLR.W D7
L001DD:MOVEA.L #attributes_table,A1
 MOVE.W state_man,D0
 CMP.W #0,D0
 BEQ gere_mouvements
 CMP.W #2,D0
 BEQ jumping_man
 CMP.W #1,D0
 BEQ falling_man
 CMP.W #3,D0
 BEQ laddering_man
L001D8:RTS

; ------------------------------
; Gère les mouvements de l'homme
; ------------------------------

gere_mouvements:
 CMPI.W #4,state_man
 BEQ L001E3
 TST.W tirencours_flag ; L'homme est-il en train de tirer ?
 BNE L001E4 ; Oui
 
 ; Joystick actionné (pas bouton feu) ?
 MOVE.B joystick_state,D0
 ANDI.W #$F,D0
 BNE L001E4
 
 MOVE.W #0,anim_man_counter
L001E4:MOVEA.L (debut_data+4988),A0
 CMPA.L #0,A0
 BEQ L001E6
 JSR L001E7
 TST.W D0
 BEQ L001E6
 MOVE.W $16(A0),(debut_data+4992)
 BRA L001E9
L001E6:MOVE.L #0,(debut_data+4988)
 MOVE.W #0,(debut_data+4992)
 MOVE.W #1,D3
 
 ; Teste s'il y a du sol à droite sous les pieds de l'homme
 MOVE.W tomb_word0,D0
 MOVE.W tomb_word1,D1
 ADDI.W #6,D0
 ADDI.W #$18,D1
 JSR get_adr_for_test
 CLR.W D0
 MOVE.B (A0),D0
 BTST #5,0(A1,D0.W)
 BNE L001E9 ; Il y a du sol sous les pieds
 
 ; Teste s'il y a du sol à gauche sous les pieds de l'homme
 MOVE.W tomb_word0,D0
 MOVE.W tomb_word1,D1
 ADDI.W #$13,D0
 ADDI.W #$18,D1
 JSR get_adr_for_test
 CLR.W D0
 MOVE.B (A0),D0
 BTST #5,0(A1,D0.W)
 BNE L001E9 ; Il y a du sol sous les pieds

 ; Ici, l'homme commence à tomber
 ; ------------------------------
 
 MOVEA.L (debut_data+4810),A0
L001EF:CMPA.L #-1,A0
 BEQ L001EC
 CMPI.L #do_radeau,6(A0)
 BNE L001EE
 JSR L001E7
 TST.W D0
 BEQ L001EE
 MOVE.L A0,(debut_data+4988)
 MOVE.W $16(A0),(debut_data+4992)
 BRA L001E9
L001EE:MOVEA.L 0(A0),A0
 BRA L001EF
L001EC:

 move.w #0,state_man ; *********
 ;MOVE.W #1,state_man ; Indique que le bonhomme tombe
 ;BRA falling_man
L001E9:

 ; ----------------
 ; Joystick en haut
 ; ----------------
 
 BTST #0,D7
 BEQ L001F0
 MOVE.W tomb_word0,D0
 MOVE.W tomb_word1,D1
 ADDI.W #$C,D0
 ADDI.W #$F,D1
 JSR get_adr_for_test
 CMPI.B #$1F,(A0)
 BEQ L001F1
 MOVE.W #2,state_man
 MOVE.W #7,nb_to_jump
 
 ; Homme qui saute
 MOVE.W #$E,D0
 JSR prepare_address_of_sound
 
 BRA jumping_man
L001F1:MOVE.W #3,state_man
 BRA laddering_man
L001F0:

 ; ********** joystick en bas
 btst #1,d7
 beq not_down
 
 move.w tomb_word1,d6 
 addq.w #4,d6
 move.w tomb_word0,d5
 jsr move_man
 move.w #0,state_man
 rts
   
not_down:

 MOVE.W tomb_word0,D5
 MOVE.W tomb_word1,D6
   
 ; -----------------
 ; Joystick à gauche
 ; ----------------- 

 BTST #2,D7
 BEQ L001F3
 SUBI.W #2,D5
 BTST #0,(man_synchro+1)
 BEQ L001F5
 ADDI.W #1,anim_man_counter
 MOVE.W man_synchro,D0
 ANDI.W #7,D0
 CMP.W #7,D0
 BNE L001F5
 
 ; Bruit de pas
 MOVE.W #$C,D0
 JSR prepare_address_of_sound
 
 BRA L001F5
L001F3:

 ; -----------------
 ; Joystick à droite
 ; -----------------

 BTST #3,D7
 BEQ L001F5
 ADDI.W #2,D5
 BTST #0,(man_synchro+1)
 BEQ L001F5
 ADDI.W #1,anim_man_counter
 MOVE.W man_synchro,D0
 ANDI.W #7,D0
 CMP.W #7,D0
 BNE L001F5
 
 ; Bruit de pas
 MOVE.W #$C,D0
 JSR prepare_address_of_sound

L001F5:ADD.W (debut_data+4992),D5
 MOVE.W #1,D3
 JSR move_man
L001E3:RTS

L001E7:
 MOVE.W tomb_word1,D0
 ADDI.W #$18,D0
 CMP.W $1C(A0),D0
 BNE L001F8
 MOVE.W tomb_word0,D0
 ADDI.W #6,D0
 CMP.W $1A(A0),D0
 BCS L001FA
 SUBI.W #$20,D0
 CMP.W $1A(A0),D0
 BCC L001F8
 MOVE.W #1,D0
 BRA L001FB
L001FA:MOVE.W tomb_word0,D0
 ADDI.W #$13,D0
 CMP.W $1A(A0),D0
 BCS L001F8
 SUBI.W #$20,D0
 CMP.W $1A(A0),D0
 BCC L001F8
 MOVE.W #1,D0
 BRA L001FB
L001F8:CLR.W D0
L001FB:RTS

; -------------------------------------
; Gestion de l'homme en train de sauter
; -------------------------------------

jumping_man:CMPI.W #4,state_man
 BEQ L001FC
 MOVE.L #0,(debut_data+4988)
 MOVE.W #0,(debut_data+4992)
 MOVE.W #1,D3
 MOVE.W nb_to_jump,D0
 SUBI.W #1,D0
 BNE L001FD
 
 move.w #0,state_man ; ********
 ;MOVE.W #1,state_man
 ;BRA falling_man
L001FD:MOVE.W D0,nb_to_jump
 BTST #0,D7
 BNE L001FE
  
 BTST #1,D7
 BEQ L001FF
L001FE:

 ; Joystick en haut
 MOVE.W tomb_word0,D0
 MOVE.W tomb_word1,D1
 ADDI.W #$C,D0
 ADDI.W #$F,D1
 JSR get_adr_for_test
 CMPI.B #$1F,(A0)
 BNE L001FF
 MOVE.W #3,state_man
 BRA laddering_man
 
L001FF:

 MOVE.W tomb_word0,D5
 MOVE.W tomb_word1,D6
 BTST #2,D7
 BEQ L00200
 
 ; Joystick sur la gauche
 SUBI.W #2,D5
 BRA L00201
L00200:
 BTST #3,D7
 BEQ L00202
 
 ; Joystick sur la droite
 ADDI.W #2,D5
L00201:
 SUBI.W #4,D6
 JSR move_man
 
 CMPI.W #4,state_man
 BEQ L00203
 TST.W D0
 BNE L001FC
 MOVE.W tomb_word0,D5
 MOVE.W tomb_word1,D6
L00202:SUBI.W #4,D6
 JSR move_man
 CMPI.W #4,state_man
 BEQ L00203
 TST.W D0
 BNE L001FC
 
 move.w #0,state_man ; ********
 ;MOVE.W #1,state_man
 ;BRA falling_man
L001FC:RTS

; ----------------------------
; Gestion de l'homme qui tombe
; ----------------------------

falling_man:
 CMPI.W #4,state_man
 BEQ L00203

 MOVE.L #0,(debut_data+4988)
 MOVE.W #0,(debut_data+4992)
 MOVE.W #1,D3
 MOVE.W tomb_word1,D0
 ADDI.W #$17,D0
 ADDI.W #4,D0
 ANDI.W #$F,D0
 ADDI.W #1,D0
 CMP.W #4,D0
 BNE L00205
 MOVE.W #5,D3
L00205:BTST #0,D7
 BNE L00206
 BTST #1,D7
 BEQ L00207
L00206:
 MOVE.W tomb_word0,D0
 MOVE.W tomb_word1,D1
 ADDI.W #$C,D0
 ADDI.W #$F,D1
 JSR get_adr_for_test
 CMPI.B #$1F,(A0)
 BNE L00207
 MOVE.W #3,state_man
 BRA laddering_man

L00207:
 MOVE.W tomb_word0,D5
 MOVE.W tomb_word1,D6
 BTST #2,D7
 BEQ L00208
 SUBI.W #2,D5
 BRA L00209
L00208:BTST #3,D7
 BEQ L0020A
 ADDI.W #2,D5
L00209:ADDI.W #4,D6
 JSR move_man
 CMPI.W #4,state_man
 BEQ L00203
 TST.W D0
 BNE L0020B
 MOVE.W tomb_word0,D5
 MOVE.W tomb_word1,D6
L0020A:ADDI.W #4,D6
 JSR move_man
 CMPI.W #4,state_man
 BEQ L00203
 TST.W D0
 BNE L0020B
 MOVE.W #0,state_man
 BRA gere_mouvements
L0020B:MOVEA.L (debut_data+4810),A0
L0020D:CMPA.L #-1,A0
 BEQ L00203
 CMPI.L #do_radeau,6(A0)
 BNE L0020C
 JSR L001E7
 TST.W D0
 BEQ L0020C
 MOVE.L A0,(debut_data+4988)
 MOVE.W $16(A0),(debut_data+4992)
 MOVE.W #0,state_man
 BRA gere_mouvements
L0020C:
 MOVEA.L 0(A0),A0
 BRA L0020D
L00203:RTS

; ----------------------------------
; Gestion de l'homme sur une échelle
; ----------------------------------

laddering_man:
 CMPI.W #4,state_man
 BEQ L0020E
 MOVE.L #0,(debut_data+4988)
 MOVE.W #0,(debut_data+4992)
 MOVE.W #5,D3
 MOVE.W tomb_word0,D0
 MOVE.W tomb_word1,D1
 ADDI.W #$C,D0
 ADDI.W #$F,D1
 JSR get_adr_for_test
 CMPI.B #$1F,(A0)
 BEQ L0020F
 
 move.w #0,state_man ; ****
 ;MOVE.W #1,state_man
 ;BRA falling_man
L0020F:MOVE.W tomb_word0,D5
 MOVE.W tomb_word1,D6
 SWAP D6
 MOVE.W tomb_word1,D6
 BTST #0,D7
 BEQ L00210
 MOVE.W D5,D0
 MOVE.W D6,D1
 ADDI.W #$C,D0
 ADDI.W #$B,D1
 JSR get_adr_for_test
 CMPI.B #$1F,(A0)
 BNE L00211
 SUBI.W #4,D6
 SWAP D6
 SUBI.W #4,D6
 MOVE.W #1,D3
 BRA L00211
L00210:BTST #1,D7
 BEQ L00211
 ADDI.W #4,D6
 SWAP D6
 ADDI.W #4,D6
 MOVE.W #1,D3
 MOVE.W D6,D0
 ADDI.W #$17,D0
 ANDI.W #$F,D0
 ADDI.W #1,D0
 CMP.W #4,D0
 BNE L00211
 MOVE.W #5,D3
L00211:BTST #2,D7
 BEQ L00212
 SUBI.W #2,D5
 BRA L00213
L00212:BTST #3,D7
 BEQ L00213
 ADDI.W #2,D5
L00213:MOVE.B D7,D0
 ANDI.B #$F,D0
 BEQ L00214
 ADDI.W #1,anim_man_counter
 BTST #0,(man_synchro+1)
 BEQ L00214
 
 ; Bruit de pas
 MOVE.W #$D,D0 
 JSR prepare_address_of_sound

L00214:JSR move_man
 CMPI.W #4,state_man
 BEQ L00203
 TST.W D0
 BNE L0020E
 MOVE.W tomb_word0,D5
 SWAP D6
 JSR move_man
 CMPI.W #4,state_man
 BEQ L00203
L0020E:RTS

; --------------------------------------------------------
; Déplace le bonhomme à sa nouvelle position (si possible)
; --------------------------------------------------------

move_man:
 MOVEA.L #(debut_data+4842),A2
 CLR.W D4
 SWAP D7
 CLR.W D7
 MOVE.W D5,D0
 MOVE.W D6,D1
 ADDI.W #6,D0
 ADDI.W #0,D1
 JSR get_adr_for_test
 MOVE.B (A0),D4
 CMP.B #$20,D4
 BNE L00217 
 MOVE.W D5,D0
 ADDI.W #6,D0
 ANDI.W #$1F,D0
 CMP.W #$A,D0
 BCS L00217
 MOVE.B #$18,D4
L00217:MOVE.W D4,(A2)+
 MOVE.L A0,(A2)+
 OR.B 0(A1,D4.W),D7
 MOVE.W D5,D0
 MOVE.W D6,D1
 ADDI.W #$13,D0
 ADDI.W #0,D1
 JSR get_adr_for_test
 MOVE.B (A0),D4
 CMP.B #$20,D4
 BNE L00218
 MOVE.W D5,D0
 ADDI.W #$13,D0
 ANDI.W #$1F,D0
 CMP.W #$A,D0
 BCS L00218
 MOVE.B #$18,D4
L00218:MOVE.W D4,(A2)+
 MOVE.L A0,(A2)+
 OR.B 0(A1,D4.W),D7
 MOVE.W D5,D0
 MOVE.W D6,D1
 ADDI.W #6,D0
 ADDI.W #$F,D1
 JSR get_adr_for_test
 MOVE.B (A0),D4
 CMP.B #$20,D4
 BNE L00219
 MOVE.W D5,D0
 ADDI.W #6,D0
 ANDI.W #$1F,D0
 CMP.W #$A,D0
 BCS L00219
 MOVE.B #$18,D4
L00219:MOVE.W D4,(A2)+
 MOVE.L A0,(A2)+
 OR.B 0(A1,D4.W),D7
 MOVE.W D5,D0
 MOVE.W D6,D1
 ADDI.W #$13,D0
 ADDI.W #$F,D1
 JSR get_adr_for_test
 MOVE.B (A0),D4
 CMP.B #$20,D4
 BNE L0021A
 MOVE.W D5,D0
 ADDI.W #$13,D0
 ANDI.W #$1F,D0
 CMP.W #$A,D0
 BCS L0021A
 MOVE.B #$18,D4
L0021A:MOVE.W D4,(A2)+
 MOVE.L A0,(A2)+
 OR.B 0(A1,D4.W),D7
 MOVE.W D5,D0
 MOVE.W D6,D1
 ADDI.W #6,D0
 ADDI.W #$17,D1
 JSR get_adr_for_test
 MOVE.B (A0),D4
 CMP.B #$20,D4
 BNE L0021B
 MOVE.W D5,D0
 ADDI.W #6,D0
 ANDI.W #$1F,D0
 CMP.W #$A,D0
 BCS L0021B
 MOVE.B #$18,D4
L0021B:MOVE.W D4,(A2)+
 MOVE.L A0,(A2)+
 BTST D3,0(A1,D4.W)
 BEQ L0021C
 ORI.B #-1,D7
L0021C:MOVE.W D5,D0
 MOVE.W D6,D1
 ADDI.W #$13,D0
 ADDI.W #$17,D1
 JSR get_adr_for_test
 MOVE.B (A0),D4
 CMP.B #$20,D4
 BNE L0021D
 MOVE.W D5,D0
 ADDI.W #$13,D0
 ANDI.W #$1F,D0
 CMP.W #$A,D0
 BCS L0021D
 MOVE.B #$18,D4
L0021D:MOVE.W D4,(A2)+
 MOVE.L A0,(A2)+
 BTST D3,0(A1,D4.W)
 BEQ L0021E
 ORI.B #-1,D7
L0021E:MOVEA.L #(debut_data+4842),A2
 MOVE.L 2(A2),D0
 CMP.L 8(A2),D0
 BNE L0021F
 MOVE.W $C(A2),6(A2)
 MOVE.L $E(A2),8(A2)
 MOVE.W $18(A2),$C(A2)
 MOVE.L $1A(A2),$E(A2)
 MOVE.W #2,(debut_data+4878)
 MOVE.L 8(A2),D0
 CMP.L 2(A2),D0
 BNE L00221
 MOVE.W $C(A2),6(A2)
 MOVE.L $E(A2),8(A2)
 MOVE.W #1,(debut_data+4878)
 BRA L00222
L00221:CMP.L $E(A2),D0
 BNE L00222
 MOVE.W #1,(debut_data+4878)
 BRA L00222
L0021F:MOVE.W #5,(debut_data+4878)
 MOVE.L $E(A2),D0
 CMP.L 2(A2),D0
 BNE L00223
 MOVE.W $18(A2),$C(A2)
 MOVE.L $1A(A2),$E(A2)
 MOVE.W $1E(A2),$12(A2)
 MOVE.L $20(A2),$14(A2)
 MOVE.W #3,(debut_data+4878)
 BRA L00222
L00223:CMP.L $1A(A2),D0
 BNE L00222
 MOVE.W #3,(debut_data+4878)
L00222:MOVE.W (debut_data+4878),D0
 MOVEA.L #(debut_data+4842),A2
 
 bclr #1,d7 ; ******** POINT : TEST MUR
 
 BTST #1,D7
 BNE L00226
 
L0022B:

 ; -----
 ; Porte
 ; -----
 
 CMPI.W #$20,(A2)+
 BNE L00227
 CMPI.W #0,nb_cle
 BNE L00228
 ORI.B #-1,D7
 BRA L00229
L00228:

 ; Bruit de porte
 MOVE.W #2,D0
 JSR prepare_address_of_sound
 
 SUBI.W #1,nb_cle
 MOVEA.L (A2)+,A0
 MOVE.B #$21,(A0)
 CMPI.B #$20,$50(A0)
 BNE L0022A
 MOVE.B #$21,$50(A0)
 BRA L00229
L0022A:MOVE.B #$21,-$50(A0)
 BRA L00229
L00227:ADDQ.L #4,A2
 DBRA D0,L0022B
 
L00229:BTST #1,D7
 BEQ L0022C
L00226:TST.W move_man_flag
 BNE L0022E
 SWAP D7
 MOVE.W tomb_word0,D5
 MOVE.W tomb_word1,D6
 MOVE.W #1,move_man_flag
 BRA move_man
L0022C:MOVE.W D5,D0
 SUB.W tomb_word0,D0
 MOVE.W D6,D1
 SUB.W tomb_word1,D1
 CMP.W (debut_data+510),D5
 BCC L0022F
 CMPI.W #2,pos_in_tomb_x
 BCS L00230
 ADD.W pos_in_tomb_x,D0
 MOVE.W D0,(debut_data+518)
 BRA L00230
L0022F:CMP.W (debut_data+512),D5
 BCS L00230
 MOVE.W pos_in_tomb_x,D2
 CMP.W tomb_word2,D2
 BCC L00230
 ADD.W pos_in_tomb_x,D0
 MOVE.W D0,(debut_data+518)
L00230:CMP.W (debut_data+514),D6
 BCC L00232
 CMPI.W #2,pos_in_tomb_y
 BCS L00233
 ADD.W pos_in_tomb_y,D1
 MOVE.W D1,(debut_data+520)
 BRA L00233
L00232:CMP.W (debut_data+516),D6
 BCS L00233
 MOVE.W pos_in_tomb_y,D2
 CMP.W tomb_word3,D2
 BCC L00233
 ADD.W pos_in_tomb_y,D1
 MOVE.W D1,(debut_data+520)
L00233:MOVE.W D5,tomb_word0
 MOVE.W D6,tomb_word1
 MOVE.W (debut_data+518),D0
 MOVE.W D0,D1
 ADDI.W #$80,D0
 ADDI.W #$A0,D1
 MOVE.W D0,(debut_data+522)
 MOVE.W D1,(debut_data+524)
 MOVE.W (debut_data+520),D0
 MOVE.W D0,D1
 ADDI.W #$48,D0
 ADDI.W #$60,D1
 MOVE.W D0,(debut_data+526)
 MOVE.W D1,(debut_data+528)
L0022E:JSR test_touched_objects
 BTST #1,D7
 BNE L00236 
 TST.W move_man_flag
 BNE L00236
 MOVE.W #1,D0
 BRA L00237
L00236:CLR.W D0
L00237:SWAP D7
 CLR.W move_man_flag
 RTS

; ---------------------------------------------------------------
; Rend l'adresse d'un élément (1 octet) à partir d'une coordonnée
; ---------------------------------------------------------------
 
get_adr_for_test:
 MOVEA.L #tomb_table1,A0
 ANDI.W #-$10,D1 
 ADDA.W D1,A0
 ASL.W #2,D1
 ADDA.W D1,A0
 MOVE.W D0,D1
 LSR.W #5,D1
 ADDA.W D1,A0
 RTS

; ---------------------------
; Teste les objets rencontrés
; ---------------------------

test_touched_objects:
 MOVE.W (debut_data+4878),D2
 MOVEA.L #(debut_data+4842),A0
 MOVEA.L #attributes_table,A1
L00255:MOVE.W (A0)+,D0
 MOVEA.L (A0)+,A2
 
 ; Teste si l'arrière plan est mortel (feu, eau...) - POINT : TEST MORT PAR ELEMENTS
 ;BTST #3,0(A1,D0.W) ******
 ;BEQ L00238
 bra l00238
  
 MOVE.W #4,state_man
 MOVE.W #0,move_man_counter
 CLR.W tirencours_flag ; L'homme ne tire plus
 BRA L00239
 
L00238:
 BTST #6,0(A1,D0.W)
 BEQ L0023A
 
 ; ------
 ; Coffre
 ; ------
 
 CMP.B #$2C,D0
 BNE L0023B
 CMPI.W #0,nb_cle
 BEQ L0023A
 SUBI.W #1,nb_cle
 MOVE.L A2,D0
 SUBI.L #tomb_table1,D0
 ASL.W #2,D0
 MOVEA.L #tomb_table3,A3
 ADDA.W D0,A3      
 MOVEA.L (A3),A4
 MOVE.B (A4)+,D0
 MOVE.L A4,(A3)
 MOVEA.L #(debut_data+880),A3
 MOVE.W (debut_data+878),D1
 
L00240:CMP.B (A3)+,D0
 BEQ L0023F
 ADDQ.L #1,A3
 DBRA D1,L00240
L0023F:MOVE.B (A3),(A2)
 BRA L0023A
L0023B:

 ; ------------------
 ; Vie supplémentaire
 ; ------------------

 ; Vie supplémentaire
 CMP.B #$3F,D0
 BNE L00241
 CMPI.W #7,nb_lifes
 BEQ L0023A
 ADDI.W #1,nb_lifes
 MOVE.B #$18,(A2)
 
 ; Bruit de vie supplémentaire
 MOVE.W #6,D0 
 JSR prepare_address_of_sound
 
 BRA L0023A
L00241:

 ; ---
 ; Clé
 ; ---

 ; Clé
 CMP.B #$1B,D0
 BNE L00243
 CMPI.W #4,nb_cle
 BEQ L0023A
 
 ; Bruit de la clé
 MOVE.W #5,D0
 JSR prepare_address_of_sound
 
 MOVE.B #$18,(A2)
 ADDI.W #1,nb_cle
 BRA L0023A
L00243:

 ; ---
 ; Arc
 ; ---

 ; Arc
 CMP.B #$28,D0
 BNE L00244
 CMPI.W #3,nb_arc
 BEQ L0023A
 
 ; Bruit de l'arc (pris)
 MOVE.W #7,D0
 JSR prepare_address_of_sound
 
 MOVE.B #$18,(A2)
 ADDI.W #1,nb_arc
 BRA L0023A
L00244:

 ; ----------
 ; Pièce d'or
 ; ----------

 ; Pièce d'or
 CMP.B #$36,D0
 BNE L00245

 ; Bruit : point de non-retour, pièce d'or
 MOVE.W #4,D0
 JSR prepare_address_of_sound
 
 MOVE.B #$18,(A2)
 MOVE.L A2,D0
 SUBI.L #tomb_table1,D0
 ASL.W #2,D0
 MOVEA.L #tomb_table3,A3
 ADDA.W D0,A3
 MOVEA.L (A3),A3
 CLR.W D0
 MOVE.B (A3),D0
 ANDI.B #3,D0
 MOVE.W D0,points_to_add
 MOVEA.L #(debut_data+5032),A3
 ASL.W #2,D0
 ADDA.W D0,A3
 MOVE.L (A3),D0
 JSR add_points
 MOVE.L A2,D0
 SUBI.L #tomb_table1,D0
 MOVE.W D0,D1
 DIVU #$50,D0
 ASL.W #4,D0
 MOVE.W D0,(debut_data+5094)
 MULU #5,D0
 SUB.W D0,D1
 ASL.W #5,D1
 MOVE.W D1,(debut_data+5092)
 MOVE.W #$10,(debut_data+5096)
 BRA L0023A
L00245:

 ; -------
 ; Sablier
 ; -------

 ; Sablier
 CMP.B #$2E,D0
 BNE L0024A
 
 ; Bruit du sablier
 MOVE.W #6,D0
 JSR prepare_address_of_sound
 
 MOVE.B #$18,(A2)
 MOVE.L A2,D0
 SUBI.L #tomb_table1,D0
 ASL.W #2,D0
 MOVEA.L #tomb_table3,A3
 ADDA.W D0,A3
 MOVEA.L (A3),A3
 CLR.W D0
 MOVE.B (A3),D0
 ADD.W D0,nb_seconds
 BRA L0023A
L0024A:

 ; -------------------
 ; Point de non-retour
 ; -------------------

 ; Point de non-retour
 CMP.B #$2F,D0
 BNE L0024B
 MOVE.L A2,D0
 SUBI.L #tomb_table1,D0
 ASL.W #2,D0
 MOVEA.L #tomb_table3,A3
 ADDA.W D0,A3
 MOVEA.L (A3),A3
 CLR.W D0
 MOVE.B (A3)+,D0
 CMP.B #-1,D0
 BNE L0024C
 MOVE.B #$18,(A2)
                  
 ; Bruit : point de non-retour, pièce d'or
 MOVE.W #4,D0
 JSR prepare_address_of_sound
  
 MOVE.L A2,non_retour_adr
 CLR.L D0
 MOVE.B $D(A3),D0
 ASL.L #4,D0
 ASL.L #8,D0
 JSR add_points
 BRA L0023A
L0024C:
 
 ; -----------------------
 ; Point d'action multiple
 ; -----------------------

 ; Point d'action multiple 
 CMP.B #-2,D0
 BNE L0024E
 MOVE.W fixation_counter,D1
 ADDQ.W #1,D1
 CMP.B (A3),D1
 BNE L0023A 
 MOVE.L A3,D1
 MOVEA.L (debut_data+874),A3
 CMPI.W #-1,(A3)
 BNE L0023A
 MOVEA.L D1,A3
 CLR.W D1
 MOVE.B (A3),D1
 MOVE.W D1,fixation_counter
 ADDI.L #2,(debut_data+874)
 MOVE.W #-$8000,fixation_synchro
 BRA L0023A

L0024E:MOVE.B (A3),D1
 MOVEA.L #(debut_bss+213760),A3
 MOVE.B D1,0(A3,D0.W)
 BRA L0023A
L0024B:

 ; -------
 ; Flèches
 ; -------

 ; Flèches
 CMP.B #$29,D0
 BNE L00252
 CMPI.W #$C,nb_fleches
 BEQ L0023A
 
 ; Bruit des flèches
 MOVE.W #7,D0
 JSR prepare_address_of_sound
 
 MOVE.B #$18,(A2)
 MOVE.L A2,D0
 SUBI.L #tomb_table1,D0
 ASL.W #2,D0
 MOVEA.L #tomb_table3,A3
 ADDA.W D0,A3
 MOVEA.L (A3),A3
 CLR.W D0
 MOVE.B (A3),D0
 ADD.W D0,nb_fleches
 CMPI.W #$D,nb_fleches
 BCS L0023A
 MOVE.W #$C,nb_fleches
 BRA L0023A
 
L00252:

 ; ------
 ; Sortie
 ; ------
 
 CMP.B #$33,D0
 BEQ L00253
 CMP.B #$34,D0
 BNE L0023A

 ; Sortie
L00253:MOVE.W #1,exit_found

L0023A:DBRA D2,L00255
L00239:RTS

; -
; 
; -

L0006D:MOVE.W points_to_add,D0
 CMP.W #-1,D0
 BEQ L00256
 MOVE.W (debut_data+5092),D1
 MOVE.W (debut_data+5094),D2
 ADDI.W #7,D1
 ADDI.W #5,D2
 SUBI.W #1,(debut_data+5094)
 SUBI.W #1,(debut_data+5096)
 BNE L00257
 MOVE.W #-1,points_to_add
L00257:JSR aff_points_gotten
L00256:RTS

; ---------------------------
; Lit les données de la tombe
; ---------------------------

read_tomb_datas:
 CLR.W fixation_counter
 MOVE.L #-1,(debut_data+4810)
 JSR L00259
 MOVE.L #(debut_bss+205312),(debut_data+542)
 MOVEA.L #tomb_table1,A0
 
 MOVE.W #$EFF,D7
 MOVE.W #0,D6
L0025F:CMP.W #$50,D6
 BCC L0025C
 MOVE.B #$19,(A0)
 ADDQ.W #1,D6
 BRA L0025D
L0025C:MOVE.B #$1A,(A0)
 ADDQ.W #1,D6
 CMP.W #$A0,D6
 BNE L0025D
 CLR.W D6
L0025D:MOVE.B #$18,$F00(A0)
 ADDQ.L #1,A0
 DBRA D7,L0025F
 
 MOVEA.L #tomb_table3,A0
 MOVE.W #$EFF,D7
L00260:CLR.L (A0)+
 DBRA D7,L00260
 
 ; Sélection de la tombe (0-4)
 MOVE.W tomb_number,D0
 ASL.W #2,D0
 MOVEA.L #tombs5,A0
 MOVEA.L 0(A0,D0.W),A6
 MOVE.W (A6)+,tomb_word0
 MOVE.W (A6)+,tomb_word1
 MOVE.W (A6)+,tomb_word2
 MOVE.W (A6)+,tomb_word3
 CLR.L D0
 MOVE.W (A6)+,D0
 ASL.L #4,D0
 ASL.L #8,D0
 MOVE.L D0,tomb_word4
 MOVE.L A6,tomb_datas
 
 ; Interprétation des 32 premiers octets
 MOVEA.L #tomb_table1,A0
 MOVEA.L #(debut_data+746),A1
 
 MOVE.W #3,D7
 MOVE.W #-1,D5
L00267:

 MOVE.W #7,D6
L00265:CMPI.B #$31,(A6)+
 BNE L00264
 ADDI.W #1,D5
 MOVE.L A0,(A1)+
L00264:ADDA.W #$A,A0
 DBRA D6,L00265
 
 ADDA.W #$370,A0
 DBRA D7,L00267

 ; 
 MOVEA.L #(debut_data+746),A0
L00271:

 ORI.L #-$10000,D5
 MOVEA.L #(debut_data+954),A5
 MOVEA.L (A0)+,A1
 
 MOVE.W #$B,D7
L0026F:

 MOVE.W #9,D6
L0026D:JSR L0026A
 MOVE.B D0,(A1)
 LEA $A(A6),A6
 LEA $F00(A1),A1
 JSR L0026A
 MOVE.B D0,(A1)+
 LEA -$F00(A1),A1
 LEA -9(A6),A6
 DBRA D6,L0026D
 
 ADDA.W #$46,A1
 ADDA.W #$A,A6
 DBRA D7,L0026F
 
 JSR L00270
 DBRA D5,L00271
 
 ;
 MOVE.W tomb_word0,D0
 SUBI.W #$90,D0
 BCC L00272
 CLR.W D0
L00272:CMP.W tomb_word2,D0
 BCS L00273
 MOVE.W tomb_word2,D0
L00273:MOVE.W D0,pos_in_tomb_x
 MOVE.W tomb_word1,D0
 SUBI.W #$54,D0
 BCC L00274
 CLR.W D0
L00274:CMP.W tomb_word3,D0
 BCS L00275
 MOVE.W tomb_word3,D0
L00275:MOVE.W D0,pos_in_tomb_y
 MOVE.W pos_in_tomb_x,D0
 MOVE.W D0,D1
 ADDI.W #$80,D0
 ADDI.W #$A0,D1
 MOVE.W D0,(debut_data+510)
 MOVE.W D1,(debut_data+512)
 MOVE.W pos_in_tomb_y,D0
 MOVE.W D0,D1
 ADDI.W #$48,D0
 ADDI.W #$60,D1
 MOVE.W D0,(debut_data+514)
 MOVE.W D1,(debut_data+516)
 JSR L00276
 MOVE.W fixation_counter,D7
 BEQ L00277
 
 SUBQ.W #1,D7
L00278:MOVE.W #-2,fixation_synchro
 MOVE.W D7,-(A7)
 JSR do_fixe_elements
 MOVE.W (A7)+,D7
 ADDI.L #2,(debut_data+874)
 DBRA D7,L00278
 
 MOVE.W #-2,fixation_synchro
 JSR do_fixe_elements
 MOVE.W #-$8000,fixation_synchro
L00277:
 
 MOVEA.L #tomb_table1,A0
 MOVEA.L physscreen2,A2 ; Utilise l'écran comme buffer
 MOVE.W #$4F,D7
L0027D:CMPI.B #$19,(A0)
 BEQ L0027A
 CMPI.B #$1A,(A0)
 BNE L0027B
L0027A:MOVE.B (A0),(A2)+
 BRA L0027C
L0027B:MOVE.B #-1,(A2)+
L0027C:ADDQ.L #1,A0
 DBRA D7,L0027D
 
 MOVE.W #$2E,D7
L00289:MOVE.W #$4F,D6
L00288:MOVE.B (A0),D0
 
 CMP.B #$19,D0
 BEQ L0027E
 CMP.B #$1A,D0
 BEQ L0027E
 MOVE.B #-1,(A2)+
 BRA L0027F
L0027E:MOVEA.L #(debut_data+730),A3
 MOVE.W #7,D5
 CLR.W D1
L00283:MOVE.W (A3)+,D0
 MOVE.B 0(A0,D0.W),D0
 
 CMP.B #$19,D0
 BEQ L00281
 CMP.B #$1A,D0
 BNE L00282
L00281:ADDQ.B #1,D1
L00282:ADD.W D1,D1
 DBRA D5,L00283
 LSR.W #1,D1
 MOVEA.L #(debut_data+684),A3
L00287:CMPI.B #-1,(A3)
 BEQ L00285
 MOVE.B D1,D0
 AND.B (A3)+,D0
 CMP.B (A3)+,D0
 BEQ L00286
 ADDQ.L #1,A3
 BRA L00287
L00285:MOVE.B (A0),(A2)+
 BRA L0027F
L00286:MOVE.B (A3),(A2)+
L0027F:ADDQ.L #1,A0
 DBRA D6,L00288
 DBRA D7,L00289
 
 MOVEA.L physscreen2,A0 ; Utilise l'écran comme buffer
 ADDA.W #$50,A0
 MOVE.W #$EAF,D7
L00293:CMPI.B #$19,(A0)
 BEQ L0028B
 CMPI.B #$1A,(A0)
 BNE L0028C
L0028B:CMPI.B #$3D,-$50(A0)
 BEQ L0028E
 CMPI.B #$3B,-$50(A0)
 BNE L00290
L0028E:CMPI.B #-1,-1(A0)
 BNE L0028C
 MOVE.B #$3D,(A0)
 BRA L0028C
L00290:CMPI.B #$3E,-$50(A0)
 BEQ L00292
 CMPI.B #$3C,-$50(A0)
 BNE L0028C
L00292:CMPI.B #-1,1(A0)
 BNE L0028C
 MOVE.B #$3E,(A0)
L0028C:ADDQ.L #1,A0
 DBRA D7,L00293
 
 SUBA.W #$51,A0
 MOVE.W #$E5F,D7
L0029A:CMPI.B #$3A,(A0)
 BEQ L00295
 CMPI.B #$19,(A0)
 BEQ L00295
 CMPI.B #$1A,(A0)
 BNE L00296
L00295:CMPI.B #$3E,$50(A0)
 BNE L00297
 CMPI.B #-1,1(A0)
 BNE L00296
 CMPI.B #-1,-$50(A0)
 BNE L00298
 MOVE.B #$3C,(A0)
 BRA L00296
L00298:MOVE.B #$3E,(A0)
 BRA L00296
L00297:CMPI.B #$3D,$50(A0)
 BNE L00296
 CMPI.B #-1,-1(A0)
 BNE L00296
 CMPI.B #-1,-$50(A0)
 BNE L00299
 MOVE.B #$3B,(A0)
 BRA L00296
L00299:MOVE.B #$3D,(A0)
L00296:SUBQ.L #1,A0
 DBRA D7,L0029A
 
 MOVEA.L physscreen2,A0 ; Utilise l'écran comme buffer
 MOVEA.L #tomb_table1,A1
 MOVE.W #$EFF,D7
L0029C:MOVE.B (A0)+,D0
 
 CMP.B #-1,D0
 BEQ L0029B
 MOVE.B D0,(A1)
L0029B:ADDQ.L #1,A1
 DBRA D7,L0029C
 RTS
 
; -------------------------------
; Pour le debugging (pas utilisé)
; -------------------------------
 
 MOVEA.L #L0029D,A5
 MOVE.W #$A,D5
L0029F:CMP.B (A5)+,D0
 BEQ L0029E
 DBRA D5,L0029F
 RTS 
L0029E:ADDQ.W #1,D1
 RTS 
L0029D:DC.W $19,$1A,$3A,$3B,$3C,$3D,$3E,$00,$01,$02,$03,$00

; -
;
; -
  
L0026A:MOVE.B (A6),D0
 
 CMP.B #$42,D0
 BEQ L002A0
 CMP.B #$4B,D0
 BEQ L002A0
 CMP.B #$41,D0
 BCS L002A0
 CMP.B #$5B,D0
 BCC L002A0
 ADDI.L #$10000,D5
 SUBI.B #$41,D0
 ANDI.W #$FF,D0
 ASL.W #2,D0
 MOVE.L A1,0(A5,D0.W)
 MOVE.B #$18,D0
 BRA L002A3
L002A0:MOVE.W (debut_data+878),D1
 MOVEA.L #(debut_data+880),A4
L002A5:CMP.B (A4),D0
 BEQ L002A4
 ADDA.W #2,A4
 DBRA D1,L002A5
 MOVE.B #$18,D0
 BRA L002A3
L002A4:MOVE.B 1(A4),D0
L002A3:RTS

; -
;
; -

L00270:SWAP D5
 CMP.W #-1,D5
 BEQ L002A6
 MOVEA.L (debut_data+542),A1
L002D3:MOVE.B (A6)+,D1
 SUBI.B #$41,D1
 ANDI.W #$FF,D1
 ASL.W #2,D1
 MOVEA.L 0(A5,D1.W),A2
 MOVE.L A2,(debut_data+4830)
L0038C:MOVE.L A2,D2
 SUBI.L #tomb_table1,D2
 CMP.W #$F00,D2
 BCS L002A8
 SUBI.W #$F00,D2
 SUBI.L #$F00,(debut_data+4830)
L002A8:ASL.W #2,D2
 MOVEA.L #tomb_table3,A3
 ADDA.W D2,A3
 MOVE.B (A6)+,D0
 
 CMP.B #$41,D0
 BNE L002A9
 MOVE.L A6,(A3)
 ADDQ.L #1,A6
 MOVE.B #$29,(A2)
 BRA L002AA
L002A9:
 CMP.B #$44,D0
 BNE L002AB
 MOVE.B (A6)+,(A2)
 BRA L002AA
L002AB:
 CMP.B #$50,D0
 BNE L002AC
 MOVE.L A6,(A3)
 ADDQ.L #1,A6
 MOVE.B #$36,(A2)
 BRA L002AA
L002AC:
 CMP.B #$4B,D0
 BNE L002AD
 MOVE.B #$1B,(A2)
 BRA L002AA
L002AD:
 CMP.B #$42,D0
 BNE L002AE
 MOVE.B #$28,(A2)
 BRA L002AA
L002AE:
 CMP.B #$48,D0
 BNE L002AF
 MOVE.L A6,(A3)
 ADDQ.L #1,A6
 MOVE.B #$2E,(A2)
 BRA L002AA
L002AF:
 CMP.B #$55,D0
 BNE L002B0
 MOVE.L A1,(A3)
 MOVE.B (A6)+,D0
 ANDI.W #$FF,D0
 MOVE.W D0,$12(A1)
 MOVE.L A6,$14(A1)
 MOVE.L (debut_data+4830),$A(A1)
 MOVE.L A3,D1
 SUBI.L #tomb_table3,D1
 LSR.W #2,D1
 MOVE.W D1,D0
 DIVU #$50,D1
 ASL.W #4,D1
 MOVE.W D1,$10(A1)
 MULU #5,D1
 SUB.W D1,D0
 ASL.W #5,D0
 MOVE.W D0,$E(A1)
 MOVE.L #break_vase,6(A1)
 CLR.L 0(A1)
 CLR.W 4(A1)
 ADDA.W #$18,A1
L002B2:CMPI.B #-2,(A6)+
 BNE L002B2
 MOVE.B #$2A,(A2)
 MOVE.B #$2B,-$50(A2)
 BRA L002AA
L002B0:
 CMP.B #$43,D0
 BNE L002B6
 MOVE.L A6,(A3)
L002B7:CMPI.B #-2,(A6)+
 BNE L002B7
 MOVE.B #$2C,(A2)
 BRA L002AA
L002B6:
 CMP.B #$7B,D0
 BNE L002B9
 MOVE.W #0,D1
 MOVE.B #$22,(A2)
 BRA L002BA
L002B9:
 CMP.B #$7D,D0
 BNE L002BC
 MOVE.W #1,D1
 MOVE.B #$23,(A2)
L002BA:MOVE.W D1,$16(A1)
 MOVE.L A1,(A3)
 CLR.L 0(A1)
 CLR.W 4(A1)
 MOVE.L #project_from_wall,6(A1)
 MOVE.L (debut_data+4830),$A(A1)
 MOVE.L A3,D1
 SUBI.L #tomb_table3,D1
 LSR.W #2,D1
 MOVE.W D1,D0
 DIVU #$50,D1
 ASL.W #4,D1
 MOVE.W D1,$10(A1)
 MULU #5,D1
 SUB.W D1,D0
 ASL.W #5,D0
 MOVE.W D0,$E(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$14(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$18(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$1C(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$12(A1)
 MOVEA.L #(debut_bss+213760),A4
 MOVE.B (A6)+,0(A4,D1.W)
 MOVE.B (A6)+,D1
 MOVE.W D1,$1A(A1)
 ADDA.W #$1E,A1
 BRA L002AA
L002BC:
 CMP.B #$29,D0
 BNE L002BF
 MOVE.W #0,D1
 MOVE.B #$14,(A2)
 BRA L002C0
L002BF:
 CMP.B #$28,D0
 BNE L002C1
 MOVE.W #1,D1
 MOVE.B #$15,(A2)
L002C0:MOVE.W D1,$16(A1)
 MOVE.L A1,(A3)
 CLR.L 0(A1)
 CLR.W 4(A1)
 MOVE.L #project_from_dragoon,6(A1)
 MOVE.L (debut_data+4830),$A(A1)
 MOVE.L A3,D1
 SUBI.L #tomb_table3,D1
 LSR.W #2,D1
 MOVE.W D1,D0
 DIVU #$50,D1
 ASL.W #4,D1
 MOVE.W D1,$10(A1)
 MULU #5,D1
 SUB.W D1,D0
 ASL.W #5,D0
 MOVE.W D0,$E(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$14(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$18(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$1C(A1)
 MOVE.B (A6)+,D1
 ASL.W #8,D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$1E(A1)
 MOVE.B (A6)+,D1
 ASL.W #8,D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$20(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 ASL.W #1,D1
 MOVE.W D1,$22(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 ASL.W #1,D1
 MOVE.W D1,$24(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$12(A1)
 MOVEA.L #(debut_bss+213760),A4
 MOVE.B (A6)+,0(A4,D1.W)
 MOVE.B (A6)+,D1
 MOVE.W D1,$1A(A1)
 ADDA.W #$26,A1
 BRA L002AA
L002C1:
 CMP.B #$54,D0
 BNE L002C3
 CMPI.B #-1,(A6)
 BNE L002C4
 CMPA.L non_retour_adr,A2
 BNE L002C5
 ADDQ.L #1,A6
 MOVE.B (A6)+,(A2)
 MOVE.B (A6)+,-$50(A2)
 MOVE.B (A6)+,D0
 ASL.W #8,D0
 MOVE.B (A6)+,D0
 MOVE.W D0,tomb_word0
 MOVE.B (A6)+,D0
 ASL.W #8,D0
 MOVE.B (A6)+,D0
 MOVE.W D0,tomb_word1
 CLR.W D0
 MOVE.B (A6)+,D0
 MOVE.W D0,nb_arc
 MOVE.B (A6)+,D0
 MOVE.W D0,nb_fleches
 MOVE.B (A6)+,D0
 MOVE.W D0,nb_cle
 MOVE.B (A6)+,D0
 MOVE.W D0,nb_seconds
 MOVE.B (A6)+,D0
 ASL.W #8,D0
 MOVE.B (A6)+,D0
 MOVE.W D0,fixation_synchro
 CLR.W fixation_counter
 CLR.W D0
 MOVE.B (A6)+,D0
 MOVE.W D0,fixation_counter
 ADDQ.L #1,A6
 BRA L002AA
L002C5:MOVE.L A6,(A3)
 ADDA.W #$F,A6
 BRA L002C6
L002C4:MOVE.L A6,(A3)
 ADDQ.L #2,A6
L002C6:MOVE.B #$2F,(A2)
 BRA L002AA
L002C3:
 CMP.B #$2D,D0
 BNE L002C7
 MOVE.B #$30,(A2)
 MOVE.L A1,(A3)
 CLR.L 0(A1)
 CLR.W 4(A1)
 MOVE.L #do_radeau,6(A1)
 MOVE.L (debut_data+4830),$A(A1)
 MOVE.L A3,D1
 SUBI.L #tomb_table3,D1
 LSR.W #2,D1
 MOVE.W D1,D0
 DIVU #$50,D1
 ASL.W #4,D1
 MOVE.W D1,$10(A1)
 MOVE.W D1,$1C(A1)
 MULU #5,D1
 SUB.W D1,D0
 ASL.W #5,D0
 MOVE.W D0,$E(A1)
 MOVE.W D0,$1A(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 ASL.W #4,D1
 NEG.W D1
 ADD.W $E(A1),D1
 MOVE.W D1,$12(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 ASL.W #4,D1
 ADD.W $E(A1),D1
 ADDI.W #1,D1
 MOVE.W D1,$14(A1)
 MOVE.B (A6)+,D1
 EXT.W D1
 MOVE.W D1,$16(A1)
 MOVE.B (A6)+,D1
 EXT.W D1
 MOVE.W D1,$18(A1)
 ADDA.W #$1E,A1
 BRA L002AA
L002C7:
 CMP.B #$2B,D0
 BNE L002C8
 CLR.W D1
 MOVE.B (A6)+,D1
 MOVE.B D1,(A2)
 MOVE.W D1,$12(A1)
 MOVE.L A1,(A3)
 CLR.L 0(A1)
 CLR.W 4(A1)
 MOVE.L #L002C9,6(A1)
 MOVE.L (debut_data+4830),$A(A1)
 MOVE.L A3,D1
 SUBI.L #tomb_table3,D1
 LSR.W #2,D1
 MOVE.W D1,D0
 DIVU #$50,D1
 ASL.W #4,D1
 MOVE.W D1,$10(A1)
 MULU #5,D1
 SUB.W D1,D0
 ASL.W #5,D0
 MOVE.W D0,$E(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$14(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$16(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$18(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$1E(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$20(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$1A(A1)
 MOVEA.L #(debut_bss+213760),A4
 MOVE.B (A6)+,0(A4,D1.W)
 MOVE.B (A6)+,$1C(A1)
 ADDA.W #$22,A1
 BRA L002AA
L002C8:
 CMP.B #$4D,D0
 BNE L002CB
 MOVE.B #$30,(A2)
 MOVE.L A1,(A3)
 CLR.L 0(A1)
 CLR.W 4(A1)
 MOVE.L #pharaon_killed,6(A1)
 MOVE.L (debut_data+4830),$A(A1)
 MOVE.L A3,D1
 SUBI.L #tomb_table3,D1
 LSR.W #2,D1
 MOVE.W D1,D0
 DIVU #$50,D1
 ASL.W #4,D1
 MOVE.W D1,$10(A1)
 MOVE.W D1,D2
 SUBI.W #8,D2
 MOVE.W D2,$14(A1)
 MULU #5,D1
 SUB.W D1,D0
 ASL.W #5,D0
 MOVE.W D0,$E(A1)
 MOVE.W D0,$12(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 ASL.W #4,D1
 NEG.W D1
 ADD.W $E(A1),D1
 MOVE.W D1,$16(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 ASL.W #4,D1
 ADD.W $E(A1),D1
 ADDI.W #1,D1
 MOVE.W D1,$18(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$1A(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$1C(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$22(A1)
 CLR.W $20(A1)
 CLR.W $1E(A1)
 ADDA.W #$24,A1
 BRA L002AA
L002CB:
 CMP.B #$51,D0
 BNE L002CD
 MOVE.B #$30,(A2)
 MOVE.L A1,(A3)
 CLR.L 0(A1)
 CLR.W 4(A1)
 MOVE.L #aff_killing_stones,6(A1)
 MOVE.L (debut_data+4830),$A(A1)
 MOVE.L A3,D1
 SUBI.L #tomb_table3,D1
 LSR.W #2,D1
 MOVE.W D1,D0
 DIVU #$50,D1
 ASL.W #4,D1
 MOVE.W D1,$10(A1)
 MOVE.W D1,D2
 MOVE.W D2,$1A(A1)
 MULU #5,D1
 SUB.W D1,D0
 ASL.W #5,D0
 MOVE.W D0,$E(A1)
 MOVE.W D0,$18(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$16(A1)
 MOVE.B (A6)+,$14(A1)
 MOVE.B (A6)+,D1
 EXT.W D1
 MOVE.W D1,$1E(A1)
 MOVE.B (A6)+,D1
 ASL.W #8,D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$22(A1)
 CLR.W $12(A1)
 CLR.W $1C(A1)
 CLR.W $20(A1)
 CLR.W $24(A1)
 ADDA.W #$26,A1
 BRA L002AA
L002CD:
 CMP.B #$58,D0
 BNE L002D0
 MOVE.B #$30,(A2)
 MOVE.L A1,(A3)
 CLR.L 0(A1)
 CLR.W 4(A1)
 MOVE.L #aff_sphere,6(A1)
 MOVE.L (debut_data+4830),$A(A1)
 MOVE.L A3,D1
 SUBI.L #tomb_table3,D1
 LSR.W #2,D1
 MOVE.W D1,D0
 DIVU #$50,D1
 ASL.W #4,D1
 MOVE.W D1,$10(A1)
 MOVE.W D1,D2
 MULU #5,D1
 SUB.W D1,D0
 ASL.W #5,D0
 MOVE.W D0,$E(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 ASL.W #8,D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$14(A1)
 MOVE.B (A6)+,D1
 ASL.W #8,D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$16(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$18(A1)
 CLR.W $12(A1)
 ADDA.W #$1A,A1
 BRA L002AA
L002D0:
 CMP.B #$53,D0
 BNE L002AA
 MOVE.B #$30,(A2)
 MOVE.L A1,(A3)
 CLR.L 0(A1)
 CLR.W 4(A1)
 MOVE.L #serpent_killed,6(A1)
 MOVE.L (debut_data+4830),$A(A1)
 MOVE.L A3,D1
 SUBI.L #tomb_table3,D1
 LSR.W #2,D1
 MOVE.W D1,D0
 DIVU #$50,D1
 ASL.W #4,D1
 MOVE.W D1,$10(A1)
 MOVE.W D1,D2
 SUBI.W #8,D2
 MOVE.W D2,$14(A1)
 MULU #5,D1
 SUB.W D1,D0
 ASL.W #5,D0
 MOVE.W D0,$E(A1)
 MOVE.W D0,$12(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 ASL.W #4,D1
 NEG.W D1
 ADD.W $E(A1),D1
 MOVE.W D1,$16(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 ASL.W #4,D1
 ADD.W $E(A1),D1
 ADDI.W #1,D1
 MOVE.W D1,$18(A1)
 CLR.W D1
 MOVE.B (A6)+,D1
 MOVE.W D1,$1A(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$1C(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$20(A1)
 MOVE.B (A6)+,D1
 MOVE.W D1,$26(A1)
 CLR.W $24(A1)
 CLR.W $1E(A1)
 CLR.W $22(A1)
 ADDA.W #$28,A1
 BRA L002AA
L002AA:DBRA D5,L002D3
 MOVE.L A1,(debut_data+542)
L002A6:SWAP D5
 RTS

; -
; cette routine n'agit qu'une fois sur deux
; -
 
L00042:

 ; N'agit qu'une fois sur deux
 MOVE.W a_main_counter,D0
 ADDI.W #1,D0
 MOVE.W D0,a_main_counter
 BTST #0,D0
 BEQ L002D4
 
 MOVE.W (debut_data+530),D0
 ADDI.W #1,D0
 ANDI.W #3,D0
 MOVE.W D0,(debut_data+530)
 MULU #$500,D0
 MOVEA.L #(debut_bss+32768),A0
 ADDA.W D0,A0
 MOVEA.L #(debut_bss+173568),A1
 MOVE.L A0,D0
 ADDI.L #$10E00,D0
 MOVE.L D0,$D8(A1)
 MOVE.L A0,D0
 ADDI.L #$1A900,D0
 MOVE.L D0,$154(A1)
 MOVE.W #5,D7
L002DB:MOVE.L A0,(A1)+
 ADDA.W #$1400,A0
 DBRA D7,L002DB
 MOVE.L (debut_bss+173568),D0
 ADDI.L #$21200,D0
 MOVE.L D0,(debut_bss+173992)
 MOVE.L (debut_bss+173576),D0
 ADDI.L #$14500,D0
 MOVE.L D0,(debut_bss+173860)
 MOVE.L (debut_bss+173584),D0
 ADDI.L #$13100,D0
 MOVE.L D0,(debut_bss+173876)
 MOVE.L (debut_bss+173588),D0
 ADDI.L #$13100,D0
 MOVE.L D0,(debut_bss+173892)
L002D4:RTS

; -
;
; -

L00276:MOVEA.L #(debut_bss+197120),A0
 MOVE.L A0,(debut_data+874)
L002E8:MOVE.B (A6)+,D0
 CMP.B #-1,D0
 BEQ L002E7
 ASL.W #8,D0
 MOVE.B (A6)+,D0
 ADDI.W #-$8000,D0
 MOVE.W D0,(A0)+
 CMP.W #-1,D0
 BEQ L002E8
 MOVE.B (A6)+,D0
L002EA:ANDI.W #$FF,D0
 MOVE.W D0,D1
 ANDI.W #3,D1
 LSR.W #4,D0
 SUBI.B #8,D0
 MULU #$A,D0
 MULU #$3C0,D1
 ADD.W D1,D0
 MOVE.B (A6)+,D1
L002E9:ANDI.W #$FF,D1
 MOVE.W D1,(A0)+
 MOVE.B (A6)+,D1
 ANDI.W #$FF,D1
 MOVE.W D1,D2
 ANDI.W #$F,D2
 LSR.W #4,D1
 MULU #$50,D2
 ADD.W D1,D2
 ADD.W D0,D2
 MOVE.W D2,(A0)+
 MOVE.B (A6)+,D1
 CMP.B #-1,D1
 BEQ L002E8
 BTST #7,D1
 BEQ L002E9
 MOVE.B D1,D0
 BRA L002EA
L002E7:MOVE.W #-1,(A0)
 RTS

; ---------------------------------------------------------------
; S'occupe de l'apparition ou disparition d'éléments du "paysage"
; ---------------------------------------------------------------
 
do_fixe_elements:
 MOVEA.L (debut_data+874),A0
L002EE:CMPI.W #-1,(A0)
 BEQ L002EB
 MOVE.W fixation_synchro,D0
 SUB.W (A0)+,D0
 BCS L002EB
 CMP.W #$F,D0
 BCS L002EC
L002EF:MOVE.W (A0),D0
 BTST #$F,D0
 BEQ L002ED
 
 ; Elément qui se fixe
 MOVE.W #$F,D0
 JSR prepare_address_of_sound
 
 MOVE.L A0,(debut_data+874)
 BRA L002EE
L002ED:ADDA.W #2,A0
 BRA L002EF
L002EC:MOVE.W (A0)+,D7
 MOVEA.L #tomb_table1,A1
 MOVEA.L #tomb_table2,A2
 MOVE.W (A0)+,D0
 ADDA.W D0,A1
 ADDA.W D0,A2
L002F0:MOVE.B (A1),D0
 MOVE.B (A2),(A1)+
 MOVE.B D0,(A2)+
 DBRA D7,L002F0
 MOVE.W (A0),D0
 BTST #$F,D0
 BEQ L002EC
 BRA L002EE
L002EB:RTS

; -----------------------------------------------------------------
; Suite de la routine d'affichage de bloc 32xXX (partie principale)
; -----------------------------------------------------------------

aff_bloc32x24_main:
 SUB.W pos_in_tomb_x,D1
 SUB.W pos_in_tomb_y,D2

 MOVE.W D1,D3
 ADDI.W #$1F,D3
 BMI L002F1
 CMP.W #$15F,D3
 BCC L002F1
 MOVE.W D2,D4
 ADD.W D0,D4
 BMI L002F1
 MOVE.W #$C0,D5
 ADD.W D0,D5
 CMP.W D5,D4
 BCC L002F1
 
 MOVEA.L physscreen2,A1
 TST.W D1
 BPL L002F3
 CMP.W #-$10,D1
 BCS L002F4
 MOVE.W #3,D7
 BRA L002F5
L002F4:MOVE.W #1,D7
 LEA -8(A1),A1
 BRA L002F5
L002F3:MOVE.W D1,D3
 ANDI.W #-$10,D3
 LSR.W #1,D3
 ADDI.W #8,D3
 ADDA.W D3,A1
 MOVE.W #7,D7
 CMP.W #$120,D1
 BCS L002F5
 CMP.W #$130,D1
 BCC L002F9
 MOVE.W #6,D7
 BRA L002F5
L002F9:MOVE.W #4,D7
L002F5:TST.W D2
 BPL L002FA
 MOVE.W D2,D5
 ADD.W D0,D5
 MOVE.W D2,D3
 NEG.W D3
 ASL.W #4,D3
 ADDA.W D3,A0
 BRA L002FB
L002FA:MOVE.W D2,D3
 ASL.W #5,D3
 ADDA.W D3,A1
 ASL.W #2,D3
 ADDA.W D3,A1
 MOVE.W D0,D5
 ADD.W D0,D2
 CMP.W #$C0,D2
 BCS L002FB
 SUBI.W #$BF,D2
 SUB.W D2,D5
L002FB:SWAP D7
 ADDI.W #$20,D1
 ANDI.W #$F,D1
 MOVE.W #-1,D6
 LSR.W D1,D6
 NOT.W D6
 SWAP D6
 MOVE.W D1,D6
 MOVE.W #$10,D7
 SUB.W D6,D7
L00304:SWAP D5
 MOVEM.L (A0)+,D0-D3
 ROL.L D7,D0
 ROL.L D7,D1
 ROR.L D6,D2
 ROR.L D6,D3
 MOVE.L D0,D4
 OR.L D1,D4
 OR.L D2,D4
 OR.L D3,D4
 NOT.W D4
 SWAP D6
 SWAP D7
 BTST #1,D7
 BNE L002FD
 ADDQ.L #8,A1
 BRA L002FE
L002FD:MOVE.W (A1)+,D5
 AND.W D4,D5
 OR.W D5,D0
 MOVE.W (A1)+,D5
 AND.W D4,D5
 OR.W D5,D1
 MOVE.W (A1)+,D5
 AND.W D4,D5
 OR.W D5,D2
 AND.W (A1)+,D4
 OR.W D4,D3
 MOVEM.W D0-D3,-8(A1)
L002FE:SWAP D4
 MOVE.W D4,D5
 AND.W D6,D5
 EOR.W D5,D4
 SWAP D4
 MOVE.W D5,D4
 NOT.L D4
 BTST #0,D7
 BNE L002FF
 SWAP D0
 SWAP D1
 SWAP D2
 SWAP D3
 MOVE.W D6,D5
 NOT.W D5
 AND.W D5,D0
 AND.W D5,D1
 AND.W D5,D2
 AND.W D5,D3
 LEA -$10(A1),A1
 BRA L00300
L002FF:SWAP D0
 MOVE.W D0,D3
 AND.W D6,D3
 EOR.W D3,D0
 MOVE.W (A1),D5
 AND.W D4,D5
 OR.W D3,D5
 MOVE.W D5,(A1)+
 SWAP D1
 MOVE.W D1,D3
 AND.W D6,D3
 EOR.W D3,D1
 MOVE.W (A1),D5
 AND.W D4,D5
 OR.W D3,D5
 MOVE.W D5,(A1)+
 SWAP D2
 MOVE.W D2,D3
 AND.W D6,D3
 EOR.W D3,D2
 MOVE.W (A1),D5
 AND.W D4,D5
 OR.W D3,D5
 MOVE.W D5,(A1)+
 SWAP D3
 MOVE.W D3,D5
 AND.W D6,D5
 EOR.W D5,D3
 AND.W (A1),D4
 OR.W D4,D5
 MOVE.W D5,(A1)+
 LEA -$18(A1),A1
L00300:BTST #2,D7
 BNE L00302
 ADDQ.L #8,A1
 BRA L00303
L00302:SWAP D4
 MOVE.W (A1)+,D5
 AND.W D4,D5
 OR.W D5,D0
 MOVE.W (A1)+,D5
 AND.W D4,D5
 OR.W D5,D1
 MOVE.W (A1)+,D5
 AND.W D4,D5
 OR.W D5,D2
 AND.W (A1)+,D4
 OR.W D4,D3
 MOVEM.W D0-D3,-8(A1)
L00303:LEA $A0(A1),A1
 SWAP D7
 SWAP D6
 SWAP D5
 DBRA D5,L00304
 MOVE.W #1,D0
 RTS 
L002F1:CLR.W D0
 RTS
 
L00322:SUB.W pos_in_tomb_x,D1
 SUB.W pos_in_tomb_y,D2
 MOVE.W D1,D3
 ADDI.W #$1F,D3
 BMI L00305
 CMP.W #$15F,D3
 BCC L00305
 MOVE.W D2,D4
 ADD.W D0,D4
 BMI L00305
 MOVE.W #$C0,D5
 ADD.W D0,D5
 CMP.W D5,D4
 BCC L00305
 
 MOVEA.L physscreen2,A1
 TST.W D1
 BPL L00306
 CMP.W #-$10,D1
 BCS L00307
 MOVE.W #3,D7
 BRA L00308
L00307:MOVE.W #1,D7
 LEA -8(A1),A1
 BRA L00308
L00306:MOVE.W D1,D3
 ANDI.W #-$10,D3
 LSR.W #1,D3
 ADDI.W #8,D3
 ADDA.W D3,A1
 MOVE.W #7,D7
 CMP.W #$120,D1
 BCS L00308
 CMP.W #$130,D1
 BCC L00309
 MOVE.W #6,D7
 BRA L00308
L00309:MOVE.W #4,D7
L00308:TST.W D2
 BPL L0030A
 MOVE.W D2,D5
 ADD.W D0,D5
 MOVE.W D2,D3
 NEG.W D3
 ASL.W #4,D3
 ADDA.W D3,A0
 BRA L0030B
L0030A:MOVE.W D2,D3
 ASL.W #5,D3
 ADDA.W D3,A1
 ASL.W #2,D3
 ADDA.W D3,A1
 MOVE.W D0,D5
 ADD.W D0,D2
 CMP.W #$C0,D2
 BCS L0030B
 SUBI.W #$BF,D2
 SUB.W D2,D5
L0030B:SWAP D7
 ADDI.W #$20,D1
 ANDI.W #$F,D1
 MOVE.W #-1,D6
 LSR.W D1,D6
 NOT.W D6
 SWAP D6
 MOVE.W D1,D6
 MOVE.W #$10,D7
 SUB.W D6,D7
L00311:SWAP D5
 MOVEM.L (A0)+,D0-D3
 ROL.L D7,D0
 ROL.L D7,D1
 ROR.L D6,D2
 ROR.L D6,D3
 MOVE.L D0,D4
 OR.L D1,D4
 OR.L D2,D4
 OR.L D3,D4
 NOT.W D4
 SWAP D6
 SWAP D7
 BTST #1,D7
 BNE L0030C
 ADDQ.L #8,A1
 BRA L0030D
L0030C:MOVEM.W D0-D3,(A1)
 ADDQ.L #8,A1
L0030D:SWAP D4
 MOVE.W D4,D5
 AND.W D6,D5
 EOR.W D5,D4
 SWAP D4
 MOVE.W D5,D4
 NOT.L D4
 SWAP D0
 SWAP D1
 SWAP D2
 SWAP D3
 MOVE.W D6,D5
 NOT.W D5
 AND.W D5,D0
 AND.W D5,D1
 AND.W D5,D2
 AND.W D5,D3
 LEA -$10(A1),A1
 BRA L0030E
 SWAP D0
 MOVE.W D0,D3
 AND.W D6,D3
 EOR.W D3,D0
 MOVE.W D3,(A1)+
 SWAP D1
 MOVE.W D1,D3
 AND.W D6,D3
 EOR.W D3,D1
 MOVE.W D3,(A1)+
 SWAP D2
 MOVE.W D2,D3
 AND.W D6,D3
 EOR.W D3,D2
 MOVE.W D3,(A1)+
 SWAP D3
 MOVE.W D3,D5
 AND.W D6,D5
 EOR.W D5,D3
 MOVE.W D4,(A1)+
 LEA -$18(A1),A1
L0030E:BTST #2,D7
 BNE L0030F
 ADDQ.L #8,A1
 BRA L00310
L0030F:MOVEM.W D0-D3,(A1)
 ADDQ.L #8,A1
L00310:LEA $A0(A1),A1
 SWAP D7
 SWAP D6
 SWAP D5
 DBRA D5,L00311
 MOVE.W #1,D0
 RTS 
L00305:CLR.W D0
 RTS
 
aff_white_bloc:
 SUB.W pos_in_tomb_x,D1
 SUB.W pos_in_tomb_y,D2
 MOVE.W D1,D3
 ADDI.W #$1F,D3
 BMI L00312
 CMP.W #$15F,D3
 BCC L00312
 MOVE.W D2,D4
 ADD.W D0,D4
 BMI L00312
 MOVE.W #$C0,D5
 ADD.W D0,D5
 CMP.W D5,D4
 BCC L00312
 
 MOVEA.L physscreen2,A1
 TST.W D1
 BPL L00313
 CMP.W #-$10,D1
 BCS L00314
 MOVE.W #3,D7
 BRA L00315
L00314:MOVE.W #1,D7
 LEA -8(A1),A1
 BRA L00315
L00313:MOVE.W D1,D3
 ANDI.W #-$10,D3
 LSR.W #1,D3
 ADDI.W #8,D3
 ADDA.W D3,A1
 MOVE.W #7,D7
 CMP.W #$120,D1
 BCS L00315
 CMP.W #$130,D1
 BCC L00316
 MOVE.W #6,D7
 BRA L00315
L00316:MOVE.W #4,D7
L00315:TST.W D2
 BPL L00317
 MOVE.W D2,D5
 ADD.W D0,D5
 MOVE.W D2,D3
 NEG.W D3
 ASL.W #4,D3
 ADDA.W D3,A0
 BRA L00318
L00317:MOVE.W D2,D3
 ASL.W #5,D3
 ADDA.W D3,A1
 ASL.W #2,D3
 ADDA.W D3,A1
 MOVE.W D0,D5
 ADD.W D0,D2
 CMP.W #$C0,D2
 BCS L00318
 SUBI.W #$BF,D2
 SUB.W D2,D5
L00318:SWAP D7
 ADDI.W #$20,D1
 ANDI.W #$F,D1
 MOVE.W #-1,D6
 LSR.W D1,D6
 NOT.W D6
 SWAP D6
 MOVE.W D1,D6
 MOVE.W #$10,D7
 SUB.W D6,D7
L0031F:SWAP D5
 MOVEM.L (A0)+,D0-D3
 ROL.L D7,D0
 ROL.L D7,D1
 ROR.L D6,D2
 ROR.L D6,D3
 MOVE.L D0,D4
 OR.L D1,D4
 OR.L D2,D4
 OR.L D3,D4
 MOVE.L D4,D0
 MOVE.L D4,D1
 MOVE.L D4,D2
 MOVE.L D4,D3
 NOT.W D4
 SWAP D6
 SWAP D7
 BTST #1,D7
 BNE L00319
 ADDQ.L #8,A1
 BRA L0031A
L00319:MOVE.W (A1)+,D5
 AND.W D4,D5
 OR.W D5,D0
 MOVE.W (A1)+,D5
 AND.W D4,D5
 OR.W D5,D1
 MOVE.W (A1)+,D5
 AND.W D4,D5
 OR.W D5,D2
 AND.W (A1)+,D4
 OR.W D4,D3
 MOVEM.W D0-D3,-8(A1)
L0031A:SWAP D4
 MOVE.W D4,D5
 AND.W D6,D5
 EOR.W D5,D4
 SWAP D4
 MOVE.W D5,D4
 NOT.L D4
 BTST #0,D7
 BNE L0031B
 SWAP D0
 SWAP D1
 SWAP D2
 SWAP D3
 MOVE.W D6,D5
 NOT.W D5
 AND.W D5,D0
 AND.W D5,D1
 AND.W D5,D2
 AND.W D5,D3
 LEA -$10(A1),A1
 BRA L0031C
L0031B:SWAP D0
 MOVE.W D0,D3
 AND.W D6,D3
 EOR.W D3,D0
 MOVE.W (A1),D5
 AND.W D4,D5
 OR.W D3,D5
 MOVE.W D5,(A1)+
 SWAP D1
 MOVE.W D1,D3
 AND.W D6,D3
 EOR.W D3,D1
 MOVE.W (A1),D5
 AND.W D4,D5
 OR.W D3,D5
 MOVE.W D5,(A1)+
 SWAP D2
 MOVE.W D2,D3
 AND.W D6,D3
 EOR.W D3,D2
 MOVE.W (A1),D5
 AND.W D4,D5
 OR.W D3,D5
 MOVE.W D5,(A1)+
 SWAP D3
 MOVE.W D3,D5
 AND.W D6,D5
 EOR.W D5,D3
 AND.W (A1),D4
 OR.W D4,D5
 MOVE.W D5,(A1)+
 LEA -$18(A1),A1
L0031C:BTST #2,D7
 BNE L0031D
 ADDQ.L #8,A1
 BRA L0031E
L0031D:SWAP D4
 MOVE.W (A1)+,D5
 AND.W D4,D5
 OR.W D5,D0
 MOVE.W (A1)+,D5
 AND.W D4,D5
 OR.W D5,D1
 MOVE.W (A1)+,D5
 AND.W D4,D5
 OR.W D5,D2
 AND.W (A1)+,D4
 OR.W D4,D3
 MOVEM.W D0-D3,-8(A1)
L0031E:LEA $A0(A1),A1
 SWAP D7
 SWAP D6
 SWAP D5
 DBRA D5,L0031F
 MOVE.W #1,D0
 RTS
L00312:CLR.W D0
 RTS

; ---------------------
; Affiche un bloc 32xXX
; ---------------------
;
; in : d0.w = numéro de bloc (0-84, dont certains non 32x24)
;      d1.w = coordonnée X
;      d2.w = coordonnée Y
 
aff_bloc32x24:
 MOVEA.L blocsothers,A0
 ASL.W #7,D0
 ADDA.W D0,A0
 ADD.W D0,D0
 ADDA.W D0,A0
 MOVE.W #$17,D0
 BRA aff_bloc32x24_main
 
 ; Pas utilisé (debugging)
 MOVEA.L blocsothers,A0
 ASL.W #7,D0
 ADDA.W D0,A0
 ADD.W D0,D0
 ADDA.W D0,A0
 MOVE.W #$17,D0
 BRA L00322
 
; -
;
; -
 
L00352:
 MOVEA.L blocsothers,A0
 ASL.W #7,D0
 ADDA.W D0,A0
 ADD.W D0,D0
 ADDA.W D0,A0
 MOVE.W #$17,D0
 BRA aff_white_bloc
 
; -------------------------------------
; Affiche les grands chiffres (sablier)
; -------------------------------------
 
aff_09_sablier:
 MOVEA.L blocsothers,A0
 ADDA.W #$300,A0
 ASL.W #6,D0
 ADDA.W D0,A0
 ADD.W D0,D0
 ADDA.W D0,A0
 MOVE.W #$B,D0
 BRA aff_bloc32x24_main

; -----------------------------------------------------
; Affiche des pierres "tuantes" ou une sphère "passage"
; -----------------------------------------------------
 
aff_wall_sphere:MOVEA.L blocsothers,A0
 ADDA.L #$6480,A0
 ASL.W #8,D0
 ADDA.W D0,A0
 MOVE.W #$F,D0
 BRA aff_bloc32x24_main
 
; -----------------------
; Affiche les projectiles
; -----------------------
 
aff_proj:
 MOVEA.L blocsothers,A0
 ASL.W #6,D0
 ADDA.W D0,A0
 MOVE.W #3,D0
 BRA aff_bloc32x24_main

; --------------------------
; Affiche les points obtenus
; --------------------------
  
aff_points_gotten:
 MOVEA.L blocsothers,A0
 ADDA.W #$180,A0
 ASL.W #5,D0
 ADDA.W D0,A0
 ADD.W D0,D0
 ADDA.W D0,A0
 MOVE.W #5,D0
 BRA aff_bloc32x24_main
 
; ------------------------------
; Affiche un radeau ou une bille
; ------------------------------

aff_radeau_bille:MOVEA.L blocsothers,A0
 ADDA.W #$2880,A0
 ASL.W #7,D0
 ADDA.W D0,A0
 MOVE.W #7,D0
 BRA aff_bloc32x24_main

; ----------------------------------------
; Initialisation de la fonte de caractères
; ----------------------------------------

font_init:
 MOVE.W nb_doublecar,D7 ; Vaut normalement 100/2-1 = 49
 MOVEA.L fonts8x8,A0
L0032F:
 MOVEA.L #(debut_bss+214038),A1
 MOVEA.L #font_mask_table,A2
 
 MOVE.W #32-1,D6
L0032C:
 MOVE.W (A0)+,D0
 CMP.W #4,D7
 BCS L0032B
 AND.W (A2)+,D0
L0032B:MOVE.W D0,(A1)+
 DBRA D6,L0032C
 
 LEA -$40(A0),A0
 LEA -$40(A1),A1
 MOVE.W #7,D6
L0032E:MOVE.B (A1)+,(A0)
 MOVE.B (A1)+,$20(A0)
 MOVE.B (A1)+,1(A0)
 MOVE.B (A1)+,$21(A0)
 MOVE.B (A1)+,2(A0)
 MOVE.B (A1)+,$22(A0)
 MOVE.B (A1)+,3(A0)
 MOVE.B (A1)+,$23(A0)
 ADDQ.L #4,A0
 DBRA D6,L0032E
 LEA $20(A0),A0
 DBRA D7,L0032F
 RTS

; ---------------------------------------------------------
; Prépare les blocs graphiques qui suivent les 113 premiers
; ---------------------------------------------------------
 
blocsothers_init:MOVEA.L blocsothers,A0
 MOVE.W nb_blocsothers85,D7 ; Valeur constante 2039
L00331:
 MOVE.W (A0)+,D0
 MOVE.W (A0)+,D1
 MOVE.W (A0)+,D2
 MOVE.W (A0)+,D3
 SWAP D0
 SWAP D1
 SWAP D2
 SWAP D3
 MOVE.W (A0)+,D0
 MOVE.W (A0)+,D1
 MOVE.W (A0)+,D2
 MOVE.W (A0)+,D3
 SWAP D0
 SWAP D1
 MOVEM.L D0-D3,-$10(A0)
 DBRA D7,L00331
 RTS

; ---------------------------------
; Initialise la table des attributs
; ---------------------------------
 
attributes_table_init:MOVEA.L #attr_tbl_data,A0
 MOVEA.L #attributes_table,A1
 MOVE.W (debut_data+1150),D7
 CLR.W D0
L00334:MOVE.B (A0)+,D0
 MOVE.B (A0)+,0(A1,D0.W)
 DBRA D7,L00334
 RTS
 
L00055:MOVEA.L (debut_data+4806),A0
 MOVEA.L #attributes_table,A1
 MOVE.W #$C,D7
 CLR.W D0
L00339:MOVE.W #$A,D6
L00338:

 MOVE.B (A0)+,D0
 BTST #2,0(A1,D0.W)
 BEQ L00335
 
 MOVE.L A0,D1
 SUBI.L #(debut_bss+174081),D1
 ASL.W #2,D1
 ADDI.L #tomb_table3,D1
 MOVEA.L D1,A2
 MOVEA.L (A2),A2
 CMPA.L #0,A2
 BEQ L00335
 MOVE.L 0(A2),D1
 BNE L00337
 MOVE.L (debut_data+4810),0(A2)
 MOVE.L A2,(debut_data+4810)
L00337:MOVE.W #1,4(A2)
L00335:DBRA D6,L00338
 LEA $45(A0),A0
 DBRA D7,L00339
 RTS
 
L00056:MOVEA.L #-1,A5
 MOVEA.L (debut_data+4810),A6
L0033B:CMPA.L #-1,A6
 BEQ L0033A
 MOVEA.L 6(A6),A4
 JSR (A4)
 BRA L0033B
L0033A:RTS

; -
; Très souvent appelée
; -

L0033F:MOVEA.L A6,A4
 MOVEA.L 0(A6),A6
 CLR.L 0(A4)
 CMPA.L #-1,A5
 BNE L0033C
 MOVE.L A6,(debut_data+4810)
 BRA L0033D
L0033C:MOVE.L A6,0(A5)
L0033D:RTS

; ---------------------------------------------
; S'occupe des projectiles qui sortent des murs
; ---------------------------------------------

project_from_wall:MOVE.W 4(A6),D0
 BNE L0033E
 JSR L0033F
 RTS 
L0033E:
 MOVE.W $12(A6),D0
 CMP.B #-1,D0
 BEQ L00340
 MOVEA.L #(debut_bss+213760),A0
 MOVE.B 0(A0,D0.W),D0
 BEQ L00341
L00340:MOVE.W $18(A6),D0
 BEQ L00342
 SUBI.B #1,D0
 MOVE.W D0,$18(A6)
 BRA L00341
L00342:
 MOVE.W $14(A6),$18(A6)
 TST.B $1D(A6)
 BEQ L00341
 BMI L00344
 SUBI.W #1,$1C(A6)
L00344:

 ; Affiche le projectile (hors d'un mur) et s'occupe de sa trajectoire
 MOVE.W $16(A6),D0
 MOVE.W #0,D1
 MOVE.W $E(A6),D2
 MOVE.W $10(A6),D3
 JSR do_project
 
 ; Bruit de projectile
 MOVE.W #8,D0
 JSR prepare_address_of_sound

L00341:MOVE.W $1A(A6),D1
 CMP.B #-1,D1
 BEQ L00345
 MOVE.W $12(A6),D0
 MOVEA.L #(debut_bss+213760),A0
 MOVE.B D1,0(A0,D0.W)
L00345:MOVE.W #0,4(A6)
 MOVEA.L A6,A5
 MOVEA.L 0(A6),A6
 RTS

; ------------------------------------------------
; S'occupe des projectiles qui sortent des dragons?
; ------------------------------------------------
 
project_from_dragoon:MOVE.W 4(A6),D0
 BNE L00346
 JSR L0033F
 RTS 
L00346:
 MOVE.W $12(A6),D0
 CMP.B #-1,D0
 BEQ L00347
 MOVEA.L #(debut_bss+213760),A0
 MOVE.B 0(A0,D0.W),D0
 BEQ L00348
L00347:MOVE.W $18(A6),D0
 BEQ L00349
 SUBI.B #1,D0
 MOVE.W D0,$18(A6)
 BRA L00348
L00349:MOVE.W $14(A6),$18(A6)
 TST.B $1D(A6)
 BEQ L00348
 BMI L0034A
 SUBI.W #1,$1C(A6)
L0034A:

 ; Affiche le projectile (dragon) et s'occupe de sa trajectoire
 MOVE.W $16(A6),D0
 MOVE.W #3,D1
 MOVE.W $E(A6),D2
 MOVE.W $10(A6),D3
 JSR do_project
 
 ; Bruit de projectile
 MOVE.W #8,D0
 JSR prepare_address_of_sound

L00348:MOVE.W $1A(A6),D1
 CMP.B #-1,D1
 BEQ L0034B
 MOVE.W $12(A6),D0
 MOVEA.L #(debut_bss+213760),A0
 MOVE.B D1,0(A0,D0.W)
L0034B:MOVE.W #0,4(A6)
 MOVEA.L A6,A5
 MOVEA.L 0(A6),A6
 RTS

; ---------------------------------------------
; S'occupe du radeau (déplacement et affichage)
; ---------------------------------------------
 
do_radeau:
 MOVE.W #0,D0 ; 0, pour radeau
 MOVE.W $1A(A6),D1
 MOVE.W $1C(A6),D2
 
 ; Affiche le radeau
 MOVEM.L A5-A6,-(A7) 
 JSR aff_radeau_bille
 MOVEM.L (A7)+,A5-A6
 TST.W D0
 
 BNE L0034D
 TST.W 4(A6)
 BNE L0034D
 MOVE.W $E(A6),$1A(A6)
 MOVE.W $10(A6),$1C(A6)
 JSR L0033F
 RTS
 
L0034D:MOVE.W $1A(A6),D0
 ADD.W $16(A6),D0
 BMI L0034E
 CMP.W $12(A6),D0
 BCS L0034E
 CMP.W $14(A6),D0
 BCS L0034F
L0034E:MOVE.W $18(A6),D0
 MOVE.W $16(A6),$18(A6)
 MOVE.W D0,$16(A6)
 ADD.W $1A(A6),D0
L0034F:MOVE.W D0,$1A(A6)
 MOVE.W #0,4(A6)
 MOVEA.L A6,A5
 MOVEA.L 0(A6),A6
 RTS
 
; ------------------------------------
; Un pharaon a été atteint et est mort
; ------------------------------------ 
 
pharaon_killed:MOVE.W $1A(A6),D0
 ASL.W #2,D0
 ADD.W $1E(A6),D0
 ADDI.W #$1C,D0
 BTST #0,(man_synchro+1)
 BEQ L00350
 ADDI.W #1,$1E(A6)
 ANDI.W #3,$1E(A6)
L00350:MOVE.W $12(A6),D1
 MOVE.W $14(A6),D2
 TST.W $20(A6)
 BEQ do_pharaon
 MOVEM.L A5-A6,-(A7)
 JSR L00352
 MOVEM.L (A7)+,A5-A6
 MOVE.W $20(A6),D1
 CLR.W $20(A6)
 SUB.W D1,$22(A6)
 BCS L00353
 
 ; Bruit de l'ennemi touché
 MOVE.W #9,D0
 JSR prepare_address_of_sound
 
 BRA L00354
L00353:
 
 ; Bruit de l'ennemi mort
 MOVE.W #$B,D0
 JSR prepare_address_of_sound
 
 MOVEA.L $A(A6),A0
 MOVE.B #$18,(A0)
 MOVE.W $12(A6),pharaon_killed_x
 MOVE.W $14(A6),pharaon_killed_y
 MOVE.W #1,pharaon_killed_counter
 JSR L0033F
 MOVE.L #$1000,D0
 JSR add_points
 RTS

; --------------------------------
; Gère le déplacement des pharaons
; --------------------------------
 
do_pharaon:MOVEM.L A5-A6,-(A7)
 JSR aff_bloc32x24
 MOVEM.L (A7)+,A5-A6
L00354:TST.W D0
 BNE L00356
 TST.W 4(A6)
 BNE L00356
 MOVE.W $E(A6),$12(A6)
 MOVE.W $10(A6),$14(A6)
 SUBI.W #8,$14(A6)
 JSR L0033F
 RTS
 
L00356:CMPI.W #4,state_man
 BEQ L00357
 MOVE.W tomb_word0,D0
 MOVE.W $12(A6),D1
 ADDI.W #8,D0
 ADDI.W #$18,D1
 CMP.W D0,D1
 BCS L00357
 ADDI.W #$B,D0
 ADDI.W #-$10,D1
 CMP.W D1,D0
 BCS L00357
 MOVE.W tomb_word1,D0
 MOVE.W $14(A6),D1
 ADDI.W #0,D0
 ADDI.W #$17,D1
 CMP.W D0,D1
 BCS L00357
 ADDI.W #$17,D0
 ADDI.W #-$17,D1
 CMP.W D1,D0
 BCS L00357
   
 bra l00357 ; POINT : TEST MORT PAR PHARAON *******
 MOVE.W #4,state_man
 CLR.W move_man_counter
 CLR.W tirencours_flag ; L'homme ne tire plus
L00357:CMPI.W #0,$1C(A6)
 BEQ L00359
 MOVE.W $12(A6),D0
 SUB.W tomb_word0,D0
 MOVE.W #$F,D1
 LSR.W D1,D0
 MOVE.W D0,$1A(A6)
 BRA L0035A
L00359:MOVE.W $1A(A6),D0
L0035A:ASL.W #2,D0
 ADDI.W #-2,D0
 MOVE.W $12(A6),D1
 ADD.W D0,D1
 BMI L0035B
 CMP.W $16(A6),D1
 BCS L0035B
 CMP.W $18(A6),D1
 BCS L0035C
L0035B:CMPI.W #0,$1C(A6)
 BEQ L0035D
 MOVE.W $12(A6),D1
 BRA L0035C
L0035D:NEG.W D0
 EORI.W #1,$1A(A6)
 MOVE.W $12(A6),D1
 ADD.W D0,D1
L0035C:MOVE.W D1,$12(A6)
 MOVE.W #0,4(A6)
 MOVEA.L A6,A5
 MOVEA.L 0(A6),A6
 RTS
 
; ---------------------------------------------------------
; Affiche une sphère "passage" et s'occupe de son animation
; ---------------------------------------------------------
 
aff_sphere:MOVE.W $E(A6),D1
 ADDI.W #8,D1
 MOVE.W $10(A6),D2
 MOVE.W $12(A6),D0
 ADDQ.W #1,D0
 ANDI.W #7,D0
 MOVE.W D0,$12(A6)
 LSR.W #1,D0
 ADDI.W #3,D0
 MOVEM.L A5-A6,-(A7)
 JSR aff_wall_sphere
 MOVEM.L (A7)+,A5-A6
 TST.W D0
 BNE L0035F
 JSR L0033F
 RTS
 
; -
;
; -
 
L0035F:
 CMPI.W #4,state_man
 BEQ L00360
 MOVE.W tomb_word0,D0
 MOVE.W $E(A6),D1
 ADDI.W #8,D0
 ADDI.W #$17,D1
 CMP.W D0,D1
 BCS L00360
 ADDI.W #$B,D0
 ADDI.W #-$F,D1
 CMP.W D1,D0
 BCS L00360
 MOVE.W tomb_word1,D0
 MOVE.W $10(A6),D1
 ADDI.W #0,D0
 ADDI.W #$F,D1
 CMP.W D0,D1
 BCS L00360
 ADDI.W #$17,D0
 ADDI.W #-$F,D1
 CMP.W D1,D0
 BCS L00360
 MOVE.W $14(A6),tomb_word0
 MOVE.W $16(A6),tomb_word1
 MOVE.W tomb_word0,D0
 SUBI.W #$90,D0
 BCC L00361
 CLR.W D0
L00361:CMP.W tomb_word2,D0
 BCS L00362
 MOVE.W tomb_word2,D0
L00362:MOVE.W D0,pos_in_tomb_x
 MOVE.W tomb_word1,D0
 SUBI.W #$54,D0
 BCC L00363
 CLR.W D0
L00363:CMP.W tomb_word3,D0
 BCS L00364
 MOVE.W tomb_word3,D0
L00364:MOVE.W D0,pos_in_tomb_y
 MOVE.W pos_in_tomb_x,D0
 MOVE.W D0,D1
 ADDI.W #$80,D0
 ADDI.W #$A0,D1
 MOVE.W D0,(debut_data+510)
 MOVE.W D1,(debut_data+512)
 MOVE.W pos_in_tomb_y,D0
 MOVE.W D0,D1
 ADDI.W #$48,D0
 ADDI.W #$60,D1
 MOVE.W D0,(debut_data+514)
 MOVE.W D1,(debut_data+516)
 MOVE.W pos_in_tomb_x,(debut_data+518)
 MOVE.W pos_in_tomb_y,(debut_data+520)
 MOVE.W (debut_data+510),(debut_data+522)
 MOVE.W (debut_data+512),(debut_data+524)
 MOVE.W (debut_data+514),(debut_data+526)
 MOVE.W (debut_data+516),(debut_data+528)
 TST.W $18(A6)
 BNE L00365
 MOVEA.L $A(A6),A0
 MOVE.B #$18,(A0)
 JSR L0033F
L00365:
 ; Bruit ?
 MOVE.W #$10,D0
 JSR prepare_address_of_sound
 JSR play_yamaha
 
 MOVE.W #2,D0
 JSR palette_decrescendo
 MOVE.W #$7FFF,man_synchro
 RTS
 
L00360:MOVE.W #0,4(A6)
 MOVEA.L A6,A5
 MOVEA.L 0(A6),A6
 RTS

; -------------------------------
; Affichage les "pierres tuantes"
; -------------------------------                                              
 
aff_killing_stones:
 TST.W $12(A6)
 BNE L00367
 MOVEA.L $A(A6),A0
 MOVE.B $14(A6),(A0)
 ADDQ.W #1,$12(A6)
L00367:MOVE.W $16(A6),D0
 MOVE.W $18(A6),D1
 MOVE.W $1A(A6),D2
 MOVEM.L A5-A6,-(A7)
 JSR aff_wall_sphere
 MOVEM.L (A7)+,A5-A6
 CMPI.W #4,state_man
 BEQ L00368
 MOVE.W tomb_word0,D0
 MOVE.W $18(A6),D1
 ADDI.W #8,D0
 ADDI.W #$1F,D1
 CMP.W D0,D1
 BCS L00368
 ADDI.W #$B,D0
 ADDI.W #-$1F,D1
 CMP.W D1,D0
 BCS L00368
 MOVE.W tomb_word1,D0
 MOVE.W $1A(A6),D1
 ADDI.W #0,D0
 ADDI.W #$F,D1
 CMP.W D0,D1
 BCS L00368
 ADDI.W #$17,D0
 ADDI.W #-$F,D1
 CMP.W D1,D0
 BCS L00368
 
 bra l00368 ; *************
 
 MOVE.W #4,state_man ; POINT : TEST MORT PAR PIERRES "TUANTES"
 CLR.W move_man_counter
 CLR.W tirencours_flag ; L'homme ne tire plus
L00368:MOVE.W $18(A6),D0
 ANDI.W #$1F,D0
 BNE L0036A
 MOVE.W $1A(A6),D0
 ANDI.W #$F,D0
 BNE L0036A
 MOVE.W $1A(A6),D0
 MULU #5,D0
 MOVE.W $18(A6),D1
 LSR.W #5,D1
 ADD.W D1,D0
 MOVEA.L #tomb_table1,A0
 ADDA.W D0,A0
 MOVE.W $16(A6),D0
 MULU #$34,D0
 MOVEA.L #(debut_data+258),A1 ; Table (contenu ignoré)
 ADDA.W D0,A1 
 LEA $1C(A1),A2
 MOVE.B (A0),D0
L0036F:CMPI.B #-1,(A2)
 BEQ L0036C
 CMP.B (A2)+,D0
 BNE L0036D
 MOVE.B (A2)+,(A0)
 CMPI.B #1,(A2)
 BEQ L0036C
 BRA L0036E
L0036D:ADDQ.L #2,A2
 BRA.S L0036F
L0036C:LEA $10(A1),A2
 MOVE.B $50(A0),D0
L00372:CMPI.B #-1,(A2)
 BEQ L00370
 CMP.B (A2)+,D0
 BEQ L00371
 BRA L00372
L00370:MOVE.W #2,$20(A6)
 TST.W $1C(A6)
 BEQ L0036A
 MOVE.W $1C(A6),$1E(A6)
 CLR.W $1C(A6)
 BRA L0036A
L00371:MOVEA.L A1,A2
 MOVE.B 1(A0),D0
 TST.W $1E(A6)
 BPL L00373
 MOVE.B -1(A0),D0
L00373:CMPI.B #-1,(A2)
 BEQ L00374
 CMP.B (A2)+,D0
 BNE L00373
 MOVEA.L A1,A2
 NEG.W $1E(A6)
 MOVE.B 1(A0),D0
 TST.W $1E(A6)
 BPL L00375
 MOVE.B -1(A0),D0
L00375:CMPI.B #-1,(A2)
 BEQ L00374
 CMP.B (A2)+,D0
 BNE L00375
 NEG.W $1E(A6)
 CLR.W $1C(A6)
 CLR.W $20(A6)
 BRA L0036A
L00374:CLR.W $20(A6)
 MOVE.W $1E(A6),$1C(A6)
L0036A:MOVE.W $1C(A6),D0
 ADD.W D0,$18(A6)
 MOVE.W $20(A6),D1
 ADD.W D1,$1A(A6)
 OR.W D0,D1
 OR.W D1,$24(A6)
 BEQ L00376
 SUBQ.W #1,$22(A6)
 BNE L00376
L0036E:MOVEA.L $A(A6),A0
 MOVE.B #$18,(A0)
 JSR L0033F
 RTS 
L00376:MOVEA.L A6,A5
 MOVEA.L 0(A6),A6
 RTS

; ------------------------------------
; Un serpent a été atteint et est mort
; ------------------------------------
 
serpent_killed:
 MOVE.W $1A(A6),D0
 ASL.W #2,D0
 ADD.W $1E(A6),D0
 ADDI.W #$24,D0
 BTST #0,(man_synchro+1)
 BEQ L00377
 ADDI.W #1,$1E(A6)
 ANDI.W #1,$1E(A6)
L00377:MOVE.W $12(A6),D1
 MOVE.W $14(A6),D2
 TST.W $24(A6)
 BEQ do_serpent
 MOVEM.L A5-A6,-(A7)
 JSR L00352
 MOVEM.L (A7)+,A5-A6
 MOVE.W $24(A6),D1
 CLR.W $24(A6)
 SUB.W D1,$26(A6)
 BCS L00379
 
 ; Bruit de l'ennemi touché
 MOVE.W #9,D0
 JSR prepare_address_of_sound
 
 BRA L0037A
L00379:

 ; Bruit de l'ennemi mort
 MOVE.W #$B,D0
 JSR prepare_address_of_sound
 
 MOVEA.L $A(A6),A0
 MOVE.B #$18,(A0)
 MOVE.W $12(A6),serpent_killed_x
 MOVE.W $14(A6),serpent_killed_y
 MOVE.W #1,serpent_killed_counter
 JSR L0033F
 MOVE.L #$1500,D0
 JSR add_points
 RTS
 
; --------------------------------
; Gère le déplacement des serpents
; --------------------------------
 
do_serpent:
 MOVEM.L A5-A6,-(A7)
 JSR aff_bloc32x24
 MOVEM.L (A7)+,A5-A6
L0037A:TST.W D0
 BNE serpent_project
 TST.W 4(A6)
 BNE serpent_project
 MOVE.W $E(A6),$12(A6)
 MOVE.W $10(A6),$14(A6)
 SUBI.W #8,$14(A6)
 JSR L0033F
 RTS

; -----------------------------------------------
; S'occupe des projectiles tirés par les serpents
; -----------------------------------------------
 
serpent_project:
 CMPI.W #4,state_man
 BEQ L0037D
 MOVE.W tomb_word0,D0
 MOVE.W $12(A6),D1
 ADDI.W #8,D0
 ADDI.W #$18,D1
 CMP.W D0,D1
 BCS L0037D
 ADDI.W #$B,D0
 ADDI.W #-$10,D1
 CMP.W D1,D0
 BCS L0037D
 MOVE.W tomb_word1,D0
 MOVE.W $14(A6),D1
 ADDI.W #0,D0
 ADDI.W #$17,D1
 CMP.W D0,D1
 BCS L0037D
 ADDI.W #$17,D0
 ADDI.W #-$17,D1
 CMP.W D1,D0
 BCS L0037D

 bra l0037d ; **************
 
 MOVE.W #4,state_man ; POINT : TEST MORT PAR SERPENTS
 CLR.W move_man_counter
 CLR.W tirencours_flag ; L'homme ne tire plus
L0037D:CMPI.W #0,$1C(A6)
 BEQ L0037E
 MOVE.W $12(A6),D0
 SUB.W tomb_word0,D0
 MOVE.W #$F,D1
 LSR.W D1,D0
 MOVE.W D0,$1A(A6)
 BRA L0037F
L0037E:MOVE.W $1A(A6),D0
L0037F:ASL.W #2,D0
 ADDI.W #-2,D0
 MOVE.W $12(A6),D1
 ADD.W D0,D1
 BMI L00380
 CMP.W $16(A6),D1
 BCS L00380
 CMP.W $18(A6),D1
 BCS L00381
L00380:CMPI.W #0,$1C(A6)
 BEQ L00382
 MOVE.W $12(A6),D1
 BRA L00381
L00382:NEG.W D0
 EORI.W #1,$1A(A6)
 MOVE.W $12(A6),D1
 ADD.W D0,D1
L00381:MOVE.W D1,$12(A6)
 CMPI.W #0,$22(A6)
 BEQ L00383
 SUBI.W #1,$22(A6)
 BRA L00384
L00383:MOVE.W $20(A6),$22(A6)
 MOVE.W $14(A6),D0
 ADDI.W #$18,D0
 CMP.W tomb_word1,D0
 BCS L00384
 MOVE.W tomb_word1,D0
 ADDI.W #$18,D0
 CMP.W $14(A6),D0
 BCS L00384
 
 ; Affiche le projectile de serpent et s'occupe de sa trajectoire
 MOVE.W $1A(A6),D0
 MOVE.W #2,D1
 MOVE.W $12(A6),D2
 MOVE.W $14(A6),D3
 JSR do_project
 
 ; Bruit de projectile
 MOVE.W #8,D0 
 JSR prepare_address_of_sound

L00384:MOVE.W #0,4(A6)
 MOVEA.L A6,A5
 MOVEA.L 0(A6),A6
 RTS

; -
;
; -
 
L002C9:
 MOVE.W 4(A6),D0
 BNE SUPPL1
 JSR L0033F
 RTS 
SUPPL1:
 MOVE.W $1A(A6),D0
 CMP.B #-1,D0
 BEQ L00385
 MOVEA.L #(debut_bss+213760),A0
 MOVE.B 0(A0,D0.W),D0
 BEQ L00386
L00385:MOVE.W $1E(A6),D0
 BEQ L00387
 CMP.B #-1,D0
 BEQ L00386
 SUBI.B #1,D0
 MOVE.W D0,$1E(A6)
 BRA L00386
L00387:MOVE.W $18(A6),D0
 MOVE.W $16(A6),$18(A6)
 MOVE.W D0,$16(A6)
 MOVE.W D0,$1E(A6)
 MOVE.W $14(A6),D0
 MOVE.W $12(A6),$14(A6)
 MOVE.W D0,$12(A6)
 MOVEA.L $A(A6),A0
 MOVE.W $20(A6),D1
 TST.B D1
 BEQ L00386
 BMI L00388
 SUBQ.W #1,D1
 MOVE.W D1,$20(A6)
L00388:MOVE.B D0,(A0)
L00386:MOVE.B $1C(A6),D0
 CMP.B #-1,D0
 BEQ L00389
 MOVE.W $1A(A6),D1
 MOVEA.L #(debut_bss+213760),A0
 MOVE.B D0,0(A0,D1.W)
L00389:MOVE.W #0,4(A6)
 MOVEA.L A6,A5
 MOVEA.L 0(A6),A6
 RTS

; ---------------------------------------
; S'occupe d'un vase atteint d'une flèche
; ---------------------------------------
 
break_vase:
 MOVE.W $12(A6),D0
 SUB.W nb_arc,D0
 BCS L0038A
 MOVE.W D0,$12(A6)
 MOVEA.L $A(A6),A0
 MOVE.B #$2A,(A0)
 MOVE.B #$2B,-$50(A0)
 
 ; Bruit du titre qui rebondit
 MOVE.W #1,D0
 JSR prepare_address_of_sound
 
 BRA L0038B
L0038A:

 ; Bruit de vase qui éclate
 MOVE.W #3,D0
 JSR prepare_address_of_sound
 
 MOVE.W $E(A6),vase_broken_x
 MOVE.W $10(A6),D0
 SUBI.W #8,D0
 MOVE.W D0,vase_broken_y
 MOVE.W #1,vase_broken_counter
 MOVEA.L $A(A6),A0
 MOVE.B #$18,(A0)
 MOVE.B #$18,-$50(A0)
 MOVEA.L A0,A2
 MOVEA.L (debut_data+542),A1
 CLR.W D5
 MOVE.L $14(A6),D0
 MOVEM.L A5-A6,-(A7)
 MOVEA.L D0,A6
 JSR L0038C
 MOVEM.L (A7)+,A5-A6
L0038B:JSR L0033F
 RTS
 
; ---------------------------------------------------------
; "Crée" un projectile, une flèche, une boule de serpent...
; ---------------------------------------------------------
;
; in : d0.w = direction (0, sur la gauche et 1, sur la droite )
;      d1.w = 0, hors d'un mur
;             1, flèche
;             2, boule de serpent
;             3, hors d'un dragon (grosse boule qui tombe à la verticale)
;      d2.w, d3.w = coordonnées
;
; Rem : cette fonction s'occupe de "créer" les projectiles; c'est la fonction
;       'gere_project' qui gère leur déplacement
 
do_project:
 MOVEA.L (debut_data+548),A0
 CMPA.L #-1,A0
 BEQ L0038E
 MOVE.L 0(A0),(debut_data+548)
 MOVE.L (debut_data+552),0(A0)
 MOVE.L A0,(debut_data+552)
 MOVE.W D1,$14(A0)
 
 MOVEA.L #list_projects,A1
 ASL.W #5,D1
 ADDA.W D1,A1
 ASL.W #4,D0
 ADDA.W D0,A1
 ADD.W (A1)+,D2
 MOVE.W D2,4(A0)
 ADD.W (A1)+,D3
 MOVE.W D3,6(A0)
 CMPI.W #3,$14(A0)
 BNE L00391
 MOVE.W $1E(A6),8(A0)
 CLR.W $A(A0)
 CLR.W $E(A0)
 MOVE.W $22(A6),$10(A0)
 MOVE.W $24(A6),$C(A0)
 MOVE.W $20(A6),$12(A0)
 BRA L0038E
L00391:MOVE.W (A1)+,8(A0)
 MOVE.W (A1)+,$A(A0)
 MOVE.W (A1)+,$C(A0)
 MOVE.W (A1)+,$E(A0)
 MOVE.W (A1)+,$10(A0)
 MOVE.W (A1)+,$12(A0)
L0038E:RTS

; --------------------------------
; Gestion générale des projectiles
; --------------------------------

gere_project:
 MOVEA.L #-1,A5
 MOVEA.L (debut_data+552),A6
L00399:CMPA.L #-1,A6
 BEQ L00392
 CMPI.W #3,$14(A6)
 BEQ do_bille
 SUBI.W #1,$12(A6)
 BEQ L00394
 MOVE.W $C(A6),D0
 MOVE.W 4(A6),D1
 MOVE.W 6(A6),D2
 MOVEM.L A5-A6,-(A7)
 JSR aff_proj
 MOVEM.L (A7)+,A5-A6
 TST.W D0
 BNE L00396
L00394:CMPI.W #1,$14(A6)
 BNE L00397
 SUBI.W #1,nb_flechestirees
L00397:MOVEA.L A6,A4
 MOVEA.L 0(A6),A6
 MOVE.L (debut_data+548),0(A4)
 MOVE.L A4,(debut_data+548)
 CMPA.L #-1,A5
 BEQ L00398
 MOVE.L A6,0(A5)
 BRA L00399
L00398:MOVE.L A6,(debut_data+552)
 BRA L00399
L00396:MOVEA.L #tomb_table1,A0
 MOVE.W 4(A6),D0
 ADD.W $E(A6),D0
 LSR.W #5,D0
 ADDA.W D0,A0
 MOVE.W 6(A6),D0
 ADD.W $10(A6),D0
 ANDI.W #-$10,D0
 ADDA.W D0,A0
 ASL.W #2,D0
 ADDA.W D0,A0
 CLR.W D0
 MOVE.B (A0),D0
 CMPI.W #1,$14(A6)
 BNE L0039A
 CMP.B #$2B,D0
 BNE L0039B
 MOVE.B #$32,(A0)
 MOVE.B #$31,$50(A0)
 BRA L00394
L0039B:
 CMP.B #$2A,D0
 BNE L0039C
 MOVE.B #$31,(A0)
 MOVE.B #$32,-$50(A0)
 BRA L00394
L0039C:MOVEA.L (debut_data+4810),A0
 MOVEA.L #-1,A1
 MOVE.W 4(A6),D1
 ADD.W $E(A6),D1
 MOVE.W 6(A6),D2
 ADD.W $10(A6),D2
L003A1:CMPA.L #-1,A0
 BEQ L0039D
 CMPI.L #serpent_killed,6(A0)
 BNE L0039E
 MOVE.W $12(A0),D3
 ADDI.W #8,D3
 CMP.W D3,D1
 BCS L0039F
 ADDI.W #$10,D3
 CMP.W D1,D3
 BCS L0039F
 MOVE.W $14(A0),D3
 ADDI.W #0,D3
 CMP.W D3,D2
 BCS L0039F
 ADDI.W #$17,D3
 CMP.W D2,D3
 BCS L0039F
 MOVE.W nb_arc,$24(A0)
L003A0:BRA L00394
L0039E:CMPI.L #pharaon_killed,6(A0)
 BNE L0039F
 MOVE.W $12(A0),D3
 ADDI.W #8,D3
 CMP.W D3,D1
 BCS L0039F
 ADDI.W #$10,D3
 CMP.W D1,D3
 BCS L0039F
 MOVE.W $14(A0),D3
 ADDI.W #0,D3
 CMP.W D3,D2
 BCS L0039F
 ADDI.W #$17,D3
 CMP.W D2,D3
 BCS L0039F
 MOVE.W nb_arc,$20(A0)
 BRA L003A0
L0039F:MOVEA.L A0,A1
 MOVEA.L 0(A0),A0
 BRA L003A1
L0039A:CMPI.W #4,state_man
 BEQ L0039D
 MOVE.W tomb_word0,D1
 ADDI.W #8,D1
 MOVE.W 4(A6),D2
 ADD.W $E(A6),D2
 CMP.W D1,D2
 BCS L0039D
 ADDI.W #$B,D1
 CMP.W D2,D1
 BCS L0039D
 MOVE.W tomb_word1,D1
 ADDI.W #0,D1
 MOVE.W 6(A6),D2
 ADD.W $10(A6),D2
 CMP.W D1,D2
 BCS L0039D
 ADDI.W #$17,D1
 CMP.W D2,D1
 BCS L0039D
 
 bra l0039d ; POINT : TEST MORT PROJECTILES ************
 
 MOVE.W #4,state_man
 MOVE.W #0,move_man_counter
 CLR.W tirencours_flag ; L'homme ne tire plus
 BRA L00394
L0039D:MOVEA.L #attributes_table,A0
 BTST #0,0(A0,D0.W)
 BNE L00394
 MOVE.W 8(A6),D0
 ADD.W D0,4(A6)
 MOVE.W $A(A6),D0
 ADD.W D0,6(A6)
 MOVEA.L A6,A5
 MOVEA.L 0(A6),A6
 BRA L00399
L00392:RTS

; -
; (pas utilisé souvent ?)
; -

L003B5: 
 MOVEM.L D1/D7,-(A7)
 ANDI.L #$FFFF,D0
 ROR.L #8,D0
 MOVE.W #$40,D1
 MOVE.W #7,D7
L003A4:SUB.W D1,D0
 BCC L003A2
 ADD.W D1,D0
 ADD.W D1,D1
 MOVE.B #$40,D1
 BRA L003A3
L003A2:ADD.W D1,D1
 ADDI.W #$C0,D1
L003A3:ROL.L #2,D0
 DBRA D7,L003A4
 LSR.W #8,D1
 CLR.L D0
 MOVE.W D1,D0
 MOVEM.L (A7)+,D1/D7
 RTS

; ----------------------------------------------
; S'occupe des billes (déplacement et affichage)
; ----------------------------------------------
 
do_bille:
 SUBI.W #1,$12(A6)
 BEQ L003A5
 MOVE.W #1,D0 ; 1, pour bille
 MOVE.W 4(A6),D1
 MOVE.W 6(A6),D2
 
 ; Affiche la bille
 MOVEM.L A5-A6,-(A7)
 JSR aff_radeau_bille
 MOVEM.L (A7)+,A5-A6
 
 TST.W D0
 BNE L003A6
L003A5:MOVEA.L A6,A4
 MOVEA.L 0(A6),A6
 MOVE.L (debut_data+548),0(A4)
 MOVE.L A4,(debut_data+548)
 CMPA.L #-1,A5
 BEQ L003A7
 MOVE.L A6,0(A5)
 BRA L00399
L003A7:MOVE.L A6,(debut_data+552)
 BRA L00399
 
L003A6:CMPI.W #4,state_man
 BEQ L003A8
 MOVE.W tomb_word0,D0
 MOVE.W 4(A6),D1
 ADDI.W #8,D0
 ADDI.W #7,D1
 CMP.W D0,D1
 BCS L003A8
 ADDI.W #$B,D0
 ADDI.W #-7,D1
 CMP.W D1,D0
 BCS L003A8
 MOVE.W tomb_word1,D0
 MOVE.W 6(A6),D1
 ADDI.W #0,D0
 ADDI.W #7,D1
 CMP.W D0,D1
 BCS L003A8
 ADDI.W #$17,D0
 ADDI.W #-7,D1
 CMP.W D1,D0
 BCS L003A8
  
 bra l003a8 ; ************** 
  
 MOVE.W #4,state_man ; POINT : MORT PAR BOULES
 MOVE.W #0,move_man_counter
 CLR.W tirencours_flag ; L'homme ne tire plus
 BRA L00394
L003A8:MOVE.L A1,-(A7)
 MOVEA.L #attributes_table,A1
 CLR.L D0
 MOVE.W 4(A6),D0
 ASL.L #8,D0
 MOVE.B $E(A6),D0
 CLR.L D1
 MOVE.W 6(A6),D1
 ASL.L #8,D1
 MOVE.B $F(A6),D1
 MOVE.W 8(A6),D2
 MOVE.W $A(A6),D3
 EXT.L D2
 EXT.L D3
 ADD.L D2,D0
 TST.L D2
 BMI L003AA
 MOVE.L D0,D4
 LSR.L #8,D4
 ADDI.W #7,D4
 LSR.W #5,D4
 MOVE.L D1,D5
 LSR.L #8,D5
 ADDI.W #3,D5
 LSR.W #4,D5
 MULU #$50,D5
 MOVEA.L #tomb_table1,A0
 ADDA.W D5,A0
 ADDA.W D4,A0
 CLR.W D4
 MOVE.B (A0),D4
 BTST #0,0(A1,D4.W)
 BEQ L003AB
 MOVE.L D0,D4
 SUB.L D2,D0
 ANDI.W #-$2000,D0
 ADDI.W #$1800,D0
 SUB.L D0,D4
 MULS $10(A6),D4
 ASR.L #8,D4
 SUB.L D4,D0
 NEG.L D2
 MULS $10(A6),D2
 ASR.L #8,D2
 MOVE.L D0,-(A7)
 
 ; Bruit du titre qui rebondit
 MOVE.W #1,D0
 JSR prepare_address_of_sound
 
 MOVE.L (A7)+,D0
 BRA L003AB
L003AA:MOVE.L D0,D4
 LSR.L #8,D4
 LSR.W #5,D4
 MOVE.L D1,D5
 LSR.L #8,D5
 ADDI.W #3,D5
 LSR.W #4,D5
 MULU #$50,D5
 MOVEA.L #tomb_table1,A0
 ADDA.W D5,A0
 ADDA.W D4,A0
 CLR.W D4
 MOVE.B (A0),D4
 BTST #0,0(A1,D4.W)
 BEQ L003AB
 MOVE.L D0,D4
 SUB.L D2,D0
 ANDI.W #-$2000,D0
 SUB.L D0,D4
 MULS $10(A6),D4
 ASR.L #8,D4
 SUB.L D4,D0
 NEG.L D2
 MULS $10(A6),D2
 ASR.L #8,D2
 MOVE.L D0,-(A7)
 
 ; Bruit du titre qui rebondit
 MOVE.W #1,D0
 JSR prepare_address_of_sound
 
 MOVE.L (A7)+,D0
L003AB:ADD.L D3,D1
 ADDI.L #$100,D1
 MOVE.W D3,D4
 ADDI.L #$200,D3
 TST.W D4
 BMI L003AE
 CMP.W #$F00,D3
 BCS L003AF
 MOVE.W #$F00,D3
L003AF:MOVE.L D0,D4
 LSR.L #8,D4
 ADDI.W #3,D4
 LSR.W #5,D4
 MOVE.L D1,D5
 LSR.L #8,D5
 ADDI.W #7,D5
 LSR.W #4,D5
 MULU #$50,D5
 MOVEA.L #tomb_table1,A0
 ADDA.W D5,A0
 ADDA.W D4,A0
 CLR.W D4
 MOVE.B (A0),D4
 BTST #5,0(A1,D4.W)
 BEQ L003B0
 SUBI.L #$200,D3
 SUBI.L #$100,D1
 SUB.L D3,D1
 MOVE.W D1,D4
 ADDI.W #$700,D4
 ANDI.W #$FFF,D4
 NEG.W D4
 ADDI.W #$FFF,D4
 MULS #$400,D4
 ASR.L #8,D4
 MOVE.W D3,D5
 MULS D3,D5
 ASR.L #8,D5
 ADD.W D5,D4
 BCC L003B4
 MOVE.W #-1,D4
L003B4:MOVE.L D0,-(A7)
 MOVE.W D4,D0
 JSR L003B5
 MOVE.W D0,D4
 MOVE.L (A7)+,D0
 ASL.W #4,D4
 CMP.W #$F00,D4
 BCS L003B6
 MOVE.W #$F00,D4
L003B6:MOVE.W D4,D5
 SUB.W D3,D5
 ANDI.L #$FFFF,D5
 DIVS #2,D5
 NEG.W D5
 ADDI.W #$100,D5
 NEG.W D4
 MULS $C(A6),D4
 ASR.L #8,D4
 MOVE.W D5,D6
 MULS #$200,D6
 ASR.L #8,D6
 ADD.W D4,D6
 MOVE.W D6,D3
 TST.W D3
 BMI L003B7
 CLR.W D3
L003B7:MOVE.W D5,D6
 MULS #$100,D6
 ASR.L #8,D6
 ADD.W D4,D6
 MULS D5,D6
 ASR.L #8,D6
 ADDI.L #$700,D1
 ORI.W #$FFF,D1
 TST.W D6
 BPL L003B8
 ADD.W D6,D1
L003B8:SUBI.L #$700,D1
 MOVE.W 6(A6),D4
 EXT.L D4
 ASL.L #8,D4
 MOVE.B $F(A6),D4
 CMP.L D4,D1
 BEQ L003B0
 MOVE.L D0,-(A7)
 
 ; Bruit du titre qui rebondit
 MOVE.W #1,D0
 JSR prepare_address_of_sound
 
 MOVE.L (A7)+,D0
 BRA L003B0
L003AE:MOVE.L D0,D4
 LSR.L #8,D4
 ADDI.W #3,D4
 LSR.W #5,D4
 MOVE.L D1,D5
 LSR.L #8,D5
 LSR.W #4,D5
 MULU #$50,D5
 MOVEA.L #tomb_table1,A0
 ADDA.L D5,A0
 ADDA.L D4,A0
 CLR.W D4
 MOVE.B (A0),D4
 BTST #1,0(A1,D4.W)
 BEQ L003B0
 SUBI.L #$200,D3
 SUBI.L #$100,D1
 SUB.L D3,D1
 MOVE.W D1,D4
 ANDI.W #$FFF,D4
 NEG.W D4
 MULS #$400,D4
 ASR.L #8,D4
 MOVE.W D3,D5
 MULS D3,D5
 ASR.L #8,D5
 ADD.W D5,D4
 MOVE.L D0,-(A7)
 MOVE.W D4,D0
 JSR L003B5
 MOVE.W D0,D4
 MOVE.L (A7)+,D0
 ASL.W #4,D4
 NEG.W D4
 MOVE.W D4,D5
 SUB.W D3,D5
 ANDI.L #$FFFF,D5
 DIVS #2,D5
 NEG.W D5
 ADDI.W #$100,D5
 NEG.W D4
 MULS $C(A6),D4
 ASR.L #8,D4
 MOVE.W D5,D6
 MULS #$200,D6
 ASR.L #8,D6
 ADD.W D4,D6
 MOVE.W D6,D3
 CMP.W #$F00,D3
 BCS L003B9
 MOVE.W #$F00,D3
L003B9:MOVE.W D5,D6
 MULS #$100,D6
 ASR.L #8,D6
 ADD.W D4,D6
 MULS D5,D6
 ASR.L #8,D6
 ANDI.W #-$1000,D1
 ADD.W D6,D1
 MOVE.L D0,-(A7)
 
 ; Bruit du titre qui rebondit
 MOVE.W #1,D0
 JSR prepare_address_of_sound
 
 MOVE.L (A7)+,D0
L003B0:MOVE.L D0,D4
 LSR.L #8,D4
 ADDI.W #3,D4
 LSR.W #5,D4
 MOVE.L D1,D5
 LSR.L #8,D5
 LSR.W #4,D5
 MULU #$50,D5
 MOVEA.L #tomb_table1,A0
 ADDA.L D5,A0
 ADDA.L D4,A0
 CLR.W D4
 MOVE.B (A0),D4
 BTST #1,0(A1,D4.W)
 BEQ L003BB
 ADDI.L #$1000,D1
 ANDI.L #-$1000,D1
L003BB:MOVE.L D0,D4
 LSR.L #8,D4
 ADDI.W #3,D4
 LSR.W #5,D4
 MOVE.L D1,D5
 LSR.L #8,D5
 ADDI.W #7,D5
 LSR.W #4,D5
 MULU #$50,D5
 MOVEA.L #tomb_table1,A0
 ADDA.L D5,A0
 ADDA.L D4,A0
 CLR.W D4
 MOVE.B (A0),D4
 BTST #1,0(A1,D4.W)
 BEQ L003BC
 SUBI.L #$900,D1
 ANDI.L #-$1000,D1
 ADDI.L #$800,D1
L003BC:MOVE.L D0,D4
 LSR.L #8,D4
 LSR.W #5,D4
 MOVE.L D1,D5
 LSR.L #8,D5
 ADDI.W #3,D5
 LSR.W #4,D5
 MULU #$50,D5
 MOVEA.L #tomb_table1,A0
 ADDA.L D5,A0
 ADDA.L D4,A0
 CLR.W D4
 MOVE.B (A0),D4
 BTST #1,0(A1,D4.W)
 BEQ L003BF
 ADDI.L #$2000,D0
 ANDI.L #-$2000,D0
L003BF:MOVE.L D0,D4
 LSR.L #8,D4
 ADDI.W #7,D4
 LSR.W #5,D4
 MOVE.L D1,D5
 LSR.L #8,D5
 ADDI.W #3,D5
 LSR.W #4,D5
 MULU #$50,D5
 MOVEA.L #tomb_table1,A0
 ADDA.L D5,A0
 ADDA.L D4,A0
 CLR.W D4
 MOVE.B (A0),D4
 BTST #1,0(A1,D4.W)
 BEQ L003C1
 SUBI.L #$1900,D0
 ANDI.L #-$2000,D0
 ADDI.L #$1800,D0
L003C1:MOVEA.L (A7)+,A1
 MOVE.B D0,$E(A6)
 LSR.L #8,D0
 MOVE.W D0,4(A6)
 MOVE.B D1,$F(A6)
 LSR.L #8,D1
 MOVE.W D1,6(A6)
 MOVE.W D2,8(A6)
 MOVE.W D3,$A(A6)
 MOVEA.L A6,A5
 MOVEA.L 0(A6),A6
 BRA L00399
L00259:MOVEA.L #(debut_bss+214016),A0
 MOVE.L A0,(debut_data+548)
 MOVE.L #-1,(debut_data+552)
 MOVE.W #$10,D7
 SUBI.W #1,D7
L003C3:MOVEA.L A0,A1
 LEA $16(A0),A0
 MOVE.L A0,0(A1)
 DBRA D7,L003C3
 MOVE.L #-1,0(A1)
 RTS
 
; -----------------------------------------
; Affiche le caractère D0 en D1 sur l'écran
; ----------------------------------------- 
 
aff_car:SUBI.B #$20,D0
 ASL.W #5,D0
 MOVEA.L fonts8x8,A0
 ADDA.W D0,A0
 
 MOVEA.L physscreen2,A1
 ADDA.W D1,A1
 MOVE.W #7,D0
L003C4:MOVE.L (A0)+,D1
 MOVEP.L D1,0(A1)
 LEA $A0(A1),A1
 DBRA D0,L003C4
 RTS
 
; ----------------------------
; Routine d'affichage de texte
; ---------------------------- 
 
aff_text2:ADDQ.L #1,A0
 TST.W D0
 BMI L003C5
 CMP.W #$78A0,D0
 BCC L003C5
 SUBQ.L #1,A0
 MOVEA.L A0,A2
 MOVE.W D0,D2
 BTST #0,D0
 BEQ L003C7
 MOVE.W #7,D3
 MOVE.W #1,D4
 BRA L003C8
L003C7:MOVE.W #1,D3
 MOVE.W #7,D4
L003C8:MOVE.W D2,D1
 CLR.W D0
 MOVE.B (A2)+,D0
 MOVEA.L A2,A0
 BEQ L003C5
 JSR aff_car
 ADD.W D3,D2
 EXG.L D3,D4
 BRA L003C8
L003C5:RTS

; -------------------------------------------
; Affiche une chaine de caractère A0 en D0,D1
; -------------------------------------------

aff_text:SUB.W pos_in_tomb_y,D1
 BPL L003C9
 NEG.W D1
 CMP.W #$40,D1
 BCC L003CA
 MULU #$A0,D1
 NEG.W D1
 BRA L003CB
L003C9:CMP.W #$C1,D1
 BCC L003CA
 MULU #$A0,D1
L003CB:MOVE.W D0,D2
 LSR.W #4,D2
 ASL.W #3,D2
 ADD.W D2,D1
 ANDI.W #$F,D0
 LSR.W #3,D0
 ADD.W D1,D0
L003CF:MOVE.W D0,-(A7)
 JSR aff_text2
 MOVE.W (A7)+,D0
 CMPI.B #0,-1(A0)
 BEQ L003CD
L003CE:CMPI.B #0,(A0)+
 BNE L003CE
L003CD:ADDI.W #$640,D0
 CMPI.B #-1,(A0)
 BNE L003CF
L003CA:RTS

; ------------------------------------------
; Additionne D0 points au compteur de points
; ------------------------------------------

add_points:
 MOVEM.L D1/A0,-(A7)
 MOVEA.L #nb_points+4,A0
 ADDI.B #0,D0
 MOVE.B -(A0),D1
 ABCD.B D0,D1
 MOVE.B D1,(A0)
 ROR.L #8,D0
 MOVE.B -(A0),D1
 ABCD.B D0,D1
 MOVE.B D1,(A0)
 ROR.L #8,D0
 MOVE.B -(A0),D1
 ABCD.B D0,D1
 MOVE.B D1,(A0)
 ROR.L #8,D0
 MOVE.B -(A0),D1
 ABCD.B D0,D1
 MOVE.B D1,(A0)
 TST.B D1
 BEQ L003D0
 MOVE.L #$999999,nb_points
 MOVE.W #7,nb_lifes
L003D0:MOVEM.L (A7)+,D1/A0
 RTS
 
; ------------------------------
; Affichage des meilleurs scores
; ------------------------------ 
 
show_best_scores:

 ; Est-t-on au bout de la cinquième tombe ?
 CMPI.W #4,tomb_number
 BNE L003D2 ; Non
 
 ; Efface l'écran
 MOVEA.L physscreen2,A0
 MOVE.W #$1F3F,D7
L003D3:CLR.L (A0)+
 DBRA D7,L003D3
 
 CLR.W pos_in_tomb_y
 
 ; Affiche le texte de félicitation (5 tombes !)
 MOVEA.L #end5levels,A0
 MOVE.W #0,D0
 MOVE.W #0,D1
 JSR aff_text
 
 JSR swap_screens
 MOVE.W #2,D0
 JSR palette_crescendo

 ; Attend que l'on appuie sur le bouton feu du joystick
 CLR.W joystick_state
L003D5:
 MOVE.B joystick_state,D0
 ANDI.W #$F0,D0
 BEQ L003D5                
 
 MOVE.W #2,D0
 JSR palette_decrescendo

 ; Meilleurs scores
L003D2:MOVEA.L #(debut_data+3244),A0
 MOVE.L nb_points,D0
 MOVE.W #9,D7
L003D8:CMP.L (A0),D0
 BCS L003D7
 SUBA.W #$18,A0
 DBRA D7,L003D8
L003D7:CMP.W #9,D7
 BEQ L003D9
 MOVE.W #0,tomb_word0
 ADDQ.W #1,D7
 CMP.W #9,D7
 BEQ L003DA
 MOVE.W #8,D6
 SUB.W D7,D6
 MOVEA.L #(debut_data+3220),A0
L003DC:MOVE.L (A0),$18(A0)
 MOVE.L $A(A0),$22(A0)
 MOVE.L $E(A0),$26(A0)
 MOVE.L $12(A0),$2A(A0)
 SUBA.W #$18,A0
 DBRA D6,L003DC
L003DA:ASL.W #3,D7
 MOVEA.L #(debut_data+3028),A0
 ADDA.W D7,A0
 ADD.W D7,D7
 ADDA.W D7,A0
 MOVE.L nb_points,(A0)
 LEA $A(A0),A0
 MOVE.L #$5F5F5F5F,(A0)
 MOVE.L #$5F5F5F5F,4(A0)
 MOVE.L #$5F5F5F5F,8(A0)
 
 ; Efface les deux écrans
 MOVEA.L physscreen2,A1
 MOVEA.L physscreen1,A2
 MOVE.W #$1F3F,D7
L003DE:CLR.L (A1)+
 CLR.L (A2)+
 DBRA D7,L003DE

 MOVE.W #0,D7
L003EF:MOVEM.L D7-A0,-(A7)
 
 ; Efface l'écran
 MOVEA.L physscreen2,A0
 MOVE.W #$1F3F,D7
L003DF:CLR.L (A0)+
 DBRA D7,L003DF
 
 MOVE.W anim_man_counter,D0
 CLR.W pos_in_tomb_x
 CLR.W pos_in_tomb_y
 ADDI.W #1,anim_man_counter
 ANDI.W #3,D0
 MOVE.W #0,D1
 MOVE.W #0,D2
 MOVE.W #9,D7
L003E0:MOVEM.L D0-D2/D7-A0,-(A7)
 ADDI.W #$B,D0
 JSR aff_bloc32x24
 MOVEM.L (A7)+,D0-D2/D7-A0
 MOVEM.L D0-D2/D7-A0,-(A7)
 ADDI.W #7,D0
 ADDI.W #$A8,D2
 JSR aff_bloc32x24
 MOVEM.L (A7)+,D0-D2/D7-A0
 ADDI.W #$20,D1
 DBRA D7,L003E0
 MOVEA.L #bestscores,A0
 MOVE.W #$1438,D0
 JSR aff_text2
 MOVEA.L #(debut_data+3028),A0
 MOVE.W #$1BA0,D0
 MOVE.W #9,D7
L003E3:MOVE.L (A0),D1
 MOVEM.L D0/D7-A0,-(A7)
 MOVEA.L #(debut_data+5054),A0
 MOVE.W #5,D7
L003E2:MOVE.W D1,D0
 ANDI.W #$F,D0
 ADDI.W #$30,D0
 MOVE.B D0,-(A0)
 ROR.L #4,D1
 DBRA D7,L003E2
 MOVE.B #0,6(A0)
 MOVEM.L (A7)+,D0/D7-A0
 MOVEM.L D0/D7-A0,-(A7)
 MOVEA.L #line_info,A0
 JSR aff_text2
 MOVEM.L (A7)+,D0/D7-A0
 MOVEM.L D0/D7-A0,-(A7)
 ADDQ.L #4,A0
 ADDI.W #$18,D0
 JSR aff_text2
 MOVEM.L (A7)+,D0/D7-A0
 ADDI.W #$780,D0
 ADDA.W #$18,A0
 DBRA D7,L003E3
 
 ; Teste le clavier
 MOVE.W #$FF,-(A7)
 MOVE.W #6,-(A7)
 TRAP #1
 ADDQ.L #4,A7
 
 MOVEM.L (A7)+,D7-A0
 CMP.B #8,D0
 BNE L003E4
 CMP.B #0,D7
 BEQ L003E5
 SUBI.B #1,D7
 MOVE.B #$5F,-(A0)
 BRA L003E5
L003E4:CMP.B #$D,D0
 BNE L003E7
 MOVE.B #0,(A0)
 BRA L003E8
L003E7:CMP.B #$20,D0
 BCS L003E5
 CMP.B #$7F,D0
 BCC L003E5
 CMP.B #$61,D0
 BCS L003EB
 CMP.B #$7B,D0
 BCC L003EB
 ADDI.B #-$20,D0
L003EB:CMP.W #$C,D7
 BNE L003ED
 MOVE.B D0,-1(A0)
 BRA L003E5
L003ED:MOVE.B D0,(A0)+
 ADDI.W #1,D7
L003E5:JSR swap_screens
 CMPI.W #0,tomb_word0
 BNE L003EE
 MOVE.W #2,D0
 JSR palette_crescendo
 MOVE.W #1,tomb_word0
L003EE:BRA L003EF
L003E8:MOVE.W #2,D0
 JSR palette_decrescendo
L003D9:MOVE.W #0,firstpage_done
 RTS
 
; -----------------------------------------
; Passe de la palette D0 à la palette noire
; -----------------------------------------
 
palette_decrescendo:MOVEM.L D0-A6,-(A7)
 MOVEA.L #palettes,A0
 ASL.W #5,D0
 ADDA.W D0,A0 
 MOVE.W #$100,D0
 MOVE.W #8,D7
L003F1:MOVEM.L D0/D7-A0,-(A7)
 MOVEA.L #palettes,A1
 MOVE.W #$F,D6
L003F0:MOVE.W (A0),D1
 ANDI.W #$F,D1
 MULU D0,D1
 LSR.W #8,D1
 MOVE.W D1,D2
 MOVE.W (A0),D1
 ANDI.W #$F0,D1
 MULU D0,D1
 LSR.W #8,D1
 ANDI.W #$F0,D1
 OR.W D1,D2
 MOVE.W (A0)+,D1
 ANDI.W #$F00,D1
 MULU D0,D1
 LSR.L #8,D1
 ANDI.W #$F00,D1
 OR.W D1,D2
 MOVE.W D2,(A1)+
 DBRA D6,L003F0
 MOVE.W #0,D0
 JSR select_palette
 JSR wait_vbl
 MOVEM.L (A7)+,D0/D7-A0
 SUBI.W #$20,D0
 DBRA D7,L003F1
 MOVEM.L (A7)+,D0-A6
 RTS

; ----------------------------------------------------------------------
; Crée dans la palette 0 une palette D0 d'intensité (debut_data+142)/256
; ----------------------------------------------------------------------
 
create_palette:MOVEM.L D0-A6,-(A7)
 MOVEA.L #palettes,A0
 ASL.W #5,D0
 ADDA.W D0,A0
 MOVE.W #$100,D0
 MOVE.W (debut_data+142),D0
 MULU #$20,D0
 MOVEM.L D0/D7-A0,-(A7)
 MOVEA.L #palettes,A1
 MOVE.W #$F,D6
L003F2:MOVE.W (A0),D1
 ANDI.W #$F,D1
 MULU D0,D1
 LSR.W #8,D1
 MOVE.W D1,D2
 MOVE.W (A0),D1
 ANDI.W #$F0,D1
 MULU D0,D1
 LSR.W #8,D1
 ANDI.W #$F0,D1
 OR.W D1,D2
 MOVE.W (A0)+,D1
 ANDI.W #$F00,D1
 MULU D0,D1
 LSR.L #8,D1
 ANDI.W #$F00,D1
 OR.W D1,D2
 MOVE.W D2,(A1)+
 DBRA D6,L003F2
 MOVE.W #0,D0
 JSR select_palette
 MOVEM.L (A7)+,D0/D7-A0
 MOVEM.L (A7)+,D0-A6
 RTS
 
; -----------------------------------------
; Passe de la palette noire à la palette D0
; ----------------------------------------- 
 
palette_crescendo:MOVEM.L D0-A6,-(A7)
 MOVEA.L #palettes,A0
 ASL.W #5,D0
 ADDA.W D0,A0
 MOVE.W #0,D0
 MOVE.W #8,D7
L003F4:MOVEM.L D0/D7-A0,-(A7)
 MOVEA.L #palettes,A1
 MOVE.W #$F,D6
L003F3:MOVE.W (A0),D1
 ANDI.W #$F,D1
 MULU D0,D1
 LSR.W #8,D1
 MOVE.W D1,D2
 MOVE.W (A0),D1
 ANDI.W #$F0,D1
 MULU D0,D1
 LSR.W #8,D1
 ANDI.W #$F0,D1
 OR.W D1,D2
 MOVE.W (A0)+,D1
 ANDI.W #$F00,D1
 MULU D0,D1
 LSR.L #8,D1
 ANDI.W #$F00,D1
 OR.W D1,D2
 MOVE.W D2,(A1)+
 DBRA D6,L003F3
 MOVE.W #0,D0
 JSR select_palette
 JSR wait_vbl
 MOVEM.L (A7)+,D0/D7-A0
 ADDI.W #$20,D0
 DBRA D7,L003F4
 MOVEM.L (A7)+,D0-A6
 RTS

; ----------------------------------
; Calcule l'adresse du son numéro D0
; ----------------------------------
 
prepare_address_of_sound:MOVEM.L D0/A0,-(A7)
 MOVEA.L #sounds18,A0
 ASL.W #1,D0
 ADDA.W D0,A0
 ASL.W #2,D0
 ADDA.W D0,A0
 ASL.W #2,D0
 ADDA.W D0,A0
 MOVE.W sound_priority,D0
 CMP.W (A0),D0
 BCC L003F6
 MOVE.W (A0)+,sound_priority
 MOVE.W (A0)+,sound_pause
 MOVE.L A0,sound_address
L003F6:MOVEM.L (A7)+,D0/A0
 RTS
 
; ------------------------------------
; Joue un son avec le Yamaha soundchip
; ------------------------------------ 
 
play_yamaha:TST.L sound_address ; Adresse de la séquence nulle ?
 BEQ L003F8                    ; Oui, rien à jouer
 MOVE.L sound_address,-(A7)
 MOVE.W #$20,-(A7)
 TRAP #$E
 ADDQ.L #6,A7 
 CLR.L sound_address
 BRA L003F9
L003F8:SUBI.W #1,sound_pause
 BNE L003F9
 CLR.W sound_priority
L003F9:RTS

; -------------------------------------------
; Joue un son multiple (sons N° 17, 18 et 19)
; -------------------------------------------

play_big_sound:
 MOVEA.L #sound_17,A0
 MOVE.W #$14,D7
 JSR play_yamaha_wait
 
 MOVEA.L #sound_18,A0
 MOVE.W #$D,D7
 JSR play_yamaha_wait
 
 MOVEA.L #sound_17,A0
 MOVE.W #6,D7
 JSR play_yamaha_wait
 
 MOVEA.L #sound_19,A0
 MOVE.W #8,D7
 JSR play_yamaha_wait
 RTS
 
; --------------------------------------------------------------------
; Joue un son avec le Yamaha soundchip (adresse A0) et attend un délai
; -------------------------------------------------------------------- 
 
play_yamaha_wait:MOVE.L A0,-(A7)
 MOVE.W #$20,-(A7)
 TRAP #$E
 ADDQ.L #6,A7
L003FE:JSR wait_vbl
 DBRA D7,L003FE
 RTS

              DATA

debut_data:   IBYTES    "D:\ENTOMBED\ATARI\DATAS.BIN"
relocation:
 DC.L A000,A001,A002,A003,A004,A005,A006,A007,A008,A009,A010,A011,A012,A013,A014,A015
 DC.L A016,A017,A018,A019,A020,A021,A022,A023,A024,A025,A026,A027,A028,A029,A030,A031
 DC.L A032,A033,A034,A035,A036,A037,A038,A039,A040,A041,A042,A043,A044,A045,A046,A047
 DC.L A048,A049,A050,A051,A052,A053,A054,A055,A056,A057,A058,A059,A060,A061,A062,A063
 DC.L A064,A065,A066,A067,A068,A069,A070,A071,A072,A073,A074,A075,A076,A077,A078,A079
 DC.L A080,A081,A082,A083,A084,A085,A086,A087,A088,A089,A090,A091,A092,A093,A094,A095
 DC.L A096,A097,A098,A099,A100,A101,A102,A103,A104,A105,A106,A107,A108,A109,A110,A111
 DC.L A112,A113,A114,A115,A116,A117,A118,A119,A120,A121,A122,A123,A124,A125,A126,A127
 DC.L A128,A129,A130,A131,A132,A133,A134,A135,A136,A137,A138,A139,A140,A141,A142,A143
 DC.L A144,A145,A146,A147,A148,A149,A150,A151,A152,A153,A154,A155,A156,A157,A158,A159

              BSS

debut_bss:    DS.B      $34456

              END

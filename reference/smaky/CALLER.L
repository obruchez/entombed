; ----------------------------------------
; Test du programme appelant pour Entombed
; ----------------------------------------
;
; Mises … jour : 9 ao–t 1994, 10 ao–t 1994 (idem pour SmakyMod),
;                5, 8, 9,10 juillet 1995 (version pour Entombed),
;                20 juillet (routine de sauvegarde de partie)
;
; (C) 1994-95 Olivier Bruchez

              ilabel    "E:\ASSEMBLE.R\TOS\TOS.Q"

              text

              ; Taille programme
              move.l    4(sp),a5
              move.l    $c(a5),d7
              add.l     $14(a5),d7
              add.l     $1c(a5),d7
              add.l     #$100,d7

              ; Mshrink
              move.l    d7,-(sp)
              move.l    a5,-(sp)
              clr.w     -(sp)
              move.w    #$4a,-(sp)
              trap      #1
              add.l     #12,sp
              
              ; Charge le relocateur
              lea       relocator_file,a0
              bsr       load_prg
              beq       err                    ; Erreur
              lea       relocator,a1           ; Adresse programme
              move.l    a0,(a1)

              ; Charge Entombed
              lea       entombed_file,a0
              bsr       load_prg
              beq       err                    ; Erreur
              lea       entombed,a1            ; Adresse programme
              move.l    a0,(a1)

              ; Reloge Entombed
              move.l    entombed,a0
              move.l    relocator,a1
              jsr       (a1)

              ; Nouvelle routine joystick
              MOVE.W     #$22,-(sp)
              TRAP       #$E
              ADDQ.L     #2,A7
              MOVEA.L    D0,A0
              lea        save_joy,a1
              move.l     $18(a0),(a1)
              lea        joystick_rout,a1
              MOVE.L     a1,$18(A0)

              ; Etat du joystick envoy‚ en permanence
              MOVE.B     #$14,ikbd_buffer
              MOVE.L     #ikbd_buffer,-(sp)
              MOVE.W     #0,-(sp)
              MOVE.W     #$19,-(sp)
              TRAP       #$E
              ADDQ.L     #8,A7
              
              ; Adresse de l'‚cran
              move.w     #2,-(sp)
              trap       #14
              addq.l     #2,sp
              lea        old_screen,a0
              move.l     d0,(a0)
              
              ; Change adresse ‚cran
              lea        new_screen,a0
              move.l     a0,d0
              add.l      #256,d0
              and.l      #$ffffff0,d0
              move.l     d0,screen
              move.w     #-1,-(sp)
              move.l     d0,-(sp)
              move.l     d0,-(sp)
              move.w     #5,-(sp)
              trap       #14
              add.l      #12,sp
              
              ; Basse r‚solution
              ;MOVE.W     #%000111010,-(sp) ; 640x480 256 couleurs
              move.w     #%110110010,-(SP)
              MOVE.W     #88,-(SP)
              TRAP       #14
              ADDQ.L     #4,SP
              
;                 dc.l debut_entombed ; D‚but du code ex‚cutable
;                 dc.l joystick_state ; Adresse de l'‚tat du joystick
;sm_get_joystick: ds.l 1              ; Routine pour le joystick
;sm_clr_joystick: ds.l 1              ; Routine pour effacer l'‚tat du joystick
;sm_get_keyboard: ds.l 1              ; Routine pour le clavier
;sm_change_pal:   ds.l 1              ; Routine pour le changement de palette
;sm_wait_vbl:     ds.l 1              ; Routine de synchronisation VBL
;sm_aff_screen:   ds.l 1              ; Routine d'affichage du buffer
;sm_play_sound:   ds.l 1              ; Routine pour jouer un son
;sm_end_prg:      ds.l 1              ; Fin du programme, retour au programme appelant
;sm_aff_transp:   ds.l 1              ; Routine d'affichage avec transparence
;sm_aff_normal:   ds.l 1              ; Routine d'affichage sans transparence
;sm_aff_white:    ds.l 1              ; Routine d'affichage "en blanc"
;sm_open_for_save:ds.l 1              ; Routine pour ouvrir un fichier de sauvegarde
;sm_save:         ds.l 1              ; Routine pour sauvegarder dans un fichier
;sm_open_for_load:ds.l 1              ; Routine pour ouvrir un fichier en lecture
;sm_load:         ds.l 1              ; Routine pour charger des donn‚es
;sm_close:        ds.l 1              ; Routine pour fermer un fichier
              
              ; Inscrit les adresses de nos routines
              move.l   entombed,a1
              lea      get_joystick,a0
              move.l   a0,28+8(a1)
              lea      clr_joystick,a0
              move.l   a0,28+12(a1)
              lea      get_keyboard,a0
              move.l   a0,28+16(a1)
              lea      change_pal,a0
              move.l   a0,28+20(a1)
              lea      wait_vbl,a0
              move.l   a0,28+24(a1)
              lea      aff_screen,a0
              move.l   a0,28+28(a1)
              lea      play_sound,a0
              move.l   a0,28+32(a1)
              lea      end_prg,a0
              move.l   a0,28+36(a1)
              lea      aff_transp,a0
              ;;move.l   a0,28+40(a1)
              lea      aff_normal,a0
              ;;move.l   a0,28+44(a1)
              lea      aff_white,a0
              ;;move.l   a0,28+48(a1)
              lea      open_for_save,a0
              move.l   a0,28+52(a1)
              lea      save,a0
              move.l   a0,28+56(a1)
              lea      open_for_load,a0
              move.l   a0,28+60(a1)
              lea      load,a0
              move.l   a0,28+64(a1)
              lea      _close,a0
              move.l   a0,28+68(a1)
              
              ; D‚bute le jeu
              move.l   28+0(a1),a1
              jmp      (a1)
               
end_prg:
err:
              ; Ancienne routine joystick
              MOVE.W     #$22,-(sp)
              TRAP       #$E
              ADDQ.L     #2,A7
              MOVEA.L    D0,A0
              MOVE.L     save_joy,$18(A0)
 
              ; Haute r‚solution
              ;MOVE.W     #%000111010,-(sp) ; 640x480 256 couleurs 
              move.w     #%010111000,-(SP)
              MOVE.W     #88,-(SP)
              TRAP       #14
              ADDQ.L     #4,SP

              ; Ancienne adresse ‚cran
              move.w     #-1,-(sp)
              move.l     old_screen,-(sp)
              move.l     old_screen,-(sp)
              move.w     #5,-(sp)
              trap       #14
              add.l      #12,sp

              ; R‚active souris
              MOVE.B     #$8,ikbd_buffer
              MOVE.L     #ikbd_buffer,-(A7)
              MOVE.W     #0,-(A7)
              MOVE.W     #$19,-(A7)
              TRAP       #$E
              ADDQ.L     #8,A7

              ; LibŠre la m‚moire
              mfree     relocator
              mfree     entombed

              ; Quitte
              pterm0

              ;;;include   "D:\ENTOMBED\SMAKY\TOMB_LIB.L"

              text

joystick_rout:MOVE.B 2(A0),joystick_state
              RTS

              bss
              
joystick_state:ds.w     1
ikbd_buffer:  ds.w      1
screen:       ds.l      1
old_screen:   ds.l      1
save_joy:     ds.l      1
new_screen:   ds.b      256+640*480              
              
              text

; ****************************************************
; Charge un programme Atari en m‚moire sans le reloger
; ****************************************************
;
; in  : a0.l = ^nom du fichier
; out : a0.l = adresse du programme
; mod : a0.l

load_prg:
              movem.l   d0-d7/a1-a6,-(sp)

              move.l    a0,a6

              fgetdta                          ; Adresse DTA
              move.l    d0,a5

              fsfirst   #127,a6                ; Informations

              move.l    26(a5),d5              ; Taille fichier

              fopen     #0,a6
              move.w    d0,d7
              lea       header,a0
              fread     a0,#28,d7              ; Lit header
              fseek     #0,d7,#0               ; D‚but fichier

              lea       header,a0              ; Taille … r‚server :
              move.l    d5,d6                  ; Taille fichier
              add.l     10(a0),d6              ; + BSS

              malloc    d6                     ; Alloue m‚moire
              tst.l     d0                     ; Erreur ?
              bmi       \errmem
              beq       \errmem
              move.l    d0,a5
              bra       \noerr
\errmem:                   
              fclose    d7
              sub.l     a0,a0                  ; Erreur

              bra       \endr
\noerr:
              fread     a5,d5,d7               ; Charge fichier
              fclose    d7

              move.l    a5,a0                  ; Adresse programme
\endr:
              movem.l   (sp)+,d0-d7/a1-a6

              rts

; ****************************
; Charge un fichier en m‚moire
; ****************************
;
; in  : a0.l = ^nom du fichier
; out : a0.l = adresse des donn‚es
; mod : a0.l

load_file:
              movem.l   d0-d7/a1-a6,-(sp)

              move.l    a0,a6

              fgetdta                          ; Adresse DTA
              move.l    d0,a5

              fsfirst   #127,a6                ; Informations

              move.l    26(a5),d5              ; Taille fichier

              fopen     #0,a6
              move.w    d0,d7

              malloc    d5                     ; Alloue m‚moire
              tst.l     d0                     ; Erreur ?
              bmi       \errmem
              beq       \errmem
              move.l    d0,a5
              bra       \noerr
\errmem:
              fclose    d7
              sub.l     a0,a0                  ; Erreur

              bra       \endr
\noerr:
              fread     a5,d5,d7               ; Charge fichier
              fclose    d7

              move.l    a5,a0                  ; Adresse donn‚es
\endr:
              movem.l   (sp)+,d0-d7/a1-a6

              rts

              bss

              align.w
header:       ds.b      28

              data

relocator_file:dc.b     "D:\ENTOMBED\SMAKY\RELOCAT.PRG",0
entombed_file:dc.b      "D:\ENTOMBED\SMAKY\SMTOMBED.PRG",0

              bss

              align.w
relocator:    ds.l      1
entombed:     ds.l      1
fhandle:      ds.w      1

; ----------------------------------------------------
; Test de la partie ind‚pendante machine pour Entombed
; ----------------------------------------------------
;
; Mises … jour : 5 juillet 1995 (version de base), 8 juillet 1995
;                (nouvel algorithme pour la conversion 4 bits),
;                9 juillet 1995 (utilisation d'une table de 2 Mo),
;                20 juillet 1995 (option de sauvegarde/chargement)
;
; (C) 1995 Olivier Bruchez

              text
              
;                 dc.l debut_entombed ; D‚but du code ex‚cutable
;                 dc.l joystick_state ; Adresse de l'‚tat du joystick
;sm_get_joystick: ds.l 1              ; Routine pour le joystick
;sm_clr_joystick: ds.l 1              ; Routine pour effacer l'‚tat du joystick
;sm_get_keyboard: ds.l 1              ; Routine pour le clavier
;sm_change_pal:   ds.l 1              ; Routine pour le changement de palette
;sm_wait_vbl:     ds.l 1              ; Routine de synchronisation VBL
;sm_aff_screen:   ds.l 1              ; Routine d'affichage du buffer
;sm_play_sound:   ds.l 1              ; Routine pour jouer un son
;sm_end_prg:      ds.l 1              ; Fin du programme, retour au programme appelant
;sm_aff_transp:   ds.l 1              ; Routine d'affichage avec transparence
;sm_aff_normal:   ds.l 1              ; Routine d'affichage sans transparence
;sm_aff_white:    ds.l 1              ; Routine d'affichage "en blanc"
;sm_open_for_save:ds.l 1              ; Routine pour ouvrir un fichier de sauvegarde
;sm_save:         ds.l 1              ; Routine pour sauvegarder dans un fichier
;sm_open_for_load:ds.l 1              ; Routine pour ouvrir un fichier en lecture
;sm_load:         ds.l 1              ; Routine pour charger des donn‚es
;sm_close:        ds.l 1              ; Routine pour fermer un fichier


; ----------------------------------------------
; Routine pour aller chercher l'‚tat du joystick
; ----------------------------------------------
;
; out : [28+4(entombed)].b : ‚tat du joystick; description des bits :
;                             0 : haut
;                             1 : bas
;                             2 : gauche
;                             3 : droite
;                             7 : feu

get_joystick:

 move.l a0,-(sp)
 move.l entombed,a0
 move.l 28+4(a0),a0
 move.b joystick_state,(a0)
 move.l (sp)+,a0

 rts

; ---------------------------------------------
; Routine pour mettre … z‚ro l'‚tat du joystick
; ---------------------------------------------

clr_joystick:

 clr.b joystick_state

 rts
 
; ---------------------------------------------
; Routine pour aller chercher l'‚tat du clavier
; ---------------------------------------------
;
; out : d0.b : code ASCII de la touche ou z‚ro

get_keyboard:

 movem.l a0-a2/d1-d2,-(sp)
 move.w #$ff,-(sp)
 move.w #6,-(sp)
 trap #1
 addq.l #4,sp
 movem.l (sp)+,a0-a2/d1-d2

 rts

; -------------------------------
; Routine pour changer la palette
; -------------------------------
;
; in  : a0.l : adresse de la palette (16 mots de 16 bits); d‚finition d'une couleur :
;               xxxxxRRRxVVVxBBB ($777 = blanc, p. ex.)

change_pal:

 movem.l a0-a6/d0-d7,-(sp)
 
 bra \normal
 move.l a0,a6
 clr.l -(sp)
 move.w #$20,-(sp)
 trap #1
 addq.l #6,sp
 move.l d0,d7
 
 move.l a6,a0
 
 move.l #$ff9800,a1
 ;lea palette,a1
 move.w #16-1,d0
\bcl:
 move.w (a0)+,d1
 
 ; Rouge
 move.w d1,d2
 lsr.w #8,d2
 lsl.b #5,d2
 move.b d2,d3
 lsl.l #8,d3
 
 ; Vert
 move.w d1,d2
 and.b #$f0,d2
 lsl.b #1,d2 
 move.b d2,d3
 lsl.l #8,d3
 clr.b d3
 lsl.l #8,d3
 
 ; Bleu
 lsl.b #5,d1
 move.b d1,d3
 
 move.l d3,(a1)+

 dbra d0,\bcl
 
\normal:
 ;;move.l palette,-(sp)
 ;;move.w #16,-(sp)
 ;;move.w #0,-(sp)
 ;;move.w #93,-(sp)
 ;;trap #14
 ;;add.l #10,sp
 
 ;move.l d7,-(sp)
 ;move.w #$20,-(sp)
 ;trap #1
 ;addq.l #6,sp
 
 move.l a0,-(sp)
 move.w #6,-(sp)
 trap #14
 addq.l #6,sp
 
 movem.l (sp)+,a0-a6/d0-d7

 rts

              bss

              align.w
palette:      ds.l      16 

              text

; ----------------------------------------
; Routine pour se synchroniser avec la VBL
; ----------------------------------------

wait_vbl:

 movem.l a0-a2/d0-d2,-(sp)
 move.w #37,-(sp)
 trap #14
 addq.l #2,sp
 movem.l (sp)+,a0-a2/d0-d2

 rts

; -----------------------------------------
; Routine pour afficher le buffer … l'‚cran
; -----------------------------------------
;
; in  : a0.l : adresse du buffer (32000 octets)

aff_screen:

 movem.l a0-a6/d0-d7,-(sp)
 move.l screen,a1
 ;; move.w #32000/4/2-1,d0
 ;;\bcl:
 ;; move.l (a0)+,(a1)+
 ;; move.l (a0)+,(a1)+
 ;; dbra d0,\bcl
 bsr smaky_atari
 movem.l (sp)+,a0-a6/d0-d7

 rts
 
; -------------------------
; Routine pour jouer un son
; -------------------------
;
; in  : d0.b : num‚ro du son (0 … 19)

play_sound:
 rts
 
; -------------------------------
; Affiche un bloc en transparence
; -------------------------------
;
; in  : a0.l : adresse source (32xXX)
;       d0.w : nombre de lignes - 1 dans le bloc source
;       d1.w : position en X
;       d2.w : position en Y
; out : d0.w :   0, si bloc en dehors de l'‚cran
;              <>0, si bloc entiŠrement ou partiellement dans l'‚cran
; 
; Rem : clipping de (0,0) … (319,191)

aff_transp:
 rts
 
; ---------------------------------
; Affiche un bloc sans transparence
; ---------------------------------
;
; in  : a0.l : adresse source (32xXX)
;       d0.w : nombre de lignes - 1 dans le bloc source
;       d1.w : position en X
;       d2.w : position en Y
; out : d0.w :   0, si bloc en dehors de l'‚cran
;              <>0, si bloc entiŠrement ou partiellement dans l'‚cran
; 
; Rem : clipping de (0,0) … (319,191)


aff_normal:
 rts

; --------------------------
; Affiche un bloc "en blanc"
; --------------------------
;
; in  : a0.l : adresse source (32xXX)
;       d0.w : nombre de lignes - 1 dans le bloc source
;       d1.w : position en X
;       d2.w : position en Y
; out : d0.w :   0, si bloc en dehors de l'‚cran
;              <>0, si bloc entiŠrement ou partiellement dans l'‚cran
; 
; Rem : clipping de (0,0) … (319,191)

 
aff_white:
 rts 
 
 data
 
file_name: dc.b "F:\PARTIE.BIN",0

 text
 
; ---------------------------------------------------------
; Ouvre un fichier pour la sauvegarde de la partie en cours
; ---------------------------------------------------------

open_for_save:

 lea file_name,a0
 fcreate #0,a0
 move.w d0,fhandle

 rts
 
; ---------------------------
; Ouvre un fichier en lecture
; ---------------------------
 
open_for_load:

 lea file_name,a0
 fopen #0,a0
 move.w d0,fhandle

 rts
 
; ----------------------
; Sauvegarde des donn‚es
; ----------------------

save:

 fwrite a0,d0,fhandle

 rts
 
; ------------------
; Charge des donn‚es
; ------------------

load:

 fread a0,d0,fhandle

 rts
 
; -----------------------
; Ferme le fichier ouvert
; -----------------------

_close:

 fclose fhandle

 rts 
; --------------------------------------------------
; Routine d'"entrelacement" des plans en 16 couleurs
; --------------------------------------------------

smaky_atari:
              lea       liste,a6
              move.l    a6,a2
              add.l     #65536*8,a6
              move.l    a6,a3
              add.l     #65536*8,a6
              move.l    a6,a4
              add.l     #65536*8,a6
              move.l    a6,a5

              move.w    #(160*200)/8-1,d0
\bcl:
              move.l    d0,-(sp)

              ; Mot 0
              moveq     #0,d7
              move.w    (a0)+,d7
              lsl.l     #3,d7
              move.l    a2,a6
              add.l     d7,a6
              movem.l   (a6)+,d0/d1

              ; Mot 1
              moveq     #0,d7
              move.w    (a0)+,d7
              lsl.l     #3,d7
              move.l    a3,a6
              add.l     d7,a6
              or.l      (a6)+,d0
              or.l      (a6),d1

              ; Mot 2
              moveq     #0,d7
              move.w    (a0)+,d7
              lsl.l     #3,d7
              move.l    a4,a6
              add.l     d7,a6
              or.l      (a6)+,d0
              or.l      (a6),d1

              ; Mot 3
              moveq     #0,d7
              move.w    (a0)+,d7
              lsl.l     #3,d7
              move.l    a5,a6
              add.l     d7,a6
              or.l      (a6)+,d0
              or.l      (a6),d1
              
              move.l    d0,(a1)+
              move.l    d1,(a1)+

              move.l    (sp)+,d0

              dbra      d0,\bcl

              rts

              data

              align.l

liste:        ibytes    "E:\TABLE4.BIN"

              end

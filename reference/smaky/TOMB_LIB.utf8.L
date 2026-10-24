; ----------------------------------------------------
; Test de la partie indépendante machine pour Entombed
; ----------------------------------------------------
;
; Mises à jour : 5 juillet 1995 (version de base), 8 juillet 1995
;                (nouvel algorithme pour la conversion 4 bits),
;                9 juillet 1995 (utilisation d'une table de 2 Mo)
;
; (C) 1995 Olivier Bruchez

              text
              
;                 dc.l debut_entombed ; Début du code exécutable
;                 dc.l joystick_state ; Adresse de l'état du joystick
;sm_get_joystick: ds.l 1              ; Routine pour le joystick
;sm_clr_joystick: ds.l 1              ; Routine pour effacer l'état du joystick
;sm_get_keyboard: ds.l 1              ; Routine pour le clavier
;sm_change_pal:   ds.l 1              ; Routine pour le changement de palette
;sm_wait_vbl:     ds.l 1              ; Routine de synchronisation VBL
;sm_aff_screen:   ds.l 1              ; Routine d'affichage du buffer
;sm_play_sound:   ds.l 1              ; Routine pour jouer un son
;sm_end_prg:      ds.l 1              ; Fin du programme, retour au programme appelant
;sm_aff_transp:   ds.l 1              ; Routine d'affichage avec transparence
;sm_aff_normal:   ds.l 1              ; Routine d'affichage sans transparence
;sm_aff_white:    ds.l 1              ; Routine d'affichage "en blanc"

; ----------------------------------------------
; Routine pour aller chercher l'état du joystick
; ----------------------------------------------
;
; out : [28+4(entombed)].b : état du joystick; description des bits :
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
; Routine pour mettre à zéro l'état du joystick
; ---------------------------------------------

clr_joystick:

 clr.b joystick_state

 rts
 
; ---------------------------------------------
; Routine pour aller chercher l'état du clavier
; ---------------------------------------------
;
; out : d0.b : code ASCII de la touche ou zéro

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
; in  : a0.l : adresse de la palette (16 mots de 16 bits); définition d'une couleur :
;               xxxxxRRRxVVVxBBB ($777 = blanc, p. ex.)

change_pal:

 movem.l a0-a2/d0-d2,-(sp)
 move.l a0,-(sp)
 move.w #6,-(sp)
 trap #14
 addq.l #6,sp
 movem.l (sp)+,a0-a2/d0-d2

 rts

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
; Routine pour afficher le buffer à l'écran
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
; in  : d0.b : numéro du son (0 à 19)

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
; out : d0.w :   0, si bloc en dehors de l'écran
;              <>0, si bloc entièrement ou partiellement dans l'écran
; 
; Rem : clipping de (0,0) à (319,191)

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
; out : d0.w :   0, si bloc en dehors de l'écran
;              <>0, si bloc entièrement ou partiellement dans l'écran
; 
; Rem : clipping de (0,0) à (319,191)


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
; out : d0.w :   0, si bloc en dehors de l'écran
;              <>0, si bloc entièrement ou partiellement dans l'écran
; 
; Rem : clipping de (0,0) à (319,191)

 
aff_white:
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

              move.w    #32000/8-1,d0
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

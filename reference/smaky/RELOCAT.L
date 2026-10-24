; -------------------------------
; Relocateur pour programme Atari
; -------------------------------
;
; Mises … jour : 9 ao–t 1994
;
; (C) 1994 Olivier Bruchez

              text

              bra       relocator

              dc.b      "Relocateur de programmes Atari",13,10
              dc.b      "(C) 1994 Olivier Bruchez",0

              align.l

; *********************
; Routine de relocation
; *********************
;
; in  : a0.l = adresse du d‚but des donn‚es
; out : -
; mod : -

relocator:
              movem.l   d0-d7/a0-a6,-(sp)

              move.l    a0,a6                  ; D‚but fichier
              moveq     #0,d0
              move.b    1(a6),d0               ; Longueur par le bra.s
              addq.l    #2,a0                  ; Saute le bra.s
              add.l     d0,a0                  ; D‚but du programme
              move.l    a0,a1
              move.l    a0,a2
              move.l    $e(a6),d0              ; Table en fin de programme
              add.l     $2(a6),d0
              add.l     $6(a6),d0
              add.l     d0,a1                  ; D‚but de la table

              add.l     (a1)+,a2               ; PremiŠre adresse … modifier
              move.l    (a2),d0                ; Offset
              add.l     a0,d0                  ; Adresse d‚but
              move.l    d0,(a2)                ; Bonne adresse

              move.l    #$fe,d7

              ; Boucle principale
              ; -----------------

\boucle:
              moveq     #0,d0
              move.b    (a1)+,d0               ; Prochaine adresse
              beq       \exit                  ; 0 --> fin

              cmp.b     #1,d0                  ; Plus de $fe octets ?
              bne       \ok_mel                ; Non

              add.l     d7,a2                  ; + $fe octets
              bra       \boucle
\ok_mel:
              add.l     d0,a2                  ; Prochaine adresse
              move.l    (a2),d0
              add.l     a0,d0
              move.l    d0,(a2)

              bra       \boucle
\exit:

              ; Gestion BSS
              ; -----------

              move.l    $a(a6),d0              ; Longueur BSS
              beq       \pas_bss

              lea       $1c(a6),a0
              add.l     $2(a6),a0
              add.l     $6(a6),a0
\cls:
              clr.b     (a0)+                  ; Efface BSS
              subq.l    #1,d0
              bne       \cls
\pas_bss:
              movem.l   (sp)+,d0-d7/a0-a6
              rts

              end

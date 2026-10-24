# Études sur le jeu Entombed (Olivier Bruchez, juin-juillet 1995)

Texte extrait automatiquement de `ENTOMBED.CAL` (document Calligrapher, Atari ST, 9 juillet 1995).
Seuls les sauts de ligne ont été reconstitués ; le contenu est inchangé.

```text
Etudes sur le jeu Entombed par Nick Concannon

Le programme original (ENTOMBED.PRG, 51568 octets) est compacté (POMPEY PIRATES v1.5, codé par JPM). Le programme décompacté prend 121138 octets sur disque. Le fichier prend alors la structure suivante :
a) 28 octets consacrés au format PRG
b) 22994 octets de code (programme en soit)
c) 96884 octets consacrés aux données
d) 1232 octets pour la relocation

En mémoire, le programme prend la forme suivante :
a) 22994 octets de code (programme en soit)
b) 96884 octets consacrés aux données
c) 214102 octets d'octets nul (BSS)

A) Première étape de l'étude du programme

But. Désassembler la partie programme du fichier, dans le but de créer un fichier texte assembleur de la structure suivante :
a) Routine personnelle de relocation du bloc de donnée
b) Programme désassemblé et éventuellement commenté
c) Inclusion inline du bloc de donnée
d) Réservation de la BSS

Après désassemblage des 22994 octets de code (46 adresses supplémentaires à donner !) et écriture des programmes annexes en GFA Basic (4), les fichiers assembleur TOMBED1.L, TOMBED2.L et TOMBED3.L sont obtenus. Le dernier contient des adresses relatives pointant dans les blocs de données et de BSS.
Le fichier TOMBED_X.L, destiné à la compilation, est écrit. Premier problème : 160 adresses à reloger dans le bloc de donnée pointent dans le code exécutable ! Les 5 restantes pointent sur les données des 5 tombes.
Ecriture de deux programmes GFA Basic supplémentaires et résolution du problème. Constatation : ces 160 adresses pointent sur 10 x 16 mini-routines d'affichage de bloc.
Le jeu réassemblé fonctionne parfaitement. nous pouvons passer à la seconde étape.

B) Seconde étape de l'étude du programme

But. Comprendre les axes principaux du programme et commenter le listing. Comprendre l'organisation des données.

Ecriture d'un programme GFA Basic pour l'exploration graphique des données, d'un pour dresser une liste de tous les offsets directement utilisés et d'un pour jouer les sons du jeu.

Composition des données utilisées (96884 octets) :
- offset 2 : texte "ESPRITS.DAT"+0
- offset 14 : 4 palettes 16 couleurs de 32 octets chacune
- offset 142 : ?
- offset 144 : ?
- offset 146 : ?
- offset 148 : données, 110 octets
- offset 258 : données, 2038 octets
- offset 1492 : 16 sons de 42 octets chacun, puis son 17, 18 et 19 de tailles moindre
0 : tire flèche
1 : titre qui rebondit
2 : porte
3 : vase
4 : point de non-retour (étape), pièce d'or ou point d'action unique
5 : clé
6 : sablier ou vie supplémentaire
7 : flèches ou arc
8 : projectile
9 : ennemi touché
10 : homme mort
11 : ennemi mort
12 : homme qui marche
13 : idem ?
14 : homme qui saute
15 : élément qui se fixe ou disparaît, ou essai supplémentaire
16 : ?
17, 18, 19 : utilisés pour un son multiple
- offset 2296 : texte de fin des 5 niveaux
- offset 3014 : texte des meilleurs scores
- offset 3268 : texte du titre de la page d'instruction
- offset 3292 : textes d'instruction
- 3292 : arc
- 3395 : flèches
- 3422 : clé
- 3462 : pièce d'or
- 3501 : sablier
- 3593 : coffre
- 3635 : vases
- 3733 : vie supplémentaire
- offset 3754 : 160 adresses des routines d'affichage (que je remplis moi-même)
- offset 4394 : données, 340 octets/170 mots (pas accédé, directement du moins)
- offset 4734 : texte "(c) 1991 Nick Concannon"
- offset 4768 : adresses des données des 5 tombes (que je reloge moi-même)
- offset 4788 : données, 30 octets (ou octets nuls)
- offset 4818 : texte "Get Ready"
- offset 4829 ou 4830 : 42 ou 43 octets nuls
- offset 4892 : données, 96 octets
- offset 4988 : données, 112 octets (ou octets nuls)
- offset 5100 : texte de sélection des tombes
- offset 5140 : texte de confirmation de la sélection
- offset 5182 : texte indiquant qu'un niveau est complet
- offset 5196 : texte de la tombe 1
- offset 5304 : données de la tombe 1
- offset 7980 : texte de la tombe 2
- offset 8086 : données de la tombe 2
- offset 11724 : texte de la tombe 3
- offset 11824 données de la tombe 3
- offset 16166 : texte de la tombe 4
- offset 16268 : données de la tombe 4
- offset 20992 : texte de la tombe 5
- offset 21488 : données de la tombe 5
- offset 31348 : données, 64 octets, dont 3 adresses relogées
31348 : pointe sur les 113 blocs de 32x16
31352 : pointe sur les 6 blocs de 32x4 (et la suite est aussi concernée, probablement)
31356 : pointe sur la fonte de 100 caractères 8x8
- offset 31412 : 113 blocs de 32x16 (0 à 112)
0-3 : haut du feu
4-7 : intérieur du feu
8-11 : haut de l'eau
12-15 : intérieur de l'eau
16-19 : flambeau
20-23 : lances tournantes
24-26 : murs de tous genres
27 : clé
28 : passerelle avec un morceau décalé
29 : passerelle soutenue
30 : soutien de passerelle
31 : échelle claire
32 : haut de porte fermée
33 : haut de porte ouverte
34 : lance-projectile sur la gauche
35 : lance-projectile sur la droite
36 : pics vers le haut
37 : pics vers la gauche
38 : pics vers la droite
39 : pics vers la bas
40 : arc
41 : flèches
42 : bas de vase
43 : haut de vase
44 : coffre
45 : échelle foncée
46 : sablier
47 : point d'action
48 : vide
49 : bas de vase tout blanc
50 : haut de vase tout blanc
51 : haut de la sortie 1
52 : bas de la sortie 1
53 : passerelle normale
54 : mur (sert de motif de fond)
55 : bas de porte fermée
56 : bas de porte ouverte
57-60 : pièce d'or qui tourne
61-65 : murs de tous genres
66 : vie supplémentaire
67-75 : murs de tous genres
76 : haut de la sortie 2
77 : bas de la sortie 2
78-86 : murs de tous genres
87 : haut de la sortie 3
88 : bas de la sortie 3
89-97 : murs de tous genres
98 : haut de la sortie 4
99 : bas de la sortie 5
100-108 : murs de tous genres
109 : haut de la sortie 5
110 : bas de la sortie 5
111 : dragon vers la gauche
112 : dragon vers la droite
- offset 60340 : 6 blocs de 32x4 (0 à 5)
0 : projectile vers la gauche
1 : projectile vers la droite
2 : flèche vers la gauche
3 : flèche vers la droite
4 : boule de serpent 1
5 : boule de serpent 2 (identique à la 1)
- offset 60724 : 4 blocs de 32x6 (0 à 3)
0 : "100" points
1 : "250" points
2 : "500" points
3 : "1000" points
- offset 61108 : 10 blocs de 32x12 (0 à 9)
0 à 9 : caractères "0" à "9"
- offset 63028 : 20 blocs de 32x24 (0 à 19)
0-3 : homme qui marche vers la gauche
4-7 : homme qui marche vers la droite
8-11 : homme qui marche vers la gauche avec l'arc
12-15 : homme qui marche vers la droite avec l'arc
16-17 : homme qui grimpe à l'échelle droit
18-19 : homme qui grimpe à l'échelle droit avec l'arc
- offset 70708 : 3 blocs de 32x8 (0 à 2)
0 : radeau
1 : boule métallique
2 : noir
- offset 71092 : 39 blocs de 32x24 (0 à 38)
0-3 : pharaon qui marche vers la gauche
4-7 : pharaon qui marche vers la droite
8-9 : serpent qui rampe vers la gauche
10-11 : homme qui grimpe à l'échelle en regardant vers la gauche
12-13 : serpent qui rampe vers la droite
14-15 : homme qui grimpe à l'échelle en regardant vers la droite
16 : arc en hauteur
17 : flèches en hauteur
18 : clé en hauteur
19 : pièce en or en hauteur
20 : sablier en hauteur
21 : coffre en hauteur
22 : vase
23 : ?
24 : homme qui tire vers la gauche
25 : homme qui tire vers la droite
26-27 : homme qui tombe
28-29 : pharaon qui tombe
30-31 : serpent qui tombe
32-34 : vase qui se casse
35 : vie supplémentaire en hauteur
36-38 : "Game Over"
- offset 86068 : 7 blocs de 32x16 (0 à 6)
0 : mur
1 : mur vert
2 : mur orange
3-6 : sphère qui tourne
- offset 87860 : 128 octets de libre
- offset 87988 : 8 blocs de 32x24 (0 à 7)
0-7 : "E", "N", "T", "O", "M", "B", "E", "D" de la page de présentation
- offset 91060 : 1 bloc de 32x48 (0)
0 : logo de la page de présentation
- offset 91828 : 3 blocs de 32x24 (0 à 2)
0 : début du soulignement dans la page de présentation
1 : milieu
2 : fin
- offset 92980 : 100 blocs de 8x8 (0 à 99)
0-95 : lettres ASCII de code 32 à 127
96 : caractère vie
97 : caractère arc
98 : caractère flèche
99 : caractère clé
- offset 96180 : 704 octets libres (22 caractères 8x8 noirs - sécurité ?)
- offset 96884 : fin des données

C) Adaptation du jeu sur Smaky couleur

Seul problème important : l'entrelacement des plans sur Atari en mode 4 bits ne peut pas être contourné par après sur Smaky par une routine; la conversion d'un écran entier 320x200 en 16 couleurs prend bien trop de temps.

Dans le fichier de donnée (DATAS?.BIN), il faut "désentrelacer" :
- en 31412, 65472 octets (tous les graphismes)
- en 416, 64 octets (masque pour la fonte)

Ecriture d'une routine pour vérifier le bon fonctionnement de l'affichage en mode Smaky (2 Mo de table !). Le travail est terminé sur Atari. Il faut maintenant commencer l'adaptation sur Smaky.

```

/*
 *	main.cpp
 *
 *	Programme de traduction langage machine 68000 vers C créé uniquement
 *	en vue de l'adaptation de Entombed (jeu Atari) sur PC.
 *
 *	Mises à jour : 5, 6, 7, 8, 9, 11, 12 avril 1996 (première version opérationnelle)
 *	               13 avril 1996
 *
 *	A faire : - ...problème de la table de relocation
 *	          - commentaires des fonctions
 *	          - écrire deux donctions du style ReadOnlyDest et WriteOnlyDest pour
 *	            éviter d'avoir des "//" superflus dans le fichier de sortie
 *
 *	(C)	1996 Olivier Bruchez
 */

#include <stdio.h>
#include <string.h>
#include <stdarg.h>
#include "opcodes.h"
#include "systypes.h"

// Décaration des fonctions

Card8	Read8 (Card8 *);
Card16	Read16 (Card8 *);
Card32	Read32 (Card8 *);
void	Out (char *);
void	Out (char);
void	Outf (char * ...);
char	*BufOutf (char * ...);
void	LabelOut (Card8 *);
Card32	OutEffectiveDest (Card8, Card8, Card8);
Card32	OutEffectiveSource (Card8, Card8, Card8);
Card32	OutEffectiveControl (Card8, Card8, Card8);
void	CardOut (Card8);
void	LenByteOut (Card8);
void	NumberOut (Int32);
void	NumberOut (Card32);
void	AdrOut (Card32);
char	*StrChar (char);
char	*StrCard (Card8);
char	*StrLenByte (Card8);
char	*StrNumber (Card32);
char	*StrNumber (Int32);
char	*StrAdr (Card32);
char	*StrLabel (Card8 *);
void	ReadDest (char *, char *);
void	ReadSource (char *, char *);
void	WriteDest (char *, char *);
Card32	Relocate (Card8 *);
Bool	TestRelocate (Card8 *);	

// Variables globales

FILE	*out_handle;
char	line[1000];
Card8	*code, *data, *bss, *immediate, *reloc;
Card32	text_size, data_size, bss_size, reloc_size, nb_reloc;
char	out[20][100];
char	buf[1000], buf2[1000], buf3[1000];
Card8	len_btst;
Card8	*reloc_table[10000];

// Constantes

#define LEN_BTST 3

int
main ()
{
	Card8	header[28], *end_code, *pc, reg, len, i8;
	Card16	opcode, i16;
	Card32	symbol_size, file_size, jsr_counter;
	Card8	blen[4];
	Int32	inf[4], sup[4];
	Int16	i;

	FILE *fhandle = fopen ("f:\\users\\olivier\\entombed\\tombedpc.prg", "rb");

	if (fhandle) {
		
		// Lit le header d'un fichier de type .PRG
		fread (header, 1, 28, fhandle);

		// Vérifie que le fichier est bien au format .PRG
		if (Read16 (header) != 0x601A) {
			printf ("Le fichier 'tombedpc.prg' n'a pas le bon format\n");
			return -1;
		}

		// Lit la taille du fichier
		fseek (fhandle, 0, SEEK_END);
		file_size = ftell (fhandle);
		fseek (fhandle, 28, SEEK_SET);

		// Lit la taille des différentes zones de données
		text_size = Read32 (header + 2);
		data_size = Read32 (header + 6);
		bss_size = Read32 (header + 10);
		symbol_size = Read32 (header + 14);
		reloc_size = file_size - text_size - data_size - symbol_size;

		// Affiche les tailles des zones de données
		printf ("File size             : %d\n", file_size);
		printf ("Text size             : %d\n", text_size);
		printf ("Data size             : %d\n", data_size);
		printf ("Bss size              : %d\n", bss_size);
		printf ("Symbol size           : %d\n", symbol_size);
		printf ("Relocation table size : %d\n", reloc_size);

		// Alloue la taille mémoire nécessaire à la zone TEXT, la zone DATA
		// et à la table de relocation
		code = new Card8[text_size];
		data = new Card8[data_size];
		reloc = new Card8[reloc_size];

		if (code == 0) {
			return -1;
		}

		if (data == 0) {
			delete code;
			return -1;
		}

		if (reloc == 0) {
			delete code;
			delete data;
			return -1;
		}

		// Charge la zone TEXT
		fread (code, 1, text_size, fhandle);

		// Charge la zone DATA
		fread (data, 1, data_size, fhandle);

		// Charge la table de relocation
		fseek (fhandle, 28 + text_size + data_size + symbol_size, SEEK_SET);
		fread (reloc, 1, reloc_size, fhandle);

		// Ferme le fichier
		fclose (fhandle);

	} else {
		return -1;
	}

	// Ouvre le fichier de sortie

	out_handle = fopen ("f:\\users\\olivier\\entombed\\tombedpc.cpp", "wt");

	// Ouvre le fichier assembleur simplifié (une instruction par ligne)

	FILE *lst_handle = fopen ("f:\\users\\olivier\\entombed\\tombedpc.lst", "rt");
	
	// Tailles en bits

	blen[0] = 8;
	blen[1] = 16;
	blen[2] = 32;
	blen[3] = 64;

	// Limites des nombres signés

	inf[0] = -128;
	sup[0] = 127;
	inf[1] = -32768;
	sup[1] = 32767;
	inf[2] = -(Int32)(2147483648);
	sup[2] = 2147483647;

	// Boucle principale de traduction

	end_code = code + text_size;
	pc = code;
	jsr_counter = 0;

	Outf ("#include \"systypes.h\"\n");
	Outf ("Card64 tmpc64, tmpc64b;\n");
	Outf ("Card32 tmpc32, tmpc32b;\n");
	Outf ("Card16 tmpc16, tmpc16b;\n");
	Outf ("Card8 tmpc8, tmpc8b;\n");
	Outf ("Int64 tmpi64, tmpi64b;\n");
	Outf ("Int32 tmpi32, tmpi32b;\n");
	Outf ("Int16 tmpi16, tmpi16b;\n");
	Outf ("Int8 tmpi8, tmpi8b;\n");
	Outf ("Card32 d0,d1,d2,d3,d4,d5,d6,d7;\n");
	Outf ("Card8 *a0,*a1,*a2,*a3,*a4,*a5,*a6,*a7,*src_pt, *dst_pt;\n");
	Outf ("Card8 *data_adr, *bss_adr;\n");
	Outf ("Card8 n,z,v,c,x;\n");
	Outf ("void *rts;\n");

	Outf ("int main () {\n");

	nb_reloc = Relocate (reloc);

	while (pc < end_code) {

		// Affiche le pourcentage de travail effectué

		printf ("Work done : %.2f %%\r", 100 * (Real)(pc - code) / (Real)(text_size));

		// Ecrit le label de la fonction courante

		LabelOut (pc);
		Outf (":\n");

		// Met en commentaire l'instruction assembleur dont la traduction C va suivre

		Outf ("// ");
		Outf (fgets (line, 100, lst_handle));
		//Outf ("\n");

		opcode = Read16 (pc);
		immediate = pc + 2;

		/*if ((opcode & ASLR1_MASK) == ASLR1_DATA) {
			opcode |= 1 << 3; // ASL/R --> LSL/R
		}*/

		if ((opcode & RTS_MASK) == RTS_DATA) {

			// Instruction RTS

			Outf ("// RTS\n");
			Outf ("rts=(void *)(Read32(a7));\n");
			Outf ("a7+=4;\n");
			Outf ("goto *rts;\n");

		} else if ((opcode & DBRA_MASK) == DBRA_DATA) {

			// Instruction DBRA

			i16 = Read16 (immediate);
			immediate += 2;

			reg = opcode & 7;

			Outf ("// DBRA\n");
			Outf ("tmpi16=(Int16)((Card16)(d%n));\n", reg);
			Outf ("tmpi16--;\n");
			Outf ("d%n&=0xffff0000;\n", reg);
			Outf ("d%n+=(Card16)(tmpi16);\n", reg);
			Outf ("if (tmpi16 != -1) goto %l;\n", pc + 2 + (Int16)(i16));

		} else if ((opcode & SWAP_MASK) == SWAP_DATA) {

			// Instruction SWAP

			reg = opcode & 7;

			Outf ("// SWAP\n");
			Outf ("tmpc32=d%n;\n", reg);
			Outf ("tmpc16=(Card16)(tmpc32);\n");
			Outf ("tmpc32>>=16;\n");
			Outf ("tmpc32+=((Card32)(tmpc16))<<16;\n");
			Outf ("d%n=tmpc32;\n", reg);
			Outf ("if (tmpc32>>31) {n=1;} else {n=0;}\n");
			Outf ("if (tmpc32) {z=0;} else {z=1;}\n");
			Outf ("v=0;\nc=0;\n");

		} else if ((opcode & TRAP_MASK) == TRAP_DATA) {

			// Instruction TRAP

			Outf ("// TRAP #%n\n", opcode & 15);

		} else if ((opcode & BSET2_MASK) == BSET2_DATA) {

			// Instruction BSET 2 - pas dans Entombed

			Outf ("// BSET 2\n");
			immediate += 2;
			immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, LEN_BTST);

			printf ("Instructions BSET 2 pas supportées\n");

		} else if ((opcode & BTST2_MASK) == BTST2_DATA) {

			// Instruction BTST 2

			i16 = Read16 (immediate);
			immediate += 2;
			immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, LEN_BTST);

			Outf ("// BTST 2\n");
			ReadSource (BufOutf ("tmpc%c=", len_btst), ";\n");
			Outf ("if ((tmpc%c>>%n)&1) {z=0;} else {z=1;};\n", len_btst, i16);

		} else if ((opcode & EXT_MASK) == EXT_DATA) {

			// Instruction EXT

			reg = opcode & 7;

			Outf ("// EXT\n");

			if (((opcode >> 6) & 7) == 2) {

				// De l'octet au mot de 16 bits

				Outf ("tmpc16=(Card16)((Int16)((Int8)(d%n)));\n", reg);
				Outf ("d%n&=0xffff0000;\n", reg);
				Outf ("d%n+=tmpc16;\n", reg);
				Outf ("if ((Int16)(tmpc16)<0) {n=1;} else {n=0;}\n");
				Outf ("if (tmpc16) {z=0;} else {z=1;}\n");
				Outf ("v=0;\nc=0;\n");

			} else if (((opcode >> 6) & 7) == 3) {

				// Du mot de 16 bits au mot de 32 bits

				Outf ("d%n=(Card32)((Int32)((Int16)(d%n)));\n", reg, reg);
				Outf ("if ((Int32)(d%n)<0) {n=1;} else {n=0;}\n", reg);
				Outf ("if (d%n) {z=0;} else {z=1;}\n", reg);
				Outf ("v=0;\nc=0;\n");
			}

		} else if ((opcode & JMP_MASK) == JMP_DATA) {

			// Instruction JMP

			immediate += OutEffectiveControl ((opcode >> 3) & 7, opcode & 7, 2);

			Outf ("// JMP\n");
			ReadSource ("rts=",";\n");
			Outf ("goto *rts;\n");

		} else if ((opcode & JSR_MASK) == JSR_DATA) {

			// Instruction JSR

			immediate += OutEffectiveControl ((opcode >> 3) & 7, opcode & 7, 2);

			Outf ("// JSR\n");
			ReadSource ("rts=",";\n");
			Outf ("a7-=4;\n");
			Outf ("Write32(a7,(Card32)(&&J%n));\n", jsr_counter);
			Outf ("goto *rts;\n");
			Outf ("J%n:\n", jsr_counter++);

		} else if ((opcode & ASLR2_MASK) == ASLR2_DATA) {

			// Instruction ASL/ASR 2 - pas dans Entombed

			Outf ("// ASL/R 2\n");
			immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, 0);
			
			printf ("Instructions ASL/R 2 pas supportées\n");

		} else if ((opcode & LSLR2_MASK) == LSLR2_DATA) {

			// Instruction LSL/LSR 2 - pas dans Entombed

			Outf ("// LSL/R 2\n");
			immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, 0);
			
			printf ("Instructions LSL/R 2 pas supportées\n");

		} else if ((opcode & ROLR2_MASK) == ROLR2_DATA) {

			// Instruction ROL/ROR 2 - pas dans Entombed

			Outf ("// ROL/R 2\n");
			immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, 0);
			
			printf ("Instructions ROL/R 2 pas supportées\n");

		} else if ((opcode & ABCD_MASK) == ABCD_DATA) {

			// Instruction ABCD

			Outf ("// ABCD\n");

			if ((opcode >> 3) & 1) {
				OutEffectiveDest (4, (opcode >> 9) & 7, 0);
				OutEffectiveSource (4, opcode & 7, 0);

				ReadDest ("tmpc8=", ";\n");
				ReadSource ("tmpc8=Abcd(tmpc8,", ");\n");
				WriteDest ("", "tmpc8");
			} else {
				reg = (opcode >> 9) & 7;

				Outf ("d%n=Abcd(d%n,d%n);\n", reg, opcode & 7, reg);
			}

		} else if ((opcode & ADDI_MASK) == ADDI_DATA) {

			// Instruction ADDI

			Outf ("// ADDI\n");

			len = (opcode >> 6) & 3;

			immediate += OutEffectiveSource (7, 4, len);
			immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);


			ReadDest (BufOutf ("tmpi%c=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
			ReadSource (BufOutf ("tmpi%cb=(Int%c)(", len + 1, len + 1), ");\n");
			Outf ("if (tmpi%c>=0) {if (tmpi%cb>=0) {c=2;} else {c=0;}} else {if (tmpi%cb>=0) {c=0;} else {c=1;}}\n", len + 1, len + 1, len + 1);
			Outf ("tmpi%c+=tmpi%cb;\n", len + 1, len + 1);
			WriteDest ("", BufOutf ("(Card%c)(tmpi%c)", len, len + 1));
			Outf ("if ((Int%c)(tmpi%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
			Outf ("if ((Card%c)(tmpi%c)) {z=0;} else {z=1;}\n", len, len + 1);
			Outf ("if (((c==0)&&(tmpi%c>=0))||(c==1)) {c=1;} else {c=0;}\n", len + 1);
			Outf ("if ((tmpi%c>%n)||(tmpi%c<%n)) {v=1;} else {v=0;}\n", len + 1, sup[len], len + 1, inf[len]);
			Outf ("x=c;\n");

		} else if ((opcode & ANDI_MASK) == ANDI_DATA) {

			// Instruction ANDI

			Outf ("// ANDI\n");
	
			len = (opcode >> 6) & 3;

			immediate += OutEffectiveSource (7, 4, len);
			immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);

			ReadDest (BufOutf ("tmpc%c=", len), ";\n");
			ReadSource (BufOutf ("tmpc%c&=(Card%c)(", len, len), ");\n");
			WriteDest ("", BufOutf ("tmpc%c", len));
			Outf ("if ((Int%c)(tmpc%c)<0) {n=1;} else {n=0;}\n", len, len);
			Outf ("if (tmpc%c) {z=0;} else {z=1;}\n", len);
			Outf ("v=0;\nc=0;\n");

		} else if ((opcode & BRA_MASK) == BRA_DATA) {

			// Instruction BRA

			Outf ("// BRA\n");

			if (opcode & 255) {

				Outf ("goto %l;\n", pc + 2 + (Int8)(opcode & 255));

			} else {

				Outf ("goto %l;\n", pc + 2 + (Int16)(Read16 (immediate)));
				immediate += 2;

			}

		} else if ((opcode & BSR_MASK) == BSR_DATA) {

			// Instruction BSR

			Outf ("// BSR\n");

			Outf ("a7-=4;\n");
			Outf ("Write32(a7,(Card32)(&&J%n));\n", jsr_counter);
			
			if (opcode & 255) {

				Outf ("goto %l;\n", pc + 2 + (Int8)(opcode & 255));

			} else {

				Outf ("goto %l;\n", pc + 2 + (Int16)(Read16 (immediate)));
				immediate += 2;

			}

			Outf ("J%n:\n", jsr_counter++);

		} else if ((opcode & CLR_MASK) == CLR_DATA) {

			// Instruction CLR

			Outf ("// CLR\n");

			len = (opcode >> 6) & 3;

			immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);

			ReadDest ("//", "\n");
			WriteDest ("", BufOutf ("0", len));
			Outf ("n=0;\nz=1;\nv=0;\nc=0;\n");

		} else if ((opcode & CMPI_MASK) == CMPI_DATA) {

			// Instruction CMPI

			len = (opcode >> 6) & 3;

			immediate += OutEffectiveSource (7, 4, len);
			immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);

			Outf ("// CMPI\n");

			ReadDest (BufOutf ("tmpi%c=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
			ReadSource (BufOutf ("tmpi%cb=(Int%c)(", len + 1, len + 1), ");\n");
			Outf ("if (tmpi%c>=0) {if (tmpi%cb>=0) {c=0;} else {c=2;}} else {if (tmpi%cb>=0) {c=1;} else {c=0;}}\n", len + 1, len + 1, len + 1);
			Outf ("tmpi%c-=tmpi%cb;\n", len + 1, len + 1);
			WriteDest ("//", "");
			Outf ("if ((Int%c)(tmpi%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
			Outf ("if ((Int%c)(tmpi%c)) {z=0;} else {z=1;}\n", len, len + 1);
			Outf ("if (((c==0)&&(tmpi%c>=0))||(c==1)) {c=1;} else {c=0;}\n", len + 1);
			Outf ("if ((tmpi%c>%n)||(tmpi%c<%n)) {v=1;} else {v=0;}\n", len + 1, sup[len], len + 1, inf[len]);

		} else if ((opcode & EORI_MASK) == EORI_DATA) {

			// Instruction EORI

			len = (opcode >> 6) & 3;

			Outf ("// EORI\n");

			immediate += OutEffectiveSource (7, 4, len);
			immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);

			ReadDest (BufOutf ("tmpc%c=", len), ";\n");
			ReadSource (BufOutf ("tmpc%c^=(Card%c)(", len, len), ");\n");
			WriteDest ("", BufOutf ("tmpc%c", len));
			Outf ("if ((Int%c)(tmpc%c)<0) {n=1;} else {n=0;}\n", len, len);
			Outf ("if (tmpc%c) {z=0;} else {z=1;}\n", len);
			Outf ("v=0;\nc=0;\n");

		} else if ((opcode & MOVEM_MASK) == MOVEM_DATA) {

			// Instruction MOVEM - ne simule que les formes "(Ax)+, reg" et "reg, -(Ax)"

			i16 = Read16 (immediate);
			immediate += 2;
			
			Outf ("// MOVEM\n");
				
			len = ((opcode >> 6) & 1) + 1;

			if ((opcode >> 10) & 1) {

				immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, len);

				// De mémoire à registres

				// De A7 à A0
				for (i = 15; i >= 8; i--) {
					if ((i16 >> i) & 1) {
						ReadSource (BufOutf ("a%n=(Card8 *)(", i - 8), ");\n");
					}
				}

				// De D7 à D0
				for (i = 7; i >= 0; i--) {
					if ((i16 >> i) & 1) {
						if (len != 2) {
							ReadSource (BufOutf ("(Card%c)(d%n)=", len, i), ";\n");
						} else {
							ReadSource (BufOutf ("d%n=", i), ";\n");
						}
					}
				}

			} else {

				immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);

				// De registre à mémoire

				// De D0 à D7
				for (i = 15; i >= 8; i--) {
					if ((i16 >> i) & 1) {
						ReadDest ("//", "\n");
						WriteDest ("", BufOutf ("(Card%c)(d%n)", len, 15 - i));
					}
				}

				// De A0 à A7
				for (i = 7; i >= 0; i--) {
					if ((i16 >> i) & 1) {
						ReadDest ("//", "\n");
						WriteDest ("", BufOutf ("(Card%c)(a%n)", len, 7 - i));
					}
				}


			}

		} else if ((opcode & NEG_MASK) == NEG_DATA) {

			// Instruction NEG

			len = (opcode >> 6) & 3;

			Outf ("// NEG\n");

			immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);

			ReadDest (BufOutf ("tmpi%c=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
			Outf ("tmpi%c=-tmpi%c;\n", len + 1, len + 1);
			WriteDest ("", BufOutf ("(Card%c)(tmpi%c)", len, len + 1));
			Outf ("if ((Int%c)(tmpi%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
			Outf ("if ((Int%c)(tmpi%c)) {z=0;} else {z=1;}\n", len, len + 1);
			Outf ("c=0;\n");
			Outf ("if ((tmpi%c>%n)||(tmpi%c<%n)) {v=1;} else {v=0;}\n", len + 1, sup[len], len + 1, inf[len]);
			Outf ("x=c;\n");

		} else if ((opcode & ORI_MASK) == ORI_DATA) {

			// Instruction ORI

			len = (opcode >> 6) & 3;

			Outf ("// ORI\n");

			immediate += OutEffectiveSource (7, 4, len);
			immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);

			ReadDest (BufOutf ("tmpc%c=", len), ";\n");
			ReadSource (BufOutf ("tmpc%c|=(Card%c)(", len, len), ");\n");
			WriteDest ("", BufOutf ("tmpc%c", len));
			Outf ("if ((Int%c)(tmpc%c)<0) {n=1;} else {n=0;}\n", len, len);
			Outf ("if (tmpc%c) {z=0;} else {z=1;}\n", len);
			Outf ("v=0;\nc=0;\n");

		} else if ((opcode & SUBI_MASK) == SUBI_DATA) {

			// Instruction SUBI

			Outf ("// SUBI\n");

			len = (opcode >> 6) & 3;

			immediate += OutEffectiveSource (7, 4, len);
			immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);


			ReadDest (BufOutf ("tmpi%c=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
			ReadSource (BufOutf ("tmpi%cb=(Int%c)(", len + 1, len + 1), ");\n");
			Outf ("if (tmpi%c>=0) {if (tmpi%cb>=0) {c=0;} else {c=2;}} else {if (tmpi%cb>=0) {c=1;} else {c=0;}}\n", len + 1, len + 1, len + 1);
			Outf ("tmpi%c-=tmpi%cb;\n", len + 1, len + 1);
			WriteDest ("", BufOutf ("(Card%c)(tmpi%c)", len, len + 1));
			Outf ("if ((Int%c)(tmpi%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
			Outf ("if ((Card%c)(tmpi%c)) {z=0;} else {z=1;}\n", len, len + 1);
			Outf ("if (((c==0)&&(tmpi%c>=0))||(c==1)) {c=1;} else {c=0;}\n", len + 1);
			Outf ("if ((tmpi%c>%n)||(tmpi%c<%n)) {v=1;} else {v=0;}\n", len + 1, sup[len], len + 1, inf[len]);
			Outf ("x=c;\n");

		} else if ((opcode & TST_MASK) == TST_DATA) {

			// Instruction TST

			Outf ("// TST\n");

			len = (opcode >> 6) & 3;

			immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, len);

			ReadSource (BufOutf ("tmpc%c=", len), ";\n");
			Outf ("if ((Int%c)(tmpc%c)<0) {n=1;} else {n=0;}\n", len, len);
			Outf ("if (tmpc%c) {z=0;} else {z=1;}\n", len);
			Outf ("v=0;\nc=0;\n");

		} else if ((opcode & BSET1_MASK) == BSET1_DATA) {

			// Instruction BSET 1

			Outf ("// BSET 1\n");

			immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, LEN_BTST);

			i8 = (opcode >> 9) & 7;

			ReadDest (BufOutf ("tmpc%c=", len_btst), ";\n");
			Outf ("if ((tmpc%c>>d%n)&1) {z=0;} else {z=1;}\n", len_btst, i8);
			WriteDest ("", BufOutf ("tmpc%c|(1<<d%n)", len_btst, i8));

		} else if ((opcode & BTST1_MASK) == BTST1_DATA) {

			// Instruction BTST 1

			Outf ("// BTST 1\n");

			immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, LEN_BTST);

			i8 = (opcode >> 9) & 7;

			ReadSource (BufOutf ("tmpc%c=", len_btst), ";\n");
			Outf ("if ((tmpc%c>>d%n)&1) {z=0;} else {z=1;}\n", len_btst, i8);

		} else if ((opcode & DIVS_MASK) == DIVS_DATA) {

			// Instruction DIVS

			Outf ("// DIVS\n");

			i8 = (opcode >> 9) & 7;

			OutEffectiveDest (0, i8, 2);
			immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, 1);

			ReadDest ("tmpi32=(Int32)(", ");\n");
			ReadSource ("tmpi16=(Int16)(", ");\n");
			Outf ("tmpi32b=tmpi32/tmpi16;\n");
			Outf ("tmpi16b=tmpi32%%tmpi16;\n");
			WriteDest ("", "(Card32)((Card16)(tmpi32b)+(((Card16)(tmpi16b))<<16))");
			Outf ("if ((Int16)(tmpi32b)<0) {n=1;} else {n=0;}\n");
			Outf ("if ((Card16)(tmpi32b)) {z=0;} else {z=1;}\n");
			Outf ("if ((tmpi32>%n)||(tmpi32<%n)) {v=1;} else {v=0;}\n", sup[1], inf[1]);
			Outf ("c=0;\n");

		} else if ((opcode & DIVU_MASK) == DIVU_DATA) {
			
			// Instruction DIVU

			Outf ("// DIVU\n");

			i8 = (opcode >> 9) & 7;

			OutEffectiveDest (0, i8, 2);
			immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, 1);

			ReadDest ("tmpc32=", ";\n");
			ReadSource ("tmpc16=", ";\n");
			Outf ("tmpc32b=tmpc32/tmpc16;\n");
			Outf ("tmpc16b=tmpc32%%tmpc16;\n");
			WriteDest ("", "(Card32)((Card16)(tmpc32b)+(tmpc16b<<16))");
			Outf ("if ((Int16)(tmpc32b)<0) {n=1;} else {n=0;}\n");
			Outf ("if ((Card16)(tmpc32b)) {z=0;} else {z=1;}\n");
			Outf ("if (tmpi32>%n) {v=1;} else {v=0;}\n", sup[1] * 2 + 1);
			Outf ("c=0;\n");

		} else if ((opcode & LEA_MASK) == LEA_DATA) {

			// Instruction LEA

			Outf ("// LEA\n");

			immediate += OutEffectiveControl ((opcode >> 3) & 7, opcode & 7, 2);

			ReadSource (BufOutf ("a%n=(Card8 *)(", (opcode >> 9) & 7), ");\n");

		} else if ((opcode & MULS_MASK) == MULS_DATA) {

			// Instruction MULS

			Outf ("// MULS\n");

			i8 = (opcode >> 9) & 7;

			OutEffectiveDest (0, i8, 2);
			immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, 1);

			ReadDest ("tmpi16=(Int16)(", ");\n");
			ReadSource ("tmpi32=tmpi16*(Int16)(", ");\n");
			WriteDest ("", "(Card32)(tmpi32)");
			Outf ("if (tmpi32<0) {n=1;} else {n=0;}\n");
			Outf ("if (tmpi32) {z=0;} else {z=1;}\n");
			Outf ("c=0;\n");

		} else if ((opcode & MULU_MASK) == MULU_DATA) {

			// Instruction MULU

			Outf ("// MULU\n");

			i8 = (opcode >> 9) & 7;

			OutEffectiveDest (0, i8, 2);
			immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, 1);

			ReadDest ("tmpc16=(Card16)(", ");\n");
			ReadSource ("tmpc32=tmpc16*", ";\n");
			WriteDest ("", "(Card32)(tmpc32)");
			Outf ("if ((Int32)(tmpc32)<0) {n=1;} else {n=0;}\n");
			Outf ("if (tmpc32) {z=0;} else {z=1;}\n");
			Outf ("c=0;\n");



		} else if ((opcode & ASLR1_MASK) == ASLR1_DATA) {

			// Instruction ASL/ASR 1

			len = (opcode >> 6) & 3;
			reg = opcode & 7;
			i8 = (opcode >> 9) & 7;
			if ((((opcode >> 5) & 1) == 0) && (i8 == 0)) {
				i8 = 8;
			}

			if ((opcode >> 8) & 1) {

				// ASL

				immediate += OutEffectiveDest (0, reg, len);

				Outf ("// ASL\n");

				ReadDest (BufOutf ("tmpi%c=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
				Outf ("v=((Card%c)(tmpi%c)>>%n)&1;\n", len, len + 1, blen[len] - 1);

				if ((opcode >> 5) & 1) {

					Outf ("tmpi%c<<=(Card8)(d%n);\n", len + 1, i8);

				} else {

					Outf ("tmpi%c<<=%n;\n", len + 1, i8);

				}

				WriteDest ("", BufOutf ("(Card%c)(tmpi%c)", len, len + 1));
				Outf ("if ((((Card%c)(tmpi%c)>>%n)&1)==v) {v=0;} else {v=1;}\n", len, len + 1, blen[len] - 1);
				Outf ("if ((Int%c)(tmpi%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
				Outf ("if ((Int%c)(tmpi%c)) {z=0;} else {z=1;}\n", len, len + 1);

				if ((opcode >> 5) & 1) {
					
					Outf ("if ((tmpi32>%n)||(tmpi32<%n)) {c=((Card%c)(tmpi%c)>>%n)&1;} else {c=0;}\n", sup[1], inf[1], len + 1, len + 1, blen[len]);
					Outf ("if ((Card8)(d%n)) x=c;\n", i8);

				} else {

					Outf ("if ((tmpi32>%n)||(tmpi32<%n)) {c=((Card%c)(tmpi%c)>>%n)&1;} else {c=0;}\n", sup[1], inf[1], len + 1, len + 1, blen[len]);
					Outf ("x=c;\n");
				}

			} else {

				// ASR

				immediate += OutEffectiveDest (0, reg, len);

				Outf ("// ASR\n");

				ReadDest (BufOutf ("tmpi%c=(Int%c)(", len, len), ");\n");
				Outf ("v=((Card%c)(tmpi%c)>>%n)&1;\n", len, len, blen[len] - 1);

				if ((opcode >> 5) & 1) {

					Outf ("if ((Card8)(d%n)) {c=((Card%c)(tmpi%c)>>((Card8)(d%n)-1))&1;} else {c=0;}\n", i8, len, len, i8);
					Outf ("tmpi%c>>=(Card8)(d%n);\n", len, i8);

				} else {

					Outf ("c=((Card%c)(tmpi%c)>>%n)&1;\n", len, len, i8 - 1);
					Outf ("tmpi%c>>=%n;\n", len, i8);

				}

				WriteDest ("", BufOutf ("(Card%c)(tmpi%c)", len, len));
				Outf ("if ((((Card%c)(tmpi%c)>>%n)&1)==v) {v=0;} else {v=1;}\n", len, len, blen[len] - 1);
				Outf ("if (tmpi%c<0) {n=1;} else {n=0;}\n", len);
				Outf ("if (tmpi%c) {z=0;} else {z=1;}\n", len);

				if ((opcode >> 5) & 1) {
					
					Outf ("if ((Card8)(d%n)) x=c;\n", i8);

				} else {

					Outf ("x=c;\n");
				}
			}

		} else if ((opcode & ROLR1_MASK) == ROLR1_DATA) {

			// Instruction ROL/ROR 1

			len = (opcode >> 6) & 3;
			reg = opcode & 7;
			i8 = (opcode >> 9) & 7;
			if ((((opcode >> 5) & 1) == 0) && (i8 == 0)) {
				i8 = 8;
			}

			if ((opcode >> 8) & 1) {

				// ROL

				immediate += OutEffectiveDest (0, reg, len);

				Outf ("// ROL\n");

				ReadDest (BufOutf ("tmpc%c=(Card%c)(", len + 1, len + 1), ");\n");

				if ((opcode >> 5) & 1) {

					Outf ("tmpc%c<<=(Card8)(d%n);\n", len + 1, i8);

				} else {

					Outf ("tmpc%c<<=%n;\n", len + 1, i8);

				}

				Outf ("tmpc%c|=tmpc%c>>%n;\n", len + 1, len + 1, blen[len]);

				WriteDest ("", BufOutf ("(Card%c)(tmpc%c)", len, len + 1));
				Outf ("v=0;\n");
				Outf ("if ((Int%c)(tmpc%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
				Outf ("if ((Card%c)(tmpc%c)) {z=0;} else {z=1;}\n", len, len + 1);

				Outf ("c=(tmpc%c>>%n)&1;\n", len + 1, blen[len]);

			} else {

				// ROR

				immediate += OutEffectiveDest (0, reg, len);

				Outf ("// ROR\n");

				ReadDest (BufOutf ("tmpc%c=(Card%c)(", len + 1, len + 1), ");\n");

				Outf ("tmpc%c<<=%n;\n", len + 1, blen[len]);

				if ((opcode >> 5) & 1) {

					Outf ("tmpc%c>>=(Card8)(d%n);\n", len + 1, i8);
					Outf ("c=(tmpc%c>>%n)&1;\n", len + 1, blen[len] - 1);


				} else {

					Outf ("tmpc%c>>=%n;\n", len + 1, i8);
					Outf ("c=(tmpc%c>>%n)&1;\n", len + 1, blen[len] - 1);

				}

				Outf ("tmpc%c|=tmpc%c<<%n;\n", len + 1, len + 1, blen[len]);
				Outf ("tmpc%c>>=%n;\n", len + 1, blen[len]);

				WriteDest ("", BufOutf ("(Card%c)(tmpc%c)", len, len + 1));
				Outf ("v=0;\n");
				Outf ("if ((Int%c)(tmpc%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
				Outf ("if ((Card%c)(tmpc%c)) {z=0;} else {z=1;}\n", len, len + 1);

			}

		} else if ((opcode & LSLR1_MASK) == LSLR1_DATA) {

			// Instruction LSL/LSR 1

			len = (opcode >> 6) & 3;
			reg = opcode & 7;
			i8 = (opcode >> 9) & 7;
			if ((((opcode >> 5) & 1) == 0) && (i8 == 0)) {
				i8 = 8;
			}

			if ((opcode >> 8) & 1) {

				// LSL

				immediate += OutEffectiveDest (0, reg, len);

				Outf ("// LSL\n");

				ReadDest (BufOutf ("tmpc%c=(Card%c)(", len + 1, len + 1), ");\n");

				if ((opcode >> 5) & 1) {

					Outf ("tmpc%c<<=(Card8)(d%n);\n", len + 1, i8);

				} else {

					Outf ("tmpc%c<<=%n;\n", len + 1, i8);

				}

				WriteDest ("", BufOutf ("(Card%c)(tmpc%c)", len, len + 1));
				Outf ("v=0;\n");
				Outf ("if ((Int%c)(tmpc%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
				Outf ("if ((Card%c)(tmpc%c)) {z=0;} else {z=1;}\n", len, len + 1);

				if ((opcode >> 5) & 1) {
					
					Outf ("c=(tmpc%c>>%n)&1;\n", len + 1, blen[len]);
					Outf ("if ((Card8)(d%n)) x=c;\n", i8);

				} else {

					Outf ("c=(tmpc%c>>%n)&1;\n", len + 1, blen[len]);
					Outf ("x=c;\n");
				}

			} else {

				// LSR

				immediate += OutEffectiveDest (0, reg, len);

				Outf ("// LSR\n");

				ReadDest (BufOutf ("tmpc%c=", len), ";\n");

				if ((opcode >> 5) & 1) {

					Outf ("if ((Card8)(d%n)) {c=(tmpc%c>>((Card8)(d%n)-1))&1;} else {c=0;}\n", i8, len, i8);
					Outf ("tmpc%c>>=(Card8)(d%n);\n", len, i8);

				} else {

					Outf ("c=(tmpc%c>>%n)&1;\n", len, i8 - 1);
					Outf ("tmpc%c>>=%n;\n", len, i8);

				}

				WriteDest ("", BufOutf ("tmpc%c", len));
				Outf ("v=0;\n");
				Outf ("if ((Int%c)(tmpc%c)<0) {n=1;} else {n=0;}\n", len, len);
				Outf ("if (tmpc%c) {z=0;} else {z=1;}\n", len);

				if ((opcode >> 5) & 1) {
					
					Outf ("if ((Card8)(d%n)) x=c;\n", i8);

				} else {

					Outf ("x=c;\n");
				}
			}

		} else if ((opcode & ADDQ_MASK) == ADDQ_DATA) {

			// Instruction ADDQ

			len = (opcode >> 6) & 3;
			i8 = (opcode >> 9) & 7;
			if (i8 == 0) {i8 = 8;};

			Outf ("// ADDQ\n");

			immediate += OutEffectiveDest ((opcode >> 3) & 7,opcode & 7, len);

			ReadDest (BufOutf ("tmpi%c=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
			Outf ("if (tmpi%c>=0) {c=0;} else {c=1;}\n", len + 1);
			Outf ("tmpi%c+=(Int%c)(%n);\n", len + 1, len + 1, i8);
			WriteDest ("", BufOutf ("(Card%c)(tmpi%c)", len, len + 1));
			Outf ("if ((Int%c)(tmpi%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
			Outf ("if ((Card%c)(tmpi%c)) {z=0;} else {z=1;}\n", len, len + 1);
			Outf ("if ((c==1)&&(tmpi%c>=0)) {c=1;} else {c=0;}\n", len + 1);
			Outf ("if ((tmpi%c>%n)||(tmpi%c<%n)) {v=1;} else {v=0;}\n", len + 1, sup[len], len + 1, inf[len]);
			Outf ("x=c;\n");

		} else if ((opcode & MOVEA_MASK) == MOVEA_DATA) {

			// Instruction MOVEA

			Outf ("// MOVEA\n");

			len = (opcode >> 12) & 3;
			if (len == 3) {
				len = 1;
			} else {
				len = 2;
			}

			immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, len);

			ReadSource (BufOutf ("a%n=(Card8 *)(", (opcode >> 9) & 7), ");\n");

		} else if ((opcode & SUBQ_MASK) == SUBQ_DATA) {

			// Instruction SUBQ

			len = (opcode >> 6) & 3;
			i8 = (opcode >> 9) & 7;
			if (i8 == 0) {i8 = 8;};

			Outf ("// SUBQ\n");

			immediate += OutEffectiveDest ((opcode >> 3) & 7,opcode & 7, len);

			ReadDest (BufOutf ("tmpi%c=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
			Outf ("if (tmpi%c>=0) {c=0;} else {c=1;}\n", len + 1);
			Outf ("tmpi%c-=(Int%c)(%n);\n", len + 1, len + 1, i8);
			WriteDest ("", BufOutf ("(Card%c)(tmpi%c)", len, len + 1));
			Outf ("if ((Int%c)(tmpi%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
			Outf ("if ((Card%c)(tmpi%c)) {z=0;} else {z=1;}\n", len, len + 1);
			Outf ("if ((c==1)||((c==0)&&(tmpi%c>=0))) {c=1;} else {c=0;}\n", len + 1);
			Outf ("if ((tmpi%c>%n)||(tmpi%c<%n)) {v=1;} else {v=0;}\n", len + 1, sup[len], len + 1, inf[len]);
			Outf ("x=c;\n");


		} else if ((opcode & ADD_MASK) == ADD_DATA) {

			// Instruction ADD/ADDA

			len = (opcode >> 6) & 3;

			reg = (opcode >> 9) & 7;
	
			if (len != 3) {

				// ADD

				Outf ("// ADD\n");

				if ((opcode >> 8) & 1) {
					immediate += OutEffectiveSource (0, reg, len);
					immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);
				} else {
					immediate += OutEffectiveDest (0, reg, len);
					immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, len);
				}

				ReadDest (BufOutf ("tmpi%c=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
				ReadSource (BufOutf ("tmpi%cb=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
				Outf ("if (tmpi%c>=0) {if (tmpi%cb>=0) {c=2;} else {c=0;}} else {if (tmpi%cb>=0) {c=0;} else {c=1;}}\n", len + 1, len + 1, len + 1);
				Outf ("tmpi%c+=tmpi%cb;\n", len + 1, len + 1);
				WriteDest ("", BufOutf ("(Card%c)(tmpi%c)", len, len + 1));
				Outf ("if ((Int%c)(tmpi%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
				Outf ("if ((Card%c)(tmpi%c)) {z=0;} else {z=1;}\n", len, len + 1);
				Outf ("if (((c==0)&&(tmpi%c>=0))||(c==1)) {c=1;} else {c=0;}\n", len + 1);
				Outf ("if ((tmpi%c>%n)||(tmpi%c<%n)) {v=1;} else {v=0;}\n", len + 1, sup[len], len + 1, inf[len]);
				Outf ("x=c;\n");

			} else {

				// ADDA

				Outf ("// ADDA\n");

				if ((opcode >> 8) & 1) {
					len = 2;
				} else {
					len = 1;
				}

				immediate += OutEffectiveDest (1, reg, len);
				immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, len);

				ReadDest (BufOutf ("tmpc32="), ";\n");
				ReadSource (BufOutf ("tmpc32+=(Int32)((Int%c)(", len), "));\n");
				WriteDest ("", "tmpc32");
			}


		} else if ((opcode & AND_MASK) == AND_DATA) {

			// Instruction AND

			reg = (opcode >> 9) & 7;
			len = (opcode >> 6) & 3;

			Outf ("// AND\n");

			if ((opcode >> 8) & 1) {
				immediate += OutEffectiveSource (0, reg, len);
				immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);
			} else {
				immediate += OutEffectiveDest (0, reg, len);
				immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, len);
			}

			ReadDest (BufOutf ("tmpc%c=", len), ";\n");
			ReadSource (BufOutf ("tmpc%c&=", len), ";\n");
			WriteDest ("", BufOutf ("tmpc%c", len));
			Outf ("if ((Int%c)(tmpc%c)<0) {n=1;} else {n=0;}\n", len, len);
			Outf ("if (tmpc%c) {z=0;} else {z=1;}\n", len);
			Outf ("v=0;\nc=0;\n");


		} else if ((opcode & B_MASK) == B_DATA) {

			// Instruction Bxx

			i8 = (opcode >> 8) & 15;

			Outf ("// Bxx\n");

			Outf ("if (");

			switch (i8) {
			case 2:
				Outf ("!c*!z");

				break;
			case 3:
				Outf ("c+z");

				break;
			case 4:
				Outf ("!c");

				break;
			case 5:
				Outf ("c");

				break;
			case 6:
				Outf ("!z");

				break;
			case 7:
				Outf ("z");

				break;
			case 8:
				Outf ("!v");

				break;
			case 9:
				Outf ("v");

				break;
			case 10:
				Outf ("!n");

				break;
			case 11:
				Outf ("n");

				break;
			case 12:
				Outf ("n*v+!n*!v");

				break;
			case 13:
				Outf ("n*!v+!n*v");

				break;
			case 14:
				Outf ("n*v*!z+!n*!v*!z");

				break;
			case 15:
				Outf ("z+n*!v+!n*v");

				break;
			}

			Outf (") goto ");

			if (opcode & 255) {

				Outf ("%l;\n", pc + 2 + (Int8)(opcode & 255));

			} else {

				Outf ("%l;\n", pc + 2 + (Int16)(Read16 (immediate)));
				immediate += 2;

			}

		} else if ((opcode & CMP_MASK) == CMP_DATA) {

			// Instruction CMP

			len = (opcode >> 6) & 3;
			reg = (opcode >> 9) & 7;

			if (len != 3) {

				immediate += OutEffectiveDest (0, reg, len);
				immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, len);

				Outf ("// CMP\n");

				ReadDest (BufOutf ("tmpi%c=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
				ReadSource (BufOutf ("tmpi%cb=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
				Outf ("if (tmpi%c>=0) {if (tmpi%cb>=0) {c=0;} else {c=2;}} else {if (tmpi%cb>=0) {c=1;} else {c=0;}}\n", len + 1, len + 1, len + 1);
				Outf ("tmpi%c-=tmpi%cb;\n", len + 1, len + 1);
				WriteDest ("//", "");
				Outf ("if ((Int%c)(tmpi%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
				Outf ("if ((Int%c)(tmpi%c)) {z=0;} else {z=1;}\n", len, len + 1);
				Outf ("if (((c==0)&&(tmpi%c>=0))||(c==1)) {c=1;} else {c=0;}\n", len + 1);
				Outf ("if ((tmpi%c>%n)||(tmpi%c<%n)) {v=1;} else {v=0;}\n", len + 1, sup[len], len + 1, inf[len]);

			} else {

				if ((opcode >> 8) & 1) {
					len = 2;
				} else {
					len = 1;
				}

				immediate += OutEffectiveDest (1, reg, len);
				immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, len);

				Outf ("// CMPA\n");

				ReadDest (BufOutf ("tmpi64=(Int64)((Int32)("), "));\n");
				ReadSource (BufOutf ("tmpi64b=(Int64)((Int%c)(", len), "));\n");
				Outf ("if (tmpi64>=0) {if (tmpi64b>=0) {c=0;} else {c=2;}} else {if (tmpi64b>=0) {c=1;} else {c=0;}}\n");
				Outf ("tmpi64-=tmpi64b;\n");
				WriteDest ("//", "");
				Outf ("if ((Int32)(tmpi64)<0) {n=1;} else {n=0;}\n");
				Outf ("if ((Int32)(tmpi64)) {z=0;} else {z=1;}\n");
				Outf ("if (((c==0)&&(tmpi64>=0))||(c==1)) {c=1;} else {c=0;}\n");
				Outf ("if ((tmpi64>%n)||(tmpi64<%n)) {v=1;} else {v=0;}\n", sup[2], inf[2]);

			}

		} else if ((opcode & OR_MASK) == OR_DATA) {

			// Instruction OR

			len = (opcode >> 6) & 3;
			reg = (opcode >> 9) & 7;

			Outf ("// OR\n");

			if ((opcode >> 8) & 1) {
				immediate += OutEffectiveSource (0, reg, len);
				immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);
			} else {
				immediate += OutEffectiveDest (0, reg, len);
				immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, len);
			}

			ReadDest (BufOutf ("tmpc%c=", len), ";\n");
			ReadSource (BufOutf ("tmpc%c|=", len, len), ";\n");
			WriteDest ("", BufOutf ("tmpc%c", len));
			Outf ("if ((Int%c)(tmpc%c)<0) {n=1;} else {n=0;}\n", len, len);
			Outf ("if (tmpc%c) {z=0;} else {z=1;}\n", len);
			Outf ("v=0;\nc=0;\n");

		} else if ((opcode & SUB_MASK) == SUB_DATA) {

			// Instruction SUB

			len = (opcode >> 6) & 3;

			reg = (opcode >> 9) & 7;
	
			if (len != 3) {

				// SUB

				Outf ("// SUB\n");

				if ((opcode >> 8) & 1) {
					immediate += OutEffectiveSource (0, reg, len);
					immediate += OutEffectiveDest ((opcode >> 3) & 7, opcode & 7, len);
				} else {
					immediate += OutEffectiveDest (0, reg, len);
					immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, len);
				}

				ReadDest (BufOutf ("tmpi%c=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
				ReadSource (BufOutf ("tmpi%cb=(Int%c)((Int%c)(", len + 1, len + 1, len), "));\n");
				Outf ("if (tmpi%c>=0) {if (tmpi%cb>=0) {c=0;} else {c=2;}} else {if (tmpi%cb>=0) {c=1;} else {c=0;}}\n", len + 1, len + 1, len + 1);
				Outf ("tmpi%c-=tmpi%cb;\n", len + 1, len + 1);
				WriteDest ("", BufOutf ("(Card%c)(tmpi%c)", len, len + 1));
				Outf ("if ((Int%c)(tmpi%c)<0) {n=1;} else {n=0;}\n", len, len + 1);
				Outf ("if ((Card%c)(tmpi%c)) {z=0;} else {z=1;}\n", len, len + 1);
				Outf ("if (((c==0)&&(tmpi%c>=0))||(c==1)) {c=1;} else {c=0;}\n", len + 1);
				Outf ("if ((tmpi%c>%n)||(tmpi%c<%n)) {v=1;} else {v=0;}\n", len + 1, sup[len], len + 1, inf[len]);
				Outf ("x=c;\n");

			} else {

				// SUBA

				Outf ("// SUBA\n");

				if ((opcode >> 8) & 1) {
					len = 2;
				} else {
					len = 1;
				}

				immediate += OutEffectiveDest (1, reg, len);
				immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, len);

				ReadDest (BufOutf ("tmpc32="), ";\n");
				ReadSource (BufOutf ("tmpc32-=(Int32)((Int%c)(", len), "));\n");
				WriteDest ("", "tmpc32");
			}

		} else if ((opcode & MOVE_MASK) == MOVE_DATA) {
	
			// Instruction MOVE

			len = (opcode >> 12) & 3;

			switch (len) {
			case 1:
				len = 0;

				break;
			case 3:
				len = 1;

				break;
			}

			Outf ("// MOVE\n");

			immediate += OutEffectiveSource ((opcode >> 3) & 7, opcode & 7, len);
			immediate += OutEffectiveDest ((opcode >> 6) & 7, (opcode >> 9) & 7, len);

			ReadDest ("//", ";\n");
			ReadSource (BufOutf ("tmpc%c=", len), ";\n");
			WriteDest ("", BufOutf ("tmpc%c", len));
		} else {

			// Instruction inconnue

			printf ("Instruction inconnue\n");
			//gets (line);
			Outf ("// Instruction inconnue\n");
		}

		pc = immediate;

		//gets (line);

		if (*line == 'Q') break;
	}

	LabelOut (pc);
	Outf (":\n");
	
	Outf ("return 0;}\n");

	delete code;
	delete data;
	delete reloc;

	// Ferme le fichier de sortie et le fichier source assembleur
	
	fclose (out_handle);
	fclose (lst_handle);

	return 0;
}

//
//	Read8 : lit 8 bits dans un fichier au format Motorola
//

Card8
Read8 (Card8 *ptr)
{
	return *ptr;
}

//
//	Read16 : lit 16 bits dans un fichier au format Motorola
//

Card16
Read16 (Card8 *ptr)
{
	return ((*ptr) << 8) + *(ptr + 1);
}

//
//	Read32 : lit 32 bits dans un fichier au format Motorola
//

Card32
Read32 (Card8 *ptr)
{
	return ((*ptr) << 24) + ((*(ptr + 1))  << 16) + ((*(ptr + 2)) << 8) + (*(ptr + 3));
}

//
//	Out : sort du texte dans le fichier de sortie
//

void
Out (char *txt)
{
	fputs (txt, out_handle);
}

//
//	Out : sort un caractère dans le fichier de sortie
//

void
Out (char car)
{
	char	tmp[1];

	tmp[0] = car;
	tmp[1] = 0;
	fputs (tmp, out_handle);
}

//
//	Outf : sort une chaîne de caractères formatée dans le fichier de sortie
//

void
Outf (char *txt ...)
{
	va_list	ap;
	char	*pt1, *pt2, buffer[1000];

	va_start (ap, txt);

	pt1 = txt;
	pt2 = txt;
	for (;;) {
		if (*pt1) {
			if (*pt1++ == '%') {
				memcpy (buffer, pt2, (size_t)(pt1 - pt2 - 1));
				buffer[pt1 - pt2 - 1] = 0;
				Out (buffer);

				switch (*pt1++) {
				case 'l':

					LabelOut (va_arg (ap, Card8 *));

					break;
				case 'n':

					NumberOut (va_arg (ap, Int32));

					break;
				case 'a':
				
					Out (va_arg (ap, char));

					break;
				case 'c':

					CardOut (va_arg (ap, Card8));

					break;
				case 'b':

					LenByteOut (va_arg (ap, Card8));

					break;
				case 'd':

					AdrOut (va_arg (ap, Card32));

					break;
				case 'i':

					NumberOut (va_arg (ap, Card32));

					break;
				case '%':

					Out ("%");

					break;
				}

				pt2 = pt1;
			}
		} else {
			break;
		}
	}

	if ((size_t)(pt1 - pt2)) {
		memcpy (buffer, pt2, (size_t)(pt1 - pt2));
		buffer[pt1 - pt2] = 0;
		Out (buffer);
	}

	va_end (ap);
}

//
//	BufOutf : renvoie un pointeur sur une chaîne de caractères formatée
//

char *
BufOutf (char *txt ...)
{
	va_list	ap;
	char	*pt1, *pt2, buffer[1000];

	va_start (ap, txt);

	strcpy (buf2, "");

	pt1 = txt;
	pt2 = txt;
	for (;;) {
		if (*pt1) {
			if (*pt1++ == '%') {
				memcpy (buffer, pt2, (size_t)(pt1 - pt2 - 1));
				buffer[pt1 - pt2 - 1] = 0;
				strcat (buf2, buffer);

				switch (*pt1++) {
				case 'l':

					strcat (buf2, StrLabel (va_arg (ap, Card8 *)));

					break;
				case 'n':

					strcat (buf2, StrNumber (va_arg (ap, Int32)));

					break;
				case 'a':
				
					strcat (buf2, StrChar (va_arg (ap, char)));

					break;
				case 'c':

					strcat (buf2, StrCard (va_arg (ap, Card8)));

					break;
				case 'b':

					strcat (buf2, StrLenByte (va_arg (ap, Card8)));

					break;
				case 'd':

					strcat (buf2, StrAdr (va_arg (ap, Card32)));

					break;
				case 'i':

					strcat (buf2, StrNumber (va_arg (ap, Card32)));

					break;
				case '%':

					strcat (buf2, "%");

					break;
				}

				pt2 = pt1;
			}
		} else {
			break;
		}
	}

	if ((size_t)(pt1 - pt2)) {
		memcpy (buffer, pt2, (size_t)(pt1 - pt2));
		buffer[pt1 - pt2] = 0;
		strcat (buf2, buffer);
	}

	va_end (ap);

	return buf2;
}

//
//	LabelOut : sort un label dans le fichier de sortie
//

void
LabelOut (Card8 *adr)
{
	char	*tmp;

	tmp = new char[50];

	tmp[0] = 'L';
	sprintf (tmp + 1, "%d", (Card32)(adr - code));
	Out (tmp);

	delete tmp;
}

//
//	OutEffectiveDest : écrit le mode d'adressage destination désiré dans
//				       le fichier de sortie renvoie le nombre d'octets qui 
//				       ont été lus en mode immédiat (après l'opcode)
//

Card32
OutEffectiveDest (Card8 mode, Card8 reg, Card8 len)
{
	Card32	nb_immediate = 0;
	Int16	i16;
	Int32	i32;

	//printf ("Destination :\n");
	//printf (" - mode : %d\n", mode);
	//printf (" - registre : %d\n", reg);

	switch (mode) {
	case 0:
		// Dn

		if (len == LEN_BTST) {
			len = 2;
			len_btst = 2;
		}

		strcpy (out[0], "");
		strcpy (out[1], BufOutf ("(Card%c)(d%n)", len, reg));
		strcpy (out[6], ";\n");
		strcpy (out[7], "");

		if (len != 2) {
			strcpy (out[5], BufOutf ("(Card%c)(d%n)=", len, reg));
		} else {
			strcpy (out[5], BufOutf ("d%n=", 
				reg));

		}

		break;
	case 1:
		// An

		if (len == LEN_BTST) {
			len = 2;
			len_btst = 2;
		}

		strcpy (out[0], "");
		strcpy (out[1], BufOutf ("(Card32)(a%n)", reg));
		strcpy (out[5], BufOutf ("a%n=(Card8 *)(", reg));
		strcpy (out[6], ");\n");
		strcpy (out[7], "");

		break;
	case 2:
		// (An)

		if (len == LEN_BTST) {
			len = 0;
			len_btst = 0;
		}

		strcpy (out[0], "");
		strcpy (out[1], BufOutf ("Read%c(a%n)", len, reg));
		strcpy (out[5], BufOutf ("Write%c(a%n,", len, reg));
		strcpy (out[6], ");\n");
		strcpy (out[7], "");

		break;
	case 3:
		// (An)+

		if (len == LEN_BTST) {
			len = 0;
			len_btst = 0;
		}

		strcpy (out[0], "");
		strcpy (out[1], BufOutf ("Read%c(a%n)", len, reg));
		strcpy (out[5], BufOutf ("Write%c(a%n,", len, reg));
		strcpy (out[6], ");\n");
		strcpy (out[7], BufOutf ("a%n+=%b;\n", reg, len));

		break;
	case 4:
		// -(An)

		if (len == LEN_BTST) {
			len = 0;
			len_btst = 0;
		}

		strcpy (out[0], BufOutf ("a%n-=%b;\n", reg, len));
		strcpy (out[1], BufOutf ("Read%c(a%n)", len, reg));
		strcpy (out[5], BufOutf ("Write%c(a%n,", len, reg));
		strcpy (out[6], ");\n");
		strcpy (out[7], "");

		break;
	case 5:
		// d(An)

		if (len == LEN_BTST) {
			len = 0;
			len_btst = 0;
		}

		i16 = Read16 (immediate);
		nb_immediate = 2;

		strcpy (out[0], "");
		strcpy (out[1], BufOutf ("Read%c(a%n+%n)", len, reg, (Int16)(i16)));
		strcpy (out[5], BufOutf ("Write%c(a%n+%n,", len, reg, (Int16)(i16)));
		strcpy (out[6], ");\n");
		strcpy (out[7], "");

		break;
	case 6:
		// d(An, Ri)

		if (len == LEN_BTST) {
			len = 0;
			len_btst = 0;
		}

		i16 = Read16 (immediate);
		nb_immediate = 2;
		
		strcpy (out[0], "");
		strcpy (out[6], ");\n");
		strcpy (out[7], "");

		if ((i16 >> 15) & 1) {
			strcpy (out[1], BufOutf ("Read%c(a%n+(Int%c)(a%n)+%n)", len, reg, 1 + ((i16 >> 11) & 1), (i16 >> 12) & 7, (Int8)(i16 & 255)));
			strcpy (out[5], BufOutf ("Write%c(a%n+(Int%c)(a%n)+%n,", len, reg, 1 + ((i16 >> 11) & 1), (i16 >> 12) & 7, (Int8)(i16 & 255)));
		} else {
			strcpy (out[1], BufOutf ("Read%c(a%n+(Int%c)(d%n)+%n)", len, reg, 1 + ((i16 >> 11) & 1), (i16 >> 12) & 7, (Int8)(i16 & 255)));
			strcpy (out[5], BufOutf ("Write%c(a%n+(Int%c)(d%n)+%n,", len, reg, 1 + ((i16 >> 11) & 1), (i16 >> 12) & 7, (Int8)(i16 & 255)));
		}

		break;
	case 7:

		switch (reg) {
		case 0:
			// xxx.W
			
			printf ("xxx.W pas implémenté\n");
			gets (line);

			break;
		case 1:
			// xxx.L

			if (len == LEN_BTST) {
				len = 0;
				len_btst = 0;
			}

			i32 = Read32 (immediate);
			nb_immediate = 4;

			if (TestRelocate (immediate)) {
				strcpy (out[0], BufOutf ("dst_pt=(Card8 *)(%d);\n", i32));
			} else {
				strcpy (out[0], BufOutf ("dst_pt=(Card8 *)(%i);\n", i32));
			}

			strcpy (out[1], BufOutf ("Read%c(dst_pt)", len));
			strcpy (out[5], BufOutf ("Write%c(dst_pt,", len));
			strcpy (out[6], ");\n");
			strcpy (out[7], "");

			break;
		case 2:
			// relatif PC

			printf ("relatif PC pas implémenté\n");
			gets (line);

			break;
		case 3:
			// relatif PC + Ri

			printf ("relatif PC + Ri pas implémenté\n");
			gets (line);

			break;
		case 4:
			// #xxx

			printf ("Donnée immédiate impossible en destination\n");
			////gets (line);

			break;
		}

		break;
	}

	return nb_immediate;
}

//
//	OutEffectiveSource : ...
//

Card32
OutEffectiveSource (Card8 mode, Card8 reg, Card8 len)
{
	Card32	nb_immediate = 0;
	Card16	i16;
	Card32	i32;

	//printf ("Source :\n");
	//printf (" - mode : %d\n", mode);
	//printf (" - registre : %d\n", reg);

	switch (mode) {
	case 0:
		// Dn

		if (len == LEN_BTST) {
			len = 2;
			len_btst = 2;
		}

		strcpy (out[2], "");
		strcpy (out[3], BufOutf ("(Card%c)(d%n)", len, reg));
		strcpy (out[4], "");

		break;
	case 1:
		// An

		if (len == LEN_BTST) {
			len = 2;
			len_btst = 2;
		}

		strcpy (out[2], "");
		strcpy (out[3], BufOutf ("(Card32)(a%n)", reg));
		strcpy (out[4], "");

		break;
	case 2:
		// (An)

		if (len == LEN_BTST) {
			len = 0;
			len_btst = 0;
		}

		strcpy (out[2], "");
		strcpy (out[3], BufOutf ("Read%c(a%n)", len, reg));
		strcpy (out[4], "");

		break;
	case 3:
		// (An)+

		if (len == LEN_BTST) {
			len = 0;
			len_btst = 0;
		}

		strcpy (out[2], "");
		strcpy (out[3], BufOutf ("Read%c(a%n)", len, reg));
		strcpy (out[4], BufOutf ("a%n+=%b;\n", reg, len));

		break;
	case 4:
		// -(An)

		if (len == LEN_BTST) {
			len = 0;
			len_btst = 0;
		}

		strcpy (out[2], BufOutf ("a%n-=%b;\n", reg, len));
		strcpy (out[3], BufOutf ("Read%c(a%n)", len, reg));
		strcpy (out[4], "");

		break;
	case 5:
		// d(An)

		if (len == LEN_BTST) {
			len = 0;
			len_btst = 0;
		}

		i16 = Read16 (immediate);
		nb_immediate = 2;

		strcpy (out[2], "");
		strcpy (out[3], BufOutf ("Read%c(a%n+%n)", len, reg, (Int16)(i16)));
		strcpy (out[4], "");

		break;
	case 6:
		// d(An, Ri)

		if (len == LEN_BTST) {
			len = 0;
			len_btst = 0;
		}

		i16 = Read16 (immediate);
		nb_immediate = 2;

		strcpy (out[2], "");
		strcpy (out[4], "");

		if ((i16 >> 15) & 1) {
			strcpy (out[3], BufOutf ("Read%c(a%n+(Int%c)(a%n)+%n)", len, reg, 1 + ((i16 >> 11) & 1), (i16 >> 12) & 7, (Int8)(i16 & 255)));
		} else {
			strcpy (out[3], BufOutf ("Read%c(a%n+(Int%c)(d%n)+%n)", len, reg, 1 + ((i16 >> 11) & 1), (i16 >> 12) & 7, (Int8)(i16 & 255)));
		}

		break;
	case 7:

		switch (reg) {
		case 0:
			// xxx.W
			
			printf ("xxx.W pas implémenté\n");
			gets (line);

			break;
		case 1:
			// xxx.L

			if (len == LEN_BTST) {
				len = 0;
				len_btst = 0;
			}

			i32 = Read32 (immediate);
			nb_immediate = 4;

			if (TestRelocate (immediate)) {
				strcpy (out[2], BufOutf ("src_pt=(Card8 *)(%d);\n", i32));
			} else {
				strcpy (out[2], BufOutf ("src_pt=(Card8 *)(%i);\n", i32));
			}

			strcpy (out[3], BufOutf ("Read%c(src_pt)", len));
			strcpy (out[4], "");
	
			break;
		case 2:
			// relatif PC

			printf ("relatif PC pas implémenté\n");
			gets (line);

			break;
		case 3:
			// relatif PC + Ri

			printf ("relatif PC + Ri pas implémenté\n");
			gets (line);

			break;
		case 4:
			// #xxx

			strcpy (out[2], "");
			strcpy (out[4], "");

			switch (len) {
			case 0:
				
				// Donnée immédiate sur 8 bits

				i16 = Read16 (immediate) & 0xff;
				nb_immediate = 2;

				strcpy (out[3], StrNumber ((Card8)(i16)));

				break;
			case 1:
								
				// Donnée immédiate sur 16 bits

				i16 = Read16 (immediate);
				nb_immediate = 2;

				strcpy (out[3], StrNumber (i16));

				break;
			case 2:
				
				// Donnée immédiate sur 32 bits

				i32 = Read32 (immediate);
				nb_immediate = 4;

				strcpy (out[3], StrNumber (i32));

				break;
			}

			break;
		}

		break;
	}

	return nb_immediate;
}

//
//	OutEffectiveControl : ...
//

Card32
OutEffectiveControl (Card8 mode, Card8 reg, Card8 len)
{
	Card32	nb_immediate = 0;
	Int16	i16;
	Int32	i32;

	//printf ("Destination :\n");
	//printf (" - mode : %d\n", mode);
	//printf (" - registre : %d\n", reg);

	switch (mode) {
	case 0:
		// Dn

		printf ("Pas en mode d'adressage \" controle\"");

		break;
	case 1:
		// An

		printf ("Pas en mode d'adressage \" controle\"");

		break;
	case 2:
		// (An)

		strcpy (out[2], "");
		strcpy (out[3], BufOutf ("a%n", reg));
		strcpy (out[4], "");

		break;
	case 3:
		// (An)+

		printf ("Pas en mode d'adressage \" controle\"");

		break;
	case 4:
		// -(An)

		printf ("Pas en mode d'adressage \" controle\"");

		break;
	case 5:
		// d(An)

		i16 = Read16 (immediate);
		nb_immediate = 2;

		strcpy (out[2], "");
		strcpy (out[3], BufOutf ("a%n+%n", reg, (Int16)(i16)));
		strcpy (out[4], "");

		break;
	case 6:
		// d(An, Ri)

		printf ("d(An, Ri) pas implémenté\n");
		Outf ("// never d(An, Ri) in control\n");

		break;
	case 7:

		switch (reg) {
		case 0:
			// xxx.W
			
			printf ("xxx.W pas implémenté\n");
			gets (line);

			break;
		case 1:
			// xxx.L

			i32 = Read32 (immediate);
			nb_immediate = 4;

			if (TestRelocate (immediate)) {
				strcpy (out[3], BufOutf ("%d; // ZZZ\n", i32));
			} else {
				strcpy (out[3], BufOutf ("%i; // never\n", i32));
			}

			strcpy (out[2], "");
			strcpy (out[4], "");

			break;
		case 2:
			// relatif PC

			printf ("relatif PC pas implémenté\n");
			gets (line);

			break;
		case 3:
			// relatif PC + Ri

			printf ("relatif PC + Ri pas implémenté\n");
			gets (line);

			break;
		case 4:
			// #xxx

			printf ("Pas en mode d'adressage \" controle\"");
			gets (line);

			break;
		}

		break;
	}

	return nb_immediate;
}

//
//	CardOut : écrit "8", "16" ou "32" dans le fichier de sortie selon la taille
//	          de l'opération
//

void
CardOut (Card8 len)
{
	switch (len) {
	case 0:
		Out ("8");
		break;
	case 1:
		Out ("16");
		break;
	case 2:
		Out ("32");
		break;
	case 3:
		Out ("64");
		break;
	}
}

//
//	LenByteOut : écrit "1", "2" ou "4" dans le fichier de sortie selon la taille
//	             de l'opération
//

void
LenByteOut (Card8 len)
{
	switch (len) {
	case 0:
		Out ("1");
		break;
	case 1:
		Out ("2");
		break;
	case 2:
		Out ("4");
		break;
	case 3:
		Out ("8");
		break;
	}

}

//
//	NumberOut : écrit un nombre signé dans le fichier de sortie
//

void
NumberOut (Int32 nb)
{
	char	*tmp;

	tmp = new char[50];

	sprintf (tmp, "%d", nb);
	Out (tmp);

	delete tmp;

}

//
//	NumberOut : écrit un nombre non signé dans le fichier de sortie
//

void
NumberOut (Card32 nb)
{
	char	*tmp;

	tmp = new char[50];

	sprintf (tmp, "0x%x", nb);
	Out (tmp);

	delete tmp;

}

//
//	AdrOut : ...
//

void
AdrOut (Card32 adr)
{
	//printf ("AdrOut : %d\n", adr);

	if (adr >= (text_size + data_size)) {
		if (adr < (text_size + data_size + bss_size)) {
			Out ("bss_adr+");
			NumberOut (adr - text_size - data_size);
		} else {
			printf ("Erreur : ce pointeur ne pointe pas dans l'une des zones autorisées\n");
			gets (line);
		}
	} else if (adr >= text_size) {
		Out ("data_adr+");
		NumberOut (adr - text_size);
	} else {
		Out ("&&L");
		NumberOut (adr);
	}
}

//
//	...
//

char *
StrChar (char chr)
{
	buf[0] = chr;
	buf[1] = 0;

	return buf;
}

//
//	...
//

char *
StrCard (Card8 len)
{
	switch (len) {
	case 0:
		strcpy (buf,"8");
		break;
	case 1:
		strcpy (buf, "16");
		break;
	case 2:
		strcpy (buf, "32");
		break;
	case 3:
		strcpy (buf, "64");
	}

	return buf;
}

//
//	...
//

char *
StrLenByte (Card8 len)
{
	switch (len) {
	case 0:
		strcpy (buf,"1");
		break;
	case 1:
		strcpy (buf, "2");
		break;
	case 2:
		strcpy (buf, "4");
		break;
	case 3:
		strcpy (buf, "8");
		break;
	}
	
	return buf;
}

//
//	...
//

char *
StrNumber (Card32 nb)
{
	sprintf (buf, "0x%x", nb);
	
	return buf;
}

//
//	...
//

char *
StrNumber (Int32 nb)
{
	sprintf (buf, "%d", nb);
	
	return buf;
}

//
//	...
//

char *
StrAdr (Card32 adr)
{
	//printf ("StrAdr : %d\n", adr);

	if (adr >= (text_size + data_size)) {
		if (adr < (text_size + data_size + bss_size)) {
			strcpy (buf, "bss_adr+");
			sprintf (buf + strlen (buf), "%d", adr - text_size - data_size);
		} else {
			printf ("Erreur : ce pointeur ne pointe pas dans l'une des zones autorisées\n");
			gets (line);
		}
	} else if (adr >= text_size) {
		strcpy (buf, "data_adr+");
		sprintf (buf + strlen(buf), "%d", adr - text_size);
	} else {
		strcpy (buf, "&&L");
		sprintf (buf + strlen (buf), "%d", adr);
	}

	return buf;
}

//
//	...
//

char *
StrLabel (Card8 *adr)
{
	buf[0] = 'L';
	sprintf (buf + 1, "%d", (Card32)(adr - code));

	return buf;
}


//
//	...
//

void
ReadDest (char *sup1, char *sup2)
{
	Out (out[0]);
	Out (sup1);
	Out (out[1]);
	Out (sup2);
}

//
//	...
//

void
ReadSource (char *sup1, char *sup2)
{
	Out (out[2]);
	Out (sup1);
	Out (out[3]);
	Out (sup2);
	Out (out[4]);
}

//
//	...
//

void
WriteDest (char *sup1, char *sup2)
{
	Out (sup1);
	Out (out[5]);
	Out (sup2);
	Out (out[6]);
	Out (out[7]);
}

//
//	Relocate : met les adresses du segment TEXT à reloger dans un tableau et
//	           sort du code C capable de reloger le segment DATA (j'ai du mal
//	           à m'exprimer...) et retourne le nombre d'adresse à reloger dans
//	           le segment TEXT
//

Card32
Relocate (Card8 *table)
{
	Card8	offset;
	Card32	i, adr;

	adr = Read32(table);	// Première adresse à reloger, offset sur 32 bits
	table += 4;
	reloc_table[0] = code + adr;
	i = 1;

	//printf ("Premiere : %d\n", adr);

	while (offset=*table++) {
		
		if (offset != 1) {
			adr += offset;

			if (adr < text_size) {
				
				// Adresse pointe dans le segment TEXT

				reloc_table[i++] = code + adr;

			} else if (adr < text_size + data_size) {

				// Adresse pointe dans le segment DATA

				//printf ("data_adr + %d\n", adr - text_size);
				//gets (line);
				Outf ("Write32(%d,(Card32)(&&L%n));\n", adr - text_size, Read32 (data + adr - text_size));

			} else {

				// Ne devrait pas arriver

				printf ("Relocate : cette adresse ne pointe ni dans TEXT ni dans DATA\n");

			}

		} else {
			adr += 0xfe;
		}

	}

	return i;
}

//
//	Teste si une adresse est à reloger ou pas
//

Bool
TestRelocate (Card8 *adr)
{
	Card32	i;

	for (i = 0; i < nb_reloc; i++) {
		if (adr = reloc_table[i]) return TRUE;
	}

	return FALSE;
}
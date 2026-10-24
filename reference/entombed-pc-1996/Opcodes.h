/*
 * opcodes.h
 *
 * Définition des masques et des données pour chaque instruction 68000
 *
 * (C) 1996 Olivier Bruchez
 */

#ifndef _OPCODES_H_
#define _OPCODES_H_

#define RTS_MASK 0xFFFF
#define RTS_DATA 0x4E75
#define DBRA_MASK 0xFFF8
#define DBRA_DATA 0x51C8
#define SWAP_MASK 0xFFF8
#define SWAP_DATA 0x4840
#define TRAP_MASK 0xFFF0
#define TRAP_DATA 0x4E40
#define BSET2_MASK 0xFFC0
#define BSET2_DATA 0x8C0
#define BTST2_MASK 0xFFC0
#define BTST2_DATA 0x800
#define EXT_MASK 0xFE38
#define EXT_DATA 0x4800
#define JMP_MASK 0xFFC0
#define JMP_DATA 0x4EC0
#define JSR_MASK 0xFFC0
#define JSR_DATA 0x4E80
#define ASLR2_MASK 0xFEC0
#define ASLR2_DATA 0xE0C0
#define LSLR2_MASK 0xFEC0
#define LSLR2_DATA 0xE2C0
#define ROLR2_MASK 0xFEC0
#define ROLR2_DATA 0xE6C0
#define ABCD_MASK 0xF1F0
#define ABCD_DATA 0xC100
#define ADDI_MASK 0xFF00
#define ADDI_DATA 0x600
#define ANDI_MASK 0xFF00
#define ANDI_DATA 0x200
#define BRA_MASK 0xFF00
#define BRA_DATA 0x6000
#define BSR_MASK 0xFF00
#define BSR_DATA 0x6100
#define CLR_MASK 0xFF00
#define CLR_DATA 0x4200
#define CMPI_MASK 0xFF00
#define CMPI_DATA 0xC00
#define EORI_MASK 0xFF00
#define EORI_DATA 0xA00
#define MOVEM_MASK 0xFB80
#define MOVEM_DATA 0x4880
#define NEG_MASK 0xFF00
#define NEG_DATA 0x4400
#define ORI_MASK 0xFF00
#define ORI_DATA 0x0
#define SUBI_MASK 0xFF00
#define SUBI_DATA 0x400
#define TST_MASK 0xFF00
#define TST_DATA 0x4A00
#define BSET1_MASK 0xF1C0
#define BSET1_DATA 0x1C0
#define BTST1_MASK 0xF1C0
#define BTST1_DATA 0x100
#define DIVS_MASK 0xF1C0
#define DIVS_DATA 0x81C0
#define DIVU_MASK 0xF1C0
#define DIVU_DATA 0x80C0
#define LEA_MASK 0xF1C0
#define LEA_DATA 0x41C0
#define MULS_MASK 0xF1C0
#define MULS_DATA 0xC1C0
#define MULU_MASK 0xF1C0
#define MULU_DATA 0xC0C0
#define ASLR1_MASK 0xF018
#define ASLR1_DATA 0xE000
#define LSLR1_MASK 0xF018
#define LSLR1_DATA 0xE008
#define ROLR1_MASK 0xF018
#define ROLR1_DATA 0xE018
#define ADDQ_MASK 0xF100
#define ADDQ_DATA 0x5000
#define MOVEA_MASK 0xC1C0
#define MOVEA_DATA 0x40
#define SUBQ_MASK 0xF100
#define SUBQ_DATA 0x5100
#define ADD_MASK 0xF000
#define ADD_DATA 0xD000
#define AND_MASK 0xF000
#define AND_DATA 0xC000
#define B_MASK 0xF000
#define B_DATA 0x6000
#define CMP_MASK 0xF000
#define CMP_DATA 0xB000
#define OR_MASK 0xF000
#define OR_DATA 0x8000
#define SUB_MASK 0xF000
#define SUB_DATA 0x9000
#define MOVE_MASK 0xC000
#define MOVE_DATA 0x0

#endif /* _OPCODES_H_ */

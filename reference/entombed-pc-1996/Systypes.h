/*
 *	systypes.h
 *
 *	System types for WS
 *
 *	(C)	1995 Erik BRUCHEZ
 *
 */

#ifndef	_SYSTYPES_H_
#define	_SYSTYPES_H_

typedef	unsigned char	Card8;
typedef	unsigned short	Card16;
typedef	unsigned int	Card32;

typedef	signed char		Int8;
typedef	signed short	Int16;
typedef	signed int		Int32;

typedef double			Real;

typedef	unsigned char	Bool;

#define	FALSE			0
#define	TRUE			1

typedef Card32 Address;
typedef Card32 Size;
typedef Card32 Count;

#ifndef NULL
#define NULL ((void*)(0))
#endif

#endif /* _SYSTYPES_H_ */



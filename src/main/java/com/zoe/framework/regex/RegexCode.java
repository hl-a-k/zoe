package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexCode.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// This RegexCode class is internal to the regular expression package.
// It provides operator constants for use by the Builder and the Machine.

// Implementation notes:
//
// Regexps are built into RegexCodes, which contain an operation array,
// a string table, and some constants.
//
// Each operation is one of the codes below, followed by the integer
// operands specified for each op.
//
// Strings and sets are indices into a string table.




public final class RegexCode
{
	// the following primitive operations come directly from the parser

	// lef/back operands        description

	public static final int Onerep = 0; // lef,back char,min,max    a {n}
	public static final int Notonerep = 1; // lef,back char,min,max    .{n}
	public static final int Setrep = 2; // lef,back set,min,max     [\d]{n}

	public static final int Oneloop = 3; // lef,back char,min,max    a {,n}
	public static final int Notoneloop = 4; // lef,back char,min,max    .{,n}
	public static final int Setloop = 5; // lef,back set,min,max     [\d]{,n}

	public static final int Onelazy = 6; // lef,back char,min,max    a {,n}?
	public static final int Notonelazy = 7; // lef,back char,min,max    .{,n}?
	public static final int Setlazy = 8; // lef,back set,min,max     [\d]{,n}?

	public static final int One = 9; // lef      char            a
	public static final int Notone = 10; // lef      char            [^a]
	public static final int Set = 11; // lef      set             [a-z\s]  \w \s \d

	public static final int Multi = 12; // lef      string          abcd
	public static final int Ref = 13; // lef      group           \#

	public static final int Bol = 14; //                          ^
	public static final int Eol = 15; //                          $
	public static final int Boundary = 16; //                          \b
	public static final int Nonboundary = 17; //                          \B
	public static final int Beginning = 18; //                          \A
	public static final int Start = 19; //                          \G
	public static final int EndZ = 20; //                          \Z
	public static final int End = 21; //                          \Z

	public static final int Nothing = 22; //                          Reject!

	// primitive control structures

	public static final int Lazybranch = 23; // back     jump            straight first
	public static final int Branchmark = 24; // back     jump            branch first for loop
	public static final int Lazybranchmark = 25; // back     jump            straight first for loop
	public static final int Nullcount = 26; // back     val             set counter, null mark
	public static final int Setcount = 27; // back     val             set counter, make mark
	public static final int Branchcount = 28; // back     jump,limit      branch++ if zero<=c<limit
	public static final int Lazybranchcount = 29; // back     jump,limit      same, but straight first
	public static final int Nullmark = 30; // back                     save position
	public static final int Setmark = 31; // back                     save position
	public static final int Capturemark = 32; // back     group           define group
	public static final int Getmark = 33; // back                     recall position
	public static final int Setjump = 34; // back                     save backtrack state
	public static final int Backjump = 35; //                          zap back to saved state
	public static final int Forejump = 36; //                          zap backtracking state
	public static final int Testref = 37; //                          backtrack if ref undefined
	public static final int Goto = 38; //          jump            just go

	public static final int Prune = 39; //                          prune it baby
	public static final int Stop = 40; //                          done!

	public static final int ECMABoundary = 41; //                          \b
	public static final int NonECMABoundary = 42; //                          \B

	// modifiers for alternate modes

	public static final int Mask = 63; // Mask to get unmodified ordinary operator
	public static final int Rtl = 64; // bit to indicate that we're reverse scanning.
	public static final int Back = 128; // bit to indicate that we're backtracking.
	public static final int Back2 = 256; // bit to indicate that we're backtracking on a second branch.
	public static final int Ci = 512; // bit to indicate that we're case-insensitive.

	// the code

	public int[] _codes; // the code
	public String[] _strings; // the string/set table
	// not used! internal int[]           _sparseIndex;           // a list of the groups that are used
	public int _trackcount; // how many instructions use backtracking
	public java.util.Hashtable _caps; // mapping of user group numbers -> impl group slots
	public int _capsize; // number of impl group slots
	public RegexPrefix _fcPrefix; // the set of candidate first characters (may be null)
	public RegexBoyerMoore _bmPrefix; // the fixed prefix string as a Boyer-Moore machine (may be null)
	public int _anchors; // the set of zero-length start anchors (RegexFCD.Bol, etc)
	public boolean _rightToLeft; // true if right to left

	// optimizations

	// constructor

	public RegexCode(int[] codes, java.util.ArrayList<String> stringlist, int trackcount, java.util.Hashtable caps, int capsize, RegexBoyerMoore bmPrefix, RegexPrefix fcPrefix, int anchors, boolean rightToLeft)
	{
		_codes = codes;
		_strings = new String[stringlist.size()];
		_trackcount = trackcount;
		_caps = caps;
		_capsize = capsize;
		_bmPrefix = bmPrefix;
		_fcPrefix = fcPrefix;
		_anchors = anchors;
		_rightToLeft = rightToLeft;
		//stringlist.CopyTo(0, _strings, 0, stringlist.size());
		//System.arraycopy(stringlist,0,_strings,0,stringlist.size());
		int i = 0;
		for (String str : stringlist) {
			_strings[i++] = str;
		}
	}

	public static boolean OpcodeBacktracks(int Op)
	{
		Op &= Mask;

		switch (Op)
		{
			case Oneloop:
			case Notoneloop:
			case Setloop:
			case Onelazy:
			case Notonelazy:
			case Setlazy:
			case Lazybranch:
			case Branchmark:
			case Lazybranchmark:
			case Nullcount:
			case Setcount:
			case Branchcount:
			case Lazybranchcount:
			case Setmark:
			case Capturemark:
			case Getmark:
			case Setjump:
			case Backjump:
			case Forejump:
//C# TO JAVA CONVERTER TODO TASK: There is no 'goto' in Java:
			case Goto:
				return true;

			default:
				return false;
		}
	}

	public static int OpcodeSize(int Opcode)
	{
		Opcode &= Mask;

		switch (Opcode)
		{
			case Nothing:
			case Bol:
			case Eol:
			case Boundary:
			case Nonboundary:
			case ECMABoundary:
			case NonECMABoundary:
			case Beginning:
			case Start:
			case EndZ:
			case End:

			case Nullmark:
			case Setmark:
			case Getmark:
			case Setjump:
			case Backjump:
			case Forejump:
			case Stop:

				return 1;

			case One:
			case Notone:
			case Multi:
			case Ref:
			case Testref:


//C# TO JAVA CONVERTER TODO TASK: There is no 'goto' in Java:
			case Goto:
			case Nullcount:
			case Setcount:
			case Lazybranch:
			case Branchmark:
			case Lazybranchmark:
			case Prune:
			case Set:

				return 2;

			case Capturemark:
			case Branchcount:
			case Lazybranchcount:

			case Onerep:
			case Notonerep:
			case Oneloop:
			case Notoneloop:
			case Onelazy:
			case Notonelazy:
			case Setlazy:
			case Setrep:
			case Setloop:

				return 3;

			default:

				throw MakeException(SR.GetString(SR.UnexpectedOpcode, (new Integer(Opcode)).toString()));
		}
	}

	public static IllegalArgumentException MakeException(String message)
	{
		return new IllegalArgumentException(message);
	}

}
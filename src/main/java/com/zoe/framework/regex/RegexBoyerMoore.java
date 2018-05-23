package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexBoyerMoore.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// The RegexBoyerMoore object precomputes the Boyer-Moore
// tables for fast string scanning. These tables allow
// you to scan for the first occurance of a string within
// a large body of text without examining every character.
// The performance of the heuristic depends on the actual
// string and the text being searched, but usually, the longer
// the string that is being searched for, the fewer characters
// need to be examined.


import java.util.Locale;

public final class RegexBoyerMoore
{
	public int[] _positive;
	public int[] _negativeASCII;
	public int[][] _negativeUnicode;
	public String _pattern;
	public int _lowASCII;
	public int _highASCII;
	public boolean _rightToLeft;
	public boolean _caseInsensitive;
	public Locale _culture;

	public static final int infinite = 0x7FFFFFFF;

	/*
	 * Constructs a Boyer-Moore state machine for searching for the string
	 * pattern. The string must not be zero-length.
	 */
	public RegexBoyerMoore(String pattern, boolean caseInsensitive, boolean rightToLeft, Locale culture)
	{
		/*
		 * Sorry,  you just can't use Boyer-Moore to find an empty pattern.
		 * We're doing this for your own protection. (Really, for speed.)
		 */
		Debug.Assert(pattern.length() != 0, "RegexBoyerMoore called with an empty string.  This is bad for perf");

		int beforefirst;
		int last;
		int bump;
		int examine;
		int scan;
		int match;
		char ch;

		// We do the ToLower character by character for consistency.  With surrogate chars, doing
		// a ToLower on the entire string could actually change the surrogate pair.  This is more correct
		// linguistically, but since Regex doesn't support surrogates, it's more important to be 
		// consistent. 
		if (caseInsensitive)
		{
			StringBuilder sb = new StringBuilder(pattern.length());
			for (int i = 0; i < pattern.length(); i++)
			{
				sb.append(Character.toLowerCase(pattern.charAt(i)));
			}
			pattern = sb.toString();
		}

		_pattern = pattern;
		_rightToLeft = rightToLeft;
		_caseInsensitive = caseInsensitive;
		_culture = culture;

		if (!rightToLeft)
		{
			beforefirst = -1;
			last = pattern.length() - 1;
			bump = 1;
		}
		else
		{
			beforefirst = pattern.length();
			last = 0;
			bump = -1;
		}

		/*
		 * PART I - the good-suffix shift table
		 * 
		 * compute the positive requirement:
		 * if char "i" is the first one from the right that doesn't match,
		 * then we know the matcher can advance by _positive[i].
		 *
		 * <STRIP>  This algorithm appears to be a simplified variant of the 
		 *          standard Boyer-Moore good suffix calculation.  It could
		 *          be one of D.M. Sunday's variations, but I have not found which one.
		 * </STRIP>
		 * <


*/
		_positive = new int[pattern.length()];

		examine = last;
		ch = pattern.charAt(examine);
		_positive[examine] = bump;
		examine -= bump;

		//@czc
		boolean OuterloopBreak = false;
		for (;;)
		{
			// find an internal char (examine) that matches the tail

			for (;;)
			{
				if (examine == beforefirst)
				{
//C# TO JAVA CONVERTER TODO TASK: There is no 'goto' in Java:
					//goto OuterloopBreak;
					OuterloopBreak = true;
					break;
				}
				if (pattern.charAt(examine) == ch)
				{
					break;
				}
				examine -= bump;
			}

			if(OuterloopBreak){
				break;
			}

			match = last;
			scan = examine;

			// find the length of the match

			for (;;)
			{
				if (scan == beforefirst || pattern.charAt(match) != pattern.charAt(scan))
				{
					// at the end of the match, note the difference in _positive
					// this is not the length of the match, but the distance from the internal match
					// to the tail suffix. 
					if (_positive[match] == 0)
					{
						_positive[match] = match - scan;
					}

					// System.Diagnostics.Debug.WriteLine("Set positive[" + match + "] to " + (match - scan));

					break;
				}

				scan -= bump;
				match -= bump;
			}

			examine -= bump;
		}

		//OuterloopBreak:

		match = last - bump;

		// scan for the chars for which there are no shifts that yield a different candidate

		/* <STRIP>
		 *  The inside of the if statement used to say 
		 *  "_positive[match] = last - beforefirst;"
		 *  I've changed it to the below code.  This
		 *  is slightly less agressive in how much we skip, but at worst it 
		 *  should mean a little more work rather than skipping a potential
		 *  match.
		 * </STRIP>
		 */
		while (match != beforefirst)
		{
			if (_positive[match] == 0)
			{
				_positive[match] = bump;
			}

			match -= bump;
		}

		//System.Diagnostics.Debug.WriteLine("good suffix shift table:");
		//for (int i=0; i<_positive.Length; i++)
		//    System.Diagnostics.Debug.WriteLine("\t_positive[" + i + "] = " + _positive[i]);


		/*
		 * PART II - the bad-character shift table
		 * 
		 * compute the negative requirement:
		 * if char "ch" is the reject character when testing position "i",
		 * we can slide up by _negative[ch];
		 * (_negative[ch] = str.Length - 1 - str.LastIndexOf(ch))
		 *
		 * the lookup table is divided into ASCII and Unicode portions;
		 * only those parts of the Unicode 16-bit code set that actually
		 * appear in the string are in the table. (Maximum size with
		 * Unicode is 65K; ASCII only case is 512 bytes.)
		 */

		_negativeASCII = new int[128];

		for (int i = 0; i < 128; i++)
		{
			_negativeASCII[i] = last - beforefirst;
		}

		_lowASCII = 127;
		_highASCII = 0;

		for (examine = last; examine != beforefirst; examine -= bump)
		{
			ch = pattern.charAt(examine);

			if (ch < 128)
			{
				if (_lowASCII > ch)
				{
					_lowASCII = ch;
				}

				if (_highASCII < ch)
				{
					_highASCII = ch;
				}

				if (_negativeASCII[ch] == last - beforefirst)
				{
					_negativeASCII[ch] = last - examine;
				}
			}
			else
			{
//C# TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
				int i = ch >> 8;
				int j = ch & 0xFF;

				if (_negativeUnicode == null)
				{
					_negativeUnicode = new int[256][];
				}

				if (_negativeUnicode[i] == null)
				{
					int[] newarray = new int[256];

					for (int k = 0; k < 256; k++)
					{
						newarray[k] = last - beforefirst;
					}

					if (i == 0)
					{
						System.arraycopy(_negativeASCII, 0, newarray, 0, 128);
						_negativeASCII = newarray;
					}

					_negativeUnicode[i] = newarray;
				}

				if (_negativeUnicode[i][j] == last - beforefirst)
				{
					_negativeUnicode[i][j] = last - examine;
				}
			}
		}
	}

	private boolean MatchPattern(String text, int index)
	{
			if (_caseInsensitive)
			{
				if (text.length() - index < _pattern.length())
				{
					return false;
				}

				//@czc
				/*TextInfo textinfo = _culture.TextInfo;
				for (int i = 0; i < _pattern.length(); i++)
				{
					Debug.Assert(textinfo.toLowerCase(_pattern.charAt(i)).equals(_pattern.charAt(i)), "pattern should be converted to lower case in constructor!");
					if (!textinfo.toLowerCase(text.charAt(index + i)).equals(_pattern.charAt(i)))
					{
						return false;
					}
				}*/
				for (int i = 0; i < _pattern.length(); i++) {
					if (Character.toLowerCase(text.charAt(index + i)) != _pattern
							.charAt(i)) {
						return false;
					}
				}
				return true;
			}
			else
			{
				//@czc
				//return (0 == String.CompareOrdinal(_pattern, 0, text, index, _pattern.length()));
				return _pattern.equals(text.substring(index, index + _pattern.length()));
			}
	}

	/*
	 * When a regex is anchored, we can do a quick IsMatch test instead of a Scan
	 */
	public boolean IsMatch(String text, int index, int beglimit, int endlimit)
	{

		if (!_rightToLeft)
		{
			if (index < beglimit || endlimit - index < _pattern.length())
			{
				return false;
			}

			return MatchPattern(text, index);
		}
		else
		{
			if (index > endlimit || index - beglimit < _pattern.length())
			{
				return false;
			}

			return MatchPattern(text, index - _pattern.length());
		}
	}


	/*
	 * Scan uses the Boyer-Moore algorithm to find the first occurrance
	 * of the specified string within text, beginning at index, and
	 * constrained within beglimit and endlimit.
	 *
	 * The direction and case-sensitivity of the match is determined
	 * by the arguments to the RegexBoyerMoore constructor.
	 */
	public int Scan(String text, int index, int beglimit, int endlimit)
	{
		int test;
		int test2;
		int match;
		int startmatch;
		int endmatch;
		int advance;
		int defadv;
		int bump;
		char chMatch;
		char chTest;
		int[] unicodeLookup;

		if (!_rightToLeft)
		{
			defadv = _pattern.length();
			startmatch = _pattern.length() - 1;
			endmatch = 0;
			test = index + defadv - 1;
			bump = 1;
		}
		else
		{
			defadv = -_pattern.length();
			startmatch = 0;
			endmatch = -defadv - 1;
			test = index + defadv;
			bump = -1;
		}

		chMatch = _pattern.charAt(startmatch);

		for (;;)
		{
			if (test >= endlimit || test < beglimit)
			{
				return -1;
			}

			chTest = text.charAt(test);

			if (_caseInsensitive)
			{
				chTest = Character.toLowerCase(chTest);
			}

			if (chTest != chMatch)
			{
				if (chTest < 128)
				{
					advance = _negativeASCII[chTest];
				}
//C# TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
				else if (null != _negativeUnicode && (null != (unicodeLookup = _negativeUnicode[chTest >> 8])))
				{
					advance = unicodeLookup[chTest & 0xFF];
				}
				else
				{
					advance = defadv;
				}

				test += advance;
			}
			else // if (chTest == chMatch)
			{
				test2 = test;
				match = startmatch;

				for (;;)
				{
					if (match == endmatch)
					{
						return (_rightToLeft ? test2 + 1 : test2);
					}

					match -= bump;
					test2 -= bump;

					chTest = text.charAt(test2);

					if (_caseInsensitive)
					{
						chTest = Character.toLowerCase(chTest);
					}

					if (chTest != _pattern.charAt(match))
					{
						advance = _positive[match];
						if ((chTest & 0xFF80) == 0)
						{
							test2 = (match - startmatch) + _negativeASCII[chTest];
						}
//C# TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
						else if (null != _negativeUnicode && (null != (unicodeLookup = _negativeUnicode[chTest >> 8])))
						{
							test2 = (match - startmatch) + unicodeLookup[chTest & 0xFF];
						}
						else
						{
							test += advance;
							break;
						}

						if (_rightToLeft ? test2 < advance : test2 > advance)
						{
							advance = test2;
						}

						test += advance;
						break;
					}
				}
			}
		}
	}

	/*
	 * Used when dumping for debugging.
	 */
	@Override
	public String toString()
	{
		return _pattern;
	}
}
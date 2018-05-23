package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexMatch.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// Match is the result class for a regex search.
// It returns the location, length, and substring for
// the entire match as well as every captured group.

// Match is also used during the search to keep track of each capture for each group.  This is
// done using the "_matches" array.  _matches[x] represents an array of the captures for group x.  
// This array consists of start and length pairs, and may have empty entries at the end.  _matchcount[x] 
// stores how many captures a group has.  Note that _matchcount[x]*2 is the length of all the valid
// values in _matches.  _matchcount[x]*2-2 is the Start of the last capture, and _matchcount[x]*2-1 is the
// Length of the last capture
//
// For example, if group 2 has one capture starting at position 4 with length 6, 
// _matchcount[2] == 1
// _matches[2][0] == 4
// _matches[2][1] == 6
//
// Values in the _matches array can also be negative.  This happens when using the balanced match 
// construct, "(?<start-end>...)".  When the "end" group matches, a capture is added for both the "start" 
// and "end" groups.  The capture added for "start" receives the negative values, and these values point to 
// the next capture to be balanced.  They do NOT point to the capture that "end" just balanced out.  The negative 
// values are indices into the _matches array transformed by the formula -3-x.  This formula also untransforms. 
// 


import java.io.Serializable;

/** <devdoc>
	<p>
	   Represents 
		  the results from a single regular expression match.
	   </p>
	</devdoc>
*/
public class Match extends Group implements Serializable
{
	public static Match _empty = new Match(null, 1, "", 0, 0, 0);
	public GroupCollection _groupcoll;

	// input to the match
	public Regex _regex;
	public int _textbeg;
	public int _textpos;
	public int _textend;
	public int _textstart;

	// output from the match
	public int[][] _matches;
	public int[] _matchcount;
	public boolean _balancing; // whether we've done any balancing with this match.  If we
													// have done balancing, we'll need to do extra work in Tidy().

	/** <devdoc>
		<p>
		   Returns an empty Match object.
		</p>
	 </devdoc>
	*/
	public static Match getEmpty()
	{
		return _empty;
	}

	/*
	 * Nonpublic constructor
	 */
	public Match(Regex regex, int capcount, String text, int begpos, int len, int startpos)
	{
		super(text, new int[2], 0);

		_regex = regex;
		_matchcount = new int[capcount];

		_matches = new int[capcount][];
		_matches[0] = _caps;
		_textbeg = begpos;
		_textend = begpos + len;
		_textstart = startpos;
		_balancing = false;

		// No need for an exception here.  This is only called internally, so we'll use an Assert instead
		Debug.Assert(!(_textbeg < 0 || _textstart < _textbeg || _textend < _textstart || _text.length() < _textend), "The parameters are out of range.");
	}

	/*
	 * Nonpublic set-text method
	 */
	public void Reset(Regex regex, String text, int textbeg, int textend, int textstart)
	{
		_regex = regex;
		_text = text;
		_textbeg = textbeg;
		_textend = textend;
		_textstart = textstart;

		for (int i = 0; i < _matchcount.length; i++)
		{
			_matchcount[i] = 0;
		}

		_balancing = false;
	}

	/** <devdoc>
		<p>[To be supplied.]</p>
	 </devdoc>
	*/
	public GroupCollection getGroups()
	{
		if (_groupcoll == null)
		{
			_groupcoll = new GroupCollection(this, null);
		}

		return _groupcoll;
	}

	/*
	 * Returns the next match
	 */
	/** <devdoc>
		<p>Returns a new Match with the results for the next match, starting
		   at the position at which the last match ended (at the character beyond the last
		   matched character).</p>
	 </devdoc>
	*/
	public final Match NextMatch()
	{
		if (_regex == null)
		{
			return this;
		}

		return _regex.Run(false, _length, _text, _textbeg, _textend - _textbeg, _textpos);
	}


	/*
	 * Return the result string (using the replacement pattern)
	 */
	/** <devdoc>
		<p>
		   Returns the expansion of the passed replacement pattern. For
		   example, if the replacement pattern is ?$1$2?, Result returns the concatenation
		   of Group(1).ToString() and Group(2).ToString().
		</p>
	 </devdoc>
	*/
	public String Result(String replacement)
	{
		RegexReplacement repl;

		if (replacement == null)
		{
			throw new IllegalArgumentException("replacement");
		}

		if (_regex == null)
		{
			throw new UnsupportedOperationException(SR.GetString(SR.NoResultOnFailed));
		}

		repl = (RegexReplacement)_regex.replref.Get();

		if (repl == null || !repl.getPattern().equals(replacement))
		{
			repl = RegexParser.ParseReplacement(replacement, _regex.caps, _regex.capsize, _regex.capnames, _regex.roptions);
			_regex.replref.Cache(repl);
		}

		return repl.Replacement(this);
	}

	/*
	 * Used by the replacement code
	 */
	public String GroupToStringImpl(int groupnum)
	{
		int c = _matchcount[groupnum];
		if (c == 0)
		{
			return "";
		}

		int [] matches = _matches[groupnum];

		return DotNetToJavaStringHelper.substring(_text, matches[(c - 1) * 2], matches[(c * 2) - 1]);
	}

	/*
	 * Used by the replacement code
	 */
	public final String LastGroupToStringImpl()
	{
		return GroupToStringImpl(_matchcount.length - 1);
	}


	/*
	 * Convert to a thread-safe object by precomputing cache contents
	 */
	/** <devdoc>
		<p>
		   Returns a Match instance equivalent to the one supplied that is safe to share
		   between multiple threads.
		</p>
	 </devdoc>
	*/
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [HostProtection(Synchronization=true)] public static Match Synchronized(Match inner)
	public static Match Synchronized(Match inner)
	{
		if (inner == null)
		{
			throw new IllegalArgumentException("inner");
		}

		int numgroups = inner._matchcount.length;

		// Populate all groups by looking at each one
		for (int i = 0; i < numgroups; i++)
		{
			Group group = inner.getGroups().getItem(i);

			// Depends on the fact that Group.Synchronized just
			// operates on and returns the same instance
			com.zoe.framework.regex.Group.Synchronized(group);
		}

		return inner;
	}

	/*
	 * Nonpublic builder: add a capture to the group specified by "cap"
	 */
	public void AddMatch(int cap, int start, int len)
	{
		int capcount;

		if (_matches[cap] == null)
		{
			_matches[cap] = new int[2];
		}

		capcount = _matchcount[cap];

		if (capcount * 2 + 2 > _matches[cap].length)
		{
			int[] oldmatches = _matches[cap];
			int[] newmatches = new int[capcount * 8];
			for (int j = 0; j < capcount * 2; j++)
			{
				newmatches[j] = oldmatches[j];
			}
			_matches[cap] = newmatches;
		}

		_matches[cap][capcount * 2] = start;
		_matches[cap][capcount * 2 + 1] = len;
		_matchcount[cap] = capcount + 1;
	}

	/*
	 * Nonpublic builder: Add a capture to balance the specified group.  This is used by the 
	                      balanced match construct. (?<foo-foo2>...)

	   If there were no such thing as backtracking, this would be as simple as calling RemoveMatch(cap).
	   However, since we have backtracking, we need to keep track of everything. 
	 */
	public void BalanceMatch(int cap)
	{
		int capcount;
		int target;

		_balancing = true;

		// we'll look at the last capture first
		capcount = _matchcount[cap];
		target = capcount * 2 - 2;

		// first see if it is negative, and therefore is a reference to the next available
		// capture group for balancing.  If it is, we'll reset target to point to that capture.
		if (_matches[cap][target] < 0)
		{
			target = -3 - _matches[cap][target];
		}

		// move back to the previous capture
		target -= 2;

		// if the previous capture is a reference, just copy that reference to the end.  Otherwise, point to it. 
		if (target >= 0 && _matches[cap][target] < 0)
		{
			AddMatch(cap, _matches[cap][target], _matches[cap][target + 1]);
		}
		else
		{
			AddMatch(cap, -3 - target, -4 - target); // == -3 - (target + 1)
		}

	}

	/*
	 * Nonpublic builder: removes a group match by capnum
	 */
	public void RemoveMatch(int cap)
	{
		_matchcount[cap]--;
	}

	/*
	 * Nonpublic: tells if a group was matched by capnum
	 */
	public boolean IsMatched(int cap)
	{
		return cap < _matchcount.length && _matchcount[cap] > 0 && _matches[cap][_matchcount[cap] * 2 - 1] != (-3 + 1);
	}

	/*
	 * Nonpublic: returns the index of the last specified matched group by capnum
	 */
	public int MatchIndex(int cap)
	{
		int i = _matches[cap][_matchcount[cap] * 2 - 2];
		if (i >= 0)
		{
			return i;
		}

		return _matches[cap][-3 - i];
	}

	/*
	 * Nonpublic: returns the length of the last specified matched group by capnum
	 */
	public int MatchLength(int cap)
	{
		int i = _matches[cap][_matchcount[cap] * 2 - 1];
		if (i >= 0)
		{
			return i;
		}

		return _matches[cap][-3 - i];
	}

	/*
	 * Nonpublic: tidy the match so that it can be used as an immutable result
	 */
	public void Tidy(int textpos)
	{
		int[] interval;

		interval = _matches[0];
		_index = interval[0];
		_length = interval[1];
		_textpos = textpos;
		_capcount = _matchcount[0];

		if (_balancing)
		{
			// The idea here is that we want to compact all of our unbalanced captures.  To do that we
			// use j basically as a count of how many unbalanced captures we have at any given time 
			// (really j is an index, but j/2 is the count).  First we skip past all of the real captures
			// until we find a balance captures.  Then we check each subsequent entry.  If it's a balance
			// capture (it's negative), we decrement j.  If it's a real capture, we increment j and copy 
			// it down to the last free position. 
			for (int cap = 0; cap < _matchcount.length; cap++)
			{
				int limit;
				int[] matcharray;

				limit = _matchcount[cap] * 2;
				matcharray = _matches[cap];

				int i = 0;
				int j;

				for (i = 0; i < limit; i++)
				{
					if (matcharray[i] < 0)
					{
						break;
					}
				}

				for (j = i; i < limit; i++)
				{
					if (matcharray[i] < 0)
					{
						// skip negative values
						j--;
					}
					else
					{
						// but if we find something positive (an actual capture), copy it back to the last 
						// unbalanced position. 
						if (i != j)
						{
							matcharray[j] = matcharray[i];
						}
						j++;
					}
				}

				_matchcount[cap] = j / 2;
			}

			_balancing = false;
		}
	}

}
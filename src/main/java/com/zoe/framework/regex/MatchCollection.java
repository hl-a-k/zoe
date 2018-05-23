package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexMatchCollection.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// The MatchCollection lists the successful matches that
// result when searching a string for a regular expression.




/*
 * This collection returns a sequence of successful match results, either
 * from GetMatchCollection() or GetExecuteCollection(). It stops when the
 * first failure is encountered (it does not return the failed match).
 */

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;

/** <devdoc>
	<p>
	   Represents the set of names appearing as capturing group
	   names in a regular expression.
	</p>
 </devdoc>
*/
//C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the methods implemented will need adjustment:
public class MatchCollection extends java.util.AbstractCollection<Match> implements Serializable
{
	public Regex _regex;
	public java.util.ArrayList _matches;
	public boolean _done;
	public String _input;
	public int _beginning;
	public int _length;
	public int _startat;
	public int _prevlen;

	private static int infinite = 0x7FFFFFFF;

	/*
	 * Nonpublic constructor
	 */
	public MatchCollection(Regex regex, String input, int beginning, int length, int startat)
	{
		if (startat < 0 || startat > input.length())
		{
			throw new IllegalArgumentException("startat ：" + SR.GetString(SR.BeginIndexNotNegative));
		}

		_regex = regex;
		_input = input;
		_beginning = beginning;
		_length = length;
		_startat = startat;
		_prevlen = -1;
		_matches = new java.util.ArrayList();
		_done = false;
	}

	public final Match GetMatch(int i)
	{
		if (i < 0)
		{
			return null;
		}

		if (_matches.size() > i)
		{
			return (Match)_matches.get(i);
		}

		if (_done)
		{
			return null;
		}

		Match match;

		do
		{
			match = _regex.Run(false, _prevlen, _input, _beginning, _length, _startat);

			if (!match.getSuccess())
			{
				_done = true;
				return null;
			}

			_matches.add(match);

			_prevlen = match._length;
			_startat = match._textpos;

		} while (_matches.size() <= i);

		return match;
	}

	/** <devdoc>
		<p>
		   Returns the number of captures.
		</p>
	 </devdoc>
	*/
	public final int getCount()
	{
		if (_done)
		{
			return _matches.size();
		}

		GetMatch(infinite);

		return _matches.size();
	}

	/** <devdoc>
		<p>[To be supplied.]</p>
	 </devdoc>
	*/
	public final Object getSyncRoot()
	{
		return this;
	}

	/** <devdoc>
		<p>[To be supplied.]</p>
	 </devdoc>
	*/
	public final boolean getIsSynchronized()
	{
		return false;
	}

	/** <devdoc>
		<p>[To be supplied.]</p>
	 </devdoc>
	*/
	public final boolean getIsReadOnly()
	{
		return true;
	}


	/** <devdoc>
		<p>
		   Returns the ith Match in the collection.
		</p>
	 </devdoc>
	*/
	public Match getItem(int i)
	{
		Match match;

		match = GetMatch(i);

		if (match == null)
		{
			throw new IllegalArgumentException("i");
		}

		return match;
	}

	/** <devdoc>
		<p>
		   Copies all the elements of the collection to the given array
		   starting at the given index.
		</p>
	 </devdoc>
	*/
	public final void CopyTo(Array array, int arrayIndex)
	{
		//@czc:不判断数组维数
		/*if ((array != null) && (array.Rank != 1))
		{
			throw new IllegalArgumentException(SR.GetString(SR.Arg_RankMultiDimNotSupported));
		}*/

		// property access to force computation of whole array
		int count = getCount();
		try
		{
			//_matches.CopyTo(array, arrayIndex);
			//@czc
			System.arraycopy(_matches, arrayIndex, array, 0, _matches.size());
		}
		catch (Exception ex)
		{
			throw new IllegalArgumentException(SR.GetString(SR.Arg_InvalidArrayType), ex);
		}
	}

	/** <devdoc>
		<p>
		   Provides an enumerator in the same order as Item[i].
		</p>
	 </devdoc>
	*/
	public final java.util.Iterator GetEnumerator()
	{
		return new MatchEnumerator(this);
	}

	/**
	 * Returns an iterator over the elements contained in this collection.
	 *
	 * @return an iterator over the elements contained in this collection
	 */
	@Override
	public Iterator<Match> iterator() {
		return new MatchEnumerator(this);
	}

	@Override
	public int size() {
		return getCount();
	}
}
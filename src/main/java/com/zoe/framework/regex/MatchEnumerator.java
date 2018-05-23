package com.zoe.framework.regex;

import java.io.Serializable;

/*
 * This non-public enumerator lists all the group matches.
 * Should it be public?
 */
//C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the methods implemented will need adjustment:
public class MatchEnumerator implements java.util.Iterator<Match>, Serializable
{
	public MatchCollection _matchcoll;
	public Match _match = null;
	public int _curindex;
	public boolean _done;

	/*
	 * Nonpublic constructor
	 */
	public MatchEnumerator(MatchCollection matchcoll)
	{
		_matchcoll = matchcoll;
	}

	/*
	 * Advance to the next match
	 */
	public final boolean MoveNext()
	{
		if (_done)
		{
			return false;
		}

		_match = _matchcoll.GetMatch(_curindex);
		_curindex++;

		if (_match == null)
		{
			_done = true;
			return false;
		}

		return true;
	}

	/*
	 * The current match
	 */
	public final Match getCurrent()
	{
		if (_match == null)
		{
			throw new UnsupportedOperationException(SR.GetString(SR.EnumNotStarted));
		}
		return _match;
	}

	/*
	 * Position before the first item
	 */
	public final void Reset()
	{
		_curindex = 0;
		_done = false;
		_match = null;
	}

	/**
	 * Returns {@code true} if the iteration has more elements.
	 * (In other words, returns {@code true} if {@link #next} would
	 * return an element rather than throwing an exception.)
	 *
	 * @return {@code true} if the iteration has more elements
	 */
	@Override
	public boolean hasNext() {
		return MoveNext();
	}

	/**
	 * Returns the next element in the iteration.
	 *
	 * @return the next element in the iteration
	 * @throws java.util.NoSuchElementException if the iteration has no more elements
	 */
	@Override
	public Match next() {
		return getCurrent();
	}
}
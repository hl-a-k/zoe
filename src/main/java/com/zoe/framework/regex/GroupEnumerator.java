package com.zoe.framework.regex;

/*
 * This non-public enumerator lists all the captures
 * Should it be public?
 */
//C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the methods implemented will need adjustment:
public class GroupEnumerator implements java.util.Iterator<Capture>
{
	public GroupCollection _rgc;
	public int _curindex;

	/*
	 * Nonpublic constructor
	 */
	public GroupEnumerator(GroupCollection rgc)
	{
		_curindex = -1;
		_rgc = rgc;
	}

	/*
	 * As required by IEnumerator
	 */
	public final boolean MoveNext()
	{
		int size = _rgc.size();

		if (_curindex >= size)
		{
			return false;
		}

		_curindex++;

		return (_curindex < size);
	}

	/*
	 * As required by IEnumerator
	 */
	public final Object getCurrent()
	{
		return getCapture();
	}

	/*
	 * Returns the current capture
	 */
	public final Capture getCapture()
	{
		if (_curindex < 0 || _curindex >= _rgc.size())
		{
			throw new UnsupportedOperationException(SR.GetString(SR.EnumNotStarted));
		}

		return _rgc.getItem(_curindex);
	}

	/*
	 * Reset to before the first item
	 */
	public final void Reset()
	{
		_curindex = -1;
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
	public Capture next() {
		return getCapture();
	}
}
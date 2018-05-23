package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexCaptureCollection.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>
//------------------------------------------------------------------------------

// The CaptureCollection lists the captured Capture numbers
// contained in a compiled Regex.



/*
 * This collection returns the Captures for a group
 * in the order in which they were matched (left to right
 * or right to left). It is created by Group.Captures
 */

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;

/** <devdoc>
	<p>
	   Represents a sequence of capture substrings. The object is used
	   to return the set of captures done by a single capturing group.
	</p>
 </devdoc>
*/
//C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the methods implemented will need adjustment:
public class CaptureCollection extends java.util.AbstractCollection<Capture> implements Serializable
{
	public Group _group;
	public int _capcount;
	public Capture[] _captures;

	/*
	 * Nonpublic constructor
	 */
	public CaptureCollection(Group group)
	{
		_group = group;
		_capcount = _group._capcount;
	}

	/*
	 * The object on which to synchronize
	 */
	/** <devdoc>
		<p>[To be supplied.]</p>
	 </devdoc>
	*/
	public final Object getSyncRoot()
	{
		return _group;
	}

	/*
	 * ICollection
	 */
	/** <devdoc>
		<p>[To be supplied.]</p>
	 </devdoc>
	*/
	public final boolean getIsSynchronized()
	{
		return false;
	}

	/*
	 * ICollection
	 */
	/** <devdoc>
		<p>[To be supplied.]</p>
	 </devdoc>
	*/
	public final boolean getIsReadOnly()
	{
		return true;
	}

	/*
	 * The number of captures for the group
	 */
	/** <devdoc>
		<p>
		   Returns the number of captures.
		</p>
	 </devdoc>
	*/
	public final int getCount()
	{
		return _capcount;
	}

	/*
	 * The ith capture in the group
	 */
	/** <devdoc>
		<p>
		   Provides a means of accessing a specific capture in the collection.
		</p>
	 </devdoc>
	*/
	public final Capture getItem(int i)
	{
		return GetCapture(i);
	}

	/*
	 * As required by ICollection
	 */
	/** <devdoc>
		<p>
		   Copies all the elements of the collection to the given array
		   beginning at the given index.
		</p>
	 </devdoc>
	*/
	public final void CopyTo(Array array, int arrayIndex)
	{
		if (array == null)
		{
			throw new IllegalArgumentException("array");
		}

		for (int i = arrayIndex, j = 0; j < getCount(); i++, j++)
		{
			Array.set(array, i, this.getItem(j));
			//array.SetValue(this.getItem(j), i);
		}
	}

	/*
	 * As required by ICollection
	 */
	/** <devdoc>
		<p>
		   Provides an enumerator in the same order as Item[].
		</p>
	 </devdoc>
	*/
	public final java.util.Iterator GetEnumerator()
	{
		return new CaptureEnumerator(this);
	}

	/*
	 * Nonpublic code to return set of captures for the group
	 */
	public final Capture GetCapture(int i)
	{
		if (i == _capcount - 1 && i >= 0)
		{
			return _group;
		}

		if (i >= _capcount || i < 0)
		{
			throw new IllegalArgumentException("i");
		}

		// first time a capture is accessed, compute them all
		if (_captures == null)
		{
			_captures = new Capture[_capcount];
			for (int j = 0; j < _capcount - 1; j++)
			{
				_captures[j] = new Capture(_group._text, _group._caps[j * 2], _group._caps[j * 2 + 1]);
			}
		}

		return _captures[i];
	}

	/**
	 * Returns an iterator over the elements contained in this collection.
	 *
	 * @return an iterator over the elements contained in this collection
	 */
	@Override
	public Iterator<Capture> iterator() {
		return new CaptureEnumerator(this);
	}

	@Override
	public int size() {
		return _capcount;
	}
}
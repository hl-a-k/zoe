package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexGroupCollection.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>
//------------------------------------------------------------------------------

// The GroupCollection lists the captured Capture numbers
// contained in a compiled Regex.


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
public class GroupCollection extends java.util.AbstractCollection<Capture> implements Serializable
{
	public Match _match;
	public java.util.Hashtable _captureMap;

	// cache of Group objects fed to the user
	public Group[] _groups;

	/*
	 * Nonpublic constructor
	 */
	public GroupCollection(Match match, java.util.Hashtable caps)
	{
		_match = match;
		_captureMap = caps;
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
		return _match;
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

	/** <devdoc>
		<p>
		   Returns the number of groups.
		</p>
	 </devdoc>
	*/
	public final int getCount()
	{
		return _match._matchcount.length;
	}

	/** <devdoc>
		<p>[To be supplied.]</p>
	 </devdoc>
	*/
	public final Group getItem(int groupnum)
	{
		return GetGroup(groupnum);
	}

	/** <devdoc>
		<p>[To be supplied.]</p>
	 </devdoc>
	*/
	public final Group getItem(String groupname)
	{
		if (_match._regex == null)
		{
			return Group._emptygroup;
		}

		return GetGroup(_match._regex.GroupNumberFromName(groupname));
	}

	public final Group GetGroup(int groupnum)
	{
		if (_captureMap != null)
		{
			Object o;

			o = _captureMap.get(groupnum);
			if (o == null)
			{
				return Group._emptygroup;
			}
				//throw new ArgumentOutOfRangeException("groupnum");

			return GetGroupImpl((Integer)o);
		}
		else
		{
			//if (groupnum >= _match._regex.CapSize || groupnum < 0)
			//   throw new ArgumentOutOfRangeException("groupnum");
			if (groupnum >= _match._matchcount.length || groupnum < 0)
			{
				return Group._emptygroup;
			}

			return GetGroupImpl(groupnum);
		}
	}


	/*
	 * Caches the group objects
	 */
	public final Group GetGroupImpl(int groupnum)
	{
		if (groupnum == 0)
		{
			return _match;
		}

		// Construct all the Group objects the first time GetGroup is called

		if (_groups == null)
		{
			_groups = new Group[_match._matchcount.length - 1];
			for (int i = 0; i < _groups.length; i++)
			{
				_groups[i] = new Group(_match._text, _match._matches[i + 1], _match._matchcount[i + 1]);
			}
		}

		return _groups[groupnum - 1];
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
		return new GroupEnumerator(this);
	}

	/**
	 * Returns an iterator over the elements contained in this collection.
	 *
	 * @return an iterator over the elements contained in this collection
	 */
	@Override
	public Iterator<Capture> iterator() {
		return new GroupEnumerator(this);
	}

	@Override
	public int size() {
		return _match._matchcount.length;
	}
}
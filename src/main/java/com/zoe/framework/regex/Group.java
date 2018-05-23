package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexGroup.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// Group represents the substring or substrings that
// are captured by a single capturing group after one
// regular expression match.


import java.io.Serializable;

/** <devdoc>
	Group 
	   represents the results from a single capturing group. A capturing group can
	   capture zero, one, or more strings in a single match because of quantifiers, so
	   Group supplies a collection of Capture objects. 
	</devdoc>
*/
public class Group extends Capture implements Serializable
{
	// the empty group object
	public static Group _emptygroup = new Group("", new int[0], 0);

	public int[] _caps;
	public int _capcount;
	public CaptureCollection _capcoll;

	public Group(String text, int[] caps, int capcount)
	{
		super(text, capcount == 0 ? 0 : caps[(capcount - 1) * 2], capcount == 0 ? 0 : caps[(capcount * 2) - 1]);

		_caps = caps;
		_capcount = capcount;
	}

	/*
	 * True if the match was successful
	 */
	/** <devdoc>
		<p>Indicates whether the match is successful.</p>
	 </devdoc>
	*/
	public final boolean getSuccess()
	{
		return _capcount != 0;
	}

	/*
	 * The collection of all captures for this group
	 */
	/** <devdoc>
		<p>
		   Returns a collection of all the captures matched by the capturing
		   group, in innermost-leftmost-first order (or innermost-rightmost-first order if
		   compiled with the "r" option). The collection may have zero or more items.
		</p>
	 </devdoc>
	*/
	public final CaptureCollection getCaptures()
	{
		if (_capcoll == null)
		{
			_capcoll = new CaptureCollection(this);
		}

		return _capcoll;
	}

	/*
	 * Convert to a thread-safe object by precomputing cache contents
	 */
	/** <devdoc>
		<p>Returns 
		   a Group object equivalent to the one supplied that is safe to share between
		   multiple threads.</p>
	 </devdoc>
	*/
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [HostProtection(Synchronization=true)] public static Group Synchronized(Group inner)
	public static Group Synchronized(Group inner)
	{
		if (inner == null)
		{
			throw new IllegalArgumentException("inner");
		}

		// force Captures to be computed.

		CaptureCollection capcoll;
		Capture dummy;

		capcoll = inner.getCaptures();

		if (inner._capcount > 0)
		{
			dummy = capcoll.getItem(0);
		}

		return inner;
	}
}
package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexCapture.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// Capture is just a location/length pair that indicates the
// location of a regular expression match. A single regexp
// search may return multiple Capture within each capturing
// RegexGroup.


import java.io.Serializable;

/** <devdoc>
	<p> 
	   Represents the results from a single subexpression capture. The object represents
	   one substring for a single successful capture.</p>
 </devdoc>
*/
public class Capture implements Serializable
{
	public String _text;
	public int _index;
	public int _length;

	public Capture(String text, int i, int l)
	{
		_text = text;
		_index = i;
		_length = l;
	}

	/*
	 * The index of the beginning of the matched capture
	 */
	/** <devdoc>
		<p>Returns the position in the original string where the first character of
		   captured substring was found.</p>
	 </devdoc>
	*/
	public final int getIndex()
	{
		return _index;
	}

	/*
	 * The length of the matched capture
	 */
	/** <devdoc>
		<p>
		   Returns the length of the captured substring.
		</p>
	 </devdoc>
	*/
	public final int getLength()
	{
		return _length;
	}

	/** <devdoc>
		<p>[To be supplied.]</p>
	 </devdoc>
	*/
	public final String getValue()
	{
		return _text.substring(_index, _index + _length);
	}

	/*
	 * The capture as a string
	 */
	/** <devdoc>
		<p>
		   Returns 
			  the substring that was matched.
		   </p>
		</devdoc>
	*/
	@Override
	public String toString()
	{
		return getValue();
	}

	/*
	 * The original string
	 */
	public final String GetOriginalString()
	{
		return _text;
	}

	/*
	 * The substring to the left of the capture
	 */
	public final String GetLeftSubstring()
	{
		return _text.substring(0, _index);
	}

	/*
	 * The substring to the right of the capture
	 */
	public final String GetRightSubstring()
	{
		return DotNetToJavaStringHelper.substring(_text, _index + _length, _text.length() - _index - _length);
	}
}
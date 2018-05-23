package com.zoe.framework.regex;

// ==++==
// 
//   Copyright (c) Microsoft Corporation.  All rights reserved.
// 
// ==--==
//
// <OWNER>[....]</OWNER>

// A constant used by methods that take a timeout (Object.Wait, Thread.Sleep
// etc) to indicate that no timeout should occur.
//
// <

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Runtime.InteropServices.ComVisible(true)] public static class Timeout
public final class Timeout
{
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Runtime.InteropServices.ComVisible(false)] public static readonly TimeSpan InfiniteTimeSpan = new TimeSpan(0, 0, 0, 0, Timeout.Infinite);
	public static final TimeSpan InfiniteTimeSpan = new TimeSpan(0, 0, 0, 0, Timeout.Infinite);

	public static final int Infinite = -1;
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to 'unchecked' in this context:
//ORIGINAL LINE: internal const uint UnsignedInfinite = unchecked((uint)-1);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
	public static final int UnsignedInfinite = (int)-1;
}
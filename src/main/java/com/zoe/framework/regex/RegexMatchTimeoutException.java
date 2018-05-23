package com.zoe.framework.regex;

/**------------------------------------------------------------------------------
 <copyright file="RegexMatchTimeoutException.cs" company="Microsoft">
	 Copyright (c) Microsoft Corporation.  All rights reserved.
 </copyright>                               

 <owner>gpaperin</owner>
------------------------------------------------------------------------------
*/


import java.io.Serializable;

/**
 This is the exception that is thrown when a RegEx matching timeout occurs.
*/
public class RegexMatchTimeoutException extends RuntimeException implements Serializable
{


	private String regexInput = null;

	private String regexPattern = null;

	private TimeSpan matchTimeout = TimeSpan.FromTicks(-1);


	/** 
	 This is the preferred constructor to use.
	 The other constructors are provided for compliance to Fx design guidelines.
	 
	 @param regexInput Matching timeout occured during mathing within the specified input.
	 @param regexPattern Matching timeout occured during mathing to the specified pattern.
	 @param matchTimeout Matching timeout occured becasue matching took longer than the specified timeout.
	*/
	public RegexMatchTimeoutException(String regexInput, String regexPattern, TimeSpan matchTimeout)
	{
		super(SR.GetString(SR.RegexMatchTimeoutException_Occurred));
		Init(regexInput, regexPattern, matchTimeout);
	}


	/** 
	 This constructor is provided in compliance with common NetFx design patterns;
	 developers should prefer using the constructor
	 <code>public RegexMatchTimeoutException(string input, string pattern, TimeSpan matchTimeout)</code>.
	*/
	public RegexMatchTimeoutException()
	{
		super();
		Init();
	}


	/** 
	 This constructor is provided in compliance with common NetFx design patterns;
	 developers should prefer using the constructor
	 <code>public RegexMatchTimeoutException(string input, string pattern, TimeSpan matchTimeout)</code>.
	 
	 @param message The error message that explains the reason for the exception.
	*/
	public RegexMatchTimeoutException(String message)
	{
		super(message);
		Init();
	}


	private void Init()
	{
		Init("", "", TimeSpan.FromTicks(-1));
	}

	private void Init(String input, String pattern, TimeSpan timeout)
	{
		this.regexInput = input;
		this.regexPattern = pattern;
		this.matchTimeout = timeout;
	}

	 private String getPattern()
	 {
		return regexPattern;
	 }
	 private String getInput()
	 {
		return regexInput;
	 }

	 private TimeSpan getMatchTimeout()
	 {
		return matchTimeout;
	 }
} // public class RegexMatchTimeoutException
 // namespace com.zoe.framework.regex3

// file RegexMatchTimeoutException.cs

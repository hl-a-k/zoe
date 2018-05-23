package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexRunnerFactory.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// This RegexRunnerFactory class is a base class for compiled regex code.
// we need to compile a factory because Type.CreateInstance is much slower
// than calling the constructor directly.



/** <internalonly/>
*/
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [EditorBrowsable(EditorBrowsableState.Never)] public abstract class RegexRunnerFactory
public abstract class RegexRunnerFactory
{
	protected RegexRunnerFactory()
	{
	}
	protected abstract RegexRunner CreateInstance();
}
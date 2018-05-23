package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexTree.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// RegexTree is just a wrapper for a node tree with some
// global information attached.



public final class RegexTree
{
	public RegexTree(RegexNode root, java.util.Hashtable caps, int[] capnumlist, int captop, java.util.Hashtable capnames, String[] capslist, RegexOptions opts)
	{
		_root = root;
		_caps = caps;
		_capnumlist = capnumlist;
		_capnames = capnames;
		_capslist = capslist;
		_captop = captop;
		_options = opts;
	}

	public RegexNode _root;
	public java.util.Hashtable _caps;
	public int[] _capnumlist;
	public java.util.Hashtable _capnames;
	public String[] _capslist;
	public RegexOptions _options;
	public int _captop;
}
package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexNode.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// This RegexNode class is internal to the Regex package.
// It is built into a parsed tree for a regular expression.

// Implementation notes:
// 
// Since the node tree is a temporary data structure only used
// during compilation of the regexp to integer codes, it's
// designed for clarity and convenience rather than
// space efficiency.
//
// RegexNodes are built into a tree, linked by the _children list.
// Each node also has a _parent and _ichild member indicating
// its parent and which child # it is in its parent's list.
//
// RegexNodes come in as many types as there are constructs in
// a regular expression, for example, "concatenate", "alternate",
// "one", "rept", "group". There are also node types for basic
// peephole optimizations, e.g., "onerep", "notsetrep", etc.
//
// Because perl 5 allows "lookback" groups that scan backwards,
// each node also gets a "direction". Normally the value of
// boolean _backward = false.
//
// During parsing, top-level nodes are also stacked onto a parse
// stack (a stack of trees). For this purpose we have a _next
// pointer. [Note that to save a few bytes, we could overload the
// _parent pointer instead.]
//
// On the parse stack, each tree has a "role" - basically, the
// nonterminal in the grammar that the parser has currently
// assigned to the tree. That code is stored in _role.
//
// Finally, some of the different kinds of nodes have data.
// Two integers (for the looping constructs) are stored in
// _operands, an an object (either a string or a set)
// is stored in _data


import java.util.Collections;

public final class RegexNode
{
	/*
	 * RegexNode types
	 */

	// the following are leaves, and correspond to primitive operations

	//    static final int Onerep     = RegexCode.Onerep;     // c,n      a {n}
	//    static final int Notonerep  = RegexCode.Notonerep;  // c,n      .{n}
	//    static final int Setrep     = RegexCode.Setrep;     // set,n    \d {n}

	public static final int Oneloop = RegexCode.Oneloop; // c,n      a*
	public static final int Notoneloop = RegexCode.Notoneloop; // c,n      .*
	public static final int Setloop = RegexCode.Setloop; // set,n    \d*

	public static final int Onelazy = RegexCode.Onelazy; // c,n      a*?
	public static final int Notonelazy = RegexCode.Notonelazy; // c,n      .*?
	public static final int Setlazy = RegexCode.Setlazy; // set,n    \d*?

	public static final int One = RegexCode.One; // char     a
	public static final int Notone = RegexCode.Notone; // char     . [^a]
	public static final int Set = RegexCode.Set; // set      [a-z] \w \s \d

	public static final int Multi = RegexCode.Multi; // string   abcdef
	public static final int Ref = RegexCode.Ref; // index    \1

	public static final int Bol = RegexCode.Bol; //          ^
	public static final int Eol = RegexCode.Eol; //          $
	public static final int Boundary = RegexCode.Boundary; //          \b
	public static final int Nonboundary = RegexCode.Nonboundary; //          \B
	public static final int ECMABoundary = RegexCode.ECMABoundary; // \b
	public static final int NonECMABoundary = RegexCode.NonECMABoundary; // \B
	public static final int Beginning = RegexCode.Beginning; //          \A
	public static final int Start = RegexCode.Start; //          \G
	public static final int EndZ = RegexCode.EndZ; //          \Z
	public static final int End = RegexCode.End; //          \z

	// (note: End               = 21;)

	// interior nodes do not correpond to primitive operations, but
	// control structures compositing other operations

	// concat and alternate take n children, and can run forward or backwards

	public static final int Nothing = 22; //          []
	public static final int Empty = 23; //          ()

	public static final int Alternate = 24; //          a|b
	public static final int Concatenate = 25; //          ab

	public static final int Loop = 26; // m,x      * + ? {,}
	public static final int Lazyloop = 27; // m,x      *? +? ?? {,}?

	public static final int Capture = 28; // n        ()
	public static final int Group = 29; //          (?:)
	public static final int Require = 30; //          (?=) (?<=)
	public static final int Prevent = 31; //          (?!) (?<!)
	public static final int Greedy = 32; //          (?>) (?<)
	public static final int Testref = 33; //          (?(n) | )
	public static final int Testgroup = 34; //          (?(...) | )

	/*
	 * RegexNode data members
	 * 
	 */

	public int _type;

	public java.util.ArrayList<RegexNode> _children;

	public String _str;
	public char _ch;
	public int _m;
	public int _n;
	public RegexOptions _options;

	public RegexNode _next;

	public RegexNode(int type, RegexOptions options)
	{
		_type = type;
		_options = options;
	}

	public RegexNode(int type, RegexOptions options, char ch)
	{
		_type = type;
		_options = options;
		_ch = ch;
	}

	public RegexNode(int type, RegexOptions options, String str)
	{
		_type = type;
		_options = options;
		_str = str;
	}

	public RegexNode(int type, RegexOptions options, int m)
	{
		_type = type;
		_options = options;
		_m = m;
	}

	public RegexNode(int type, RegexOptions options, int m, int n)
	{
		_type = type;
		_options = options;
		_m = m;
		_n = n;
	}

	public boolean UseOptionR()
	{
		return (_options.getValue() & RegexOptions.RightToLeft.getValue()) != 0;
	}

	public RegexNode ReverseLeft()
	{
		if (UseOptionR() && _type == Concatenate && _children != null)
		{
			//_children.Reverse(0, _children.size());
			Collections.reverse(_children);
		}

		return this;
	}


	// Pass type as OneLazy or OneLoop
	public void MakeRep(int type, int min, int max)
	{
		_type += (type - One);
		_m = min;
		_n = max;
	}

	/*
	 * Reduce
	 *
	 * Removes redundant nodes from the subtree, and returns a reduced subtree.
	 */
	public RegexNode Reduce()
	{
		RegexNode n;

		switch (Type())
		{
			case Alternate:
				n = ReduceAlternation();
				break;

			case Concatenate:
				n = ReduceConcatenation();
				break;

			case Loop:
			case Lazyloop:
				n = ReduceRep();
				break;

			case Group:
				n = ReduceGroup();
				break;

			case Set:
			case Setloop:
				n = ReduceSet();
				break;

			default:
				n = this;
				break;
		}

		return n;
	}


	/*
	 * StripEnation:
	 *
	 * Simple optimization. If a concatenation or alternation has only
	 * one child strip out the intermediate node. If it has zero children,
	 * turn it into an empty.
	 * 
	 */

	public RegexNode StripEnation(int emptyType)
	{
		switch (ChildCount())
		{
			case 0:
				return new RegexNode(emptyType, _options);
			case 1:
				return Child(0);
			default:
				return this;
		}
	}

	/*
	 * ReduceGroup:
	 *
	 * Simple optimization. Once parsed into a tree, noncapturing groups
	 * serve no function, so strip them out.
	 */

	public RegexNode ReduceGroup()
	{
		RegexNode u;

		for (u = this; u.Type() == Group;)
		{
			u = u.Child(0);
		}

		return u;
	}

	/*
	 * ReduceRep:
	 *
	 * Nested repeaters just get multiplied with each other if they're not
	 * too lumpy
	 */

	public RegexNode ReduceRep()
	{
		RegexNode u;
		RegexNode child;
		int type;
		int min;
		int max;

		u = this;
		type = Type();
		min = _m;
		max = _n;

		for (;;)
		{
			if (u.ChildCount() == 0)
			{
				break;
			}

			child = u.Child(0);

			// multiply reps of the same type only
			if (child.Type() != type)
			{
				int childType = child.Type();

				if (!(childType >= Oneloop && childType <= Setloop && type == Loop || childType >= Onelazy && childType <= Setlazy && type == Lazyloop))
				{
					break;
				}
			}

			// child can be too lumpy to blur, e.g., (a {100,105}) {3} or (a {2,})?
			// [but things like (a {2,})+ are not too lumpy...]
			if (u._m == 0 && child._m > 1 || child._n < child._m * 2)
			{
				break;
			}

			u = child;
			if (u._m > 0)
			{
				u._m = min = ((Integer.MAX_VALUE - 1) / u._m < min) ? Integer.MAX_VALUE : u._m * min;
			}
			if (u._n > 0)
			{
				u._n = max = ((Integer.MAX_VALUE - 1) / u._n < max) ? Integer.MAX_VALUE : u._n * max;
			}
		}

		return min == Integer.MAX_VALUE ? new RegexNode(Nothing, _options) : u;
	}

	/*
	 * ReduceSet:
	 *
	 * Simple optimization. If a set is a singleton, an inverse singleton,
	 * or empty, it's transformed accordingly.
	 */

	public RegexNode ReduceSet()
	{
		// Extract empty-set, one and not-one case as special

		if (RegexCharClass.IsEmpty(_str))
		{
			_type = Nothing;
			_str = null;
		}
		else if (RegexCharClass.IsSingleton(_str))
		{
			_ch = RegexCharClass.SingletonChar(_str);
			_str = null;
			_type += (One - Set);
		}
		else if (RegexCharClass.IsSingletonInverse(_str))
		{
			_ch = RegexCharClass.SingletonChar(_str);
			_str = null;
			_type += (Notone - Set);
		}

		return this;
	}

	/*
	 * ReduceAlternation:
	 *
	 * Basic optimization. Single-letter alternations can be replaced
	 * by faster set specifications, and nested alternations with no
	 * intervening operators can be flattened:
	 *
	 * a|b|c|def|g|h -> [a-c]|def|[gh]
	 * apple|(?:orange|pear)|grape -> apple|orange|pear|grape
	 *
	 * <
*/

	public RegexNode ReduceAlternation()
	{
		// Combine adjacent sets/chars

		boolean wasLastSet;
		boolean lastNodeCannotMerge;
		RegexOptions optionsLast;
		RegexOptions optionsAt;
		int i;
		int j;
		RegexNode at;
		RegexNode prev;

		if (_children == null)
		{
			return new RegexNode(RegexNode.Nothing, _options);
		}

		wasLastSet = false;
		lastNodeCannotMerge = false;
		optionsLast = RegexOptions.forValue(0);

		for (i = 0, j = 0; i < _children.size(); i++, j++)
		{
			at = _children.get(i);

			if (j < i)
			{
				_children.set(j, at);
			}

			for (;;)
			{
				if (at._type == Alternate)
				{
					for (int k = 0; k < at._children.size(); k++)
					{
						at._children.get(k)._next = this;
					}

					//_children.InsertRange(i + 1, at._children);
					_children.addAll(i + 1, at._children);
					j--;
				}
				else if (at._type == Set || at._type == One)
				{
					// Cannot merge sets if L or I options differ, or if either are negated.
					optionsAt = RegexOptions.forValue(at._options.getValue() & (RegexOptions.RightToLeft.getValue() | RegexOptions.IgnoreCase.getValue()));


					if (at._type == Set)
					{
						if (!wasLastSet || optionsLast != optionsAt || lastNodeCannotMerge || !RegexCharClass.IsMergeable(at._str))
						{
							wasLastSet = true;
							lastNodeCannotMerge = !RegexCharClass.IsMergeable(at._str);
							optionsLast = optionsAt;
							break;
						}
					}
					else if (!wasLastSet || optionsLast != optionsAt || lastNodeCannotMerge)
					{
						wasLastSet = true;
						lastNodeCannotMerge = false;
						optionsLast = optionsAt;
						break;
					}


					// The last node was a Set or a One, we're a Set or One and our options are the same.
					// Merge the two nodes.
					j--;
					prev = _children.get(j);

					RegexCharClass prevCharClass;
					if (prev._type == RegexNode.One)
					{
						prevCharClass = new RegexCharClass();
						prevCharClass.AddChar(prev._ch);
					}
					else
					{
						prevCharClass = RegexCharClass.Parse(prev._str);
					}

					if (at._type == RegexNode.One)
					{
						prevCharClass.AddChar(at._ch);
					}
					else
					{
						RegexCharClass atCharClass = RegexCharClass.Parse(at._str);
						prevCharClass.AddCharClass(atCharClass);
					}

					prev._type = RegexNode.Set;
					prev._str = prevCharClass.ToStringClass();

				}
				else if (at._type == RegexNode.Nothing)
				{
					j--;
				}
				else
				{
					wasLastSet = false;
					lastNodeCannotMerge = false;
				}
				break;
			}
		}

		if (j < i)
		{
			//_children.removeRange(j, i - j + j);
			ArrayExt.removeRange(_children, j, i - j + j);
		}

		return StripEnation(RegexNode.Nothing);
	}

	/*
	 * ReduceConcatenation:
	 *
	 * Basic optimization. Adjacent strings can be concatenated.
	 *
	 * (?:abc)(?:def) -> abcdef
	 */

	public RegexNode ReduceConcatenation()
	{
		// Eliminate empties and concat adjacent strings/chars

		boolean wasLastString;
		RegexOptions optionsLast;
		RegexOptions optionsAt;
		int i;
		int j;

		if (_children == null)
		{
			return new RegexNode(RegexNode.Empty, _options);
		}

		wasLastString = false;
		optionsLast = RegexOptions.forValue(0);

		for (i = 0, j = 0; i < _children.size(); i++, j++)
		{
			RegexNode at;
			RegexNode prev;

			at = _children.get(i);

			if (j < i)
			{
				_children.set(j, at);
			}

			if (at._type == RegexNode.Concatenate && ((at._options.getValue() & RegexOptions.RightToLeft.getValue()) == (_options.getValue() & RegexOptions.RightToLeft.getValue())))
			{
				for (int k = 0; k < at._children.size(); k++)
				{
					at._children.get(k)._next = this;
				}

				//_children.InsertRange(i + 1, at._children);
				_children.addAll(i + 1 , at._children);
				j--;
			}
			else if (at._type == RegexNode.Multi || at._type == RegexNode.One)
			{
				// Cannot merge strings if L or I options differ
				optionsAt = RegexOptions.forValue(at._options.getValue() & (RegexOptions.RightToLeft.getValue() | RegexOptions.IgnoreCase.getValue()));

				if (!wasLastString || optionsLast != optionsAt)
				{
					wasLastString = true;
					optionsLast = optionsAt;
					continue;
				}

				prev = _children.get(--j);

				if (prev._type == RegexNode.One)
				{
					prev._type = RegexNode.Multi;
					prev._str = String.valueOf(prev._ch);
				}

				if ((optionsAt.getValue() & RegexOptions.RightToLeft.getValue()) == 0)
				{
					if (at._type == RegexNode.One)
					{
						prev._str += (new Character(at._ch)).toString();
					}
					else
					{
						prev._str += at._str;
					}
				}
				else
				{
					if (at._type == RegexNode.One)
					{
						prev._str = (new Character(at._ch)).toString() + prev._str;
					}
					else
					{
						prev._str = at._str + prev._str;
					}
				}

			}
			else if (at._type == RegexNode.Empty)
			{
				j--;
			}
			else
			{
				wasLastString = false;
			}
		}

		if (j < i)
		{
			//_children.removeRange(j, i - j + j);
			ArrayExt.removeRange(_children, j, i - j + j);
		}

		return StripEnation(RegexNode.Empty);
	}

	public RegexNode MakeQuantifier(boolean lazy, int min, int max)
	{
		RegexNode result;

		if (min == 0 && max == 0)
		{
			return new RegexNode(RegexNode.Empty, _options);
		}

		if (min == 1 && max == 1)
		{
			return this;
		}

		switch (_type)
		{
			case RegexNode.One:
			case RegexNode.Notone:
			case RegexNode.Set:

				MakeRep(lazy ? RegexNode.Onelazy : RegexNode.Oneloop, min, max);
				return this;

			default:
				result = new RegexNode(lazy ? RegexNode.Lazyloop : RegexNode.Loop, _options, min, max);
				result.AddChild(this);
				return result;
		}
	}

	public void AddChild(RegexNode newChild)
	{
		RegexNode reducedChild;

		if (_children == null)
		{
			_children = new java.util.ArrayList<RegexNode>(4);
		}

		reducedChild = newChild.Reduce();

		_children.add(reducedChild);
		reducedChild._next = this;
	}
	public RegexNode Child(int i)
	{
		return _children.get(i);
	}

	public int ChildCount()
	{
		return _children == null ? 0 : _children.size();
	}

	public int Type()
	{
		return _type;
	}

}
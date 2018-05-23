package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexReplacement.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// The RegexReplacement class represents a substitution string for
// use when using regexs to search/replace, etc. It's logically
// a sequence intermixed (1) constant strings and (2) group numbers.


import java.util.Collections;

public final class RegexReplacement
{
	/*
	 * Since RegexReplacement shares the same parser as Regex,
	 * the constructor takes a RegexNode which is a concatenation
	 * of constant strings and backreferences.
	 */
	public RegexReplacement(String rep, RegexNode concat, java.util.Hashtable _caps)
	{
		StringBuilder sb;
		java.util.ArrayList<String> strings;
		java.util.ArrayList<Integer> rules;
		int slot;

		_rep = rep;

		if (concat.Type() != RegexNode.Concatenate)
		{
			throw new IllegalArgumentException(SR.GetString(SR.ReplacementError));
		}

		sb = new StringBuilder();
		strings = new java.util.ArrayList<String>();
		rules = new java.util.ArrayList<Integer>();

		for (int i = 0; i < concat.ChildCount(); i++)
		{
			RegexNode child = concat.Child(i);

			switch (child.Type())
			{
				case RegexNode.Multi:
					sb.append(child._str);
					break;
				case RegexNode.One:
					sb.append(child._ch);
					break;
				case RegexNode.Ref:
					if (sb.length() > 0)
					{
						rules.add(strings.size());
						strings.add(sb.toString());
						sb.setLength(0);
					}
					slot = child._m;

					if (_caps != null && slot >= 0)
					{
						slot = (int)_caps.get(slot);
					}

					rules.add(-Specials - 1 - slot);
					break;
				default:
					throw new IllegalArgumentException(SR.GetString(SR.ReplacementError));
			}
		}

		if (sb.length() > 0)
		{
			rules.add(strings.size());
			strings.add(sb.toString());
		}

		_strings = strings;
		_rules = rules;
	}

	public String _rep;
	public java.util.ArrayList<String> _strings; // table of string constants
	public java.util.ArrayList<Integer> _rules; // negative -> group #, positive -> string #

	// constants for special insertion patterns

	public static final int Specials = 4;
	public static final int LeftPortion = -1;
	public static final int RightPortion = -2;
	public static final int LastGroup = -3;
	public static final int WholeString = -4;

	/*       
	 * Given a Match, emits into the StringBuilder the evaluated
	 * substitution pattern.
	 */
	private void ReplacementImpl(StringBuilder sb, Match match)
	{
		for (int i = 0; i < _rules.size(); i++)
		{
			int r = _rules.get(i);
			if (r >= 0) // string lookup
			{
				sb.append(_strings.get(r));
			}
			else if (r < -Specials) // group lookup
			{
				sb.append(match.GroupToStringImpl(-Specials - 1 - r));
			}
			else
			{
				switch (-Specials - 1 - r) // special insertion patterns
				{
					case LeftPortion:
						sb.append(match.GetLeftSubstring());
						break;
					case RightPortion:
						sb.append(match.GetRightSubstring());
						break;
					case LastGroup:
						sb.append(match.LastGroupToStringImpl());
						break;
					case WholeString:
						sb.append(match.GetOriginalString());
						break;
				}
			}
		}
	}

	/*       
	 * Given a Match, emits into the List<String> the evaluated
	 * Right-to-Left substitution pattern.
	 */
	private void ReplacementImplRTL(java.util.ArrayList<String> al, Match match)
	{
		for (int i = _rules.size() - 1; i >= 0; i--)
		{
			int r = _rules.get(i);
			if (r >= 0) // string lookup
			{
				al.add(_strings.get(r));
			}
			else if (r < -Specials) // group lookup
			{
				al.add(match.GroupToStringImpl(-Specials - 1 - r));
			}
			else
			{
				switch (-Specials - 1 - r) // special insertion patterns
				{
					case LeftPortion:
						al.add(match.GetLeftSubstring());
						break;
					case RightPortion:
						al.add(match.GetRightSubstring());
						break;
					case LastGroup:
						al.add(match.LastGroupToStringImpl());
						break;
					case WholeString:
						al.add(match.GetOriginalString());
						break;
				}
			}
		}
	}

	/*
	 * The original pattern string
	 */
	public String getPattern()
	{
		return _rep;
	}

	/*
	 * Returns the replacement result for a single match
	 */
	public String Replacement(Match match)
	{
		StringBuilder sb = new StringBuilder();

		ReplacementImpl(sb, match);

		return sb.toString();
	}

	/*
	 * Three very similar algorithms appear below: replace (pattern),
	 * replace (evaluator), and split.
	 */


	/*
	 * Replaces all ocurrances of the regex in the string with the
	 * replacement pattern.
	 *
	 * Note that the special case of no matches is handled on its own:
	 * with no matches, the input string is returned unchanged.
	 * The right-to-left case is split out because StringBuilder
	 * doesn't handle right-to-left string building directly very well.
	 */
	public String Replace(Regex regex, String input, int count, int startat)
	{
		Match match;

		if (count < -1)
		{
			throw new IllegalArgumentException("count : " + SR.GetString(SR.CountTooSmall));
		}
		if (startat < 0 || startat > input.length())
		{
			throw new IllegalArgumentException("startat : " + SR.GetString(SR.BeginIndexNotNegative));
		}

		if (count == 0)
		{
			return input;
		}

		match = regex.Match(input, startat);
		if (!match.getSuccess())
		{
			return input;
		}
		else
		{
			StringBuilder sb;

			if (!regex.getRightToLeft())
			{
				sb = new StringBuilder();
				int prevat = 0;

				do
				{
					if (match.getIndex() != prevat)
					{
						sb.append(input, prevat, match.getIndex() - prevat);
					}

					prevat = match.getIndex() + match.getLength();
					ReplacementImpl(sb, match);
					if (--count == 0)
					{
						break;
					}

					match = match.NextMatch();
				} while (match.getSuccess());

				if (prevat < input.length())
				{
					sb.append(input, prevat, input.length() - prevat);
				}
			}
			else
			{
				java.util.ArrayList<String> al = new java.util.ArrayList<String>();
				int prevat = input.length();

				do
				{
					if (match.getIndex() + match.getLength() != prevat)
					{
						al.add(DotNetToJavaStringHelper.substring(input, match.getIndex() + match.getLength(), prevat - match.getIndex() - match.getLength()));
					}

					prevat = match.getIndex();
					ReplacementImplRTL(al, match);
					if (--count == 0)
					{
						break;
					}

					match = match.NextMatch();
				} while (match.getSuccess());

				sb = new StringBuilder();

				if (prevat > 0)
				{
					sb.append(input, 0, prevat);
				}

				for (int i = al.size() - 1; i >= 0; i--)
				{
					sb.append(al.get(i));
				}
			}

			return sb.toString();
		}
	}

	/*
	 * Replaces all ocurrances of the regex in the string with the
	 * replacement evaluator.
	 *
	 * Note that the special case of no matches is handled on its own:
	 * with no matches, the input string is returned unchanged.
	 * The right-to-left case is split out because StringBuilder
	 * doesn't handle right-to-left string building directly very well.
	 */
	public static String Replace(MatchEvaluator evaluator, Regex regex, String input, int count, int startat)
	{
		Match match;

		if (evaluator == null)
		{
			throw new IllegalArgumentException("evaluator");
		}
		if (count < -1)
		{
			throw new IllegalArgumentException("count : " + SR.GetString(SR.CountTooSmall));
		}
		if (startat < 0 || startat > input.length())
		{
			throw new IllegalArgumentException("startat : " + SR.GetString(SR.BeginIndexNotNegative));
		}

		if (count == 0)
		{
			return input;
		}

		match = regex.Match(input, startat);

		if (!match.getSuccess())
		{
			return input;
		}
		else
		{
			StringBuilder sb;

			if (!regex.getRightToLeft())
			{
				sb = new StringBuilder();
				int prevat = 0;

				do
				{
					if (match.getIndex() != prevat)
					{
						sb.append(input, prevat, match.getIndex() - prevat);
					}

					prevat = match.getIndex() + match.getLength();

					sb.append(evaluator.invoke(match));

					if (--count == 0)
					{
						break;
					}

					match = match.NextMatch();
				} while (match.getSuccess());

				if (prevat < input.length())
				{
					sb.append(input, prevat, input.length() - prevat);
				}
			}
			else
			{
				java.util.ArrayList<String> al = new java.util.ArrayList<String>();
				int prevat = input.length();

				do
				{
					if (match.getIndex() + match.getLength() != prevat)
					{
						al.add(DotNetToJavaStringHelper.substring(input, match.getIndex() + match.getLength(), prevat - match.getIndex() - match.getLength()));
					}

					prevat = match.getIndex();

					al.add(evaluator.invoke(match));

					if (--count == 0)
					{
						break;
					}

					match = match.NextMatch();
				} while (match.getSuccess());

				sb = new StringBuilder();

				if (prevat > 0)
				{
					sb.append(input, 0, prevat);
				}

				for (int i = al.size() - 1; i >= 0; i--)
				{
					sb.append(al.get(i));
				}
			}

			return sb.toString();
		}
	}

	/*
	 * Does a split. In the right-to-left case we reorder the
	 * array to be forwards.
	 */
	public static String[] split(Regex regex, String input, int count, int startat)
	{
		Match match;
		String[] result;

		if (count < 0)
		{
			throw new IllegalArgumentException("count : " + SR.GetString(SR.CountTooSmall));
		}

		if (startat < 0 || startat > input.length())
		{
			throw new IllegalArgumentException("startat : " + SR.GetString(SR.BeginIndexNotNegative));
		}

		if (count == 1)
		{
			result = new String[1];
			result[0] = input;
			return result;
		}

		count -= 1;

		match = regex.Match(input, startat);

		if (!match.getSuccess())
		{
			result = new String[1];
			result[0] = input;
			return result;
		}
		else
		{
			java.util.ArrayList<String> al = new java.util.ArrayList<String>();

			if (!regex.getRightToLeft())
			{
				int prevat = 0;

				for (;;)
				{
					al.add(input.substring(prevat, match.getIndex()));

					prevat = match.getIndex() + match.getLength();

					// add all matched capture groups to the list.
					for (int i = 1; i < match.getGroups().size(); i++)
					{
						if (match.IsMatched(i))
						{
							al.add(match.getGroups().getItem(i).toString());
						}
					}

					if (--count == 0)
					{
						break;
					}

					match = match.NextMatch();

					if (!match.getSuccess())
					{
						break;
					}
				}

				al.add(input.substring(prevat, input.length()));
			}
			else
			{
				int prevat = input.length();

				for (;;)
				{
					al.add(DotNetToJavaStringHelper.substring(input, match.getIndex() + match.getLength(), prevat - match.getIndex() - match.getLength()));

					prevat = match.getIndex();

					// add all matched capture groups to the list.
					for (int i = 1; i < match.getGroups().size(); i++)
					{
						if (match.IsMatched(i))
						{
							al.add(match.getGroups().getItem(i).toString());
						}
					}

					if (--count == 0)
					{
						break;
					}

					match = match.NextMatch();

					if (!match.getSuccess())
					{
						break;
					}
				}

				al.add(input.substring(0, prevat));

				//al.Reverse(0, al.size());
				Collections.reverse(al);
			}

			return al.toArray(new String[0]);
		}
	}
}
package com.zoe.framework.regex;

public final class RegexPrefix
{
	public String _prefix;
	public boolean _caseInsensitive;

	public static RegexPrefix _empty = new RegexPrefix("", false);

	public RegexPrefix(String prefix, boolean ci)
	{
		_prefix = prefix;
		_caseInsensitive = ci;
	}

	public String getPrefix()
	{
		return _prefix;
	}

	public boolean getCaseInsensitive()
	{
		return _caseInsensitive;
	}
	public static RegexPrefix getEmpty()
	{
		return _empty;
	}
}
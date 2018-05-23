package com.zoe.framework.regex;

import java.util.Locale;

public final class RegexFC
{
	public RegexCharClass _cc;
	public boolean _nullable;
	public boolean _caseInsensitive;

	public RegexFC(boolean nullable)
	{
		_cc = new RegexCharClass();
		_nullable = nullable;
	}

	public RegexFC(char ch, boolean not, boolean nullable, boolean caseInsensitive)
	{
		_cc = new RegexCharClass();

		if (not)
		{
			if (ch > 0)
			{
				_cc.AddRange('\0', (char)(ch - 1));
			}
			if (ch < 0xFFFF)
			{
				_cc.AddRange((char)(ch + 1), '\uFFFF');
			}
		}
		else
		{
			_cc.AddRange(ch, ch);
		}

		_caseInsensitive = caseInsensitive;
		_nullable = nullable;
	}

	public RegexFC(String charClass, boolean nullable, boolean caseInsensitive)
	{
		_cc = RegexCharClass.Parse(charClass);

		_nullable = nullable;
		_caseInsensitive = caseInsensitive;
	}

	public boolean AddFC(RegexFC fc, boolean concatenate)
	{
		if (!_cc.getCanMerge() || !fc._cc.getCanMerge())
		{
			return false;
		}

		if (concatenate)
		{
			if (!_nullable)
			{
				return true;
			}

			if (!fc._nullable)
			{
				_nullable = false;
			}
		}
		else
		{
			if (fc._nullable)
			{
				_nullable = true;
			}
		}

		_caseInsensitive |= fc._caseInsensitive;
		_cc.AddCharClass(fc._cc);
		return true;
	}

	public String GetFirstChars(Locale culture)
	{
		if (_caseInsensitive)
		{
			_cc.AddLowercase(culture);
		}

		return _cc.ToStringClass();
	}

	public boolean IsCaseInsensitive()
	{
		return _caseInsensitive;
	}
}
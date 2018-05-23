package com.zoe.framework.regex;

/*
 * MatchSparse is for handling the case where slots are
 * sparsely arranged (e.g., if somebody says use slot 100000)
 */
public class MatchSparse extends Match
{
	// the lookup hashtable
//C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s shadowing via the 'new' keyword:
//ORIGINAL LINE: internal new Hashtable _caps;
	public java.util.Hashtable _caps;

	/*
	 * Nonpublic constructor
	 */
	public MatchSparse(Regex regex, java.util.Hashtable caps, int capcount, String text, int begpos, int len, int startpos)
	{
		super(regex, capcount, text, begpos, len, startpos);

		_caps = caps;
	}

	@Override
	public GroupCollection getGroups()
	{
		if (_groupcoll == null)
		{
			_groupcoll = new GroupCollection(this, _caps);
		}

		return _groupcoll;
	}

}
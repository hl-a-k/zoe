package com.zoe.framework.regex;

/*
 * Used to cache byte codes or compiled factories
 */
public final class CachedCodeEntry
{
	public String _key;
	public RegexCode _code;
	public java.util.Hashtable _caps;
	public java.util.Hashtable _capnames;
	public String[] _capslist;
	public int _capsize;
	public RegexRunnerFactory _factory;
	public ExclusiveReference _runnerref;
	public SharedReference _replref;

	public CachedCodeEntry(String key, java.util.Hashtable capnames, String[] capslist, RegexCode code, java.util.Hashtable caps, int capsize, ExclusiveReference runner, SharedReference repl)
	{

		_key = key;
		_capnames = capnames;
		_capslist = capslist;

		_code = code;
		_caps = caps;
		_capsize = capsize;

		_runnerref = runner;
		_replref = repl;
	}

	public void AddCompiled(RegexRunnerFactory factory)
	{
		_factory = factory;
		_code = null;
	}
}
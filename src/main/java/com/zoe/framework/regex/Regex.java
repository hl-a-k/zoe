package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="Regex.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// The Regex class represents a single compiled instance of a regular
// expression.


import java.io.Serializable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/** <devdoc>
	<p>
	   Represents an immutable, compiled regular expression. Also
	   contains static methods that allow use of regular expressions without instantiating
	   a Regex explicitly.
	</p>
 </devdoc>
*/
public class Regex implements Serializable
{

	// Fields used by precompiled regexes
	protected String pattern;
	protected RegexRunnerFactory factory; // if compiled, this is the RegexRunner subclass

	protected RegexOptions roptions; // the top-level options from the options string


	// *********** Match timeout fields { ***********

	// We need this because time is queried using Environment.TickCount for performance reasons
	// (Environment.TickCount returns millisecs as an int and cycles):
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [NonSerialized()] private static readonly TimeSpan MaximumMatchTimeout = TimeSpan.FromMilliseconds(Int32.MaxValue - 1);
	private static final TimeSpan MaximumMatchTimeout = TimeSpan.FromMilliseconds(Integer.MAX_VALUE - 1);

	// InfiniteMatchTimeout specifies that match timeout is switched OFF. It allows for faster code paths
	// compared to simply having a very large timeout.
	// We do not want to ask users to use System.Threading.Timeout.InfiniteTimeSpan as a parameter because:
	//   (1) We do not want to imply any relation between having using a RegEx timeout and using multi-threading.
	//   (2) We do not want to require users to take ref to a contract assembly for threading just to use RegEx.
	//       There may in theory be a SKU that has RegEx, but no multithreading.
	// We create a public Regex.InfiniteMatchTimeout constant, which for consistency uses the save underlying
	// value as Timeout.InfiniteTimeSpan creating an implementation detail dependency only.
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [NonSerialized()] public static readonly TimeSpan InfiniteMatchTimeout = Timeout.InfiniteTimeSpan;
	public static final TimeSpan InfiniteMatchTimeout = Timeout.InfiniteTimeSpan;

	// All these protected internal fields in this class really should not be protected. The historic reason
	// for this is that classes extending Regex that are generated via CompileToAssembly rely on the fact that
	// these are accessible as protected in order to initialise them in the generated constructor of the
	// extending class. We should update this initialisation logic to using a protected constructor, but until
	// that is done we stick to the existing pattern however ugly it may be.
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [OptionalField(VersionAdded = 2)] protected internal TimeSpan internalMatchTimeout;
	protected TimeSpan internalMatchTimeout = new TimeSpan(); // timeout for the execution of this regex


	// During static initialisation of Regex we check 
	private static final String DefaultMatchTimeout_ConfigKeyName = "REGEX_DEFAULT_MATCH_TIMEOUT";


	// FallbackDefaultMatchTimeout specifies the match timeout to use if no other timeout was specified
	// by one means or another. For now it is set to InfiniteMatchTimeout, meaning timeouts are OFF by
	// default (for Dev12 we plan to set a positive value).
	// Having this field is helpful to read the code as it makes it clear when we mean
	// "default that is currently no-timeouts" and when we mean "actually no-timeouts".
	// In Silverlight, DefaultMatchTimeout is always set to FallbackDefaultMatchTimeout,
	// on desktop, DefaultMatchTimeout can be configured via AppDomain and falls back to
	// FallbackDefaultMatchTimeout, if no AppDomain setting is present (see InitDefaultMatchTimeout()).
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [NonSerialized()] internal static readonly TimeSpan FallbackDefaultMatchTimeout = InfiniteMatchTimeout;
	public static final TimeSpan FallbackDefaultMatchTimeout = InfiniteMatchTimeout;


	// DefaultMatchTimeout specifies the match timeout to use if no other timeout was specified
	// by one means or another. Typically, it is set to InfiniteMatchTimeout in Dev 11
	// (we plan to set a positive timeout in Dev12).
	// Hosts (e.g.) ASP may set an AppDomain property via SetData to change the default value.        
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [NonSerialized()] internal static readonly TimeSpan DefaultMatchTimeout = InitDefaultMatchTimeout();
	public static final TimeSpan DefaultMatchTimeout = InitDefaultMatchTimeout();

	// *********** } match timeout fields ***********


	// desktop build still uses non-generic collections for AppCompat with .NET Framework 3.5 pre-compiled assemblies
	protected java.util.Hashtable caps;
	protected java.util.Hashtable capnames;
	protected String[] capslist; // if captures are sparse or named captures are used, this is the sorted list of names
	protected int capsize; // the size of the capture array

	public ExclusiveReference runnerref; // cached runner
	public SharedReference replref; // cached parsed replacement pattern
	public RegexCode code; // if interpreted, this is the code for RegexIntepreter
	public boolean refsInitialized = false;

	public static java.util.LinkedList<CachedCodeEntry> livecode = new java.util.LinkedList<CachedCodeEntry>(); // the cached of code and factories that are currently loaded
	public static int cacheSize = 15;

	public static final int MaxOptionShift = 10;

	protected Regex()
	{

		// If a compiled-to-assembly RegEx was generated using an earlier version, then internalMatchTimeout will be uninitialised.
		// Let's do it here.
		// In distant future, when RegEx generated using pre Dev11 are not supported any more, we can remove this to aid performance:

		this.internalMatchTimeout = DefaultMatchTimeout;
	}

	/*
	 * Compiles and returns a Regex object corresponding to the given pattern
	 */
	/** <devdoc>
		<p>
		   Creates and compiles a regular expression object for the specified regular
		   expression.
		</p>
	 </devdoc>
	*/
	public Regex(String pattern)
	{
		this(pattern, RegexOptions.None, DefaultMatchTimeout, false);
	}

	/*
	 * Returns a Regex object corresponding to the given pattern, compiled with
	 * the specified options.
	 */
	/** <devdoc>
		<p>
		   Creates and compiles a regular expression object for the
		   specified regular expression
		   with options that modify the pattern.
		</p>
	 </devdoc>
	*/
	public Regex(String pattern, RegexOptions options)
	{
		this(pattern, options, DefaultMatchTimeout, false);
	}

	public Regex(String pattern, RegexOptions options, TimeSpan matchTimeout)
	{
		this(pattern, options, matchTimeout, false);
	}

	private Regex(String pattern, RegexOptions options, TimeSpan matchTimeout, boolean useCache)
	{
		RegexTree tree;
		CachedCodeEntry cached = null;
		String cultureKey = null;

		if (pattern == null)
		{
			throw new IllegalArgumentException("pattern");
		}
//C# TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		if (options.getValue() < RegexOptions.None.getValue() || ((options.getValue()) >> MaxOptionShift) != 0)
		{
			throw new IllegalArgumentException("options");
		}
		if ((options.getValue() & RegexOptions.ECMAScript.getValue()) != 0 && (options.getValue() & ~(RegexOptions.ECMAScript.getValue() | RegexOptions.IgnoreCase.getValue() | RegexOptions.Multiline.getValue() | RegexOptions.Compiled.getValue() | RegexOptions.CultureInvariant.getValue())) != 0)
		{
			throw new IllegalArgumentException("options");
		}

		ValidateMatchTimeout(matchTimeout);

		// Try to look up this regex in the cache.  We do this regardless of whether useCache is true since there's
		// really no reason not to. 
		if ((options.getValue() & RegexOptions.CultureInvariant.getValue()) != 0)
		{
			cultureKey = Locale.CHINESE.toString(); // "English (United States)"
		}
		else
		{
			cultureKey =  Locale.getDefault().toString();
		}

		String key = (new Integer(options.getValue())).toString() + ":" + cultureKey + ":" + pattern;
		cached = LookupCachedAndUpdate(key);

		this.pattern = pattern;
		this.roptions = options;

		this.internalMatchTimeout = matchTimeout;

		if (cached == null)
		{
			// Parse the input
			tree = RegexParser.Parse(pattern, roptions);

			// Extract the relevant information
			capnames = tree._capnames;
			capslist = tree._capslist;
			code = RegexWriter.Write(tree);
			caps = code._caps;
			capsize = code._capsize;

			InitializeReferences();

			tree = null;
			if (useCache)
			{
				cached = CacheCode(key);
			}
		}
		else
		{
			caps = cached._caps;
			capnames = cached._capnames;
			capslist = cached._capslist;
			capsize = cached._capsize;
			code = cached._code;
			factory = cached._factory;
			runnerref = cached._runnerref;
			replref = cached._replref;
			refsInitialized = true;
		}
	}

	//* Note: "&lt;" is the XML entity for smaller ("<").
	/** 
	 Validates that the specified match timeout value is valid.
	 The valid range is <code>TimeSpan.Zero &lt; matchTimeout &lt;= Regex.MaximumMatchTimeout</code>.
	 
	 @param matchTimeout The timeout value to validate.
	 @exception IllegalArgumentException If the specified timeout is not within a valid range.
	 
	*/
	protected static void ValidateMatchTimeout(TimeSpan matchTimeout)
	{

		if (TimeSpan.OpEquality(InfiniteMatchTimeout, matchTimeout))
		{
			return;
		}

		// Change this to make sure timeout is not longer then Environment.Ticks cycle length:
		if (TimeSpan.ZERO.getTicks() < matchTimeout.getTicks() && matchTimeout.getTicks() <= MaximumMatchTimeout.getTicks())
		{
			return;
		}

		throw new IllegalArgumentException("matchTimeout");
	}

	/** 
	 Specifies the default RegEx matching timeout value (i.e. the timeout that will be used if no
	 explicit timeout is specified).       
	 The default is queried from the current <code>AppDomain</code> through <code>GetData</code> using
	 the key specified in <code>Regex.DefaultMatchTimeout_ConfigKeyName</code>. For that key, the
	 current <code>AppDomain</code> is expected to either return <code>null</code> or a <code>TimeSpan</code>
	 value specifying the default timeout within a valid range.
	 If the AddDomain's data value for that key is not a <code>TimeSpan</code> value or if it is outside the
	 valid range, an exception is thrown which will result in a <code>TypeInitializationException</code> for RegEx.
	 If the AddDomain's data value for that key is <code>null</code>, a fallback value is returned
	 (see <code>FallbackDefaultMatchTimeout</code> in code).
	 
	 @return The default RegEx matching timeout for this AppDomain        
	*/
	private static TimeSpan InitDefaultMatchTimeout()
	{

		// Query AppDomain:
		//AppDomain ad = AppDomain.CurrentDomain;
		//Object defTmOut = ad.GetData(DefaultMatchTimeout_ConfigKeyName);

		Object defTmOut = "".equals("") ? null : 1;
		// If no default is specified, use fallback:
		if (defTmOut == null)
		{
			return FallbackDefaultMatchTimeout;
		}

		// If default has invalid type, throw. It will result in a TypeInitializationException:
		if (!(defTmOut instanceof TimeSpan))
		{
			throw new ClassCastException(SR.GetString(SR.IllegalDefaultRegexMatchTimeoutInAppDomain, DefaultMatchTimeout_ConfigKeyName));
		}

		// Convert default value:
		TimeSpan defaultTimeout = (TimeSpan) defTmOut;

		// If default timeout is outside the valid range, throw. It will result in a TypeInitializationException:
		try
		{
			ValidateMatchTimeout(defaultTimeout);

		}
		catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException(SR.GetString(SR.IllegalDefaultRegexMatchTimeoutInAppDomain, DefaultMatchTimeout_ConfigKeyName));
		}

		// We are good:
		return defaultTimeout;
	} // private static TimeSpan InitDefaultMatchTimeout

	/*
	 * Escape metacharacters within the string
	 */
	/** <devdoc>
		<p>
		   Escapes 
			  a minimal set of metacharacters (\, *, +, ?, |, {, [, (, ), ^, $, ., #, and
			  whitespace) by replacing them with their \ codes. This converts a string so that
			  it can be used as a constant within a regular expression safely. (Note that the
			  reason # and whitespace must be escaped is so the string can be used safely
			  within an expression parsed with x mode. If future Regex features add
			  additional metacharacters, developers should depend on Escape to escape those
			  characters as well.)
		   </p>
		</devdoc>
	*/
	public static String Escape(String str)
	{
		if (str == null)
		{
			throw new IllegalArgumentException("str");
		}

		return RegexParser.Escape(str);
	}

	/*
	 * Unescape character codes within the string
	 */
	/** <devdoc>
		<p>
		   Unescapes any escaped characters in the input string.
		</p>
	 </devdoc>
	*/
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [SuppressMessage("Microsoft.Naming","CA1704:IdentifiersShouldBeSpelledCorrectly", MessageId="Unescape", Justification="[....]: already shipped since v1 - can't fix without causing a breaking change")] public static String Unescape(String str)
	public static String Unescape(String str)
	{
		if (str == null)
		{
			throw new IllegalArgumentException("str");
		}

		return RegexParser.Unescape(str);
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [SuppressMessage("Microsoft.Concurrency", "CA8001", Justification = "Reviewed for thread-safety")] public static int CacheSize
	public static int getCacheSize()
	{
		return cacheSize;
	}
	public static void setCacheSize(int value)
	{
		if (value < 0)
		{
			throw new IllegalArgumentException("value");
		}

		cacheSize = value;
		if (livecode.size() > cacheSize)
		{
			synchronized (livecode)
			{
				while (livecode.size() > cacheSize)
				{
					livecode.removeLast();
				}
			}
		}
	}

	/** <devdoc>
		<p>
		   Returns the options passed into the constructor
		</p>
	 </devdoc>
	*/
	public final RegexOptions getOptions()
	{
		return roptions;
	}


	/** 
	 The match timeout used by this Regex instance.
	*/
	public final TimeSpan getMatchTimeout()
	{
		return internalMatchTimeout;
	}


	/*
	 * True if the regex is leftward
	 */
	/** <devdoc>
		<p>
		   Indicates whether the regular expression matches from right to
		   left.
		</p>
	 </devdoc>
	*/
	public final boolean getRightToLeft()
	{
		return UseOptionR();
	}

	/** <devdoc>
		<p>
		   Returns the regular expression pattern passed into the constructor
		</p>
	 </devdoc>
	*/
	@Override
	public String toString()
	{
		return pattern;
	}

	/*
	 * Returns an array of the group names that are used to capture groups
	 * in the regular expression. Only needed if the regex is not known until
	 * runtime, and one wants to extract captured groups. (Probably unusual,
	 * but supplied for completeness.)
	 */
	/** <devdoc>
		Returns 
		   the GroupNameCollection for the regular expression. This collection contains the
		   set of strings used to name capturing groups in the expression. 
		</devdoc>
	*/
	public final String[] GetGroupNames()
	{
		String[] result;

		if (capslist == null)
		{
			int max = capsize;
			result = new String[max];

			for (int i = 0; i < max; i++)
			{
				result[i] = String.valueOf(i);
			}
		}
		else
		{
			result = new String[capslist.length];

			System.arraycopy(capslist, 0, result, 0, capslist.length);
		}

		return result;
	}

	/*
	 * Returns an array of the group numbers that are used to capture groups
	 * in the regular expression. Only needed if the regex is not known until
	 * runtime, and one wants to extract captured groups. (Probably unusual,
	 * but supplied for completeness.)
	 */
	/** <devdoc>
		returns 
		   the integer group number corresponding to a group name. 
		</devdoc>
	*/
	public final int[] GetGroupNumbers()
	{
		int[] result;

		if (caps == null)
		{
			int max = capsize;
			result = new int[max];

			for (int i = 0; i < max; i++)
			{
				result[i] = i;
			}
		}
		else
		{
			result = new int[caps.size()];

			Iterator de = caps.entrySet().iterator();
			while (de.hasNext())
			{
				Map.Entry entry = (Map.Entry)de.next();
				result[(int)entry.getValue()] = (int)entry.getKey();
			}
		}

		return result;
	}

	/*
	 * Given a group number, maps it to a group name. Note that nubmered
	 * groups automatically get a group name that is the decimal string
	 * equivalent of its number.
	 *
	 * Returns null if the number is not a recognized group number.
	 */
	/** <devdoc>
		<p>
		   Retrieves a group name that corresponds to a group number.
		</p>
	 </devdoc>
	*/
	public final String GroupNameFromNumber(int i)
	{
		if (capslist == null)
		{
			if (i >= 0 && i < capsize)
			{
				return (new Integer(i)).toString();
			}

			return "";
		}
		else
		{
			if (caps != null)
			{
				Object obj = caps.get(i);
				if (obj == null)
				{
					return "";
				}
				i = (Integer)obj;
			}

			if (i >= 0 && i < capslist.length)
			{
				return capslist[i];
			}

			return "";
		}
	}

	/*
	 * Given a group name, maps it to a group number. Note that nubmered
	 * groups automatically get a group name that is the decimal string
	 * equivalent of its number.
	 *
	 * Returns -1 if the name is not a recognized group name.
	 */
	/** <devdoc>
		<p>
		   Returns a group number that corresponds to a group name.
		</p>
	 </devdoc>
	*/
	public final int GroupNumberFromName(String name)
	{
		int result = -1;

		if (name == null)
		{
			throw new IllegalArgumentException("name");
		}

		// look up name if we have a hashtable of names
		if (capnames != null)
		{
			Object ret = capnames.get(name);
			if (ret == null)
			{
				return -1;
			}
			return (Integer)ret;
		}

		// convert to an int if it looks like a number
		result = 0;
		for (int i = 0; i < name.length(); i++)
		{
			char ch = name.charAt(i);

			if (ch > '9' || ch < '0')
			{
				return -1;
			}

			result *= 10;
			result += (ch - '0');
		}

		// return int if it's in range
		if (result >= 0 && result < capsize)
		{
			return result;
		}

		return -1;
	}

	/*
	 * Static version of simple IsMatch call
	 */
	/**    <devdoc>
		   <p>
			  Searches the input 
				 string for one or more occurrences of the text supplied in the pattern
				 parameter.
		   </p>
		</devdoc>
	*/
	public static boolean IsMatch(String input, String pattern)
	{
		return IsMatch(input, pattern, RegexOptions.None, DefaultMatchTimeout);
	}

	/*
	 * Static version of simple IsMatch call
	 */
	/** <devdoc>
		<p>
		   Searches the input string for one or more occurrences of the text 
			  supplied in the pattern parameter with matching options supplied in the options
			  parameter.
		   </p>
		</devdoc>
	*/
	public static boolean IsMatch(String input, String pattern, RegexOptions options)
	{
		return IsMatch(input, pattern, options, DefaultMatchTimeout);
	}

	public static boolean IsMatch(String input, String pattern, RegexOptions options, TimeSpan matchTimeout)
	{
		return (new Regex(pattern, options, matchTimeout, true)).IsMatch(input);
	}

	/*
	 * Returns true if the regex finds a match within the specified string
	 */
	/** <devdoc>
		<p>
		   Searches the input string for one or 
			  more matches using the previous pattern, options, and starting
			  position.
		   </p>
		</devdoc>
	*/
	public final boolean IsMatch(String input)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return IsMatch(input, UseOptionR() ? input.length() : 0);
	}

	/*
	 * Returns true if the regex finds a match after the specified position
	 * (proceeding leftward if the regex is leftward and rightward otherwise)
	 */
	/** <devdoc>
		<p>
		   Searches the input 
			  string for one or more matches using the previous pattern and options, with
			  a new starting position.
		</p>
	 </devdoc>
	*/
	public final boolean IsMatch(String input, int startat)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return (null == Run(true, -1, input, 0, input.length(), startat));
	}

	/*
	 * Static version of simple Match call
	 */
	/**    <devdoc>
		   <p>
			  Searches the input string for one or more occurrences of the text 
				 supplied in the pattern parameter.
		   </p>
		</devdoc>
	*/
	public static Match Match(String input, String pattern)
	{
		return Match(input, pattern, RegexOptions.None, DefaultMatchTimeout);
	}

	/*
	 * Static version of simple Match call
	 */
	/** <devdoc>
		<p>
		   Searches the input string for one or more occurrences of the text 
			  supplied in the pattern parameter. Matching is modified with an option
			  string.
		   </p>
		</devdoc>
	*/
	public static Match Match(String input, String pattern, RegexOptions options)
	{
		return Match(input, pattern, options, DefaultMatchTimeout);
	}


	public static Match Match(String input, String pattern, RegexOptions options, TimeSpan matchTimeout)
	{
		return (new Regex(pattern, options, matchTimeout, true)).Match(input);
	}

	/*
	 * Finds the first match for the regular expression starting at the beginning
	 * of the string (or at the end of the string if the regex is leftward)
	 */
	/** <devdoc>
		<p>
		   Matches a regular expression with a string and returns
		   the precise result as a RegexMatch object.
		</p>
	 </devdoc>
	*/
	public final Match Match(String input)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return Match(input, UseOptionR() ? input.length() : 0);
	}

	/*
	 * Finds the first match, starting at the specified position
	 */
	/** <devdoc>
		Matches a regular expression with a string and returns
		the precise result as a RegexMatch object.
	 </devdoc>
	*/
	public final Match Match(String input, int startat)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return Run(false, -1, input, 0, input.length(), startat);
	}

	/*
	 * Finds the first match, restricting the search to the specified interval of
	 * the char array.
	 */
	/** <devdoc>
		<p>
		   Matches a
		   regular expression with a string and returns the precise result as a
		   RegexMatch object.
		</p>
	 </devdoc>
	*/
	public final Match Match(String input, int beginning, int length)
	{
		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return Run(false, -1, input, beginning, length, UseOptionR() ? beginning + length : beginning);
	}

	/*
	 * Static version of simple Matches call
	 */
	/**    <devdoc>
		   <p>
			  Returns all the successful matches as if Match were
			  called iteratively numerous times.
		   </p>
		</devdoc>
	*/
	public static MatchCollection Matches(String input, String pattern)
	{
		return Matches(input, pattern, RegexOptions.None, DefaultMatchTimeout);
	}

	/*
	 * Static version of simple Matches call
	 */
	/** <devdoc>
		<p>
		   Returns all the successful matches as if Match were called iteratively
		   numerous times.
		</p>
	 </devdoc>
	*/
	public static MatchCollection Matches(String input, String pattern, RegexOptions options)
	{
		return Matches(input, pattern, options, DefaultMatchTimeout);
	}

	public static MatchCollection Matches(String input, String pattern, RegexOptions options, TimeSpan matchTimeout)
	{
		return (new Regex(pattern, options, matchTimeout, true)).Matches(input);
	}

	/*
	 * Finds the first match for the regular expression starting at the beginning
	 * of the string Enumerator(or at the end of the string if the regex is leftward)
	 */
	/** <devdoc>
		<p>
		   Returns
		   all the successful matches as if Match was called iteratively numerous
		   times.
		</p>
	 </devdoc>
	*/
	public final MatchCollection Matches(String input)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return Matches(input, UseOptionR() ? input.length() : 0);
	}

	/*
	 * Finds the first match, starting at the specified position
	 */
	/** <devdoc>
		<p>
		   Returns
		   all the successful matches as if Match was called iteratively numerous
		   times.
		</p>
	 </devdoc>
	*/
	public final MatchCollection Matches(String input, int startat)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return new MatchCollection(this, input, 0, input.length(), startat);
	}

	/*
	 * Static version of simple Replace call
	 */
	/** <devdoc>
		<p>
		   Replaces 
			  all occurrences of the pattern with the <paramref name="replacement"/> pattern, starting at
			  the first character in the input string. 
		   </p>
		</devdoc>
	*/
	public static String Replace(String input, String pattern, String replacement)
	{
		return Replace(input, pattern, replacement, RegexOptions.None, DefaultMatchTimeout);
	}

	/*
	 * Static version of simple Replace call
	 */
	/** <devdoc>
		<p>
		   Replaces all occurrences of 
			  the <paramref name="pattern "/>with the <paramref name="replacement "/>
			  pattern, starting at the first character in the input string. 
		   </p>
		</devdoc>
	*/
	public static String Replace(String input, String pattern, String replacement, RegexOptions options)
	{
		return Replace(input, pattern, replacement, options, DefaultMatchTimeout);
	}

	public static String Replace(String input, String pattern, String replacement, RegexOptions options, TimeSpan matchTimeout)
	{
		return (new Regex(pattern, options, matchTimeout, true)).Replace(input, replacement);
	}

	/*
	 * Does the replacement
	 */
	/** <devdoc>
		<p>
		   Replaces all occurrences of 
			  the <paramref name="pattern "/> with the <paramref name="replacement"/> pattern, starting at the
			  first character in the input string, using the previous patten. 
		   </p>
		</devdoc>
	*/
	public final String Replace(String input, String replacement)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return Replace(input, replacement, -1, UseOptionR() ? input.length() : 0);
	}

	/*
	 * Does the replacement
	 */
	/** <devdoc>
		<p>
		Replaces all occurrences of the (previously defined) <paramref name="pattern "/>with the 
		<paramref name="replacement"/> pattern, starting at the first character in the input string. 
	 </p>
	 </devdoc>
	*/
	public final String Replace(String input, String replacement, int count)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return Replace(input, replacement, count, UseOptionR() ? input.length() : 0);
	}

	/*
	 * Does the replacement
	 */
	/** <devdoc>
		<p>
		Replaces all occurrences of the <paramref name="pattern "/>with the recent 
		<paramref name="replacement"/> pattern, starting at the character position 
		<paramref name="startat."/>
	 </p>
	 </devdoc>
	*/
	public final String Replace(String input, String replacement, int count, int startat)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		if (replacement == null)
		{
			throw new IllegalArgumentException("replacement");
		}

		// a little code to grab a cached parsed replacement object
		RegexReplacement repl = (RegexReplacement) replref.Get();

		if (repl == null || !repl.getPattern().equals(replacement))
		{
			repl = RegexParser.ParseReplacement(replacement, caps, capsize, capnames, this.roptions);
			replref.Cache(repl);
		}

		return repl.Replace(this, input, count, startat);
	}

	/*
	 * Static version of simple Replace call
	 */
	/** <devdoc>
		<p>
		Replaces all occurrences of the <paramref name="pattern "/>with the 
		<paramref name="replacement"/> pattern 
		<paramref name="."/>
	 </p>
	 </devdoc>
	*/
	public static String Replace(String input, String pattern, MatchEvaluator evaluator)
	{
		return Replace(input, pattern, evaluator, RegexOptions.None, DefaultMatchTimeout);
	}

	/*
	 * Static version of simple Replace call
	 */
	/** <devdoc>
		<p>
		Replaces all occurrences of the <paramref name="pattern "/>with the recent 
		<paramref name="replacement"/> pattern, starting at the first character<paramref name="."/>
	 </p>
	 </devdoc>
	*/
	public static String Replace(String input, String pattern, MatchEvaluator evaluator, RegexOptions options)
	{
		return Replace(input, pattern, evaluator, options, DefaultMatchTimeout);
	}

	public static String Replace(String input, String pattern, MatchEvaluator evaluator, RegexOptions options, TimeSpan matchTimeout)
	{
		return (new Regex(pattern, options, matchTimeout, true)).Replace(input, evaluator);
	}

	/*
	 * Does the replacement
	 */
	/** <devdoc>
		<p>
		Replaces all occurrences of the <paramref name="pattern "/>with the recent 
		<paramref name="replacement"/> pattern, starting at the first character 
		position<paramref name="."/>
	 </p>
	 </devdoc>
	*/
	public final String Replace(String input, MatchEvaluator evaluator)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return Replace(input, evaluator, -1, UseOptionR() ? input.length() : 0);
	}

	/*
	 * Does the replacement
	 */
	/** <devdoc>
		<p>
		Replaces all occurrences of the <paramref name="pattern "/>with the recent 
		<paramref name="replacement"/> pattern, starting at the first character 
		position<paramref name="."/>
	 </p>
	 </devdoc>
	*/
	public final String Replace(String input, MatchEvaluator evaluator, int count)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return Replace(input, evaluator, count, UseOptionR() ? input.length() : 0);
	}

	/*
	 * Does the replacement
	 */
	/** <devdoc>
		<p>
		Replaces all occurrences of the (previouly defined) <paramref name="pattern "/>with 
		   the recent <paramref name="replacement"/> pattern, starting at the character
		position<paramref name=" startat."/> 
	 </p>
	 </devdoc>
	*/
	public final String Replace(String input, MatchEvaluator evaluator, int count, int startat)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		//MatchEvaluator tempVar = (Match match) -> return evaluator(match);
		return RegexReplacement.Replace(evaluator, this, input, count, startat);
	}

	/*
	 * Static version of simple Split call
	 */
	/**    <devdoc>
		   <p>
			  Splits the <paramref name="input "/>string at the position defined
			  by <paramref name="pattern"/>.
		   </p>
		</devdoc>
	*/
	public static String[] split(String input, String pattern)
	{
		return split(input, pattern, RegexOptions.None, DefaultMatchTimeout);
	}

	/*
	 * Static version of simple Split call
	 */
	/** <devdoc>
		<p>
		   Splits the <paramref name="input "/>string at the position defined by <paramref name="pattern"/>.
		</p>
	 </devdoc>
	*/
	public static String[] split(String input, String pattern, RegexOptions options)
	{
		return split(input, pattern, options, DefaultMatchTimeout);
	}

	public static String[] split(String input, String pattern, RegexOptions options, TimeSpan matchTimeout)
	{
		return (new Regex(pattern, options, matchTimeout, true)).split(java.util.regex.Pattern.quote(input.toString()), -1);
	}

	/*
	 * Does a split
	 */
	/** <devdoc>
		<p>
		   Splits the <paramref name="input "/>string at the position defined by
		   a previous <paramref name="pattern"/>
		   .
		</p>
	 </devdoc>
	*/
	public final String[] split(String input)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return split(input, 0, UseOptionR() ? input.length() : 0);
	}

	/*
	 * Does a split
	 */
	/** <devdoc>
		<p>
		   Splits the <paramref name="input "/>string at the position defined by a previous
		<paramref name="pattern"/> . 
		</p>
	 </devdoc>
	*/
	public final String[] split(String input, int count)
	{

		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return RegexReplacement.split(this, input, count, UseOptionR() ? input.length() : 0);
	}

	/*
	 * Does a split
	 */
	/** <devdoc>
		<p>
		   Splits the <paramref name="input "/>string at the position defined by a previous
		<paramref name="pattern"/> . 
		</p>
	 </devdoc>
	*/
	public final String[] split(String input, int count, int startat)
	{
		if (input == null)
		{
			throw new IllegalArgumentException("input");
		}

		return RegexReplacement.split(this, input, count, startat);
	}

	/** <devdoc>
	 </devdoc>
	*/
	protected final void InitializeReferences()
	{
		if (refsInitialized)
		{
			throw new UnsupportedOperationException(SR.GetString(SR.OnlyAllowedOnce));
		}

		refsInitialized = true;
		runnerref = new ExclusiveReference();
		replref = new SharedReference();
	}


	/*
	 * Internal worker called by all the public APIs
	 */
	public final Match Run(boolean quick, int prevlen, String input, int beginning, int length, int startat)
	{
		Match match;
		RegexRunner runner = null;

		if (startat < 0 || startat > input.length())
		{
			throw new IllegalArgumentException("start : " + SR.GetString(SR.BeginIndexNotNegative));
		}

		if (length < 0 || length > input.length())
		{
			throw new IllegalArgumentException("length : " + SR.GetString(SR.LengthNotNegative));
		}

		// There may be a cached runner; grab ownership of it if we can.

		runner = (RegexRunner)runnerref.Get();

		// Create a RegexRunner instance if we need to

		if (runner == null)
		{
			// Use the compiled RegexRunner factory if the code was compiled to MSIL

			if (factory != null)
			{
				runner = factory.CreateInstance();
			}
			else
			{
				runner = new RegexInterpreter(code, UseOptionInvariant() ? Locale.CHINESE :  Locale.getDefault());
			}
		}

		try
		{
			// Do the scan starting at the requested position            
			match = runner.Scan(this, input, beginning, beginning + length, startat, prevlen, quick, internalMatchTimeout);
		}
		finally
		{
			// Release or fill the cache slot
			runnerref.Release(runner);
		}
		return match;
	}

	/*
	 * Find code cache based on options+pattern
	 */
	private static CachedCodeEntry LookupCachedAndUpdate(String key)
	{
		synchronized (livecode)
		{
			/*for (LinkedListNode<CachedCodeEntry> current = livecode.getFirst(); current != null; current = current.Next)
			{
				if (key.equals(current.Value._key))
				{
					// If we find an entry in the cache, move it to the head at the same time. 
					livecode.remove(current);
					livecode.addFirst(current);
					return current.Value;
				}
			}*/
			Iterator it = livecode.iterator();
			while (it.hasNext()) {
				CachedCodeEntry entry = (CachedCodeEntry) it.next();
				if (key.equals(entry._key)) {
					livecode.remove(entry);
					livecode.addFirst(entry);
					return entry;
				}
			}
		}

		return null;
	}

	/*
	 * Add current code to the cache
	 */
	private CachedCodeEntry CacheCode(String key)
	{
		CachedCodeEntry newcached = null;

		synchronized (livecode)
		{
			// first look for it in the cache and move it to the head
			/*for (LinkedListNode<CachedCodeEntry> current = livecode.First; current != null; current = current.Next)
			{
				if (key.equals(current.Value._key))
				{
					livecode.remove(current);
					livecode.addFirst(current);
					return current.Value;
				}
			}*/
			Iterator it = livecode.iterator();
			while (it.hasNext()) {
				CachedCodeEntry entry = (CachedCodeEntry) it.next();
				if (key.equals(entry._key)) {
					livecode.remove(entry);
					livecode.addFirst(entry);
					return entry;
				}
			}

			// it wasn't in the cache, so we'll add a new one.  Shortcut out for the case where cacheSize is zero.
			if (cacheSize != 0)
			{
				newcached = new CachedCodeEntry(key, capnames, capslist, code, caps, capsize, runnerref, replref);
				livecode.addFirst(newcached);
				if (livecode.size() > cacheSize)
				{
					livecode.removeLast();
				}
			}
		}

		return newcached;
	}

	/*
	 * True if the O option was set
	 */
	/** <internalonly/>
	 <devdoc>
	 </devdoc>
	*/
	protected final boolean UseOptionC()
	{
		return (roptions.getValue() & RegexOptions.Compiled.getValue()) != 0;
	}

	/*
	 * True if the L option was set
	 */
	/** <internalonly/>
	 <devdoc>
	 </devdoc>
	*/
	protected final boolean UseOptionR()
	{
		return (roptions.getValue() & RegexOptions.RightToLeft.getValue()) != 0;
	}

	public final boolean UseOptionInvariant()
	{
		return (roptions.getValue() & RegexOptions.CultureInvariant.getValue()) != 0;
	}
}
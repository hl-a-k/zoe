package com.zoe.framework.regex;


//------------------------------------------------------------------------------
// <copyright file="RegexRunner.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// This RegexRunner class is a base class for compiled regex code.

// Implementation notes:
// 
// RegexRunner provides a common calling convention and a common
// runtime environment for the interpreter and the compiled code.
//
// It provides the driver code that call's the subclass's Go()
// method for either scanning or direct execution.
//
// It also maintains memory allocation for the backtracking stack,
// the grouping stack and the longjump crawlstack, and provides
// methods to push new subpattern match results into (or remove
// backtracked results from) the Match instance.




/** <internalonly/>
*/

// 




//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [EditorBrowsable(EditorBrowsableState.Never)] public abstract class RegexRunner
public abstract class RegexRunner
{
	protected int runtextbeg; // beginning of text to search
	protected int runtextend; // end of text to search
	protected int runtextstart; // starting point for search

	protected String runtext; // text to search
	protected int runtextpos; // current position in text

	protected int [] runtrack; // The backtracking stack.  Opcodes use this to store data regarding
	protected int runtrackpos; // what they have matched and where to backtrack to.  Each "frame" on
												// the stack takes the form of [CodePosition Data1 Data2...], where 
												// CodePosition is the position of the current opcode and 
												// the data values are all optional.  The CodePosition can be negative, and
												// these values (also called "back2") are used by the BranchMark family of opcodes
												// to indicate whether they are backtracking after a successful or failed
												// match.  
												// When we backtrack, we pop the CodePosition off the stack, set the current
												// instruction pointer to that code position, and mark the opcode 
												// with a backtracking flag ("Back").  Each opcode then knows how to 
												// handle its own data. 

	protected int [] runstack; // This stack is used to track text positions across different opcodes.
	protected int runstackpos; // For example, in /(a*b)+/, the parentheses result in a SetMark/CaptureMark
												// pair. SetMark records the text position before we match a*b.  Then
												// CaptureMark uses that position to figure out where the capture starts.
												// Opcodes which push onto this stack are always paired with other opcodes
												// which will pop the value from it later.  A successful match should mean
												// that this stack is empty. 

	protected int [] runcrawl; // The crawl stack is used to keep track of captures.  Every time a group
	protected int runcrawlpos; // has a capture, we push its group number onto the runcrawl stack.  In
												// the case of a balanced match, we push BOTH groups onto the stack. 

	protected int runtrackcount; // count of states that may do backtracking

	protected Match runmatch; // result object
	protected Regex runregex; // regex object

	private int timeout; // timeout in millisecs (needed for actual)
	private boolean ignoreTimeout;
	private int timeoutOccursAt;


	// GPaperin: We have determined this value in a series of experiments where x86 retail
	// builds (ono-lab-optimised) were run on different pattern/input pairs. Larger values
	// of TimeoutCheckFrequency did not tend to increase performance; smaller values
	// of TimeoutCheckFrequency tended to slow down the execution.
	private static final int TimeoutCheckFrequency = 1000;
	private int timeoutChecksToSkip;

	protected RegexRunner()
	{
	}

	/*
	 * Scans the string to find the first match. Uses the Match object
	 * both to feed text in and as a place to store matches that come out.
	 *
	 * All the action is in the abstract Go() method defined by subclasses. Our
	 * responsibility is to load up the class members (as done here) before
	 * calling Go.
	 *
	 * <




*/
	protected final Match Scan(Regex regex, String text, int textbeg, int textend, int textstart, int prevlen, boolean quick)
	{
		return Scan(regex, text, textbeg, textend, textstart, prevlen, quick, regex.getMatchTimeout());
	}

	protected final Match Scan(Regex regex, String text, int textbeg, int textend, int textstart, int prevlen, boolean quick, TimeSpan timeout)
	{

		int bump;
		int stoppos;
		boolean initted = false;

		// We need to re-validate timeout here because Scan is historically protected and
		// thus there is a possibility it is called from user code:
		Regex.ValidateMatchTimeout(timeout);

		this.ignoreTimeout = (TimeSpan.OpEquality(Regex.InfiniteMatchTimeout, timeout));
		this.timeout = this.ignoreTimeout ? (int) Regex.InfiniteMatchTimeout.getTotalMilliseconds() : (int)(timeout.getTotalMilliseconds() + 0.5); // Round

		runregex = regex;
		runtext = text;
		runtextbeg = textbeg;
		runtextend = textend;
		runtextstart = textstart;

		bump = runregex.getRightToLeft() ? - 1 : 1;
		stoppos = runregex.getRightToLeft() ? runtextbeg : runtextend;

		runtextpos = textstart;

		// If previous match was empty or failed, advance by one before matching

		if (prevlen == 0)
		{
			if (runtextpos == stoppos)
			{
				return Match.getEmpty();
			}

			runtextpos += bump;
		}

		StartTimeoutWatch();

		for (; ;)
		{
			if (FindFirstChar())
			{

				CheckTimeout();

				if (!initted)
				{
					InitMatch();
					initted = true;
				}
				Go();

				if (runmatch._matchcount [0] > 0)
				{
					// <
					return TidyMatch(quick);
				}

				// reset state for another go
				runtrackpos = runtrack.length;
				runstackpos = runstack.length;
				runcrawlpos = runcrawl.length;
			}

			// failure!

			if (runtextpos == stoppos)
			{
				TidyMatch(true);
				return Match.getEmpty();
			}

			// <

			// Bump by one and start again

			runtextpos += bump;
		}

		// We never get here
	}

	private void StartTimeoutWatch()
	{

		if (ignoreTimeout)
		{
			return;
		}

		timeoutChecksToSkip = TimeoutCheckFrequency;

		// We are using Environment.TickCount and not Timewatch for performance reasons.
		// Environment.TickCount is an int that cycles. We intentionally let timeoutOccursAt
		// overflow it will still stay ahead of Environment.TickCount for comparisons made
		// in DoCheckTimeout():
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to an 'unchecked' block in Java:
		/*unchecked
		{
			timeoutOccursAt = Environment.TickCount + timeout;
		}*/
		timeoutOccursAt = 0 + timeout;
	}

	protected final void CheckTimeout()
	{

		if (ignoreTimeout)
		{
			return;
		}

		if (--timeoutChecksToSkip != 0)
		{
			return;
		}

		timeoutChecksToSkip = TimeoutCheckFrequency;
		DoCheckTimeout();
	}

	private void DoCheckTimeout()
	{

		// Note that both, Environment.TickCount and timeoutOccursAt are ints and can overflow and become negative.
		// See the comment in StartTimeoutWatch().

		//int currentMillis = Environment.TickCount;
		int currentMillis = 0;

		if (currentMillis < timeoutOccursAt)
		{
			return;
		}

		if (0 > timeoutOccursAt && 0 < currentMillis)
		{
			return;
		}

		throw new RegexMatchTimeoutException(runtext, runregex.pattern, TimeSpan.FromMilliseconds(timeout));
	}

	/*
	 * The responsibility of Go() is to run the regular expression at
	 * runtextpos and call Capture() on all the captured subexpressions,
	 * then to leave runtextpos at the ending position. It should leave
	 * runtextpos where it started if there was no match.
	 */
	protected abstract void Go();

	/*
	 * The responsibility of FindFirstChar() is to advance runtextpos
	 * until it is at the next position which is a candidate for the
	 * beginning of a successful match.
	 */
	protected abstract boolean FindFirstChar();

	/*
	 * InitTrackCount must initialize the runtrackcount field; this is
	 * used to know how large the initial runtrack and runstack arrays
	 * must be.
	 */
	protected abstract void InitTrackCount();

	/*
	 * Initializes all the data members that are used by Go()
	 */
	private void InitMatch()
	{
		// Use a hashtable'ed Match object if the capture numbers are sparse

		if (runmatch == null)
		{
			if (runregex.caps != null)
			{
				runmatch = new MatchSparse(runregex, runregex.caps, runregex.capsize, runtext, runtextbeg, runtextend - runtextbeg, runtextstart);
			}
			else
			{
				runmatch = new Match(runregex, runregex.capsize, runtext, runtextbeg, runtextend - runtextbeg, runtextstart);
			}
		}
		else
		{
			runmatch.Reset(runregex, runtext, runtextbeg, runtextend, runtextstart);
		}

		// note we test runcrawl, because it is the last one to be allocated
		// If there is an alloc failure in the middle of the three allocations,
		// we may still return to reuse this instance, and we want to behave
		// as if the allocations didn't occur. (we used to test _trackcount != 0)

		if (runcrawl != null)
		{
			runtrackpos = runtrack.length;
			runstackpos = runstack.length;
			runcrawlpos = runcrawl.length;
			return;
		}

		InitTrackCount();

		int tracksize = runtrackcount * 8;
		int stacksize = runtrackcount * 8;

		if (tracksize < 32)
		{
			tracksize = 32;
		}
		if (stacksize < 16)
		{
			stacksize = 16;
		}

		runtrack = new int [tracksize];
		runtrackpos = tracksize;

		runstack = new int [stacksize];
		runstackpos = stacksize;

		runcrawl = new int [32];
		runcrawlpos = 32;
	}

	/*
	 * Put match in its canonical form before returning it.
	 */
	private Match TidyMatch(boolean quick)
	{
		if (!quick)
		{
			Match match = runmatch;

			runmatch = null;

			match.Tidy(runtextpos);
			return match;
		}
		else
		{
			// in quick mode, a successful match returns null, and
			// the allocated match object is left in the cache

			return null;
		}
	}

	/*
	 * Called by the implemenation of Go() to increase the size of storage
	 */
	protected final void EnsureStorage()
	{
		if (runstackpos < runtrackcount * 4)
		{
			DoubleStack();
		}
		if (runtrackpos < runtrackcount * 4)
		{
			DoubleTrack();
		}
	}

	/*
	 * Called by the implemenation of Go() to decide whether the pos
	 * at the specified index is a boundary or not. It's just not worth
	 * emitting inline code for this logic.
	 */
	protected final boolean IsBoundary(int index, int startpos, int endpos)
	{
		return (index > startpos && RegexCharClass.IsWordChar(runtext.charAt (index - 1))) != (index < endpos && RegexCharClass.IsWordChar(runtext.charAt (index)));
	}

	protected final boolean IsECMABoundary(int index, int startpos, int endpos)
	{
		return (index > startpos && RegexCharClass.IsECMAWordChar(runtext.charAt (index - 1))) != (index < endpos && RegexCharClass.IsECMAWordChar(runtext.charAt (index)));
	}

	protected static boolean CharInSet(char ch, String set, String category)
	{
		String charClass = RegexCharClass.ConvertOldStringsToClass(set, category);
		return RegexCharClass.CharInClass(ch, charClass);
	}

	protected static boolean CharInClass(char ch, String charClass)
	{
		return RegexCharClass.CharInClass(ch, charClass);
	}

	/*
	 * Called by the implemenation of Go() to increase the size of the
	 * backtracking stack.
	 */
	protected final void DoubleTrack()
	{
		int [] newtrack;

		newtrack = new int [runtrack.length * 2];

		System.arraycopy(runtrack, 0, newtrack, runtrack.length, runtrack.length);
		runtrackpos += runtrack.length;
		runtrack = newtrack;
	}

	/*
	 * Called by the implemenation of Go() to increase the size of the
	 * grouping stack.
	 */
	protected final void DoubleStack()
	{
		int [] newstack;

		newstack = new int [runstack.length * 2];

		System.arraycopy(runstack, 0, newstack, runstack.length, runstack.length);
		runstackpos += runstack.length;
		runstack = newstack;
	}

	/*
	 * Increases the size of the longjump unrolling stack.
	 */
	protected final void DoubleCrawl()
	{
		int [] newcrawl;

		newcrawl = new int [runcrawl.length * 2];

		System.arraycopy(runcrawl, 0, newcrawl, runcrawl.length, runcrawl.length);
		runcrawlpos += runcrawl.length;
		runcrawl = newcrawl;
	}

	/*
	 * Save a number on the longjump unrolling stack
	 */
	protected final void Crawl(int i)
	{
		if (runcrawlpos == 0)
		{
			DoubleCrawl();
		}

		runcrawl [--runcrawlpos] = i;
	}

	/*
	 * Remove a number from the longjump unrolling stack
	 */
	protected final int Popcrawl()
	{
		return runcrawl [runcrawlpos++];
	}

	/*
	 * Get the height of the stack
	 */
	protected final int Crawlpos()
	{
		return runcrawl.length - runcrawlpos;
	}

	/*
	 * Called by Go() to capture a subexpression. Note that the
	 * capnum used here has already been mapped to a non-sparse
	 * index (by the code generator RegexWriter).
	 */
	protected final void Capture(int capnum, int start, int end)
	{
		if (end < start)
		{
			int T;

			T = end;
			end = start;
			start = T;
		}

		Crawl(capnum);
		runmatch.AddMatch(capnum, start, end - start);
	}

	/*
	 * Called by Go() to capture a subexpression. Note that the
	 * capnum used here has already been mapped to a non-sparse
	 * index (by the code generator RegexWriter).
	 */
	protected final void TransferCapture(int capnum, int uncapnum, int start, int end)
	{
		int start2;
		int end2;

		// these are the two intervals that are cancelling each other

		if (end < start)
		{
			int T;

			T = end;
			end = start;
			start = T;
		}

		start2 = MatchIndex(uncapnum);
		end2 = start2 + MatchLength(uncapnum);

		// The new capture gets the innermost defined interval

		if (start >= end2)
		{
			end = start;
			start = end2;
		}
		else if (end <= start2)
		{
			start = start2;
		}
		else
		{
			if (end > end2)
			{
				end = end2;
			}
			if (start2 > start)
			{
				start = start2;
			}
		}

		Crawl(uncapnum);
		runmatch.BalanceMatch(uncapnum);

		if (capnum != -1)
		{
			Crawl(capnum);
			runmatch.AddMatch(capnum, start, end - start);
		}
	}

	/*
	 * Called by Go() to revert the last capture
	 */
	protected final void Uncapture()
	{
		int capnum = Popcrawl();
		runmatch.RemoveMatch(capnum);
	}

	/*
	 * Call out to runmatch to get around visibility issues
	 */
	protected final boolean IsMatched(int cap)
	{
		return runmatch.IsMatched(cap);
	}

	/*
	 * Call out to runmatch to get around visibility issues
	 */
	protected final int MatchIndex(int cap)
	{
		return runmatch.MatchIndex(cap);
	}

	/*
	 * Call out to runmatch to get around visibility issues
	 */
	protected final int MatchLength(int cap)
	{
		return runmatch.MatchLength(cap);
	}

}
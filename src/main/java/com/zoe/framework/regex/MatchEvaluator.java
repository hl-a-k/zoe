package com.zoe.framework.regex;
/*
 * Callback class
 */
/** <devdoc>
 </devdoc>
*/
@FunctionalInterface
public interface MatchEvaluator
{
	String invoke(Match match);
}
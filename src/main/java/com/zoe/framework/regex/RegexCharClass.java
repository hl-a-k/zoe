package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexCharClass.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------

// This RegexCharClass class provides the "set of Unicode chars" functionality
// used by the regexp engine.

// The main function of RegexCharClass is as a builder to turn ranges, characters and 
// Unicode categories into a single string.  This string is used as a black box 
// representation of a character class by the rest of Regex.  The format is as follows.
//
// Char index   Use
//      0       Flags - currently this only holds the "negate" flag
//      1       length of the string representing the "set" portion, eg [a-z0-9] only has a "set"
//      2       length of the string representing the "category" portion, eg [\p{Lu}] only has a "category"
//      3...m   The set.  These are a series of ranges which define the characters included in the set. 
//              To determine if a given character is in the set, we binary search over this set of ranges
//              and see where the character should go.  Based on whether the ending index is odd or even,
//              we know if the character is in the set. 
//      m+1...n The categories.  This is a list of UnicodeCategory enum values which describe categories
//              included in this class.  


import java.util.Locale;

public final class RegexCharClass
{
	// instance data
	private java.util.ArrayList<SingleRange> _rangelist;
	private StringBuilder _categories;
	private boolean _canonical;
	private boolean _negate;
	private RegexCharClass _subtractor;

	// Constants
	private static final int FLAGS = 0;
	private static final int SETLENGTH = 1;
	private static final int CATEGORYLENGTH = 2;
	private static final int SETSTART = 3;

	private static final char Nullchar = '\0';
	private static final char Lastchar = '\uFFFF';

	private static final char GroupChar = (char) 0;


	private static final short SpaceConst = 100;
	private static final short NotSpaceConst = -100;

	private static final char ZeroWidthJoiner = '\u200D';
	private static final char ZeroWidthNonJoiner = '\u200C';


	private static final String InternalRegexIgnoreCase = "__InternalRegexIgnoreCase__";
	private static final String Space = "\u0064";
	private static final String NotSpace = NegateCategory(Space);
	private static String Word;
	private static String NotWord;

	public static String SpaceClass;
	public static String NotSpaceClass;
	public static String WordClass;
	public static String NotWordClass;
	public static String DigitClass;
	public static String NotDigitClass;

	private static final String ECMASpaceSet = "\u0009\u000E\u0020\u0021";
	private static final String NotECMASpaceSet = "\0\u0009\u000E\u0020\u0021";
	private static final String ECMAWordSet = "\u0030\u003A\u0041\u005B\u005F\u0060\u0061\u007B\u0130\u0131";
	private static final String NotECMAWordSet = "\0\u0030\u003A\u0041\u005B\u005F\u0060\u0061\u007B\u0130\u0131";
	private static final String ECMADigitSet = "\u0030\u003A";
	private static final String NotECMADigitSet = "\0\u0030\u003A";

	public static final String ECMASpaceClass = "\u0000\u0004\u0000" + ECMASpaceSet;
	public static final String NotECMASpaceClass = "\u0001\u0004\u0000" + ECMASpaceSet;
	//@czc:\ u 000A --> \ \ u 000A
	public static final String ECMAWordClass = "\u0000\\u000A\u0000" + ECMAWordSet;
	public static final String NotECMAWordClass = "\u0001\\u000A\u0000" + ECMAWordSet;
	public static final String ECMADigitClass = "\u0000\u0002\u0000" + ECMADigitSet;
	public static final String NotECMADigitClass = "\u0001\u0002\u0000" + ECMADigitSet;

	public static final String AnyClass = "\u0000\u0001\u0000\u0000";
	public static final String EmptyClass = "\u0000\u0000\u0000";

	private static java.util.HashMap<String, String> _definedCategories;

	/*
	 *   The property table contains all the block definitions defined in the 
	 *   XML schema spec (http://www.w3.org/TR/2001/PR-xmlschema-2-20010316/#charcter-classes), Unicode 4.0 spec (www.unicode.org), 
	 *   and Perl 5.6 (see Programming Perl, 3rd edition page 167).   Three blocks defined by Perl (and here) may 
	 *   not be in the Unicode: IsHighPrivateUseSurrogates, IsHighSurrogates, and IsLowSurrogates.   
	 *   
	**/
	// Has to be sorted by the first column
	private static final String[][] _propTable =
	{
		{"IsAlphabeticPresentationForms", "\uFB00\uFB50"},
		{"IsArabic", "\u0600\u0700"},
		{"IsArabicPresentationForms-A", "\uFB50\uFE00"},
		{"IsArabicPresentationForms-B", "\uFE70\uFF00"},
		{"IsArmenian", "\u0530\u0590"},
		{"IsArrows", "\u2190\u2200"},
		{"IsBasicLatin", "\u0000\u0080"},
		{"IsBengali", "\u0980\u0A00"},
		{"IsBlockElements", "\u2580\u25A0"},
		{"IsBopomofo", "\u3100\u3130"},
		{"IsBopomofoExtended", "\u31A0\u31C0"},
		{"IsBoxDrawing", "\u2500\u2580"},
		{"IsBraillePatterns", "\u2800\u2900"},
		{"IsBuhid", "\u1740\u1760"},
		{"IsCJKCompatibility", "\u3300\u3400"},
		{"IsCJKCompatibilityForms", "\uFE30\uFE50"},
		{"IsCJKCompatibilityIdeographs", "\uF900\uFB00"},
		{"IsCJKRadicalsSupplement", "\u2E80\u2F00"},
		{"IsCJKSymbolsandPunctuation", "\u3000\u3040"},
		{"IsCJKUnifiedIdeographs", "\u4E00\uA000"},
		{"IsCJKUnifiedIdeographsExtensionA", "\u3400\u4DC0"},
		{"IsCherokee", "\u13A0\u1400"},
		{"IsCombiningDiacriticalMarks", "\u0300\u0370"},
		{"IsCombiningDiacriticalMarksforSymbols","\u20D0\u2100"},
		{"IsCombiningHalfMarks", "\uFE20\uFE30"},
		{"IsCombiningMarksforSymbols", "\u20D0\u2100"},
		{"IsControlPictures", "\u2400\u2440"},
		{"IsCurrencySymbols", "\u20A0\u20D0"},
		{"IsCyrillic", "\u0400\u0500"},
		{"IsCyrillicSupplement", "\u0500\u0530"},
		{"IsDevanagari", "\u0900\u0980"},
		{"IsDingbats", "\u2700\u27C0"},
		{"IsEnclosedAlphanumerics", "\u2460\u2500"},
		{"IsEnclosedCJKLettersandMonths", "\u3200\u3300"},
		{"IsEthiopic", "\u1200\u1380"},
		{"IsGeneralPunctuation", "\u2000\u2070"},
		{"IsGeometricShapes", "\u25A0\u2600"},
		{"IsGeorgian", "\u10A0\u1100"},
		{"IsGreek", "\u0370\u0400"},
		{"IsGreekExtended", "\u1F00\u2000"},
		{"IsGreekandCoptic", "\u0370\u0400"},
		{"IsGujarati", "\u0A80\u0B00"},
		{"IsGurmukhi", "\u0A00\u0A80"},
		{"IsHalfwidthandFullwidthForms", "\uFF00\uFFF0"},
		{"IsHangulCompatibilityJamo", "\u3130\u3190"},
		{"IsHangulJamo", "\u1100\u1200"},
		{"IsHangulSyllables", "\uAC00\uD7B0"},
		{"IsHanunoo", "\u1720\u1740"},
		{"IsHebrew", "\u0590\u0600"},
		{"IsHighPrivateUseSurrogates", "\uDB80\uDC00"},
		{"IsHighSurrogates", "\uD800\uDB80"},
		{"IsHiragana", "\u3040\u30A0"},
		{"IsIPAExtensions", "\u0250\u02B0"},
		{"IsIdeographicDescriptionCharacters", "\u2FF0\u3000"},
		{"IsKanbun", "\u3190\u31A0"},
		{"IsKangxiRadicals", "\u2F00\u2FE0"},
		{"IsKannada", "\u0C80\u0D00"},
		{"IsKatakana", "\u30A0\u3100"},
		{"IsKatakanaPhoneticExtensions", "\u31F0\u3200"},
		{"IsKhmer", "\u1780\u1800"},
		{"IsKhmerSymbols", "\u19E0\u1A00"},
		{"IsLao", "\u0E80\u0F00"},
		{"IsLatin-1Supplement", "\u0080\u0100"},
		{"IsLatinExtended-A", "\u0100\u0180"},
		{"IsLatinExtended-B", "\u0180\u0250"},
		{"IsLatinExtendedAdditional", "\u1E00\u1F00"},
		{"IsLetterlikeSymbols", "\u2100\u2150"},
		{"IsLimbu", "\u1900\u1950"},
		{"IsLowSurrogates", "\uDC00\uE000"},
		{"IsMalayalam", "\u0D00\u0D80"},
		{"IsMathematicalOperators", "\u2200\u2300"},
		{"IsMiscellaneousMathematicalSymbols-A","\u27C0\u27F0"},
		{"IsMiscellaneousMathematicalSymbols-B","\u2980\u2A00"},
		{"IsMiscellaneousSymbols", "\u2600\u2700"},
		{"IsMiscellaneousSymbolsandArrows", "\u2B00\u2C00"},
		{"IsMiscellaneousTechnical", "\u2300\u2400"},
		{"IsMongolian", "\u1800\u18B0"},
		{"IsMyanmar", "\u1000\u10A0"},
		{"IsNumberForms", "\u2150\u2190"},
		{"IsOgham", "\u1680\u16A0"},
		{"IsOpticalCharacterRecognition", "\u2440\u2460"},
		{"IsOriya", "\u0B00\u0B80"},
		{"IsPhoneticExtensions", "\u1D00\u1D80"},
		{"IsPrivateUse", "\uE000\uF900"},
		{"IsPrivateUseArea", "\uE000\uF900"},
		{"IsRunic", "\u16A0\u1700"},
		{"IsSinhala", "\u0D80\u0E00"},
		{"IsSmallFormVariants", "\uFE50\uFE70"},
		{"IsSpacingModifierLetters", "\u02B0\u0300"},
		{"IsSpecials", "\uFFF0"},
		{"IsSuperscriptsandSubscripts", "\u2070\u20A0"},
		{"IsSupplementalArrows-A", "\u27F0\u2800"},
		{"IsSupplementalArrows-B", "\u2900\u2980"},
		{"IsSupplementalMathematicalOperators", "\u2A00\u2B00"},
		{"IsSyriac", "\u0700\u0750"},
		{"IsTagalog", "\u1700\u1720"},
		{"IsTagbanwa", "\u1760\u1780"},
		{"IsTaiLe", "\u1950\u1980"},
		{"IsTamil", "\u0B80\u0C00"},
		{"IsTelugu", "\u0C00\u0C80"},
		{"IsThaana", "\u0780\u07C0"},
		{"IsThai", "\u0E00\u0E80"},
		{"IsTibetan", "\u0F00\u1000"},
		{"IsUnifiedCanadianAboriginalSyllabics","\u1400\u1680"},
		{"IsVariationSelectors", "\uFE00\uFE10"},
		{"IsYiRadicals", "\uA490\uA4D0"},
		{"IsYiSyllables", "\uA000\uA490"},
		{"IsYijingHexagramSymbols", "\u4DC0\u4E00"},
		{"_xmlC", "\u002D\u002F\u0030\u003B\u0041\u005B\u005F\u0060\u0061\u007B\u00B7\u00B8\u00C0\u00D7\u00D8\u00F7\u00F8\u0132\u0134\u013F\u0141\u0149\u014A\u017F\u0180\u01C4\u01CD\u01F1\u01F4\u01F6\u01FA\u0218\u0250\u02A9\u02BB\u02C2\u02D0\u02D2\u0300\u0346\u0360\u0362\u0386\u038B\u038C\u038D\u038E\u03A2\u03A3\u03CF\u03D0\u03D7\u03DA\u03DB\u03DC\u03DD\u03DE\u03DF\u03E0\u03E1\u03E2\u03F4\u0401\u040D\u040E\u0450\u0451\u045D\u045E\u0482\u0483\u0487\u0490\u04C5\u04C7\u04C9\u04CB\u04CD\u04D0\u04EC\u04EE\u04F6\u04F8\u04FA\u0531\u0557\u0559\u055A\u0561\u0587\u0591\u05A2\u05A3\u05BA\u05BB\u05BE\u05BF\u05C0\u05C1\u05C3\u05C4\u05C5\u05D0\u05EB\u05F0\u05F3\u0621\u063B\u0640\u0653\u0660\u066A\u0670\u06B8\u06BA\u06BF\u06C0\u06CF\u06D0\u06D4\u06D5\u06E9\u06EA\u06EE\u06F0\u06FA\u0901\u0904\u0905\u093A\u093C\u094E\u0951\u0955\u0958\u0964\u0966\u0970\u0981\u0984\u0985\u098D\u098F\u0991\u0993\u09A9\u09AA\u09B1\u09B2\u09B3\u09B6\u09BA\u09BC\u09BD\u09BE\u09C5\u09C7\u09C9\u09CB\u09CE\u09D7\u09D8\u09DC" + "\u09DE\u09DF\u09E4\u09E6\u09F2\u0A02\u0A03\u0A05\u0A0B\u0A0F\u0A11\u0A13\u0A29\u0A2A\u0A31\u0A32\u0A34\u0A35\u0A37\u0A38\u0A3A\u0A3C\u0A3D\u0A3E\u0A43\u0A47\u0A49\u0A4B\u0A4E\u0A59\u0A5D\u0A5E\u0A5F\u0A66\u0A75\u0A81\u0A84\u0A85\u0A8C\u0A8D\u0A8E\u0A8F\u0A92\u0A93\u0AA9\u0AAA\u0AB1\u0AB2\u0AB4\u0AB5\u0ABA\u0ABC\u0AC6\u0AC7\u0ACA\u0ACB\u0ACE\u0AE0\u0AE1\u0AE6\u0AF0\u0B01\u0B04\u0B05\u0B0D\u0B0F\u0B11\u0B13\u0B29\u0B2A\u0B31\u0B32\u0B34\u0B36\u0B3A\u0B3C\u0B44\u0B47\u0B49\u0B4B\u0B4E\u0B56\u0B58\u0B5C\u0B5E\u0B5F\u0B62\u0B66\u0B70\u0B82\u0B84\u0B85\u0B8B\u0B8E\u0B91\u0B92\u0B96\u0B99\u0B9B\u0B9C\u0B9D\u0B9E\u0BA0\u0BA3\u0BA5\u0BA8\u0BAB\u0BAE\u0BB6\u0BB7\u0BBA\u0BBE\u0BC3\u0BC6\u0BC9\u0BCA\u0BCE\u0BD7\u0BD8\u0BE7\u0BF0\u0C01\u0C04\u0C05\u0C0D\u0C0E\u0C11\u0C12\u0C29\u0C2A\u0C34\u0C35\u0C3A\u0C3E\u0C45\u0C46\u0C49\u0C4A\u0C4E\u0C55\u0C57\u0C60\u0C62\u0C66\u0C70\u0C82\u0C84\u0C85\u0C8D\u0C8E\u0C91\u0C92\u0CA9\u0CAA\u0CB4\u0CB5\u0CBA\u0CBE\u0CC5\u0CC6\u0CC9\u0CCA\u0CCE\u0CD5\u0CD7\u0CDE\u0CDF\u0CE0\u0CE2" + "\u0CE6\u0CF0\u0D02\u0D04\u0D05\u0D0D\u0D0E\u0D11\u0D12\u0D29\u0D2A\u0D3A\u0D3E\u0D44\u0D46\u0D49\u0D4A\u0D4E\u0D57\u0D58\u0D60\u0D62\u0D66\u0D70\u0E01\u0E2F\u0E30\u0E3B\u0E40\u0E4F\u0E50\u0E5A\u0E81\u0E83\u0E84\u0E85\u0E87\u0E89\u0E8A\u0E8B\u0E8D\u0E8E\u0E94\u0E98\u0E99\u0EA0\u0EA1\u0EA4\u0EA5\u0EA6\u0EA7\u0EA8\u0EAA\u0EAC\u0EAD\u0EAF\u0EB0\u0EBA\u0EBB\u0EBE\u0EC0\u0EC5\u0EC6\u0EC7\u0EC8\u0ECE\u0ED0\u0EDA\u0F18\u0F1A\u0F20\u0F2A\u0F35\u0F36\u0F37\u0F38\u0F39\u0F3A\u0F3E\u0F48\u0F49\u0F6A\u0F71\u0F85\u0F86\u0F8C\u0F90\u0F96\u0F97\u0F98\u0F99\u0FAE\u0FB1\u0FB8\u0FB9\u0FBA\u10A0\u10C6\u10D0\u10F7\u1100\u1101\u1102\u1104\u1105\u1108\u1109\u110A\u110B\u110D\u110E\u1113\u113C\u113D\u113E\u113F\u1140\u1141\u114C\u114D\u114E\u114F\u1150\u1151\u1154\u1156\u1159\u115A\u115F\u1162\u1163\u1164\u1165\u1166\u1167\u1168\u1169\u116A\u116D\u116F\u1172\u1174\u1175\u1176\u119E\u119F\u11A8\u11A9\u11AB\u11AC\u11AE\u11B0\u11B7\u11B9\u11BA\u11BB\u11BC\u11C3\u11EB\u11EC\u11F0\u11F1\u11F9\u11FA\u1E00\u1E9C\u1EA0\u1EFA\u1F00" + "\u1F16\u1F18\u1F1E\u1F20\u1F46\u1F48\u1F4E\u1F50\u1F58\u1F59\u1F5A\u1F5B\u1F5C\u1F5D\u1F5E\u1F5F\u1F7E\u1F80\u1FB5\u1FB6\u1FBD\u1FBE\u1FBF\u1FC2\u1FC5\u1FC6\u1FCD\u1FD0\u1FD4\u1FD6\u1FDC\u1FE0\u1FED\u1FF2\u1FF5\u1FF6\u1FFD\u20D0\u20DD\u20E1\u20E2\u2126\u2127\u212A\u212C\u212E\u212F\u2180\u2183\u3005\u3006\u3007\u3008\u3021\u3030\u3031\u3036\u3041\u3095\u3099\u309B\u309D\u309F\u30A1\u30FB\u30FC\u30FF\u3105\u312D\u4E00\u9FA6\uAC00\uD7A4"},
		{"_xmlD", "\u0030\u003A\u0660\u066A\u06F0\u06FA\u0966\u0970\u09E6\u09F0\u0A66\u0A70\u0AE6\u0AF0\u0B66\u0B70\u0BE7\u0BF0\u0C66\u0C70\u0CE6\u0CF0\u0D66\u0D70\u0E50\u0E5A\u0ED0\u0EDA\u0F20\u0F2A\u1040\u104A\u1369\u1372\u17E0\u17EA\u1810\u181A\uFF10\uFF1A"},
		{"_xmlI", "\u003A\u003B\u0041\u005B\u005F\u0060\u0061\u007B\u00C0\u00D7\u00D8\u00F7\u00F8\u0132\u0134\u013F\u0141\u0149\u014A\u017F\u0180\u01C4\u01CD\u01F1\u01F4\u01F6\u01FA\u0218\u0250\u02A9\u02BB\u02C2\u0386\u0387\u0388\u038B\u038C\u038D\u038E\u03A2\u03A3\u03CF\u03D0\u03D7\u03DA\u03DB\u03DC\u03DD\u03DE\u03DF\u03E0\u03E1\u03E2\u03F4\u0401\u040D\u040E\u0450\u0451\u045D\u045E\u0482\u0490\u04C5\u04C7\u04C9\u04CB\u04CD\u04D0\u04EC\u04EE\u04F6\u04F8\u04FA\u0531\u0557\u0559\u055A\u0561\u0587\u05D0\u05EB\u05F0\u05F3\u0621\u063B\u0641\u064B\u0671\u06B8\u06BA\u06BF\u06C0\u06CF\u06D0\u06D4\u06D5\u06D6\u06E5\u06E7\u0905\u093A\u093D\u093E\u0958\u0962\u0985\u098D\u098F\u0991\u0993\u09A9\u09AA\u09B1\u09B2\u09B3\u09B6\u09BA\u09DC\u09DE\u09DF\u09E2\u09F0\u09F2\u0A05\u0A0B\u0A0F\u0A11\u0A13\u0A29\u0A2A\u0A31\u0A32\u0A34\u0A35\u0A37\u0A38\u0A3A\u0A59\u0A5D\u0A5E\u0A5F\u0A72\u0A75\u0A85\u0A8C\u0A8D\u0A8E\u0A8F\u0A92\u0A93\u0AA9\u0AAA\u0AB1\u0AB2\u0AB4\u0AB5\u0ABA\u0ABD\u0ABE\u0AE0\u0AE1\u0B05\u0B0D\u0B0F" + "\u0B11\u0B13\u0B29\u0B2A\u0B31\u0B32\u0B34\u0B36\u0B3A\u0B3D\u0B3E\u0B5C\u0B5E\u0B5F\u0B62\u0B85\u0B8B\u0B8E\u0B91\u0B92\u0B96\u0B99\u0B9B\u0B9C\u0B9D\u0B9E\u0BA0\u0BA3\u0BA5\u0BA8\u0BAB\u0BAE\u0BB6\u0BB7\u0BBA\u0C05\u0C0D\u0C0E\u0C11\u0C12\u0C29\u0C2A\u0C34\u0C35\u0C3A\u0C60\u0C62\u0C85\u0C8D\u0C8E\u0C91\u0C92\u0CA9\u0CAA\u0CB4\u0CB5\u0CBA\u0CDE\u0CDF\u0CE0\u0CE2\u0D05\u0D0D\u0D0E\u0D11\u0D12\u0D29\u0D2A\u0D3A\u0D60\u0D62\u0E01\u0E2F\u0E30\u0E31\u0E32\u0E34\u0E40\u0E46\u0E81\u0E83\u0E84\u0E85\u0E87\u0E89\u0E8A\u0E8B\u0E8D\u0E8E\u0E94\u0E98\u0E99\u0EA0\u0EA1\u0EA4\u0EA5\u0EA6\u0EA7\u0EA8\u0EAA\u0EAC\u0EAD\u0EAF\u0EB0\u0EB1\u0EB2\u0EB4\u0EBD\u0EBE\u0EC0\u0EC5\u0F40\u0F48\u0F49\u0F6A\u10A0\u10C6\u10D0\u10F7\u1100\u1101\u1102\u1104\u1105\u1108\u1109\u110A\u110B\u110D\u110E\u1113\u113C\u113D\u113E\u113F\u1140\u1141\u114C\u114D\u114E\u114F\u1150\u1151\u1154\u1156\u1159\u115A\u115F\u1162\u1163\u1164\u1165\u1166\u1167\u1168\u1169\u116A\u116D\u116F\u1172\u1174\u1175\u1176\u119E\u119F\u11A8\u11A9\u11AB\u11AC" + "\u11AE\u11B0\u11B7\u11B9\u11BA\u11BB\u11BC\u11C3\u11EB\u11EC\u11F0\u11F1\u11F9\u11FA\u1E00\u1E9C\u1EA0\u1EFA\u1F00\u1F16\u1F18\u1F1E\u1F20\u1F46\u1F48\u1F4E\u1F50\u1F58\u1F59\u1F5A\u1F5B\u1F5C\u1F5D\u1F5E\u1F5F\u1F7E\u1F80\u1FB5\u1FB6\u1FBD\u1FBE\u1FBF\u1FC2\u1FC5\u1FC6\u1FCD\u1FD0\u1FD4\u1FD6\u1FDC\u1FE0\u1FED\u1FF2\u1FF5\u1FF6\u1FFD\u2126\u2127\u212A\u212C\u212E\u212F\u2180\u2183\u3007\u3008\u3021\u302A\u3041\u3095\u30A1\u30FB\u3105\u312D\u4E00\u9FA6\uAC00\uD7A4"},
		{"_xmlW", "\u0024\u0025\u002B\u002C\u0030\u003A\u003C\u003F\u0041\u005B\u005E\u005F\u0060\u007B\u007C\u007D\u007E\u007F\u00A2\u00AB\u00AC\u00AD\u00AE\u00B7\u00B8\u00BB\u00BC\u00BF\u00C0\u0221\u0222\u0234\u0250\u02AE\u02B0\u02EF\u0300\u0350\u0360\u0370\u0374\u0376\u037A\u037B\u0384\u0387\u0388\u038B\u038C\u038D\u038E\u03A2\u03A3\u03CF\u03D0\u03F7\u0400\u0487\u0488\u04CF\u04D0\u04F6\u04F8\u04FA\u0500\u0510\u0531\u0557\u0559\u055A\u0561\u0588\u0591\u05A2\u05A3\u05BA\u05BB\u05BE\u05BF\u05C0\u05C1\u05C3\u05C4\u05C5\u05D0\u05EB\u05F0\u05F3\u0621\u063B\u0640\u0656\u0660\u066A\u066E\u06D4\u06D5\u06DD\u06DE\u06EE\u06F0\u06FF\u0710\u072D\u0730\u074B\u0780\u07B2\u0901\u0904\u0905\u093A\u093C\u094E\u0950\u0955\u0958\u0964\u0966\u0970\u0981\u0984\u0985\u098D\u098F\u0991\u0993\u09A9\u09AA\u09B1\u09B2\u09B3\u09B6\u09BA\u09BC\u09BD\u09BE\u09C5\u09C7\u09C9\u09CB\u09CE\u09D7\u09D8\u09DC\u09DE\u09DF\u09E4\u09E6\u09FB\u0A02\u0A03\u0A05\u0A0B\u0A0F\u0A11\u0A13\u0A29\u0A2A\u0A31\u0A32\u0A34\u0A35" + "\u0A37\u0A38\u0A3A\u0A3C\u0A3D\u0A3E\u0A43\u0A47\u0A49\u0A4B\u0A4E\u0A59\u0A5D\u0A5E\u0A5F\u0A66\u0A75\u0A81\u0A84\u0A85\u0A8C\u0A8D\u0A8E\u0A8F\u0A92\u0A93\u0AA9\u0AAA\u0AB1\u0AB2\u0AB4\u0AB5\u0ABA\u0ABC\u0AC6\u0AC7\u0ACA\u0ACB\u0ACE\u0AD0\u0AD1\u0AE0\u0AE1\u0AE6\u0AF0\u0B01\u0B04\u0B05\u0B0D\u0B0F\u0B11\u0B13\u0B29\u0B2A\u0B31\u0B32\u0B34\u0B36\u0B3A\u0B3C\u0B44\u0B47\u0B49\u0B4B\u0B4E\u0B56\u0B58\u0B5C\u0B5E\u0B5F\u0B62\u0B66\u0B71\u0B82\u0B84\u0B85\u0B8B\u0B8E\u0B91\u0B92\u0B96\u0B99\u0B9B\u0B9C\u0B9D\u0B9E\u0BA0\u0BA3\u0BA5\u0BA8\u0BAB\u0BAE\u0BB6\u0BB7\u0BBA\u0BBE\u0BC3\u0BC6\u0BC9\u0BCA\u0BCE\u0BD7\u0BD8\u0BE7\u0BF3\u0C01\u0C04\u0C05\u0C0D\u0C0E\u0C11\u0C12\u0C29\u0C2A\u0C34\u0C35\u0C3A\u0C3E\u0C45\u0C46\u0C49\u0C4A\u0C4E\u0C55\u0C57\u0C60\u0C62\u0C66\u0C70\u0C82\u0C84\u0C85\u0C8D\u0C8E\u0C91\u0C92\u0CA9\u0CAA\u0CB4\u0CB5\u0CBA\u0CBE\u0CC5\u0CC6\u0CC9\u0CCA\u0CCE\u0CD5\u0CD7\u0CDE\u0CDF\u0CE0\u0CE2\u0CE6\u0CF0\u0D02\u0D04\u0D05\u0D0D\u0D0E\u0D11\u0D12\u0D29\u0D2A\u0D3A\u0D3E\u0D44\u0D46\u0D49" + "\u0D4A\u0D4E\u0D57\u0D58\u0D60\u0D62\u0D66\u0D70\u0D82\u0D84\u0D85\u0D97\u0D9A\u0DB2\u0DB3\u0DBC\u0DBD\u0DBE\u0DC0\u0DC7\u0DCA\u0DCB\u0DCF\u0DD5\u0DD6\u0DD7\u0DD8\u0DE0\u0DF2\u0DF4\u0E01\u0E3B\u0E3F\u0E4F\u0E50\u0E5A\u0E81\u0E83\u0E84\u0E85\u0E87\u0E89\u0E8A\u0E8B\u0E8D\u0E8E\u0E94\u0E98\u0E99\u0EA0\u0EA1\u0EA4\u0EA5\u0EA6\u0EA7\u0EA8\u0EAA\u0EAC\u0EAD\u0EBA\u0EBB\u0EBE\u0EC0\u0EC5\u0EC6\u0EC7\u0EC8\u0ECE\u0ED0\u0EDA\u0EDC\u0EDE\u0F00\u0F04\u0F13\u0F3A\u0F3E\u0F48\u0F49\u0F6B\u0F71\u0F85\u0F86\u0F8C\u0F90\u0F98\u0F99\u0FBD\u0FBE\u0FCD\u0FCF\u0FD0\u1000\u1022\u1023\u1028\u1029\u102B\u102C\u1033\u1036\u103A\u1040\u104A\u1050\u105A\u10A0\u10C6\u10D0\u10F9\u1100\u115A\u115F\u11A3\u11A8\u11FA\u1200\u1207\u1208\u1247\u1248\u1249\u124A\u124E\u1250\u1257\u1258\u1259\u125A\u125E\u1260\u1287\u1288\u1289\u128A\u128E\u1290\u12AF\u12B0\u12B1\u12B2\u12B6\u12B8\u12BF\u12C0\u12C1\u12C2\u12C6\u12C8\u12CF\u12D0\u12D7\u12D8\u12EF\u12F0\u130F\u1310\u1311\u1312\u1316\u1318\u131F\u1320\u1347\u1348\u135B\u1369\u137D\u13A0" + "\u13F5\u1401\u166D\u166F\u1677\u1681\u169B\u16A0\u16EB\u16EE\u16F1\u1700\u170D\u170E\u1715\u1720\u1735\u1740\u1754\u1760\u176D\u176E\u1771\u1772\u1774\u1780\u17D4\u17D7\u17D8\u17DB\u17DD\u17E0\u17EA\u180B\u180E\u1810\u181A\u1820\u1878\u1880\u18AA\u1E00\u1E9C\u1EA0\u1EFA\u1F00\u1F16\u1F18\u1F1E\u1F20\u1F46\u1F48\u1F4E\u1F50\u1F58\u1F59\u1F5A\u1F5B\u1F5C\u1F5D\u1F5E\u1F5F\u1F7E\u1F80\u1FB5\u1FB6\u1FC5\u1FC6\u1FD4\u1FD6\u1FDC\u1FDD\u1FF0\u1FF2\u1FF5\u1FF6\u1FFF\u2044\u2045\u2052\u2053\u2070\u2072\u2074\u207D\u207F\u208D\u20A0\u20B2\u20D0\u20EB\u2100\u213B\u213D\u214C\u2153\u2184\u2190\u2329\u232B\u23B4\u23B7\u23CF\u2400\u2427\u2440\u244B\u2460\u24FF\u2500\u2614\u2616\u2618\u2619\u267E\u2680\u268A\u2701\u2705\u2706\u270A\u270C\u2728\u2729\u274C\u274D\u274E\u274F\u2753\u2756\u2757\u2758\u275F\u2761\u2768\u2776\u2795\u2798\u27B0\u27B1\u27BF\u27D0\u27E6\u27F0\u2983\u2999\u29D8\u29DC\u29FC\u29FE\u2B00\u2E80\u2E9A\u2E9B\u2EF4\u2F00\u2FD6\u2FF0\u2FFC\u3004\u3008\u3012\u3014\u3020\u3030\u3031\u303D\u303E\u3040" + "\u3041\u3097\u3099\u30A0\u30A1\u30FB\u30FC\u3100\u3105\u312D\u3131\u318F\u3190\u31B8\u31F0\u321D\u3220\u3244\u3251\u327C\u327F\u32CC\u32D0\u32FF\u3300\u3377\u337B\u33DE\u33E0\u33FF\u3400\u4DB6\u4E00\u9FA6\uA000\uA48D\uA490\uA4C7\uAC00\uD7A4\uF900\uFA2E\uFA30\uFA6B\uFB00\uFB07\uFB13\uFB18\uFB1D\uFB37\uFB38\uFB3D\uFB3E\uFB3F\uFB40\uFB42\uFB43\uFB45\uFB46\uFBB2\uFBD3\uFD3E\uFD50\uFD90\uFD92\uFDC8\uFDF0\uFDFD\uFE00\uFE10\uFE20\uFE24\uFE62\uFE63\uFE64\uFE67\uFE69\uFE6A\uFE70\uFE75\uFE76\uFEFD\uFF04\uFF05\uFF0B\uFF0C\uFF10\uFF1A\uFF1C\uFF1F\uFF21\uFF3B\uFF3E\uFF3F\uFF40\uFF5B\uFF5C\uFF5D\uFF5E\uFF5F\uFF66\uFFBF\uFFC2\uFFC8\uFFCA\uFFD0\uFFD2\uFFD8\uFFDA\uFFDD\uFFE0\uFFE7\uFFE8\uFFEF\uFFFC\uFFFE"}
	};


	/**************************************************************************
	    Let U be the set of Unicode character values and let L be the lowercase
	    function, mapping from U to U. To perform case insensitive matching of
	    character sets, we need to be able to map an interval I in U, say

	        I = [chMin, chMax] = { ch : chMin <= ch <= chMax }

	    to a set A such that A contains L(I) and A is contained in the union of
	    I and L(I).

	    The table below partitions U into intervals on which L is non-decreasing.
	    Thus, for any interval J = [a, b] contained in one of these intervals,
	    L(J) is contained in [L(a), L(b)].

	    It is also true that for any such J, [L(a), L(b)] is contained in the
	    union of J and L(J). This does not follow from L being non-decreasing on
	    these intervals. It follows from the nature of the L on each interval.
	    On each interval, L has one of the following forms:

	        (1) L(ch) = constant            (LowercaseSet)
	        (2) L(ch) = ch + offset         (LowercaseAdd)
	        (3) L(ch) = ch | 1              (LowercaseBor)
	        (4) L(ch) = ch + (ch & 1)       (LowercaseBad)

	    It is easy to verify that for any of these forms [L(a), L(b)] is
	    contained in the union of [a, b] and L([a, b]).
	***************************************************************************/

	private static final int LowercaseSet = 0; // Set to arg.
	private static final int LowercaseAdd = 1; // Add arg.
	private static final int LowercaseBor = 2; // Bitwise or with 1.
	private static final int LowercaseBad = 3; // Bitwise and with 1 and add original.

	private static final LowerCaseMapping[] _lcTable = new LowerCaseMapping[]
	{
		new LowerCaseMapping('\u0041', '\u005A', LowercaseAdd, 32),
		new LowerCaseMapping('\u00C0', '\u00DE', LowercaseAdd, 32),
		new LowerCaseMapping('\u0100', '\u012E', LowercaseBor, 0),
		new LowerCaseMapping('\u0130', '\u0130', LowercaseSet, 0x0069),
		new LowerCaseMapping('\u0132', '\u0136', LowercaseBor, 0),
		new LowerCaseMapping('\u0139', '\u0147', LowercaseBad, 0),
		new LowerCaseMapping('\u014A', '\u0176', LowercaseBor, 0),
		new LowerCaseMapping('\u0178', '\u0178', LowercaseSet, 0x00FF),
		new LowerCaseMapping('\u0179', '\u017D', LowercaseBad, 0),
		new LowerCaseMapping('\u0181', '\u0181', LowercaseSet, 0x0253),
		new LowerCaseMapping('\u0182', '\u0184', LowercaseBor, 0),
		new LowerCaseMapping('\u0186', '\u0186', LowercaseSet, 0x0254),
		new LowerCaseMapping('\u0187', '\u0187', LowercaseSet, 0x0188),
		new LowerCaseMapping('\u0189', '\u018A', LowercaseAdd, 205),
		new LowerCaseMapping('\u018B', '\u018B', LowercaseSet, 0x018C),
		new LowerCaseMapping('\u018E', '\u018E', LowercaseSet, 0x01DD),
		new LowerCaseMapping('\u018F', '\u018F', LowercaseSet, 0x0259),
		new LowerCaseMapping('\u0190', '\u0190', LowercaseSet, 0x025B),
		new LowerCaseMapping('\u0191', '\u0191', LowercaseSet, 0x0192),
		new LowerCaseMapping('\u0193', '\u0193', LowercaseSet, 0x0260),
		new LowerCaseMapping('\u0194', '\u0194', LowercaseSet, 0x0263),
		new LowerCaseMapping('\u0196', '\u0196', LowercaseSet, 0x0269),
		new LowerCaseMapping('\u0197', '\u0197', LowercaseSet, 0x0268),
		new LowerCaseMapping('\u0198', '\u0198', LowercaseSet, 0x0199),
		new LowerCaseMapping('\u019C', '\u019C', LowercaseSet, 0x026F),
		new LowerCaseMapping('\u019D', '\u019D', LowercaseSet, 0x0272),
		new LowerCaseMapping('\u019F', '\u019F', LowercaseSet, 0x0275),
		new LowerCaseMapping('\u01A0', '\u01A4', LowercaseBor, 0),
		new LowerCaseMapping('\u01A7', '\u01A7', LowercaseSet, 0x01A8),
		new LowerCaseMapping('\u01A9', '\u01A9', LowercaseSet, 0x0283),
		new LowerCaseMapping('\u01AC', '\u01AC', LowercaseSet, 0x01AD),
		new LowerCaseMapping('\u01AE', '\u01AE', LowercaseSet, 0x0288),
		new LowerCaseMapping('\u01AF', '\u01AF', LowercaseSet, 0x01B0),
		new LowerCaseMapping('\u01B1', '\u01B2', LowercaseAdd, 217),
		new LowerCaseMapping('\u01B3', '\u01B5', LowercaseBad, 0),
		new LowerCaseMapping('\u01B7', '\u01B7', LowercaseSet, 0x0292),
		new LowerCaseMapping('\u01B8', '\u01B8', LowercaseSet, 0x01B9),
		new LowerCaseMapping('\u01BC', '\u01BC', LowercaseSet, 0x01BD),
		new LowerCaseMapping('\u01C4', '\u01C5', LowercaseSet, 0x01C6),
		new LowerCaseMapping('\u01C7', '\u01C8', LowercaseSet, 0x01C9),
		new LowerCaseMapping('\u01CA', '\u01CB', LowercaseSet, 0x01CC),
		new LowerCaseMapping('\u01CD', '\u01DB', LowercaseBad, 0),
		new LowerCaseMapping('\u01DE', '\u01EE', LowercaseBor, 0),
		new LowerCaseMapping('\u01F1', '\u01F2', LowercaseSet, 0x01F3),
		new LowerCaseMapping('\u01F4', '\u01F4', LowercaseSet, 0x01F5),
		new LowerCaseMapping('\u01FA', '\u0216', LowercaseBor, 0),
		new LowerCaseMapping('\u0386', '\u0386', LowercaseSet, 0x03AC),
		new LowerCaseMapping('\u0388', '\u038A', LowercaseAdd, 37),
		new LowerCaseMapping('\u038C', '\u038C', LowercaseSet, 0x03CC),
		new LowerCaseMapping('\u038E', '\u038F', LowercaseAdd, 63),
		new LowerCaseMapping('\u0391', '\u03AB', LowercaseAdd, 32),
		new LowerCaseMapping('\u03E2', '\u03EE', LowercaseBor, 0),
		new LowerCaseMapping('\u0401', '\u040F', LowercaseAdd, 80),
		new LowerCaseMapping('\u0410', '\u042F', LowercaseAdd, 32),
		new LowerCaseMapping('\u0460', '\u0480', LowercaseBor, 0),
		new LowerCaseMapping('\u0490', '\u04BE', LowercaseBor, 0),
		new LowerCaseMapping('\u04C1', '\u04C3', LowercaseBad, 0),
		new LowerCaseMapping('\u04C7', '\u04C7', LowercaseSet, 0x04C8),
		new LowerCaseMapping('\u04CB', '\u04CB', LowercaseSet, 0x04CC),
		new LowerCaseMapping('\u04D0', '\u04EA', LowercaseBor, 0),
		new LowerCaseMapping('\u04EE', '\u04F4', LowercaseBor, 0),
		new LowerCaseMapping('\u04F8', '\u04F8', LowercaseSet, 0x04F9),
		new LowerCaseMapping('\u0531', '\u0556', LowercaseAdd, 48),
		new LowerCaseMapping('\u10A0', '\u10C5', LowercaseAdd, 48),
		new LowerCaseMapping('\u1E00', '\u1EF8', LowercaseBor, 0),
		new LowerCaseMapping('\u1F08', '\u1F0F', LowercaseAdd, -8),
		new LowerCaseMapping('\u1F18', '\u1F1F', LowercaseAdd, -8),
		new LowerCaseMapping('\u1F28', '\u1F2F', LowercaseAdd, -8),
		new LowerCaseMapping('\u1F38', '\u1F3F', LowercaseAdd, -8),
		new LowerCaseMapping('\u1F48', '\u1F4D', LowercaseAdd, -8),
		new LowerCaseMapping('\u1F59', '\u1F59', LowercaseSet, 0x1F51),
		new LowerCaseMapping('\u1F5B', '\u1F5B', LowercaseSet, 0x1F53),
		new LowerCaseMapping('\u1F5D', '\u1F5D', LowercaseSet, 0x1F55),
		new LowerCaseMapping('\u1F5F', '\u1F5F', LowercaseSet, 0x1F57),
		new LowerCaseMapping('\u1F68', '\u1F6F', LowercaseAdd, -8),
		new LowerCaseMapping('\u1F88', '\u1F8F', LowercaseAdd, -8),
		new LowerCaseMapping('\u1F98', '\u1F9F', LowercaseAdd, -8),
		new LowerCaseMapping('\u1FA8', '\u1FAF', LowercaseAdd, -8),
		new LowerCaseMapping('\u1FB8', '\u1FB9', LowercaseAdd, -8),
		new LowerCaseMapping('\u1FBA', '\u1FBB', LowercaseAdd, -74),
		new LowerCaseMapping('\u1FBC', '\u1FBC', LowercaseSet, 0x1FB3),
		new LowerCaseMapping('\u1FC8', '\u1FCB', LowercaseAdd, -86),
		new LowerCaseMapping('\u1FCC', '\u1FCC', LowercaseSet, 0x1FC3),
		new LowerCaseMapping('\u1FD8', '\u1FD9', LowercaseAdd, -8),
		new LowerCaseMapping('\u1FDA', '\u1FDB', LowercaseAdd, -100),
		new LowerCaseMapping('\u1FE8', '\u1FE9', LowercaseAdd, -8),
		new LowerCaseMapping('\u1FEA', '\u1FEB', LowercaseAdd, -112),
		new LowerCaseMapping('\u1FEC', '\u1FEC', LowercaseSet, 0x1FE5),
		new LowerCaseMapping('\u1FF8', '\u1FF9', LowercaseAdd, -128),
		new LowerCaseMapping('\u1FFA', '\u1FFB', LowercaseAdd, -126),
		new LowerCaseMapping('\u1FFC', '\u1FFC', LowercaseSet, 0x1FF3),
		new LowerCaseMapping('\u2160', '\u216F', LowercaseAdd, 16),
		new LowerCaseMapping('\u24B6', '\u24D0', LowercaseAdd, 26),
		new LowerCaseMapping('\uFF21', '\uFF3A', LowercaseAdd, 32)
	};

	static
	{
		// addressing Dictionary versus Hashtable thread safety difference by using
		// a temp Dictionary. Note that this is just a theoretical concern since this
		// is a static ctor and getter methods aren't called until after this is 
		// done; this is just to avoid the long-term possibility of thread safety 
		// problems.
		java.util.HashMap<String, String> tempCategories = new java.util.HashMap<String, String>(32);

		char[] groups = new char[9];
		StringBuilder word = new StringBuilder(11);

		word.append(GroupChar);
		groups[0] = GroupChar;

		// We need the UnicodeCategory enum values as a char so we can put them in a string
		// in the hashtable.  In order to get there, we first must cast to an int, 
		// then cast to a char
		// Also need to distinguish between positive and negative values.  UnicodeCategory is zero 
		// based, so we add one to each value and subtract it off later

		// Others
		groups[1] = (char)(UnicodeCategory.Control.getValue() + 1);
		tempCategories.put("Cc", (new Character(groups[1])).toString()); // Control
		groups[2] = (char)(UnicodeCategory.Format.getValue() + 1);
		tempCategories.put("Cf", (new Character(groups[2])).toString()); // Format
		groups[3] = (char)(UnicodeCategory.OtherNotAssigned.getValue() + 1);
		tempCategories.put("Cn", (new Character(groups[3])).toString()); // Not assigned
		groups[4] = (char)(UnicodeCategory.PrivateUse.getValue() + 1);
		tempCategories.put("Co", (new Character(groups[4])).toString()); // Private use
		groups[5] = (char)(UnicodeCategory.Surrogate.getValue() + 1);
		tempCategories.put("Cs", (new Character(groups[5])).toString()); // Surrogate

		groups[6] = GroupChar;
		tempCategories.put("C", new String(groups, 0, 7));

		// Letters
		groups[1] = (char)(UnicodeCategory.LowercaseLetter.getValue() + 1);
		tempCategories.put("Ll", (new Character(groups[1])).toString()); // Lowercase
		groups[2] = (char)(UnicodeCategory.ModifierLetter.getValue() + 1);
		tempCategories.put("Lm", (new Character(groups[2])).toString()); // Modifier
		groups[3] = (char)(UnicodeCategory.OtherLetter.getValue() + 1);
		tempCategories.put("Lo", (new Character(groups[3])).toString()); // Other
		groups[4] = (char)(UnicodeCategory.TitlecaseLetter.getValue() + 1);
		tempCategories.put("Lt", (new Character(groups[4])).toString()); // Titlecase
		groups[5] = (char)(UnicodeCategory.UppercaseLetter.getValue() + 1);
		tempCategories.put("Lu", (new Character(groups[5])).toString()); // Uppercase

		//groups[6] = GroupChar;
		tempCategories.put("L", new String(groups, 0, 7));
		word.append(new String(groups, 1, 5));

		// InternalRegexIgnoreCase = {LowercaseLetter} OR {TitlecaseLetter} OR {UppercaseLetter}
		// !!!This category should only ever be used in conjunction with RegexOptions.IgnoreCase code paths!!!
		tempCategories.put(InternalRegexIgnoreCase, String.format(Locale.CHINESE, "%1$s%2$s%3$s%4$s%5$s", GroupChar, groups[1], groups[4], groups[5], groups[6]));

		// Marks        
		groups[1] = (char)(UnicodeCategory.SpacingCombiningMark.getValue() + 1);
		tempCategories.put("Mc", (new Character(groups[1])).toString()); // Spacing combining
		groups[2] = (char)(UnicodeCategory.EnclosingMark.getValue() + 1);
		tempCategories.put("Me", (new Character(groups[2])).toString()); // Enclosing
		groups[3] = (char)(UnicodeCategory.NonSpacingMark.getValue() + 1);
		tempCategories.put("Mn", (new Character(groups[3])).toString()); // Non-spacing

		groups[4] = GroupChar;
		tempCategories.put("M", new String(groups, 0, 5));
		//word.Append(groups[1]);
		word.append(groups[3]);

		// Numbers
		groups[1] = (char)(UnicodeCategory.DecimalDigitNumber.getValue() + 1);
		tempCategories.put("Nd", (new Character(groups[1])).toString()); // Decimal digit
		groups[2] = (char)(UnicodeCategory.LetterNumber.getValue() + 1);
		tempCategories.put("Nl", (new Character(groups[2])).toString()); // Letter
		groups[3] = (char)(UnicodeCategory.OtherNumber.getValue() + 1);
		tempCategories.put("No", (new Character(groups[3])).toString()); // Other

		//groups[4] = GroupChar;
		tempCategories.put("N", new String(groups, 0, 5));
		word.append(groups[1]);
		//word.Append(new String(groups, 1, 3));

		// Punctuation
		groups[1] = (char)(UnicodeCategory.ConnectorPunctuation.getValue() + 1);
		tempCategories.put("Pc", (new Character(groups[1])).toString()); // Connector
		groups[2] = (char)(UnicodeCategory.DashPunctuation.getValue() + 1);
		tempCategories.put("Pd", (new Character(groups[2])).toString()); // Dash
		groups[3] = (char)(UnicodeCategory.ClosePunctuation.getValue() + 1);
		tempCategories.put("Pe", (new Character(groups[3])).toString()); // Close
		groups[4] = (char)(UnicodeCategory.OtherPunctuation.getValue() + 1);
		tempCategories.put("Po", (new Character(groups[4])).toString()); // Other
		groups[5] = (char)(UnicodeCategory.OpenPunctuation.getValue() + 1);
		tempCategories.put("Ps", (new Character(groups[5])).toString()); // Open
		groups[6] = (char)(UnicodeCategory.FinalQuotePunctuation.getValue() + 1);
		tempCategories.put("Pf", (new Character(groups[6])).toString()); // Inital quote
		groups[7] = (char)(UnicodeCategory.InitialQuotePunctuation.getValue() + 1);
		tempCategories.put("Pi", (new Character(groups[7])).toString()); // Final quote

		groups[8] = GroupChar;
		tempCategories.put("P", new String(groups, 0, 9));
		word.append(groups[1]);

		// Symbols
		groups[1] = (char)(UnicodeCategory.CurrencySymbol.getValue() + 1);
		tempCategories.put("Sc", (new Character(groups[1])).toString()); // Currency
		groups[2] = (char)(UnicodeCategory.ModifierSymbol.getValue() + 1);
		tempCategories.put("Sk", (new Character(groups[2])).toString()); // Modifier
		groups[3] = (char)(UnicodeCategory.MathSymbol.getValue() + 1);
		tempCategories.put("Sm", (new Character(groups[3])).toString()); // Math
		groups[4] = (char)(UnicodeCategory.OtherSymbol.getValue() + 1);
		tempCategories.put("So", (new Character(groups[4])).toString()); // Other

		groups[5] = GroupChar;
		tempCategories.put("S", new String(groups, 0, 6));

		// Separators
		groups[1] = (char)(UnicodeCategory.LineSeparator.getValue() + 1);
		tempCategories.put("Zl", (new Character(groups[1])).toString()); // Line
		groups[2] = (char)(UnicodeCategory.ParagraphSeparator.getValue() + 1);
		tempCategories.put("Zp", (new Character(groups[2])).toString()); // Paragraph
		groups[3] = (char)(UnicodeCategory.SpaceSeparator.getValue() + 1);
		tempCategories.put("Zs", (new Character(groups[3])).toString()); // Space

		groups[4] = GroupChar;
		tempCategories.put("Z", new String(groups, 0, 5));


		word.append(GroupChar);
		Word = word.toString();
		NotWord = NegateCategory(Word);


		SpaceClass = "\u0000\u0000\u0001" + Space;
		NotSpaceClass = "\u0001\u0000\u0001" + Space;
		WordClass = "\u0000\u0000" + (char) Word.length() + Word;
		NotWordClass = "\u0001\u0000" + (char) Word.length() + Word;
		DigitClass = "\u0000\u0000\u0001" + (char)(UnicodeCategory.DecimalDigitNumber.getValue() + 1);
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to 'unchecked' in this context:
//ORIGINAL LINE: NotDigitClass = "\x00\x00\x01" + unchecked((char)(- ((int) UnicodeCategory.DecimalDigitNumber + 1)));
		NotDigitClass = "\u0000\u0000\u0001" + (char)(- (UnicodeCategory.DecimalDigitNumber.getValue() + 1));

		_definedCategories = tempCategories;
	}

	/*
	 * RegexCharClass()
	 *
	 * Creates an empty character class.
	 */
	public RegexCharClass()
	{
		_rangelist = new java.util.ArrayList<SingleRange>(6);
		_canonical = true;
		_categories = new StringBuilder();

	}

	private RegexCharClass(boolean negate, java.util.ArrayList<SingleRange> ranges, StringBuilder categories, RegexCharClass subtraction)
	{
		_rangelist = ranges;
		_categories = categories;
		_canonical = true;
		_negate = negate;
		_subtractor = subtraction;
	}

	public boolean getCanMerge()
	{
		return !_negate && _subtractor == null;
	}

	public void setNegate(boolean value)
	{
		_negate = value;
	}

	public void AddChar(char c)
	{
		AddRange(c, c);
	}

	/*
	 * AddCharClass()
	 *
	 * Adds a regex char class
	 */
	public void AddCharClass(RegexCharClass cc)
	{
		int i;

		Debug.Assert(cc.getCanMerge() && this.getCanMerge(), "Both character classes added together must be able to merge");

		if (!cc._canonical)
		{
			// if the new char class to add isn't canonical, we're not either.
			_canonical = false;
		}
		else if (_canonical && RangeCount() > 0 && cc.RangeCount() > 0 && cc.GetRangeAt(0)._first <= GetRangeAt(RangeCount() - 1)._last)
		{
			_canonical = false;
		}

		for (i = 0; i < cc.RangeCount(); i += 1)
		{
			_rangelist.add(cc.GetRangeAt(i));
		}

		_categories.append(cc._categories.toString());
	}

	/*
	 * AddSet()
	 *
	 * Adds a set (specified by its string represenation) to the class.
	 */
	private void AddSet(String set)
	{
		int i;

		if (_canonical && RangeCount() > 0 && set.length() > 0 && set.charAt(0) <= GetRangeAt(RangeCount() - 1)._last)
		{
			_canonical = false;
		}

		for (i = 0; i < set.length() - 1; i += 2)
		{
			_rangelist.add(new SingleRange(set.charAt(i), (char)(set.charAt(i + 1) - 1)));
		}

		if (i < set.length())
		{
			_rangelist.add(new SingleRange(set.charAt(i), Lastchar));
		}
	}

	public void AddSubtraction(RegexCharClass sub)
	{
		Debug.Assert(_subtractor == null, "Can't add two subtractions to a char class. ");
		_subtractor = sub;
	}

	/*
	 * AddRange()
	 *
	 * Adds a single range of characters to the class.
	 */
	public void AddRange(char first, char last)
	{
		_rangelist.add(new SingleRange(first, last));
		if (_canonical && _rangelist.size() > 0 && first <= _rangelist.get(_rangelist.size() - 1)._last)
		{
			_canonical = false;
		}
	}

	public void AddCategoryFromName(String categoryName, boolean invert, boolean caseInsensitive, String pattern)
	{

		String cat = null;
		cat = _definedCategories.get(categoryName);
		if (cat != null && !categoryName.equals(InternalRegexIgnoreCase))
		{
			String catstr = cat;

			if (caseInsensitive)
			{
				if (categoryName.equals("Ll") || categoryName.equals("Lu") || categoryName.equals("Lt"))
				{
					// when RegexOptions.IgnoreCase is specified then {Ll}, {Lu}, and {Lt} cases should all match
					catstr = (String) _definedCategories.get(InternalRegexIgnoreCase);
				}
			}

			if (invert)
			{
				catstr = NegateCategory(catstr); // negate the category
			}

			_categories.append((String) catstr);
		}
		else
		{
			AddSet(SetFromProperty(categoryName, invert, pattern));
		}
	}

	private void AddCategory(String category)
	{
		_categories.append(category);
	}

	/*
	 * AddLowerCase()
	 *
	 * Adds to the class any lowercase versions of characters already
	 * in the class. Used for case-insensitivity.
	 */
	public void AddLowercase(Locale culture)
	{
		int i;
		int origSize;
		SingleRange range;

		_canonical = false;

		for (i = 0, origSize = _rangelist.size(); i < origSize; i++)
		{
			range = _rangelist.get(i);
			if (range._first == range._last)
			{
				range._first = range._last = Character.toLowerCase(range._first);
			}
			else
			{
				AddLowercaseRange(range._first, range._last, culture);
			}
		}
	}

	/*
	 * AddLowercaseRange()
	 *
	 * For a single range that's in the set, adds any additional ranges
	 * necessary to ensure that lowercase equivalents are also included.
	 */
	private void AddLowercaseRange(char chMin, char chMax, Locale culture)
	{
		int i, iMax, iMid;
		char chMinT, chMaxT;
		LowerCaseMapping lc = new LowerCaseMapping();

		for (i = 0, iMax = _lcTable.length; i < iMax;)
		{
			iMid = (i + iMax) / 2;
			if (_lcTable[iMid]._chMax < chMin)
			{
				i = iMid + 1;
			}
			else
			{
				iMax = iMid;
			}
		}

		if (i >= _lcTable.length)
		{
			return;
		}

		for (; i < _lcTable.length && (lc = _lcTable[i])._chMin <= chMax; i++)
		{
			if ((chMinT = lc._chMin) < chMin)
			{
				chMinT = chMin;
			}

			if ((chMaxT = lc._chMax) > chMax)
			{
				chMaxT = chMax;
			}

			switch (lc._lcOp)
			{
				case LowercaseSet:
					chMinT = (char)lc._data;
					chMaxT = (char)lc._data;
					break;
				case LowercaseAdd:
					chMinT += (char)lc._data;
					chMaxT += (char)lc._data;
					break;
				case LowercaseBor:
					chMinT |= (char)1;
					chMaxT |= (char)1;
					break;
				case LowercaseBad:
					chMinT += (char)(chMinT & 1);
					chMaxT += (char)(chMaxT & 1);
					break;
			}

			if (chMinT < chMin || chMaxT > chMax)
			{
				AddRange(chMinT, chMaxT);
			}
		}
	}

	public void AddWord(boolean ecma, boolean negate)
	{
		if (negate)
		{
			if (ecma)
			{
				AddSet(RegexCharClass.NotECMAWordSet);
			}
			else
			{
				AddCategory(RegexCharClass.NotWord);
			}
		}
		else
		{
			if (ecma)
			{
				AddSet(RegexCharClass.ECMAWordSet);
			}
			else
			{
				AddCategory(RegexCharClass.Word);
			}
		}
	}

	public void AddSpace(boolean ecma, boolean negate)
	{
		if (negate)
		{
			if (ecma)
			{
				AddSet(RegexCharClass.NotECMASpaceSet);
			}
			else
			{
				AddCategory(RegexCharClass.NotSpace);
			}
		}
		else
		{
			if (ecma)
			{
				AddSet(RegexCharClass.ECMASpaceSet);
			}
			else
			{
				AddCategory(RegexCharClass.Space);
			}
		}
	}

	public void AddDigit(boolean ecma, boolean negate, String pattern)
	{
		if (ecma)
		{
			if (negate)
			{
				AddSet(RegexCharClass.NotECMADigitSet);
			}
			else
			{
				AddSet(RegexCharClass.ECMADigitSet);
			}
		}
		else
		{
			AddCategoryFromName("Nd", negate, false, pattern);
		}
	}

	public static String ConvertOldStringsToClass(String set, String category)
	{
		StringBuilder sb = new StringBuilder(set.length() + category.length() + 3);

		if (set.length() >= 2 && set.charAt(0) == '\0' && set.charAt(1) == '\0')
		{
			sb.append((char) 0x1);
			sb.append((char)(set.length() - 2));
			sb.append((char) category.length());
			sb.append(set.substring(2));
		}
		else
		{
			sb.append((char) 0x0);
			sb.append((char) set.length());
			sb.append((char) category.length());
			sb.append(set);
		}
		sb.append(category);

		return sb.toString();
	}

	/*
	 * SingletonChar()
	 *
	 * Returns the char
	 */
	public static char SingletonChar(String set)
	{
		Debug.Assert(IsSingleton(set) || IsSingletonInverse(set), "Tried to get the singleton char out of a non singleton character class");
		return set.charAt(SETSTART);
	}

	public static boolean IsMergeable(String charClass)
	{
		return (!IsNegated(charClass) && !IsSubtraction(charClass));
	}

	public static boolean IsEmpty(String charClass)
	{
		if (charClass.charAt(CATEGORYLENGTH) == 0 && charClass.charAt(FLAGS) == 0 && charClass.charAt(SETLENGTH) == 0 && !IsSubtraction(charClass))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/*
	 * IsSingleton()
	 *
	 * True if the set contains a single character only
	 */
	public static boolean IsSingleton(String set)
	{
		if (set.charAt(FLAGS) == 0 && set.charAt(CATEGORYLENGTH) == 0 && set.charAt(SETLENGTH) == 2 && !IsSubtraction(set) && (set.charAt(SETSTART) == Lastchar || set.charAt(SETSTART) + 1 == set.charAt(SETSTART + 1)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static boolean IsSingletonInverse(String set)
	{
		if (set.charAt(FLAGS) == 1 && set.charAt(CATEGORYLENGTH) == 0 && set.charAt(SETLENGTH) == 2 && !IsSubtraction(set) && (set.charAt(SETSTART) == Lastchar || set.charAt(SETSTART) + 1 == set.charAt(SETSTART + 1)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static boolean IsSubtraction(String charClass)
	{
		return (charClass.length() > SETSTART + charClass.charAt(SETLENGTH) + charClass.charAt(CATEGORYLENGTH));
	}

	public static boolean IsNegated(String set)
	{
		return (set != null && set.charAt(FLAGS) == 1);
	}

	public static boolean IsECMAWordChar(char ch)
	{
		// According to ECMA-262, \s, \S, ., ^, and $ use Unicode-based interpretations of
		// whitespace and newline, while \d, \D\, \w, \W, \b, and \B use ASCII-only 
		// interpretations of digit, word character, and word boundary.  In other words,
		// no special treatment of Unicode ZERO WIDTH NON-JOINER (ZWNJ U+200C) and 
		// ZERO WIDTH JOINER (ZWJ U+200D) is required for ECMA word boundaries.
		return CharInClass(ch, ECMAWordClass);
	}

	public static boolean IsWordChar(char ch)
	{
		// According to UTS#18 Unicode Regular Expressions (http://www.unicode.org/reports/tr18/)
		// RL 1.4 Simple Word Boundaries  The class of <word_character> includes all Alphabetic
		// values from the Unicode character database, from UnicodeData.txt [UData], plus the U+200C
		// ZERO WIDTH NON-JOINER and U+200D ZERO WIDTH JOINER.
		return CharInClass(ch, WordClass) || ch == ZeroWidthJoiner || ch == ZeroWidthNonJoiner;
	}

	public static boolean CharInClass(char ch, String set)
	{
		return CharInClassRecursive(ch, set, 0);
	}


	public static boolean CharInClassRecursive(char ch, String set, int start)
	{
		int mySetLength = set.charAt(start + SETLENGTH);
		int myCategoryLength = set.charAt(start + CATEGORYLENGTH);
		int myEndPosition = start + SETSTART + mySetLength + myCategoryLength;

		boolean subtracted = false;

		if (set.length() > myEndPosition)
		{
			subtracted = CharInClassRecursive(ch, set, myEndPosition);
		}

		boolean b = CharInClassInternal(ch, set, start, mySetLength, myCategoryLength);

		// Note that we apply the negation *before* performing the subtraction.  This is because
		// the negation only applies to the first char class, not the entire subtraction. 
		if (set.charAt(start + FLAGS) == 1)
		{
			b = !b;
		}

		return b && !subtracted;
	}

	/*
	 * CharInClass()
	 *
	 * Determines a character's membership in a character class (via the
	 * string representation of the class).
	 */
	private static boolean CharInClassInternal(char ch, String set, int start, int mySetLength, int myCategoryLength)
	{
		int min;
		int max;
		int mid;
		min = start + SETSTART;
		max = min + mySetLength;

		while (min != max)
		{
			mid = (min + max) / 2;
			if (ch < set.charAt(mid))
			{
				max = mid;
			}
			else
			{
				min = mid + 1;
			}
		}

		// The starting position of the set within the character class determines
		// whether what an odd or even ending position means.  If the start is odd, 
		// an *even* ending position means the character was in the set.  With recursive 
		// subtractions in the mix, the starting position = start+SETSTART.  Since we know that 
		// SETSTART is odd, we can simplify it out of the equation.  But if it changes we need to 
		// reverse this check. 
		Debug.Assert((SETSTART & 0x1) == 1, "If SETSTART is not odd, the calculation below this will be reversed");
		if ((min & 0x1) == (start & 0x1))
		{
			return true;
		}
		else
		{
			if (myCategoryLength == 0)
			{
				return false;
			}

			return CharInCategory(ch, set, start, mySetLength, myCategoryLength);
		}
	}

	private static boolean CharInCategory(char ch, String set, int start, int mySetLength, int myCategoryLength)
	{
		//UnicodeCategory chcategory = Character.GetUnicodeCategory(ch);
		UnicodeCategory chcategory = UnicodeCategory.GetUnicodeCategory(ch);

		int i = start + SETSTART + mySetLength;
		int end = i + myCategoryLength;
		while (i < end)
		{
			int curcat = (short) set.charAt(i);

			if (curcat == 0)
			{
				// zero is our marker for a group of categories - treated as a unit
				RefObject<Integer> tempRef_i = new RefObject<Integer>(i);
				boolean tempVar = CharInCategoryGroup(ch, chcategory, set, tempRef_i);
					i = tempRef_i.argValue;
				if (tempVar)
				{
					return true;
				}
			}
			else if (curcat > 0)
			{
				// greater than zero is a positive case

				if (curcat == SpaceConst)
				{
					if (Character.isWhitespace(ch))
					{
						return true;
					}
					else
					{
						i++;
						continue;
					}
				}
				--curcat;

				if (chcategory == UnicodeCategory.forValue(curcat))
				{
					return true;
				}
			}
			else
			{
				// less than zero is a negative case
				if (curcat == NotSpaceConst)
				{
					if (!Character.isWhitespace(ch))
					{
						return true;
					}
					else
					{
						i++;
						continue;
					}
				}

				//curcat = -curcat;
				//--curcat;
				curcat = -1 - curcat;

				if (chcategory != UnicodeCategory.forValue(curcat))
				{
					return true;
				}
			}
			i++;
		}
		return false;
	}

	/*
	*  CharInCategoryGroup
	*  This is used for categories which are composed of other categories - L, N, Z, W...
	*  These groups need special treatment when they are negated
	*/
	private static boolean CharInCategoryGroup(char ch, UnicodeCategory chcategory, String category, RefObject<Integer> i)
	{
		i.argValue++;

		int curcat = (short) category.charAt(i.argValue);
		if (curcat > 0)
		{
			// positive case - the character must be in ANY of the categories in the group
			boolean answer = false;

			while (curcat != 0)
			{
				if (!answer)
				{
					--curcat;
					if (chcategory == UnicodeCategory.forValue(curcat))
					{
						answer = true;
					}
				}
				i.argValue++;
				curcat = (short) category.charAt(i.argValue);
			}
			return answer;
		}
		else
		{

			// negative case - the character must be in NONE of the categories in the group
			boolean answer = true;

			while (curcat != 0)
			{
				if (answer)
				{
					//curcat = -curcat;
					//--curcat;
					curcat = -1 - curcat;
					if (chcategory == UnicodeCategory.forValue(curcat))
					{
						answer = false;
					}
				}
				i.argValue++;
				curcat = (short) category.charAt(i.argValue);
			}
			return answer;
		}
	}

	private static String NegateCategory(String category)
	{
		if (category == null)
		{
			return null;
		}

		StringBuilder sb = new StringBuilder(category.length());

		for (int i = 0; i < category.length(); i++)
		{
			short ch = (short) category.charAt(i);
			sb.append((char) - ch);
		}
		return sb.toString();
	}

	public static RegexCharClass Parse(String charClass)
	{
		return ParseRecursive(charClass, 0);
	}

	private static RegexCharClass ParseRecursive(String charClass, int start)
	{
		int mySetLength = charClass.charAt(start + SETLENGTH);
		int myCategoryLength = charClass.charAt(start + CATEGORYLENGTH);
		int myEndPosition = start + SETSTART + mySetLength + myCategoryLength;

		java.util.ArrayList<SingleRange> ranges = new java.util.ArrayList<SingleRange>(mySetLength);
		int i = start + SETSTART;
		int end = i + mySetLength;
		while (i < end)
		{
			char first = charClass.charAt(i);
			i++;

			char last;
			if (i < end)
			{
				last = (char)(charClass.charAt(i) - 1);
			}
			else
			{
				last = Lastchar;
			}
			i++;
			ranges.add(new SingleRange(first, last));
		}

		RegexCharClass sub = null;
		if (charClass.length() > myEndPosition)
		{
			sub = ParseRecursive(charClass, myEndPosition);
		}

		return new RegexCharClass(charClass.charAt(start + FLAGS) == 1, ranges, new StringBuilder(charClass.substring(end, end + myCategoryLength)), sub);
	}

	/*
	 * RangeCount()
	 *
	 * The number of single ranges that have been accumulated so far.
	 */
	private int RangeCount()
	{
		return _rangelist.size();
	}

	/*
	 * ToString()
	 *
	 * Constructs the string representation of the class.
	 */
	public String ToStringClass()
	{
		if (!_canonical)
		{
			Canonicalize();
		}

		// make a guess about the length of the ranges.  We'll update this at the end. 
		// This is important because if the last range ends in LastChar, we won't append
		// LastChar to the list. 
		int rangeLen = _rangelist.size() * 2;
		StringBuilder sb = new StringBuilder(rangeLen + _categories.length() + 3);

		int flags;
		if (_negate)
		{
			flags = 1;
		}
		else
		{
			flags = 0;
		}

		sb.append((char) flags);
		sb.append((char) rangeLen);
		sb.append((char) _categories.length());

		for (int i = 0; i < _rangelist.size(); i++)
		{
			SingleRange currentRange = _rangelist.get(i);
			sb.append(currentRange._first);

			if (currentRange._last != Lastchar)
			{
				sb.append((char)(currentRange._last + 1));
			}
		}

		sb.setCharAt(SETLENGTH, (char)(sb.length() - SETSTART));

		sb.append(_categories);

		if (_subtractor != null)
		{
			sb.append(_subtractor.ToStringClass());
		}

		return sb.toString();
	}

	/*
	 * GetRangeAt(int i)
	 *
	 * The ith range.
	 */
	private SingleRange GetRangeAt(int i)
	{
		return _rangelist.get(i);
	}

	/*
	 * Canonicalize()
	 *
	 * Logic to reduce a character class to a unique, sorted form.
	 */
	private void Canonicalize()
	{
		SingleRange CurrentRange;
		int i;
		int j;
		char last;
		boolean Done;

		_canonical = true;
		//java.util.Collections.sort(_rangelist, 0, _rangelist.size(), new SingleRangeComparer());
		java.util.Collections.sort(_rangelist, new SingleRangeComparer());

		//
		// Find and eliminate overlapping or abutting ranges
		//

		if (_rangelist.size() > 1)
		{
			Done = false;

			for (i = 1, j = 0; ; i++)
			{
				for (last = _rangelist.get(j)._last; ; i++)
				{
					if (i == _rangelist.size() || last == Lastchar)
					{
						Done = true;
						break;
					}

					if ((CurrentRange = _rangelist.get(i))._first > last + 1)
					{
						break;
					}

					if (last < CurrentRange._last)
					{
						last = CurrentRange._last;
					}
				}

				_rangelist.get(j)._last = last;

				j++;

				if (Done)
				{
					break;
				}

				if (j < i)
				{
					_rangelist.set(j, _rangelist.get(i));
				}
			}
			//_rangelist.removeRange(j, _rangelist.size() - j + j);
			//@czc
			ArrayExt.removeRange(_rangelist, j, _rangelist.size() - j + j);
		}
	}

	private static String SetFromProperty(String capname, boolean invert, String pattern)
	{
		int min = 0;
		int max = _propTable.length;
		while (min != max)
		{
			int mid = (min + max) / 2;
			//int res = String.Compare(capname, _propTable[mid][0], StringComparison.Ordinal);
			int res = capname.compareTo(_propTable[mid][0]);
			if (res < 0)
			{
				max = mid;
			}
			else if (res > 0)
			{
				min = mid + 1;
			}
			else
			{
				String set = _propTable[mid][1];
				Debug.Assert(!DotNetToJavaStringHelper.isNullOrEmpty(set), "Found a null/empty element in RegexCharClass prop table");
				if (invert)
				{
					if (set.charAt(0) == Nullchar)
					{
						return set.substring(1);
					}
					return Nullchar + set;
				}
				else {return set;}
			}
		}
		throw new IllegalArgumentException(SR.GetString(SR.MakeException, pattern, SR.GetString(SR.UnknownProperty, capname)));
	}

	// Lower case mapping descriptor.
//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class will differ from the original:
//ORIGINAL LINE: private struct LowerCaseMapping
	private final static class LowerCaseMapping
	{
		public LowerCaseMapping()
		{
		}

		public LowerCaseMapping(char chMin, char chMax, int lcOp, int data)
		{
			_chMin = chMin;
			_chMax = chMax;
			_lcOp = lcOp;
			_data = data;
		}

		public char _chMin;
		public char _chMax;
		public int _lcOp;
		public int _data;

		public LowerCaseMapping clone()
		{
			LowerCaseMapping varCopy = new LowerCaseMapping();

			varCopy._chMin = this._chMin;
			varCopy._chMax = this._chMax;
			varCopy._lcOp = this._lcOp;
			varCopy._data = this._data;

			return varCopy;
		}
	}

	/*
	 * SingleRangeComparer
	 *
	 * For sorting ranges; compare based on the first char in the range.
	 */
	private final static class SingleRangeComparer implements java.util.Comparator<SingleRange>
	{
		public int compare(SingleRange x, SingleRange y)
		{
			return ((x)._first < (y)._first ? - 1 : ((x)._first > (y)._first ? 1 : 0));
		}
	}

	/*
	 * SingleRange
	 *
	 * A first/last pair representing a single range of characters.
	 */
	private final static class SingleRange
	{
		public SingleRange(char first, char last)
		{
			_first = first;
			_last = last;
		}

		public char _first;
		public char _last;
	}
}
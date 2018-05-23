package com.zoe.framework.sql2o.tools;

import junit.framework.TestCase;

public class UnderscoreToCamelCaseTests extends TestCase {

	public void testBasicConversions() {
		assertEquals("myStringVariable", CamelCaseUtils.underscoreToCamelCase("my_string_variable"));
		assertEquals("string", CamelCaseUtils.underscoreToCamelCase("string"));
		assertEquals("myReallyLongStringVariableName", CamelCaseUtils.underscoreToCamelCase("my_really_long_string_variable_name"));
		assertEquals("myString2WithNumbers4", CamelCaseUtils.underscoreToCamelCase("my_string2_with_numbers_4"));
		assertEquals("myStringWithMixedCase", CamelCaseUtils.underscoreToCamelCase("my_string_with_MixED_CaSe"));
	}
	
	public void testNullString() {
		assertNull(CamelCaseUtils.underscoreToCamelCase(null));
	}
	
	public void testEmptyStrings() {
		assertEquals("", CamelCaseUtils.underscoreToCamelCase(""));
		assertEquals(" ", CamelCaseUtils.underscoreToCamelCase(" "));
	}
	public void testWhitespace() {
		assertEquals("\t", CamelCaseUtils.underscoreToCamelCase("\t"));
		assertEquals("\n\n", CamelCaseUtils.underscoreToCamelCase("\n\n"));
	}
}

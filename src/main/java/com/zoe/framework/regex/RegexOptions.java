package com.zoe.framework.regex;

//------------------------------------------------------------------------------
// <copyright file="RegexOptions.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>                                                                
//------------------------------------------------------------------------------




	/** <devdoc>
		<p>[To be supplied.]</p>
	 </devdoc>
	*/
	public class RegexOptions
	{
		/** <devdoc>
			<p>[To be supplied.]</p>
		 </devdoc>
		*/
		public static final RegexOptions None = new RegexOptions(0x0000);

		/** <devdoc>
			<p>[To be supplied.]</p>
		 </devdoc>
		*/
		public static final RegexOptions IgnoreCase = new RegexOptions(0x0001); // "i"

		/** <devdoc>
			<p>[To be supplied.]</p>
		 </devdoc>
		*/
		public static final RegexOptions Multiline = new RegexOptions(0x0002); // "m"

		/** <devdoc>
			<p>[To be supplied.]</p>
		 </devdoc>
		*/
		public static final RegexOptions ExplicitCapture = new RegexOptions(0x0004); // "n"

		/** <devdoc>
			<p>[To be supplied.]</p>
		 </devdoc>
		*/
		public static final RegexOptions Compiled = new RegexOptions(0x0008); // "c"

		/** <devdoc>
			<p>[To be supplied.]</p>
		 </devdoc>
		*/
		public static final RegexOptions Singleline = new RegexOptions(0x0010); // "s"

		/** <devdoc>
			<p>[To be supplied.]</p>
		 </devdoc>
		*/
		public static final RegexOptions IgnorePatternWhitespace = new RegexOptions(0x0020); // "x"

		/** <devdoc>
			<p>[To be supplied.]</p>
		 </devdoc>
		*/
		public static final RegexOptions RightToLeft = new RegexOptions(0x0040); // "r"

		/** <devdoc>
			<p>[To be supplied.]</p>
		 </devdoc>
		*/
		public static final RegexOptions ECMAScript = new RegexOptions(0x0100); // "e"

		/** <devdoc>
			<p>[To be supplied.]</p>
		 </devdoc>
		*/
		public static final RegexOptions CultureInvariant = new RegexOptions(0x0200);

		private int intValue;
		private static java.util.HashMap<Integer, RegexOptions> mappings;
		private static java.util.HashMap<Integer, RegexOptions> getMappings()
		{
			if (mappings == null)
			{
				synchronized (RegexOptions.class)
				{
					if (mappings == null)
					{
						mappings = new java.util.HashMap<Integer, RegexOptions>();
					}
				}
			}
			return mappings;
		}

		private RegexOptions(int value)
		{
			intValue = value;
			synchronized (RegexOptions.class)
			{
				getMappings().put(value, this);
			}
		}

		public int getValue()
		{
			return intValue;
		}

		public static RegexOptions forValue(int value)
		{
			synchronized (RegexOptions.class)
			{
				RegexOptions enumObj = getMappings().get(value);
				if (enumObj == null)
				{
					return new RegexOptions(value);
				}
				else
				{
					return enumObj;
				}
			}
		}
	}
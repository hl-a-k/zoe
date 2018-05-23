package com.zoe.framework.regex;

/**
 * .NET 与 Java 对char的Unicode分类方式不一样，这边要用.NET的方式来
 *
 * @author Administrator
 */
public enum UnicodeCategory {

    UppercaseLetter(0),

    LowercaseLetter(1),

    TitlecaseLetter(2),

    ModifierLetter(3),

    OtherLetter(4),

    NonSpacingMark(5),

    SpacingCombiningMark(6),

    EnclosingMark(7),

    DecimalDigitNumber(8),

    LetterNumber(9),

    OtherNumber(10),

    SpaceSeparator(11),

    LineSeparator(12),

    ParagraphSeparator(13),

    Control(14),

    Format(15),

    Surrogate(16),

    PrivateUse(17),

    ConnectorPunctuation(18),

    DashPunctuation(19),

    OpenPunctuation(20),

    ClosePunctuation(21),

    InitialQuotePunctuation(22),

    FinalQuotePunctuation(23),

    OtherPunctuation(24),

    MathSymbol(25),

    CurrencySymbol(26),

    ModifierSymbol(27),

    OtherSymbol(28),

    OtherNotAssigned(29);

    private static java.util.HashMap<Integer, UnicodeCategory> mappings;
    private int intValue;

    private UnicodeCategory(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, UnicodeCategory> getMappings() {
        if (mappings == null) {
            synchronized (UnicodeCategory.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, UnicodeCategory>();
                }
            }
        }
        return mappings;
    }

    public static UnicodeCategory forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }

    /**  以下代码来自 .NET的Char类中   **/

    public static UnicodeCategory GetUnicodeCategory(char c) {
        if (IsLatin1(c)) {
            return GetLatin1UnicodeCategory(c);
        }
        return InternalGetUnicodeCategory((int) c);
    }

    private static boolean IsLatin1(char ch)
    {
        //判断是否为unicode字母表的前256位
        return (int) ch <= 255;
    }

    private static final byte[] categoryForLatin1 = new byte[] { 14, 14, 14,
            14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
            14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 11, 24, 24, 24, 26,
            24, 24, 24, 20, 21, 24, 25, 24, 19, 24, 24, 8, 8, 8, 8, 8, 8, 8, 8,
            8, 8, 24, 24, 25, 25, 25, 24, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 24, 21, 27, 18,
            27, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 20, 25, 21, 25, 14, 14, 14, 14, 14, 14, 14, 14, 14,
            14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
            14, 14, 14, 14, 14, 14, 14, 11, 24, 26, 26, 26, 26, 28, 28, 27, 28,
            1, 22, 25, 19, 28, 27, 28, 25, 10, 10, 27, 1, 28, 24, 27, 10, 1,
            23, 10, 10, 10, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 25, 1, 1, 1, 1,
            1, 1, 1, 1 };

    private static UnicodeCategory GetLatin1UnicodeCategory(char ch) {
        return UnicodeCategory.forValue(categoryForLatin1[(int) ch]);
    }

    private static UnicodeCategory InternalGetUnicodeCategory(int ch) {
        return InternalGetCategoryValue(ch, 0);
    }

    private static UnicodeCategory InternalGetCategoryValue(int ch, int offset) {
        //@czc:涉及指针，暂时无法处理！！！
        // 很特殊的字符
        return OtherLetter;
        // ushort num = CharUnicodeInfo.s_pCategoryLevel1Index[(IntPtr)(ch >>
        // 8)];
        // num = CharUnicodeInfo.s_pCategoryLevel1Index[(IntPtr)((int)num + (ch
        // >> 4 & 15))];
        // byte* ptr = (byte*)(CharUnicodeInfo.s_pCategoryLevel1Index +
        // (IntPtr)num);
        // byte b = ptr[(IntPtr)(ch & 15) / 1];
        // return CharUnicodeInfo.s_pCategoriesValue[(IntPtr)((int)(b * 2) +
        // offset) / 1];
    }
}
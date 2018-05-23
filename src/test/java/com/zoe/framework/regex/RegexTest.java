package com.zoe.framework.regex;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Created by caizhicong on 2016/7/27.
 */
public class RegexTest {
    public static final String SQLInject = "(and|exec|select|update|or|'|''|)";

    /**
     * 匹配国内固定电话 支持3-4位区号3-8位直播号码（114,110）1-4位分机号
     */
    public static final String ChinaPhone = "(^[0-9]{3,4}\\-[0-9]{3,8}(-(\\d{1,4}))?$)|(^[0-9]{3,8}$)"; // @"\d{3}-\d{8}|\d{4}-\d{7}";

    /**
     * 支持:支持手机号码(11-12位)3-4位区号3-8位直播号码（114,110）1-4位分机号
     */
    public static final String Phone = "(^[0-9]{3,4}\\-[0-9]{3,8}(-(\\d{1,4}))?$)|(^[0-9]{3,8}$)|(^0{0,1}1[3|4|5|7|8][0-9]{9}$)";

    /**
     * 支持:支持手机号码(11-12位)3-4位区号 1-4位分机号
     * 区号允许不需要横杠
     */
    public static final String Phone2 = "(^(0[0-9]{2,3}(\\-?))?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?$)|(^0{0,1}1[3|4|5|7|8][0-9]{9}$)";

    /**
     * 匹配国内手机，为11或12位，如果为12位,那么第一位为0
     */
    public static final String ChinaMobile = "^0{0,1}1[3|4|5|7|8][0-9]{9}$";

    /**
     * 匹配Email
     */
    public static final String Email = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";

    /**
     * 匹配浮点数 Float
     */
    public static final String Float = "^[0-9]*(.)?[0-9]+$";

    /**
     * 匹配金额,Money,Decimal,[0.00],[1.23],[4.56]
     */
    public static final String Decimal = "^(-)?(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){1,2})?$";

    /**
     * 匹配金额,Money,Decimal,[0.00],[1.23],[4.56]
     */
    public static final String Money = "^(-)?(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){1,2})?$";

    /**
     * 匹配IP V4
     */
    public static final String IP = "\\d+\\.\\d+\\.\\d+\\.\\d+";

    /**
     * 匹配由数字、26个英文字母或者下划线组成的字符串
     */
    public static final String Word = "^\\w+$";

    /**
     * 匹配由数字、26个英文字母或者下划线组成的字符串
     */
    public static final String URL = "^(http|https|ftp)\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&%\\$#\\=~])*$";

    /**
     * 匹配由26个英文字母(包含大小写)组成的字符串
     */
    public static final String Letter = "^[A-Za-z]+$";

    /**
     * 匹配由26个大写英文字母组成的字符串
     */
    public static final String UppercaseLetter = "^[A-Z]+$";

    /**
     * 匹配由26个小写英文字母组成的字符串
     */
    public static final String LetterAndDigit = "^[A-Za-z0-9]+$";

    /**
     * 匹配字母、数字、中文组成的字符串
     */
    public static final String LetterAndDigitAndChinese = "^[A-Za-z0-9\\u4e00-\\u9fa5]+$";

    /**
     * 匹配双字节字符(包括汉字在内)
     */
    public static final String DoubleByteCharacter = "[^\\u0000-\\u00ff]";

    /**
     * 匹配中文字符
     */
    public static final String Chinese = "[\\u4e00-\\u9fa5]";

    /**
     * 匹配HTML标签
     */
    public static final String HtmlTag = "<(\\S*?)[^>]*>.*?</\\1>|<.*? />";

    /**
     * 匹配身份证
     */
    public static final String IDCard = "\\d{15}|\\d{18}";

    /**
     * 匹配负整数
     */
    public static final String NegativeInteger = "^-[1-9]\\d*$";

    /**
     * 匹配正整数,即大于0的整数
     */
    public static final String PositiveInteger = "^[1-9]\\d*$";

    /**
     * 匹配正整数和0,即大于等于0的整数
     */
    public static final String PositiveIntegerWithZero = "^\\d+$";

    /**
     * 匹配数字字符串
     */
    public static final String Number = "^\\d+$";

    /**
     * 匹配腾讯QQ号
     */
    public static final String QQ = "[1-9][0-9]{4,}";

    /**
     * 匹配中国邮政编码(6位数字)
     */
    public static final String ZipCode = "[1-9][0-9]{4,}";

    /**
     * 验证允许使用 5 个或 9 个数字的美国邮政编码
     */
    public static final String AmericanZipCode = "^(\\d{5}-\\d{4}|\\d{5}|\\d{9})$|^([a-zA-Z]\\d[a-zA-Z] \\d[a-zA-Z]\\d)$";

    //region 正则匹配

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断输入的字符串是否匹配某种正则表达式
     *
     * @param input   输入的字符串
     * @param pattern 所需匹配的正则表达式,RegexHelper类中提供可选常量
     * @return 是否匹配
     */
    public static boolean IsMatch(String input, String pattern) {
        if (isBlank(input)) {
            return false;
        }
        return Regex.IsMatch(input, pattern);
    }

    /**
     * 正则替换,替换匹配项为空字符串
     *
     * @param input   输入的字符串
     * @param pattern 所需匹配的正则表达式
     */
    public static String Replace(String input, String pattern) {
        return Replace(input, pattern, "");
    }

    /**
     * 正则替换,替换匹配项为指定字符串
     *
     * @param input       输入的字符串
     * @param pattern     所需匹配的正则表达式
     * @param replacement
     */
    public static String Replace(String input, String pattern, String replacement) {
        return Regex.Replace(input, pattern, replacement, RegexOptions.IgnoreCase);
    }

    /**
     * 判断字符串是否为小于等于指定长度的数字
     *
     * @param input
     * @param maxLen
     * @return
     */
    public static boolean IsMaxLen(String input, int maxLen) {
        if (input == null) {
            return false;
        }
        return Regex.IsMatch(input.trim(), "^\\d{0," + maxLen + "}$");
    }

    public static void main(String[] args) {
        boolean isMatch = Regex.IsMatch("5074213", Phone);
        System.out.println(isMatch);

        isMatch = Pattern.matches(Phone, "5074213");
        System.out.println(isMatch);

        String pattern = "[\\\\|\\/|\\;|\\,|\\#]";
        pattern = "[\\/|\\\\|\\;|\\,|\\#|\\、|\\，|\\；]";
        String[] phones = "\\".split(pattern);
        phones = "\\\\".split(pattern);
        phones = "123\456".split(pattern);//\45==%
        phones = "123\\456".split(pattern);
        phones = "123\\\456".split(pattern);
        phones = "123\\\\456".split(pattern);
        phones = "123/456".split(pattern);
        phones = "123//456".split(pattern);
        phones = "123///456".split(pattern);
        phones = "123////456".split(pattern);
        phones = "\\/".split(pattern);
        phones = ";".split(pattern);
        phones = ",".split(pattern);
        phones = "#".split(pattern);
        phones = "1#2".split(pattern);
        phones = "1\\2".split(pattern);
        phones = "3;4".split(pattern);
        phones = "5,6".split(pattern);
        phones = "5,6; ".split(pattern);
        phones = "5 , 6; ".split(pattern);
        phones = "5,6#7".split(pattern);
        phones = "5,6\\8#9".split(pattern);
        phones = "5,6,1;5555;444#11111,333333".split(pattern);
    }

    @Test
    public void Test01(){
        Assert.assertTrue(Regex.IsMatch("'or 1 = 1", SQLInject));
        Assert.assertTrue(Regex.IsMatch("5074213", ChinaPhone));
        Assert.assertTrue(Regex.IsMatch("15259132168", ChinaMobile));
        Assert.assertTrue(Regex.IsMatch("18030172970", Phone));
        Assert.assertTrue(Regex.IsMatch("18030172970", Phone2));

        Assert.assertTrue(Regex.IsMatch("中国人", RegexTest.Chinese));
        Assert.assertFalse(Regex.IsMatch("ha ha ha", RegexTest.Chinese));
        Assert.assertTrue(Regex.IsMatch("1.23", RegexTest.Decimal));
        Assert.assertFalse(Regex.IsMatch("a1.23", RegexTest.Decimal));
        Assert.assertTrue(Regex.IsMatch("czc945@126.com", RegexTest.Email));
        Assert.assertTrue(Regex.IsMatch("360555192107212016", RegexTest.IDCard));
    }
}

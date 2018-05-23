package com.zoe.framework.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 卡号工具类
 * Created by caizhicong on 2017/8/5.
 */
public class CardUtils {

    public static void main(String[] args) {
        //卡号转换
        String card9 = "D58009678";
        String card12 = Cng9toXM12(card9);
        String card9_2 = CngXM12to9(card12);
        System.out.println(card9.equals(card9_2));

        String idCard15 = "370802940221002";
        String idCard18 = idCard15To18(idCard15);
        System.out.println(idCard18.equals("370802199402210029"));

        idCard15 = idCard18To15("370802199402210029");
        System.out.println(idCard15.equals("370802940221002"));

        //身份证校验
        System.out.println(isIdCard("370802940221002"));
        System.out.println(isIdCard("370802199402210029"));
        System.out.println(isIdCard("350211192404101521"));
        System.out.println(isIdCard("350681198907202012"));
        System.out.println(isIdCard("35068119890720201X"));
    }

    //region 卡号转换


    /**
     * 卡号转换
     *
     * @param cardNo 9位D开头的社保卡
     * @return
     */
    public static String Cng9toXM12(String cardNo) {
        if (cardNo.length() == 9 && cardNo.startsWith("D")) {
            cardNo = "35020" + cardNo.substring(1, 8);
        }
        return cardNo;
    }

    private static int[] iXMKey = new int[]{3, 7, 9, 10, 5, 8, 4, 2}; //厦门换卡加权因子

    /**
     * 将厦门12位卡号转换为省9位号码
     * 只转换35020开头的12位卡，其余原样返回。不截断空格
     *
     * @param strCard 12位厦门卡号
     * @return
     */
    public static String CngXM12to9(String strCard) {
        //参考文档《社会保障卡序号编制规则》
        //C9 = 11 – MOD （∑（Ci×Wi）, 11)
        if (strCard.length() == 12 && strCard.startsWith("35020")) {
            try {
                StringBuilder sbNewCard = new StringBuilder(9);
                sbNewCard.append("D");
                sbNewCard.append(strCard.substring(5));

                //计算校验码
                int j = 39;
                for (int i = 1; i < 8; i++) {
                    j += iXMKey[i] * Integer.parseInt(Character.toString(sbNewCard.charAt(i)));
                }
                int e = 11 - j % 11;
                switch (e) {
                    case 10:
                        sbNewCard.append("X");
                        break;
                    case 11:
                        sbNewCard.append("0");
                        break;
                    default:
                        sbNewCard.append(e);
                        break;
                }
                return sbNewCard.toString();
            } catch (Exception e) {
                return strCard;
            }
        } else {
            return strCard;
        }
    }

    /**
     * 各类卡号规则验证(不能使用的方法！仅作为判断规则参考)
     *
     * @param cardNo
     * @return
     */
    public static String validCard(String cardNo) {
        if (cardNo == null || cardNo.length() < 1) {
            return "无法识别的卡号";
        }
        //todo 验证卡号规则
        //E00002358
        //F00001985
        //G03743690
        if (Character.isLetter(cardNo.charAt(0)) && cardNo.length() == 9) {
            //医保卡
            return "医保卡";
        }

        //D51412771
        if (cardNo.charAt(0) == 'D' && cardNo.length() == 9) {
            return ("D开头的厦门社保卡位数应该为9位");
        }

        //A1302019861X
        if (cardNo.charAt(0) == 'A' && cardNo.length() == 12) {
            return ("A开头的正式卡应该为12位");
        }

        //B1425382030
        //B14253077942
        if (cardNo.charAt(0) == 'B' && (cardNo.length() == 11 || cardNo.length() == 12)) {
            return ("B开头的临时卡应该为11或12位");
        }

        //CO350054186
        if (cardNo.charAt(0) == 'C' && cardNo.length() == 11) {
            return ("C开头的临时卡应该为11位");
        }
        return "无法识别的卡号";
    }

    /**
     * 身份证15->18算法
     *
     * @param idCard15 15位身份证号
     */
    public static String idCard15To18(String idCard15) {
        if (idCard15 == null || idCard15.length() != 15) {
            return idCard15;
        }
        try {
            Long.parseLong(idCard15);
        } catch (NumberFormatException ex) {
            return idCard15;
        }

        //加权因子常数
        int[] iW = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        //校验码常数
        final String LastCode = "10X98765432";

        //新身份证号
        //填在第6位及第7位上填上19两个数字
        String idCard18 = idCard15.substring(0, 6) + "19" + idCard15.substring(6, 15);

        int iS = 0;
        //进行加权求和
        for (int i = 0; i < 17; i++) {
            iS += Character.digit(idCard18.charAt(i),10) * iW[i];
        }

        //取模运算，得到模值
        int iY = iS % 11;
        //从LastCode中取得以模为索引号的值，加到身份证的最后一位，即为新身份证号。
        idCard18 += LastCode.substring(iY, iY + 1);

        return idCard18;
    }

    /**
     * 身份证18->15算法
     *
     * @param idCard18 18位身份证号
     */
    public static String idCard18To15(String idCard18){
        if (idCard18 == null || idCard18.length() != 18) {
            return idCard18;
        }
        try {
            Long.parseLong(idCard18);
        } catch (NumberFormatException ex) {
            return idCard18;
        }
        String idCard15 = idCard18.substring(0, 6) + idCard18.substring(8, 17);
        return idCard15;
    }

    //endregion

    //region 身份证校验

    /*********************************** 身份证验证开始 ****************************************/
    /**
     * 身份证号码验证 1、号码的结构 公民身份号码是特征组合码，由十七位数字本体码和一位校验码组成。排列顺序从左至右依次为：六位数字地址码，
     * 八位数字出生日期码，三位数字顺序码和一位数字校验码。 2、地址码(前六位数）
     * 表示编码对象常住户口所在县(市、旗、区)的行政区划代码，按GB/T2260的规定执行。 3、出生日期码（第七位至十四位）
     * 表示编码对象出生的年、月、日，按GB/T7408的规定执行，年、月、日代码之间不用分隔符。 4、顺序码（第十五位至十七位）
     * 表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号， 顺序码的奇数分配给男性，偶数分配给女性。 5、校验码（第十八位数）
     * （1）十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0, ... , 16 ，先对前17位数字的权求和
     * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
     * （2）计算模 Y = mod(S, 11) （3）通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 X 9 8 7 6 5 4 3 2
     */

    /**
     * 省、直辖市代码表：
     *     11 : 北京  12 : 天津  13 : 河北       14 : 山西  15 : 内蒙古
     *     21 : 辽宁  22 : 吉林  23 : 黑龙江  31 : 上海  32 : 江苏
     *     33 : 浙江  34 : 安徽  35 : 福建       36 : 江西  37 : 山东
     *     41 : 河南  42 : 湖北  43 : 湖南       44 : 广东  45 : 广西      46 : 海南
     *     50 : 重庆  51 : 四川  52 : 贵州       53 : 云南  54 : 西藏
     *     61 : 陕西  62 : 甘肃  63 : 青海       64 : 宁夏  65 : 新疆
     *     71 : 台湾
     *     81 : 香港  82 : 澳门
     *     91 : 国外
     */
    private static List<String> cityCode = Arrays.asList( "11", "12", "13", "14", "15", "21",
            "22", "23", "31", "32", "33", "34", "35", "36", "37", "41", "42",
            "43", "44", "45", "46", "50", "51", "52", "53", "54", "61", "62",
            "63", "64", "65", "71", "81", "82", "91" );

    /**
     * 每位加权因子
     */
    private static int[] weightingFactors = { 7, 9, 10, 5, 8, 4, 2,1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };//加权因子
    /**
     * 校验码,其中10代表X
     */
    private static char[] checkCodes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 校验是否为合法的身份证号码（15位或18位）
     *
     * @param idCard 身份证号码
     * @return 合法返回true，否则返回false
     */
    public static boolean isIdCard(String idCard) {
        if (idCard == null) {
            return false;
        }
        if (idCard.length() == 15) {
            return isIdCard15(idCard);
        }
        return isIdCard18(idCard);
    }

    /**
     * <p>
     * 判断18位身份证的合法性
     * </p>
     * 根据〖中华人民共和国国家标准GB11643-1999〗中有关公民身份号码的规定，公民身份号码是特征组合码，由十七位数字本体码和一位数字校验码组成。
     * 排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位数字校验码。
     * <p>
     * 顺序码: 表示在同一地址码所标识的区域范围内，对同年、同月、同 日出生的人编定的顺序号，顺序码的奇数分配给男性，偶数分配 给女性。
     * </p>
     * <p>
     * 1.前1、2位数字表示：所在省份的代码； 2.第3、4位数字表示：所在城市的代码； 3.第5、6位数字表示：所在区县的代码；
     * 4.第7~14位数字表示：出生年、月、日； 5.第15、16位数字表示：所在地的派出所的代码；
     * 6.第17位数字表示性别：奇数表示男性，偶数表示女性；
     * 7.第18位数字是校检码：也有的说是个人信息码，一般是随计算机的随机产生，用来检验身份证的正确性。校检码可以是0~9的数字，有时也用x表示。
     * </p>
     * <p>
     * 第十八位数字(校验码)的计算方法为： 1.将前面的身份证号码17位数分别乘以不同的系数。从第一位到第十七位的系数分别为：7 9 10 5 8 4
     * 2 1 6 3 7 9 10 5 8 4 2
     * </p>
     * <p>
     * 2.将这17位数字和系数相乘的结果相加。
     * </p>
     * <p>
     * 3.用加出来和除以11，看余数是多少
     * </p>
     * 4.余数只可能有0 1 2 3 4 5 6 7 8 9 10这11个数字。其分别对应的最后一位身份证的号码为1 0 X 9 8 7 6 5 4 3
     * 2。
     * <p>
     * 5.通过上面得知如果余数是2，就会在身份证的第18位数字上出现罗马数字的Ⅹ。如果余数是10，身份证的最后一位号码就是2。
     * </p>
     *
     * @param idCard
     * @return
     */
    public static boolean isIdCard18(String idCard) {
        // 非18位为假
        if (idCard == null || idCard.length() != 18) {
            return false;
        }
        // 获取前17位
        String idCard17 = idCard.substring(0, 17);

        // 前17位全部为数字
        try {
            Long.parseLong(idCard17);
        } catch (Exception ex) {
            return false;
        }

        // 校验省份
        if (!cityCode.contains(idCard.substring(0, 2))) {
            return false;
        }

        // 校验出生日期
        try {
            Date birthDay = new SimpleDateFormat("yyyyMMdd").parse(idCard.substring(6, 14));
            GregorianCalendar currentDay = new GregorianCalendar();
            currentDay.setTime(birthDay);
            int year = currentDay.get(Calendar.YEAR);
            if (year < 1900 || birthDay.getTime() > new Date().getTime()) {
                return false;// 出生年月日不正确
            }
        } catch (ParseException e1) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += Character.digit(idCard17.charAt(i), 10) * weightingFactors[i];
        }
        int modValue = sum % 11;
        char expectedCheckCode = checkCodes[modValue];
        // 获取第18位
        char checkCode = idCard.charAt(17);
        return expectedCheckCode == checkCode || (expectedCheckCode == 'X' && checkCode == 'x');
    }

    /**
     * 校验15位身份证
     *
     * <pre>
     * 只校验省份和出生年月日
     * </pre>
     *
     * @param idCard
     * @return
     */
    public static boolean isIdCard15(String idCard) {
        // 非15位为假
        if (idCard == null || idCard.length() != 15) {
            return false;
        }

        // 15全部为数字
        try {
            Long.parseLong(idCard);
        } catch (Exception ex) {
            return false;
        }

        // 校验省份
        if (!cityCode.contains(idCard.substring(0, 2))) {
            return false;
        }

        try {
            Date birthDay = new SimpleDateFormat("yyMMdd").parse(idCard.substring(6, 12));
            if (birthDay.getTime() > new Date().getTime()) {
                return false;// 出生年月日不正确
            }
        } catch (ParseException e1) {
            return false;
        }
        return true;
    }

    //endregion
}

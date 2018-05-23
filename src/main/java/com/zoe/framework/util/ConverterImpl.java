package com.zoe.framework.util;


import com.zoe.framework.i18n.MessageService;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lpw
 */
@Component
public class ConverterImpl implements Converter {

    private static final String[] BIT_SIZE_FORMAT = {"0 B", "0.00 K", "0.00 M", "0.00 G", "0.00 T"};
    private static final String CHAR_SET = "utf-8";
    @Autowired
    protected MessageService message;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Map<String, DecimalFormat> decimalFormatMap = new ConcurrentHashMap<>();
    protected Map<String, FastDateFormat> dateFormatMap = new ConcurrentHashMap<>();
    protected Map<Locale, String> dateFormat = new ConcurrentHashMap<>();
    protected Map<Locale, String> dateTimeFormat = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked"})
    @Override
    public String toString(Object object) {
        if (StringUtils.isEmpty(object))
            return "";

        if (object.getClass().isArray()) {
            StringBuilder sb = new StringBuilder();
            for (int length = Array.getLength(object), i = 0; i < length; i++)
                sb.append(',').append(toString(Array.get(object, i)));

            return sb.substring(1);
        }

        if (object instanceof Iterable) {
            StringBuilder sb = new StringBuilder();
            ((Iterable) object).forEach(obj -> sb.append(',').append(toString(obj)));

            return sb.substring(1);
        }

        if (object instanceof Map) {
            StringBuilder sb = new StringBuilder();
            ((Map) object).forEach((key, value) -> sb.append(',').append(toString(key)).append('=').append(toString(value)));

            return sb.substring(1);
        }

        if (object instanceof java.sql.Date)
            return toString((java.sql.Date) object, getDateFormat());

        if (object instanceof Timestamp)
            return toString((Timestamp) object, getDateTimeFormat());

        return object.toString();
    }

    @Override
    public String toString(Number number, String format) {
        DecimalFormat df = decimalFormatMap.get(format);
        if (df == null) {
            df = new DecimalFormat(format);
            decimalFormatMap.put(format, df);
        }

        return df.format(number);
    }

    @Override
    public String toString(Object number, int decimal, int point) {
        StringBuilder sb = new StringBuilder().append("0.");
        for (int i = 0; i < point; i++)
            sb.append('0');

        return toString(toLong(number) * Math.pow(0.1D, decimal), sb.toString());
    }

    @Override
    public String[] toArray(String string, String separator) {
        if (StringUtils.isEmpty(string))
            return new String[0];

        if (separator == null || !string.contains(separator))
            return new String[]{string};

        if (!string.endsWith(separator))
            return string.split(separator);

        String[] strs = string.split(separator);
        String[] array = new String[strs.length + 1];
        System.arraycopy(strs, 0, array, 0, strs.length);
        array[strs.length] = "";

        return array;
    }

    @Override
    public String[][] toArray(String string, String[] separator) {
        if (StringUtils.isEmpty(string) || StringUtils.isEmpty(separator) || separator.length < 2 || StringUtils.isEmpty(separator[0])
                || StringUtils.isEmpty(separator[1]) || !string.contains(separator[1]))
            return new String[0][0];

        List<String> list = new ArrayList<>();
        for (String str : toArray(string, separator[0]))
            if (string.contains(separator[1]) && str.indexOf(separator[1]) == str.lastIndexOf(separator[1]))
                list.add(str);

        if (list.isEmpty())
            return new String[0][0];

        String[][] array = new String[list.size()][];
        for (int i = 0; i < array.length; i++)
            array[i] = toArray(list.get(i), separator[1]);

        return array;
    }

    @Override
    public String toBitSize(long size) {
        return toBitSize(size < 0 ? 0 : size, 0);
    }

    private String toBitSize(double size, int pattern) {
        if (size >= 1024 && pattern < BIT_SIZE_FORMAT.length - 1)
            return toBitSize(size / 1024, pattern + 1);

        return toString(size, BIT_SIZE_FORMAT[pattern]);
    }

    @Override
    public long toBitSize(String size) {
        if (StringUtils.isEmpty(size))
            return -1L;

        double value = toDouble(size.substring(0, size.length() - 1).trim(), -1);
        char unit = size.toLowerCase().charAt(size.length() - 1);
        if (unit == 't')
            return Math.round(value * 1024 * 1024 * 1024 * 1024);

        if (unit == 'g')
            return Math.round(value * 1024 * 1024 * 1024);

        if (unit == 'm')
            return Math.round(value * 1024 * 1024);

        if (unit == 'k')
            return Math.round(value * 1024);

        return Math.round(toDouble(size.trim(), -1));
    }

    protected double toDouble(String string, double failure) {
        try {
            return Double.parseDouble(string);
        } catch (Exception e) {
            logger.warn("将字符串[{}]转化为浮点数时发生异常！", string, e);

            return failure;
        }
    }

    @Override
    public int toInt(Object object) {
        if (StringUtils.isEmpty(object))
            return 0;

        try {
            return Integer.parseInt(object.toString().replaceAll(",", ""));
        } catch (Exception e) {
            logger.warn("将对象[{}]转化为数值时发生异常！", object, e);

            return 0;
        }
    }

    @Override
    public int[] toInts(String string) {
        String[] array = toArray(string, ",");
        int[] ints = new int[array.length];
        for (int i = 0; i < ints.length; i++)
            ints[i] = toInt(array[i]);

        return ints;
    }

    @Override
    public long toLong(Object object) {
        if (StringUtils.isEmpty(object))
            return 0L;

        try {
            return Long.parseLong(object.toString());
        } catch (Exception e) {
            logger.warn("将对象[{}]转化为数值时发生异常！", object, e);

            return 0L;
        }
    }

    @Override
    public float toFloat(Object object) {
        if (StringUtils.isEmpty(object))
            return 0.0F;

        try {
            return Float.parseFloat(object.toString());
        } catch (Exception e) {
            logger.warn("将对象[{}]转化为数值时发生异常！", object, e);

            return 0.0F;
        }
    }

    @Override
    public double toDouble(Object object) {
        if (StringUtils.isEmpty(object))
            return 0.0D;

        try {
            return Double.parseDouble(object.toString());
        } catch (Exception e) {
            logger.warn("将对象[{}]转化为数值时发生异常！", object, e);

            return 0.0D;
        }
    }

    @Override
    public boolean toBoolean(Object object) {
        if (StringUtils.isEmpty(object))
            return false;

        try {
            return Boolean.parseBoolean(object.toString());
        } catch (Exception e) {
            logger.warn("将对象[{}]转化为布尔值时发生异常！", object, e);

            return false;
        }
    }

    @Override
    public String toString(Date date, String format) {
        return date == null ? "" : getDateFormat(format).format(date);
    }

    @Override
    public Date toDate(Object date) {
        if (StringUtils.isEmpty(date))
            return null;

        if (date instanceof Date)
            return (Date) date;

        if (date instanceof String) {
            String dateFormat = getDateFormat();
            String string = (String) date;

            return toDate(string, string.length() == dateFormat.length() ? dateFormat : getDateTimeFormat());
        }

        return null;
    }

    protected String getDateFormat() {
        Locale locale = LocaleContextHolder.getLocale();
        String format = dateFormat.get(locale);
        if (format == null) {
            String formatKey = "zoe.framework.format.date";
            format = message.get(formatKey);
            if(StringUtils.isEmpty(format) || formatKey.equals(format)){
                format = "yyyy-MM-dd";
            }
            dateFormat.put(locale, format);
        }

        return format;
    }

    protected String getDateTimeFormat() {
        Locale locale = LocaleContextHolder.getLocale();
        String format = dateTimeFormat.get(locale);
        if (format == null) {
            String formatKey = "zoe.framework.format.date-time";
            format = message.get(formatKey);
            if(StringUtils.isEmpty(format) || formatKey.equals(format)){
                format = "yyyy-MM-dd HH:mm:ss";
            }
            dateTimeFormat.put(locale, format);
        }

        return format;
    }

    @Override
    public Date toDate(String date, String format) {
        if (StringUtils.isEmpty(date) || StringUtils.isEmpty(format) || date.length() != format.length())
            return null;

        try {
            return getDateFormat(format).parse(date);
        } catch (ParseException e) {
            logger.warn("使用格式[{}]将字符串[{}]转化为日期值时发生异常！", format, date, e);

            return null;
        }
    }

    protected FastDateFormat getDateFormat(String format) {
        FastDateFormat fdf = dateFormatMap.computeIfAbsent(format, FastDateFormat::getInstance);
        return fdf;
    }

    @Override
    public String encodeUrl(String string, String charset) {
        if (string == null)
            return null;

        try {
            return URLEncoder.encode(string, StringUtils.isEmpty(charset) ? CHAR_SET : charset);
        } catch (UnsupportedEncodingException e) {
            logger.warn("将字符串[{}]进行URL编码[{}]转换时发生异常！", string, charset, e);

            return string;
        }
    }

    @Override
    public String decodeUrl(String string, String charset) {
        if (string == null)
            return null;

        try {
            return URLDecoder.decode(string, StringUtils.isEmpty(charset) ? CHAR_SET : charset);
        } catch (UnsupportedEncodingException e) {
            logger.warn("将字符串[{}]进行URL解码[{}]转换时发生异常！", string, charset, e);

            return string;
        }
    }

    @Override
    public String toFirstLowerCase(String string) {
        return toFirstCase(string, 'A', 'Z', 'a' - 'A');
    }

    @Override
    public String toFirstUpperCase(String string) {
        return toFirstCase(string, 'a', 'z', 'A' - 'a');
    }

    protected String toFirstCase(String string, char start, char end, int shift) {
        if (StringUtils.isEmpty(string))
            return string;

        char ch = string.charAt(0);
        if (ch < start || ch > end)
            return string;

        char[] chars = string.toCharArray();
        chars[0] += shift;

        return new String(chars);
    }

    @Override
    public Map<String, String> toParameterMap(String parameters) {
        Map<String, String> map = new HashMap<>();
        StringBuilder sb = new StringBuilder("&").append(parameters);
        String value = "";
        for (int indexOf; (indexOf = sb.lastIndexOf("&")) > -1; ) {
            String string = sb.substring(indexOf + 1);
            sb.delete(indexOf, sb.length());
            if ((indexOf = string.indexOf('=')) == -1) {
                value = "&" + string + value;

                continue;
            }
            map.put(string.substring(0, indexOf), decodeUrl(string.substring(indexOf + 1) + value, null));
            value = "";
        }

        return map;
    }
}

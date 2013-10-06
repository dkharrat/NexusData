package org.nexusdata.utils;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class StringUtil {
    public static boolean isBlank(String s) {
        return s == null || s.length() == 0;
    }

    public static String join(List<String> list, String separator, boolean ignoreNull) {
        if (list.isEmpty())
            return "";

        StringBuilder b = new StringBuilder();
        for (String item : list) {
            if (!ignoreNull || item != null) {
                b.append(separator).append(item);
            }
        }

        if (b.length() == 0) {
            return "";
        }

        return b.toString().substring(separator.length());
    }

    public static String join(List<String> list, String separator) {
        return join(list, separator, false);
    }

    public static String join(String[] array, String separator, boolean ignoreNull) {
        return join(Arrays.asList(array), separator, ignoreNull);
    }

    public static String join(String[] array, String separator) {
        return join(array, separator, false);
    }

    @SuppressWarnings("unchecked")
    public static Object convertStringValueToType(String value, Class<?> propType) throws ParseException, NumberFormatException, IllegalArgumentException {
        Object result;
        if (propType.isAssignableFrom(Integer.class) || propType.isAssignableFrom(int.class)) {
            result = Integer.parseInt(value);
        } else if (propType.isAssignableFrom(Long.class) || propType.isAssignableFrom(long.class)) {
            result = Long.parseLong(value);
        } else if (propType.isAssignableFrom(String.class)) {
            result = value;
        } else if (propType.isAssignableFrom(Boolean.class) || propType.isAssignableFrom(boolean.class)) {
            result = Boolean.valueOf(value);
        } else if (Enum.class.isAssignableFrom(propType)) {
            if (value != null) {
                result = Enum.valueOf((Class<? extends Enum>)propType, value);
            } else {
                result = null;
            }
        } else if (propType.isAssignableFrom(Date.class)) {
            if (value != null) {
                result = DateUtil.parse(DateUtil.ISO8601_NO_TIMEZONE, value);
            } else {
                result = null;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported property type " + propType + " for value " + value);
        }

        return result;
    }
}

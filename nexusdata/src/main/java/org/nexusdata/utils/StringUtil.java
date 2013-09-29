package org.nexusdata.utils;

import java.util.Arrays;
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
}

package org.webcurator.common.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SafeSimpleDateFormat {


    private static ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<>();

    public static SimpleDateFormat getInstance(String format) {
        sdf.set(new SimpleDateFormat(format));
        return sdf.get();
    }

    public static SimpleDateFormat getInstance(String format, Locale locale) {
        sdf.set(new SimpleDateFormat(format, locale));
        return sdf.get();
    }

}

package org.webcurator.common.util;

import java.text.SimpleDateFormat;

public class SafeSimpleDateFormat {

    private static ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat();
        }
    };

    public static SimpleDateFormat get() {
        return sdf.get();
    }
}

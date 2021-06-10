/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Date;

/**
 * A set of static utilities that can be reused throughout the WCT.
 * These utilities should be generic functions that manipulate
 * simple objects. More complex functionality should be isolated
 * in the managers.
 *
 * @author bbeaumont
 */
public class WctUtils {
    private static final Logger log = LoggerFactory.getLogger(WctUtils.class);
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Checks if a String is null or space.
     *
     * @param str The string to test.
     * @return True if the string is null or whitespace; otherwise false.
     */
    private static boolean isNullOrWhitespace(String str) {
        return str == null || "".equals(str.trim());
    }

    /**
     * This method checks if a value has changed between the original
     * and the new values. This is typically used for testing if a
     * change in parameter should cause an object to be dirty.
     *
     * @param originalValue The original value.
     * @param newValue      The new value.
     * @return true of the value has changed
     */
    public static boolean hasChanged(Object originalValue, Object newValue) {
        // If they are both null, then it hasn't changed.
        if (originalValue == null && newValue == null) {
            return true;
        }

        // For Strings, we allow null and empty string to be the same.
        if ((originalValue == null ||
                originalValue instanceof String &&
                        isNullOrWhitespace((String) originalValue)) &&
                (newValue == null ||
                        newValue instanceof String &&
                                isNullOrWhitespace((String) newValue))) {
            return true;
        }

        return originalValue == null && newValue == null ||
                originalValue != null && originalValue.equals(newValue);
    }

    /**
     * Clears the time component on a Calendar object. The
     * passed in Calendar object is updated.
     *
     * @param cal The calendar object to clear.
     */
    public static void clearTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
    }

    /**
     * Clears the time on a Date object.
     *
     * @param dt The input date.
     * @return A new date with the same date, but with the time cleared.
     */
    public static Date clearTime(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        clearTime(cal);
        return cal.getTime();
    }

    /**
     * Returns a new date at the end of the day of the original
     * date.
     *
     * @param dt The date to find the end of day for.
     * @return A new date representing the end of the day.
     */
    public static Date endOfDay(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        clearTime(cal);

        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);

        return cal.getTime();
    }


    /**
     * Friendly display of a duration<br>
     * Format returned is <code>d:hh:mm:ss</code>
     * with 2 digits for hours/minutes/seconds.
     *
     * @param duration Duration in seconds
     * @return String friendly duration
     */
    public static String getDDhhmmss(Long duration) {
        Long days = duration / 24 / 3600;
        Long rest = duration % (24 * 3600);//duration - 24 * 3600 * days;
        Long hours = rest / 3600;
        rest %= 3600;
        Long minutes = rest / 60;
        rest %= 60;
        Long seconds = rest;

        String sDays = days.toString();

        String sHours = hours.toString();
        if (sHours.length() == 1) {
            sHours = "0" + sHours;
        }

        String sMinutes = minutes.toString();
        if (sMinutes.length() == 1) {
            sMinutes = "0" + sMinutes;
        }

        String sSeconds = seconds.toString();
        if (sSeconds.length() == 1) {
            sSeconds = "0" + sSeconds;
        }

        return sDays + ":" + sHours + ":" + sMinutes + ":" + sSeconds;
    }

    public static String getPrefixClassName(Class clazz) {
        String clazzName = clazz.getName();
        int idx = clazzName.indexOf('$');
        return idx > 0 ? clazzName.substring(0, idx) : clazzName;
    }

    public static void cleanDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return;
        }

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File f : files) {
                    cleanDirectory(f);
                }
            }
        }

        boolean rst = false;
        try {
            rst = Files.deleteIfExists(directory.toPath());
            if (rst) {
                log.debug("Succeed to delete: {}", directory.getAbsolutePath());
            } else {
                log.warn("Failed to delete: {}", directory.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to delete: {}", directory.getAbsolutePath(), e);
        }
    }

    /*Copy ans close streams*/
    public static long copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        try {
            while (EOF != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
        } catch (IOException e) {
            log.error("Failed to copy stream", e);
            throw e;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                log.error("Failed to close input stream");
            }
            try {
                output.close();
            } catch (IOException e) {
                log.error("Failed to close output stream");
            }
        }
        return count;
    }

    public static void main(String... args) {
        File dir = new File("/usr/local/wct/store/19/logs/attached/3");
        cleanDirectory(dir);
    }
}

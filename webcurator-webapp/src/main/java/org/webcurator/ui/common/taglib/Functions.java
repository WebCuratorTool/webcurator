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
package org.webcurator.ui.common.taglib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * This class provides a collection of functions that can be used within the
 * JSTL expressions in a JSP tag.
 *
 * @author bbeaumont
 */
public class Functions {
    private static final Logger log = LoggerFactory.getLogger(Functions.class);


    /**
     * Test whether an int value is contained within a collection.
     *
     * @param coll The collection to test membership of.
     * @param val  The int value to find.
     * @return true if the value is in the collection; otherwise false.
     */
    public static boolean contains(Collection coll, int val) {
        return coll != null && coll.contains(new Integer(val));
    }

    /**
     * Tests whether the object is within the specified collection.
     *
     * @param coll The collection to test membership of.
     * @param val  The value to find.
     * @return true if the object is in the collection; otherwise false.
     */
    public static boolean containsObj(Collection coll, Object val) {
        return coll != null && coll.contains(val);
    }

    /**
     * Tests whether the specified number of hours have elapsed since the specified date.
     *
     * @param xHours  The specified number of hours.
     * @param theDate The date to check.
     * @return true if the current time (now) is more than xHours after the input date; otherwise false.
     */
    public static boolean xHoursElapsed(int xHours, Date theDate) {
        log.info("xHours={}, theDate={}", xHours, theDate);
        Calendar dateToTest = Calendar.getInstance();
        dateToTest.setTime(theDate);
        dateToTest.add(Calendar.HOUR, xHours);

        Calendar dateNow = Calendar.getInstance();
        return dateNow.after(dateToTest);
    }

    /**
     * Tests whether the specified number of seconds have elapsed since the specified date.
     *
     * @param xSeconds The specified number of seconds.
     * @param theDate  The date to check.
     * @return true if the current time (now) is more than xSeconds after the input date; otherwise false.
     */
    public static boolean xSecondsElapsed(int xSeconds, Date theDate) {
        log.info("xSeconds={}, theDate={}", xSeconds, theDate);
        Calendar dateToTest = Calendar.getInstance();
        dateToTest.setTime(theDate);
        dateToTest.add(Calendar.SECOND, xSeconds);

        Calendar dateNow = Calendar.getInstance();
        return dateNow.after(dateToTest);
    }
}

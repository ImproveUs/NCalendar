package com.necer.ncalendar.utils;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by(￣▽￣)／琉璃虎on 2017/8/15.
 */
public class UtilsTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void getSunFirstDayOfWeek() throws Exception {
        DateTime dateTime = new DateTime();
        DateTime sunFirstDayOfWeek = Utils.getSunFirstDayOfWeek(dateTime);
        System.out.println(dateTime.toString("yyyy-MM-dd"));
        System.out.println(sunFirstDayOfWeek.toString("yyyy-MM-dd"));
        System.out.println(dateTime.withDayOfWeek(1).toString("dd"));
    }

}
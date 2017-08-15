package com.necer.ncalendar.listener;

import android.view.GestureDetector;

import com.necer.ncalendar.calendar.WeekCalendar;
import com.necer.ncalendar.view.WeekView;

import org.joda.time.DateTime;

/**
 * Created by necer on 2017/6/13.
 * 周视图日期点击接口
 */

public interface OnClickWeekViewListener {

    /**
     * 显示的日期被点击了
     * 回调方法位置{@link WeekView#mGestureDetector}
     * 回调出口{@link WeekCalendar#onClickCurrentWeek(org.joda.time.DateTime)}
     * @param dateTime  被点击的日期
     */
    void onClickCurrentWeek(DateTime dateTime);

}

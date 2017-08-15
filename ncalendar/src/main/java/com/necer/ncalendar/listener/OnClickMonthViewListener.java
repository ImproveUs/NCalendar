package com.necer.ncalendar.listener;

import org.joda.time.DateTime;

/**
 * Created by necer on 2017/6/13.
 * 月视图点击事件
 */

public interface OnClickMonthViewListener {

    void onClickCurrentMonth(DateTime dateTime);

    void onClickLastMonth(DateTime dateTime);

    void onClickNextMonth(DateTime dateTime);

}

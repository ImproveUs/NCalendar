package com.necer.ncalendar.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.necer.ncalendar.listener.OnClickWeekViewListener;
import com.necer.ncalendar.view.WeekView;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by necer on 2017/6/13.
 * 周视图适配器
 */
public class WeekCalendarAdapter extends CalendarAdapter {

    private OnClickWeekViewListener mOnClickWeekViewListener;

    public WeekCalendarAdapter(Context context, int count, int curr, DateTime dateTime, OnClickWeekViewListener onClickWeekViewListener, List<String> pointList) {
        super(context, count, curr, dateTime, pointList);
        this.mOnClickWeekViewListener = onClickWeekViewListener;
    }

    /**
     * 这个适配器只是复写了初始化视图的方法
     *
     * @param container
     * @param position
     * @return
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //这个....直接自己用集合维护显示过的视图,这里有个问题,如果用集合维护会导致显示过得视图不被销毁掉的
        WeekView weekView = (WeekView) mCalendarViews.get(position);
        if (weekView == null) {
            weekView = new WeekView(mContext, mDateTime.plusDays((position - mCurr) * 7), mOnClickWeekViewListener, mPointList);
            mCalendarViews.put(position, weekView);
        }
        container.addView(mCalendarViews.get(position));
        return mCalendarViews.get(position);
    }

}

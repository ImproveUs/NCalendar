package com.necer.ncalendar.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;

import com.necer.ncalendar.adapter.CalendarAdapter;
import com.necer.ncalendar.adapter.WeekCalendarAdapter;
import com.necer.ncalendar.listener.OnClickWeekCalendarListener;
import com.necer.ncalendar.listener.OnClickWeekViewListener;
import com.necer.ncalendar.listener.OnWeekCalendarPageChangeListener;
import com.necer.ncalendar.utils.Utils;
import com.necer.ncalendar.view.CalendarView;
import com.necer.ncalendar.view.WeekView;

import org.joda.time.DateTime;
import org.joda.time.Weeks;

import java.util.List;

/**
 * Created by necer on 2017/6/13.
 * 周视图
 */
public class WeekCalendar extends CalendarViewPager implements OnClickWeekViewListener {

    private OnClickWeekCalendarListener onClickWeekCalendarListener;
    private OnWeekCalendarPageChangeListener onWeekCalendarPageChangeListener;

    public WeekCalendar(Context context) {
        this(context, null);
    }

    public WeekCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected CalendarAdapter getCalendarAdapter(List<String> pointList) {
        //由于需要显示的时间是这样:日,一,二,三,四,五,六;显示的时间就得从周日开始
        //1:获取幻灯片左右边界时间,当前显示时间的第一天日期
        DateTime startSunFirstDayOfWeek = Utils.getSunFirstDayOfWeek(startDateTime);
        DateTime endSunFirstDayOfWeek = Utils.getSunFirstDayOfWeek(endDateTime);
        DateTime todaySunFirstDayOfWeek = Utils.getSunFirstDayOfWeek(DateTime.now());
        //2:计算幻灯片总共可以显示的页数
        mPageSize = Weeks.weeksBetween(startSunFirstDayOfWeek, endSunFirstDayOfWeek).getWeeks() + 1;
        //3:计算幻灯片当前显示的页数
        mCurrPage = Weeks.weeksBetween(startSunFirstDayOfWeek, todaySunFirstDayOfWeek).getWeeks();
        //4:创建周视图的适配器
        return new WeekCalendarAdapter(getContext(), mPageSize, mCurrPage, new DateTime(), this, pointList);
    }

    @Override
    protected void initCurrentCalendarView() {
        //每当显示的页面变动时,比如初始化显示,页面切换都回调监听
        currentView = calendarAdapter.getCalendarViews().get(getCurrentItem());
        if (onWeekCalendarPageChangeListener != null && currentView != null) {
            DateTime selectDateTime = currentView.getSelectDateTime();
            DateTime initialDateTime = currentView.getInitialDateTime();
            onWeekCalendarPageChangeListener.onWeekCalendarPageSelected(selectDateTime == null ? initialDateTime : selectDateTime);
        }
    }

    @Override
    public void setDate(int year, int month, int day, boolean smoothScroll) {
        DateTime dateTime = new DateTime(year, month, day, 0, 0, 0);
        int i = jumpDate(dateTime, smoothScroll);
        WeekView weekView = (WeekView) calendarAdapter.getCalendarViews().get(i);
        if (weekView == null) {
            return;
        }
        weekView.setSelectDateTime(dateTime);
    }

    @Override
    public int jumpDate(DateTime dateTime, boolean smoothScroll) {
        SparseArray<CalendarView> calendarViews = calendarAdapter.getCalendarViews();
        if (calendarViews.size() == 0) {
            return getCurrentItem();
        }
        //这里有意思哈,获取的是创建weekview的原始时间,也有道理.
        DateTime initialDateTime = calendarViews.get(getCurrentItem()).getInitialDateTime();
        int weeks = Utils.getIntervalWeek(initialDateTime, dateTime);
        int i = getCurrentItem() + weeks;
        setCurrentItem(i, smoothScroll);
        return i;
    }

    /**
     * 设置日期点击监听
     * @param onClickWeekCalendarListener
     */
    public void setOnClickWeekCalendarListener(OnClickWeekCalendarListener onClickWeekCalendarListener) {
        this.onClickWeekCalendarListener = onClickWeekCalendarListener;
    }

    /**
     * 设置页面切换监听
     * @param onWeekCalendarPageChangeListener
     */
    public void setOnWeekCalendarPageChangeListener(OnWeekCalendarPageChangeListener onWeekCalendarPageChangeListener) {
        this.onWeekCalendarPageChangeListener = onWeekCalendarPageChangeListener;
    }


    @Override
    public void onClickCurrentWeek(DateTime dateTime) {
        //这里当视图被点击后,回调出来再设置选中时间 有点乱哈
        WeekView weekView = (WeekView) calendarAdapter.getCalendarViews().get(getCurrentItem());
        weekView.setSelectDateTime(dateTime);
        //清除其他选中  作者考虑很周全哈
        if (!isMultiple) {
            clearSelect(weekView);
        }
        //继续向上层回调被点击的时间
        if (onClickWeekCalendarListener != null) {
            onClickWeekCalendarListener.onClickWeekCalendar(dateTime);
        }
    }

}

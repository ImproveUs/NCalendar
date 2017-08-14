package com.necer.ncalendar.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.necer.ncalendar.R;
import com.necer.ncalendar.adapter.CalendarAdapter;
import com.necer.ncalendar.utils.Attrs;
import com.necer.ncalendar.utils.MyLog;
import com.necer.ncalendar.utils.Utils;
import com.necer.ncalendar.view.CalendarView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by necer on 2017/6/13.
 * 抽象父类  这里继承的是ViewPager便于左右滑动视图
 */

public abstract class CalendarViewPager extends ViewPager {

    private static final String TAG = "CalendarViewPager";

    protected CalendarAdapter calendarAdapter;
    protected CalendarView currentView;
    protected DateTime startDateTime;
    protected DateTime endDateTime;
    protected int mPageSize;
    protected int mCurrPage;
    protected List<String> mPointList;

    protected boolean isMultiple;//是否多选，多选是指周与周，月与月之间

    public CalendarViewPager(Context context) {
        this(context, null);
    }

    /**
     * 构造方法初始化自定义属性
     *
     * @param context
     * @param attrs
     */
    public CalendarViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.NCalendar);
        //1:有意思 这里用一个类来封装所需要的属性  蛮科学的
        Attrs.solarTextColor = ta.getColor(R.styleable.NCalendar_solarTextColor, getResources().getColor(R.color.solarTextColor));
        Attrs.lunarTextColor = ta.getColor(R.styleable.NCalendar_lunarTextColor, getResources().getColor(R.color.lunarTextColor));
        Attrs.selectCircleColor = ta.getColor(R.styleable.NCalendar_selectCircleColor, getResources().getColor(R.color.selectCircleColor));
        Attrs.hintColor = ta.getColor(R.styleable.NCalendar_hintColor, getResources().getColor(R.color.hintColor));
        Attrs.solarTextSize = ta.getDimension(R.styleable.NCalendar_solarTextSize, Utils.sp2px(context, 14));
        Attrs.lunarTextSize = ta.getDimension(R.styleable.NCalendar_lunarTextSize, Utils.sp2px(context, 8));
        Attrs.selectCircleRadius = ta.getInt(R.styleable.NCalendar_selectCircleRadius, (int) Utils.dp2px(context, 20));
        Attrs.isShowLunar = ta.getBoolean(R.styleable.NCalendar_isShowLunar, true);

        Attrs.pointSize = ta.getDimension(R.styleable.NCalendar_pointSize, (int) Utils.dp2px(context, 2));
        Attrs.pointColor = ta.getColor(R.styleable.NCalendar_pointcolor, getResources().getColor(R.color.selectCircleColor));
        Attrs.hollowCircleColor = ta.getColor(R.styleable.NCalendar_hollowCircleColor, Color.WHITE);
        Attrs.hollowCircleStroke = ta.getInt(R.styleable.NCalendar_hollowCircleStroke, (int) Utils.dp2px(context, 1));

        isMultiple = ta.getBoolean(R.styleable.NCalendar_isMultiple, true);

        String startString = ta.getString(R.styleable.NCalendar_startDateTime);
        String endString = ta.getString(R.styleable.NCalendar_endDateTime);
        ta.recycle();
        //2:初始化一个集合
        mPointList = new ArrayList<>();
        //3:设置开始日期 这里说下至于joda-time  这里当做已会用了
        startDateTime = new DateTime(startString == null ? "1901-01-01" : startString);
        //4:设置结束日期
        endDateTime = new DateTime(endString == null ? "2099-12-31" : endString);
        Log.i(TAG, "CalendarViewPager: startDateTime=" + startDateTime.toString() + "----endDateTime=" + endDateTime.toString());
        //5:构造适配器并设置一些基本属性 *注入的集合为空
        calendarAdapter = getCalendarAdapter(mPointList);
        setAdapter(calendarAdapter);
        setCurrentItem(mCurrPage);
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                initCurrentCalendarView();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        //6:这里添加一个监听,监听视图变化时的回调
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                initCurrentCalendarView();
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        //7:这里设置了viewpager背景,有必要吗?
        setBackgroundColor(getResources().getColor(android.R.color.white));
    }

    public void setDate(int year, int month, int day) {
        setDate(year, month, day, false);
    }

    /**
     * 视图初始化时调用子类实现的方法,获取日历适配器
     *
     * @param pointList 初始集合为空
     * @return
     */
    protected abstract CalendarAdapter getCalendarAdapter(List<String> pointList);

    /**
     * 初始化当前日历视图,视图创建或是布局参数变化时回调
     *
     * @see #CalendarViewPager(Context, AttributeSet)
     */
    protected abstract void initCurrentCalendarView();

    public abstract void setDate(int year, int month, int day, boolean smoothScroll);

    public abstract int jumpDate(DateTime dateTime, boolean smoothScroll);

    public DateTime getSelectDateTime() {
        if (currentView == null) {
            return null;
        }
        return currentView.getSelectDateTime();
    }

    public DateTime getInitialDateTime() {
        // MyLog.d("getInitialDateTime:::::" + currentView);
        if (currentView == null) {
            return null;
        }
        return currentView.getInitialDateTime();
    }


    public CalendarView getCurrentCalendarView() {
        return currentView;
    }

    public void setPointList(List<String> pointList) {
        //全部页面重绘

        mPointList.clear();
        mPointList.addAll(pointList);
        SparseArray<CalendarView> calendarViews = calendarAdapter.getCalendarViews();
        for (int i = 0; i < calendarViews.size(); i++) {
            int key = calendarViews.keyAt(i);
            calendarViews.get(key).invalidate();
        }
    }

    protected void clearSelect(CalendarView currentCalendarView) {
        SparseArray<CalendarView> monthViews = calendarAdapter.getCalendarViews();
        for (int i = 0; i < monthViews.size(); i++) {
            int key = monthViews.keyAt(i);
            CalendarView view = monthViews.get(key);
            if (view.hashCode() != currentCalendarView.hashCode()) {
                view.clear();
            }
        }
    }


    private boolean isScrollEnable = true;

    public void setScrollEnable(boolean isScrollEnable) {
        this.isScrollEnable = isScrollEnable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isScrollEnable ? super.onTouchEvent(ev) : false;

    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isScrollEnable ? super.onInterceptTouchEvent(ev) : false;
    }
}

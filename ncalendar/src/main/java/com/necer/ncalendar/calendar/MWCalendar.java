package com.necer.ncalendar.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

import com.necer.ncalendar.R;
import com.necer.ncalendar.listener.OnCalendarChangeListener;
import com.necer.ncalendar.listener.OnClickMonthCalendarListener;
import com.necer.ncalendar.listener.OnClickWeekCalendarListener;
import com.necer.ncalendar.listener.OnMonthCalendarPageChangeListener;
import com.necer.ncalendar.listener.OnWeekCalendarPageChangeListener;
import com.necer.ncalendar.utils.Utils;
import com.necer.ncalendar.view.MonthView;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by necer on 2017/6/14.
 * 切换视图  这里居然实现的是线性布局  也应该是的
 */

public class MWCalendar extends LinearLayout implements NestedScrollingParent//滑动控制接口
        , OnMonthCalendarPageChangeListener//月视图页面切换接口
        , OnClickMonthCalendarListener//月视图日期点击接口
        , OnClickWeekCalendarListener//周视图日期点击接口
        , OnWeekCalendarPageChangeListener {//周视图页面切换接口

    private static final String TAG = "MWCalendar";

    private WeekCalendar weekCalendar;
    private MonthCalendar monthCalendar;
    private View nestedScrollingChild;
    private OverScroller mScroller;

    public static final int OPEN = 100;
    public static final int CLOSE = 200;
    private static int STATE = 100;//默认开
    private int rowHeigh;
    private int duration;

    public MWCalendar(Context context) {
        this(context, null);
    }

    public MWCalendar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MWCalendar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //1:嘿嘿  这里直接设置为竖直方向
        setOrientation(LinearLayout.VERTICAL);
        //2:这里用的是OverScroller哈哈
        mScroller = new OverScroller(context);
        //3:这里创建月视图并设置属性
        monthCalendar = new MonthCalendar(context, attrs);
        addView(monthCalendar);
        //4:创建周视图并设置属性
        weekCalendar = new WeekCalendar(context, attrs);
        //5:这里设置特有属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.NCalendar);
        float dimension = ta.getDimension(R.styleable.NCalendar_calendarHeight, Utils.dp2px(context, 240));
        duration = ta.getInt(R.styleable.NCalendar_duration, 500);
        ta.recycle();

        rowHeigh = (int) (dimension / 6);
        monthCalendar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, rowHeigh * 6));
        weekCalendar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, rowHeigh));

        monthCalendar.setOnMonthCalendarPageChangeListener(this);
        monthCalendar.setOnClickMonthCalendarListener(this);
        weekCalendar.setOnClickWeekCalendarListener(this);
        weekCalendar.setOnWeekCalendarPageChangeListener(this);
        //6:这里有意思,先获取视图的父布局,将周视图添加到父布局中   也就是周视图跟MWCalendar处于同一布局中  666
        post(new Runnable() {
            @Override
            public void run() {
                ViewParent parent = getParent();
                if (!(parent instanceof RelativeLayout)) {
                    throw new RuntimeException("MWCalendar的父view必须是RelativeLayout");
                }
                ((RelativeLayout) parent).addView(weekCalendar);
            }
        });
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        //这里的nestedScrollAxes始终为2   不太懂
        return true;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        //axes也始终为2
        // super.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        // super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        //这个方法就没回调过
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        //  return super.onNestedFling(target, velocityX, velocityY, consumed);
        //嘿  这个方法也不回调
        return false;
    }

    @Override
    public void onStopNestedScroll(View target) {
        //停止滑动，恢复日历的滑动 手指触摸滑动结束回调
//        Log.i(TAG, "onStopNestedScroll: ");
        //1:滑动结束后就将日历视图设置为可左右滑动
        weekCalendar.setScrollEnable(true);
        monthCalendar.setScrollEnable(true);
        //2:获取滑动的距离
        int scrollY = getScrollY();
        if (scrollY == 0 || scrollY == rowHeigh * 5) {
            return;
        }
        //3:处理开启状态的滚动逻辑
        if (STATE == OPEN) {
            //如果当前显示的是月视图,向上滑动距离超过100px,就滑动到顶部,否则回弹
            if (scrollY > 100) {
                startScroll(scrollY, rowHeigh * 5 - scrollY, duration * (rowHeigh * 5 - scrollY) / (rowHeigh * 5));
            } else {
                startScroll(scrollY, -scrollY, duration * scrollY / (rowHeigh * 5));
            }
        }
        //如果当前显示的是周视图,就不用解释了
        if (STATE == CLOSE) {
            if (scrollY < rowHeigh * 5 - 100) {
                startScroll(scrollY, -scrollY, duration * scrollY / (rowHeigh * 5));
            } else {
                startScroll(scrollY, rowHeigh * 5 - scrollY, duration * (rowHeigh * 5 - scrollY) / (rowHeigh * 5));
            }
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        //这个有意思了哈   滑动坐标系是反的  那么dy>0表示上滑
//        Log.i(TAG, "onNestedPreScroll: " + dy + "----" + consumed[1]);
        //滑动之前的回调
        weekCalendar.setScrollEnable(false);
        monthCalendar.setScrollEnable(false);
        //向上滑  并且滑动的距离小于整个高度表示需要隐藏月视图
        boolean hiddenMonthCalendar = dy > 0 && getScrollY() < rowHeigh * 5;
        //向下滑,滑动的距离大于0,顶部是否可以滚动(如果不判断是否可以滚动,那么视图就跟着recyclerview跑了)
        boolean showMonthCalendar = dy < 0 && getScrollY() >= 0 && !ViewCompat.canScrollVertically(target, -1);
        if (hiddenMonthCalendar || showMonthCalendar) {
            //只要有一个满足要求就自己消费事件 
            scrollBy(0, dy);
            consumed[1] = dy;
        }
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
//        Log.i(TAG, "onNestedPreFling: velocityX=" + velocityX + "------velocityY=" + velocityY);
        //手指滑动结束后回调,回调滑动的速度
        if (getScrollY() >= rowHeigh * 5) return false;
        fling((int) velocityY);
        return true;
    }

    @Override
    public int getNestedScrollAxes() {
        return 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //这里测量时为啥要这样子咧   没意义额  不管
        ViewGroup.LayoutParams layoutParams = nestedScrollingChild.getLayoutParams();
        layoutParams.height = getMeasuredHeight() - rowHeigh;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        Log.i(TAG, "onFinishInflate: ");
        //视图加载完成后 这里获取第一个位置的子视图recyclerview
        nestedScrollingChild = getChildAt(1);
        if (!(nestedScrollingChild instanceof NestedScrollingChild)) {
            throw new RuntimeException("子view必须实现NestedScrollingChild");
        }
        //我擦   通篇看下来   就起到一个判断是否是NestedScrollingChild的作用
    }

    private void startScroll(int startY, int dy, int duration) {
        mScroller.startScroll(0, startY, 0, dy, duration);
        invalidate();
    }

    public void fling(int velocityY) {
        mScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, rowHeigh * 5);
        invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > rowHeigh * 5) {
            y = rowHeigh * 5;
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);
        }
    }

    @Override
    public void computeScroll() {
        int scrollY = getScrollY();
        if (scrollY == 0) {
            STATE = OPEN;
            weekCalendar.setVisibility(INVISIBLE);
        } else if (scrollY == 5 * rowHeigh) {
            STATE = CLOSE;
            weekCalendar.setVisibility(VISIBLE);
        } else {
            DateTime selectDateTime = weekCalendar.getSelectDateTime();
            DateTime initialDateTime = weekCalendar.getInitialDateTime();
            DateTime dateTime = selectDateTime == null ? initialDateTime : selectDateTime;

            MonthView currentCalendarView = (MonthView) monthCalendar.getCurrentCalendarView();
            int weekRow = currentCalendarView.getWeekRow(dateTime);
            weekCalendar.setVisibility(scrollY >= weekRow * rowHeigh ? VISIBLE : INVISIBLE);
        }

        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }

    @Override
    public void onMonthCalendarPageSelected(DateTime dateTime) {
        if (STATE == OPEN) {

            DateTime selectDateTime = monthCalendar.getSelectDateTime();
            if (selectDateTime == null) {
                weekCalendar.jumpDate(dateTime, true);
            } else {
                weekCalendar.setDate(selectDateTime.getYear(), selectDateTime.getMonthOfYear(), selectDateTime.getDayOfMonth());
            }

            if (onClickCalendarListener != null) {
                onClickCalendarListener.onCalendarPageChanged(dateTime);
            }
        }
    }

    @Override
    public void onClickMonthCalendar(DateTime dateTime) {
        weekCalendar.setDate(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
        if (onClickCalendarListener != null) {
            onClickCalendarListener.onClickCalendar(dateTime);
        }
    }

    @Override
    public void onClickWeekCalendar(DateTime dateTime) {
        monthCalendar.setDate(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
        if (onClickCalendarListener != null) {
            onClickCalendarListener.onClickCalendar(dateTime);
        }
    }


    @Override
    public void onWeekCalendarPageSelected(DateTime dateTime) {
        if (STATE == CLOSE) {
            DateTime selectDateTime = weekCalendar.getSelectDateTime();
            if (selectDateTime == null) {
                monthCalendar.jumpDate(dateTime, true);
            } else {
                monthCalendar.setDate(selectDateTime.getYear(), selectDateTime.getMonthOfYear(), selectDateTime.getDayOfMonth());
            }

            if (onClickCalendarListener != null) {
                onClickCalendarListener.onCalendarPageChanged(dateTime);
            }
        }
    }


    public void setDate(int year, int month, int day) {
        monthCalendar.setDate(year, month, day);
        weekCalendar.setDate(year, month, day);
    }

    public void setPointList(List<String> pointList) {
        monthCalendar.setPointList(pointList);
        weekCalendar.setPointList(pointList);
    }


    private OnCalendarChangeListener onClickCalendarListener;

    public void setOnClickCalendarListener(OnCalendarChangeListener onClickCalendarListener) {
        this.onClickCalendarListener = onClickCalendarListener;
    }

    public void open() {
        if (STATE == CLOSE) {
            startScroll(rowHeigh * 5, -rowHeigh * 5, duration);
        }
    }

    public void close() {
        if (STATE == OPEN) {
            startScroll(0, rowHeigh * 5, duration);
        }
    }
}

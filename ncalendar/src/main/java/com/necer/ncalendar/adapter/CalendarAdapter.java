package com.necer.ncalendar.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.necer.ncalendar.view.CalendarView;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by necer on 2017/6/13.
 * 适配器抽象类继承的PagerAdapter
 */

public abstract class CalendarAdapter extends PagerAdapter {

    protected Context mContext;
    protected int mCount;//总页数
    protected int mCurr;//当前位置
    protected SparseArray<CalendarView> mCalendarViews;
    protected DateTime mDateTime;
    protected List<String> mPointList;

    /**
     * 构造方法
     * @param context       上下文
     * @param count         总共可以显示的页数
     * @param curr          初始化时当前显示页数
     * @param dateTime      当前时间
     * @param pointList
     */
    public CalendarAdapter(Context context, int count, int curr, DateTime dateTime, List<String> pointList) {
        this.mDateTime = dateTime;
        this.mContext = context;
        this.mCount = count;
        this.mCurr = curr;
        this.mPointList = pointList;
        //这个高性能哈,这个集合居然是用来填充视图的,默认键是整型,只需要声明值得类型就可以替代hashmap
        mCalendarViews = new SparseArray<>();
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
        container.removeView((View) object);
    }

    /**
     * 返回视图集合
     * @return
     */
    public SparseArray<CalendarView> getCalendarViews() {
        return mCalendarViews;
    }

}

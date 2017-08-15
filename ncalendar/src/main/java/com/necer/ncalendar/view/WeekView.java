package com.necer.ncalendar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.necer.ncalendar.listener.OnClickWeekViewListener;
import com.necer.ncalendar.utils.Utils;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by necer on 2017/6/13.
 * 周视图,终于遇到本尊了
 */
public class WeekView extends CalendarView {

    private List<DateTime> weekDateTimeList;
    private List<String> lunarList;

    private OnClickWeekViewListener onClickWeekViewListener;

    /**
     * 周视图的构造
     *
     * @param context                 上下文
     * @param dateTime                根据当前显示的页数计算出的时间
     * @param onClickWeekViewListener
     * @param pointList
     */
    public WeekView(Context context, DateTime dateTime, OnClickWeekViewListener onClickWeekViewListener, List<String> pointList) {
        super(context, pointList);
        this.onClickWeekViewListener = onClickWeekViewListener;
        this.mInitialDateTime = dateTime;
        //初始化数据集 并设置公历和农历
        Utils.NCalendar monthCalendar = Utils.getWeekCalendar(dateTime);
        lunarList = monthCalendar.lunarList;
        weekDateTimeList = monthCalendar.dateTimeList;
    }

    /**
     * 用于父类将自定属性,画笔说明的都初始化好了  这里子类就只需要绘制就行了
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //1:获取空间的额宽和高
        mWidth = getWidth();
        mHeight = getHeight();
        //2:用于存储每一个绘制单元的矩形数据
        mRectList.clear();
        //3:这个不难,一个for循环
        for (int i = 0; i < 7; i++) {
            //这里为便于理解,假设i=3即星期二
            //4:设置绘制单元的矩形参数并添加到集合中
            Rect rect = new Rect(i * mWidth / 7, 0, i * mWidth / 7 + mWidth / 7, mHeight);
            mRectList.add(rect);
            //5:获取公历时间 并计算文字的基准线
            DateTime dateTime = weekDateTimeList.get(i);
            Paint.FontMetricsInt fontMetrics = mSorlarPaint.getFontMetricsInt();
            int baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2;
            if (Utils.isToday(dateTime)) {
                //6;如果是今天
                mSorlarPaint.setColor(mSelectCircleColor);
                int radius = Math.min(Math.min(rect.width() / 2, rect.height() / 2), mSelectCircleRadius);
                canvas.drawCircle(rect.centerX(), rect.centerY(), radius, mSorlarPaint);
                mSorlarPaint.setColor(Color.WHITE);
                canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
            } else if (mSelectDateTime != null && dateTime.toLocalDate().equals(mSelectDateTime.toLocalDate())) {
                //7:如果选中的是今天
                mSorlarPaint.setColor(mSelectCircleColor);
                int radius = Math.min(Math.min(rect.width() / 2, rect.height() / 2), mSelectCircleRadius);
                canvas.drawCircle(rect.centerX(), rect.centerY(), radius, mSorlarPaint);
                mSorlarPaint.setColor(mHollowCircleColor);
                canvas.drawCircle(rect.centerX(), rect.centerY(), radius - mHollowCircleStroke, mSorlarPaint);
                mSorlarPaint.setColor(mSolarTextColor);
                canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
            } else {
                //8:设置普通视图,自定义属性控制是否显示农历
                mSorlarPaint.setColor(mSolarTextColor);
                canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
                if (isShowLunar) {
                    String lunar = lunarList.get(i);
                    canvas.drawText(lunar, rect.centerX(), rect.bottom - Utils.dp2px(getContext(), 5), mLunarPaint);
                }
            }
            //9:绘制圆点,暂时没看出来效果
            if (mPointList.contains(dateTime.toLocalDate().toString())) {
                mSorlarPaint.setColor(mPointColor);
                canvas.drawCircle(rect.centerX(), rect.bottom - mPointSize, mPointSize, mSorlarPaint);
            }
        }
    }

    /**
     * 这里吧onTouchEvent事件的消费交给了GestureDetector处理
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 这里手势处理点击事件   就不解释了  至于为什么要拦截down   先不管了
     */
    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            for (int i = 0; i < mRectList.size(); i++) {
                Rect rect = mRectList.get(i);
                if (rect.contains((int) e.getX(), (int) e.getY())) {
                    DateTime selectDateTime = weekDateTimeList.get(i);
                    onClickWeekViewListener.onClickCurrentWeek(selectDateTime);
                    break;
                }
            }
            return true;
        }
    });

}

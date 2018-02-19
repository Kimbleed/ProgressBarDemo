package com.example.a49479.progressbardemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by Awesome on 2017/5/18.
 */

public class MyProgressBarView extends View {

    //距离左边框
    private int marginLeftRight = 60;
    //进度条高
    private int mProgressHeight = 160;

    //进度百分比
    private float percentage = 0;
    private int widthByPercent = marginLeftRight;

    //屏幕宽高
    private int hScreen, wScreen;

    //sin曲线部分
    int angle = 0;
    boolean isRun = false;      // true sin曲线运动  false sin曲线不动

    //控制sin曲线
    public SinRunnable runnable;
    private HandlerThread thread;//后台线程
    public Handler handler;     //属于HandlerThread  的Handler

    //唯一一支笔
    private Paint mPaint;

    //进度条状态值
    public static final int PROGRESSBAR_STATE_UN_START = 100;

    public static final int PROGRESSBAR_STATE_DOWNLOADING = 101;

    public static final int PROGRESSBAR_STATE_DONE = 103;

    //进度条当前状态
    private int state = PROGRESSBAR_STATE_UN_START;


    public static int DEFAULT_DONE_PROGRESS_BG_COLOR = 0xFFbdbdbd;
    public static int DEFAULT_DOWNLOADING_PROGRESS_BG_COLOR = 0xFFFF8C00;
    public static int DEFAULT_VIEW_BG_COLOR = 0xFFffffff;

    private int mProgressBgColor= DEFAULT_DOWNLOADING_PROGRESS_BG_COLOR;
    private int mViewBgColor= DEFAULT_VIEW_BG_COLOR;

    public MyProgressBarView(Context context) {
        super(context);
        initPaint();

    }

    public MyProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public MyProgressBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    public int getMarginLeftRight() {
        return marginLeftRight;
    }

    public void setMarginLeftRight(int marginLeftRight) {
        this.marginLeftRight = marginLeftRight;
    }

    public int getmProgressHeight() {
        return mProgressHeight;
    }

    public void setmProgressHeight(int mProgressHeight) {
        this.mProgressHeight = mProgressHeight;
    }

    public float getPercentage() {
        return percentage;
    }


    //设置百分比
    public void setPercentage(float percentage) {
            if (percentage <= 100 && percentage >= 0) {
                this.percentage = percentage;
                widthByPercent = (int)((wScreen - marginLeftRight * 2) * percentage / 100.0) + marginLeftRight;
                invalidate();
            }
    }

    public int getState() {
        return state;
    }

    //设置进度条样式  3种
    //UN_START
    //DONWLOADING
    //DONE
    public void setState(int state) {
        this.state = state;
    }

    public int getWidthByPercent() {
        return widthByPercent;
    }

    public void setWidthByPercent(int widthByPercent) {
        this.widthByPercent = widthByPercent;
    }

    public int gethScreen() {
        return hScreen;
    }

    public void sethScreen(int hScreen) {
        this.hScreen = hScreen;
    }

    public int getwScreen() {
        return wScreen;
    }

    public void setwScreen(int wScreen) {
        this.wScreen = wScreen;
    }

    public Paint getmPaint() {
        return mPaint;
    }

    public void setmPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        runnable = new SinRunnable();
        runnable.start();
    }

    public void stop(){
        if(runnable!=null)
            runnable.stop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        wScreen = getWidth();
        hScreen = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        wScreen = getWidth();
        hScreen = getHeight();
        mProgressHeight = hScreen *65/100;

        if (state == PROGRESSBAR_STATE_UN_START) {
            mPaint.setXfermode(null);
            canvas.save();
            canvas.drawARGB(255, 255, 255, 255);
            mPaint.setColor(DEFAULT_DOWNLOADING_PROGRESS_BG_COLOR);
            drawProgressBg(canvas, mPaint);

            mPaint.setColor(DEFAULT_VIEW_BG_COLOR);
            drawProgressBg2(canvas, mPaint);

            mPaint.setColor(DEFAULT_DOWNLOADING_PROGRESS_BG_COLOR);
            drawText(canvas, mPaint, "下载");
            canvas.restore();
        } else if (state == PROGRESSBAR_STATE_DOWNLOADING) {
            canvas.drawARGB(255, 255, 255, 255);
            //先绘制的是 进度条底色
            //在percentage<5 && >95 的时候 背景色在动画图层中
            // 利用Xfermode SCREEN 将波浪动画 在背景色上的 过滤掉
            if (percentage >= 5 && percentage <= 95) {
                mPaint.setColor(0xFFfafafa);
                drawProgressBg(canvas, mPaint);
            }
            //sc 我将其命名为动画图层
            //动画图层  独立于这一行代码以上的图层
            int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
            if (percentage < 5 || percentage > 95) {
                mPaint.setColor(0xFFfafafa);
                drawProgressBg(canvas, mPaint);
            }


            //然后绘制的是进度条百分比部分
            drawProgress(canvas, mPaint);
            drawSin(canvas, mPaint);

            //最后绘制 文字部分
            mPaint.setColor(DEFAULT_DOWNLOADING_PROGRESS_BG_COLOR);
            drawText(canvas, mPaint, "下载中 " + percentage + "%");
            //还原
            mPaint.setXfermode(null);
            canvas.restoreToCount(sc);
        } else if (state == PROGRESSBAR_STATE_DONE) {
            mPaint.setXfermode(null);
            canvas.save();
            mPaint.setColor(DEFAULT_DONE_PROGRESS_BG_COLOR);
            drawProgressBg(canvas, mPaint);

            mPaint.setColor(DEFAULT_VIEW_BG_COLOR);
            drawProgressBg2(canvas, mPaint);

            mPaint.setColor(DEFAULT_DONE_PROGRESS_BG_COLOR);
            drawText(canvas, mPaint, "已下载");
            canvas.restore();
        }
    }


    //进度条背景色  灰色
    private void drawProgressBg(Canvas canvas, Paint p) {
        RectF rectBg = new RectF(marginLeftRight, hScreen / 2 - mProgressHeight / 2, wScreen - marginLeftRight, hScreen / 2 + mProgressHeight / 2);
        canvas.drawRoundRect(rectBg, 10, 10, mPaint);
    }

    private void drawProgressBg2(Canvas canvas, Paint p) {
        RectF rectBg = new RectF(marginLeftRight + 2, hScreen / 2 - mProgressHeight / 2 + 2, wScreen - marginLeftRight - 2, hScreen / 2 + mProgressHeight / 2 - 2);
        canvas.drawRoundRect(rectBg, 10, 10, mPaint);
    }


    //画黄色进度条
    private void drawProgress(Canvas canvas, Paint p) {
        p.setColor(DEFAULT_DOWNLOADING_PROGRESS_BG_COLOR);
        RectF rectProgress = new RectF(marginLeftRight, hScreen / 2 - mProgressHeight / 2, widthByPercent, hScreen / 2 + mProgressHeight / 2);
        canvas.drawRoundRect(rectProgress, 10, 10, p);
    }

    //画文字
    private void drawText(Canvas canvas, Paint p, String msg) {
        if (percentage >= 5 && percentage<=100 && state == PROGRESSBAR_STATE_DOWNLOADING) {
            //设置xfermode
            //不设置>=5的 判定的话，  字体颜色为白色 因为将 底色过滤掉了 漏出最顶层的白色
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        }

        p.setTextSize(50);

        float value = p.measureText(msg);

        canvas.drawText(msg, wScreen /2 -value/2, hScreen / 2 + 12, p);

        //下载中 字符串  的定位 只能尽量居中
//        if(state == PROGRESSBAR_STATE_DOWNLOADING)
//            canvas.drawText(msg, wScreen * 49 / 128, hScreen / 2 + 12, p);
//        else
//            canvas.drawText(msg, wScreen /2 -50, hScreen / 2 + 12, p);
    }

    //画sin函数
    public void drawSin(Canvas canvas, Paint p) {
        p.setColor(DEFAULT_DOWNLOADING_PROGRESS_BG_COLOR);
        if (percentage < 5 || percentage > 95)
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        int height = hScreen / 2 + mProgressHeight / 2;
        double lineY = 0;
        double lineX2 = 0;
        for (int i = hScreen / 2 - mProgressHeight / 2; i < height; i++) {
            lineY = i;
            if (isRun) {
                lineX2 = 10 * Math.sin((i + angle) * Math.PI / 180) + 20;
            } else {
                lineX2 = 20;
            }
            double lineStart = lineX2 + widthByPercent - 5;
            double lineEnd = widthByPercent - 20;

            canvas.drawLine((int) lineStart, (int) lineY,
                    (int) lineEnd, (int) lineY + 1, p);
        }
    }

    //让sin曲线动起来
    class SinRunnable implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (isRun) {
                angle++;
                if (angle == 360) {
                    angle = 0;
                }
                try {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyProgressBarView.this.invalidate();
                        }
                    });
                    Thread.sleep(7);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        public void start() {
            isRun = true;
            if (thread == null) {
                thread = new HandlerThread("MyHandlerThread");
                thread.start();
            }
            Log.i("thread", (thread == null) + "");
            if (handler == null) {
                handler = new Handler(thread.getLooper());
                handler.post(runnable);
            }
        }

        public void stop() {
            isRun = false;
            angle = 0;
            if (handler != null)
                handler.removeCallbacksAndMessages(null);
        }
    }

}

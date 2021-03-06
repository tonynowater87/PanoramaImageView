package com.gjiazhe.panoramaimageview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by gjz on 19/12/2016.
 */

public class PanoramaImageView extends ImageView {

    public static final String TAG = PanoramaImageView.class.getSimpleName();

    // Image's scroll orientation
    public final static byte ORIENTATION_NONE = -1;
    public final static byte ORIENTATION_HORIZONTAL = 0;
    public final static byte ORIENTATION_VERTICAL = 1;
    private byte mOrientation = ORIENTATION_NONE;

    // Enable panorama effect or not
    private boolean mEnablePanoramaMode;

    // If true, the image scroll left(top) when the device clockwise rotate along y-axis(x-axis).
    private boolean mInvertScrollDirection;

    // Image's width and height
    private int mDrawableWidth;
    private int mDrawableHeight;

    // View's width and height
    private int mWidth;
    private int mHeight;

    // Image's offset from initial state(center in the view).
    private float mMaxOffset;

    // The scroll progress.
    private float mProgress;

    // Show scroll bar or not
    private boolean mEnableScrollbar;

    // The paint to draw scrollbar
    private Paint mScrollbarPaint;

    // Observe scroll state
    private OnPanoramaScrollListener mOnPanoramaScrollListener;

    private static final float MOVE_UNIT = 350f;//if value is bigger then move slower

    private GestureDetector mGestureDetector;

    private boolean mIsGyroscopeEnable = false;

    public PanoramaImageView(Context context) {
        this(context, null);
    }

    public PanoramaImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanoramaImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setScaleType(ScaleType.CENTER_CROP);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PanoramaImageView);
        mEnablePanoramaMode = typedArray.getBoolean(R.styleable.PanoramaImageView_piv_enablePanoramaMode, true);
        mInvertScrollDirection = typedArray.getBoolean(R.styleable.PanoramaImageView_piv_invertScrollDirection, false);
        mEnableScrollbar = typedArray.getBoolean(R.styleable.PanoramaImageView_piv_show_scrollbar, true);
        typedArray.recycle();

        if (mEnableScrollbar) {
            initScrollbarPaint();
        }

        mGestureDetector = new GestureDetector(context, new MyGestureListener());
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if (mOrientation == ORIENTATION_HORIZONTAL) {
                mProgress += distanceX / MOVE_UNIT;
            } else if (mOrientation == ORIENTATION_VERTICAL) {
                mProgress += distanceY / MOVE_UNIT;
            } else {
                return false;
            }

            //Log.d(TAG, String.format("onScroll, e1:%f, e2:%f, distanceX:%f, mProgress:%f", e1.getX(), e2.getX(), distanceX, mProgress));

            if (mProgress < -1) mProgress = -1f;
            if (mProgress > 1) mProgress = 1f;

            updateProgress(mProgress);

            return true;
        }
    }

    private void initScrollbarPaint() {
        mScrollbarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScrollbarPaint.setColor(Color.WHITE);
        mScrollbarPaint.setStrokeWidth(dp2px(1.5f));
    }
    public void setGyroscopeObserver(GyroscopeObserver observer) {
        mIsGyroscopeEnable = observer != null;
        if (observer != null) {
            observer.addPanoramaImageView(this);
        }
    }

    void updateProgress(float progress) {
        //Log.d(TAG, "updateProgress:" + progress);//1 ~ 0 ~ -1

        if (mEnablePanoramaMode) {
            mProgress = mInvertScrollDirection? -progress : progress;
            invalidate();
            if (mOnPanoramaScrollListener != null) {
                mOnPanoramaScrollListener.onScrolled(this, -mProgress);
            }
        }
    }

    private void updateProgressPostInvalidate(float progress) {
        if (mEnablePanoramaMode) {
            mProgress = mInvertScrollDirection? -progress : progress;
            postInvalidate();
            if (mOnPanoramaScrollListener != null) {
                mOnPanoramaScrollListener.onScrolled(this, -mProgress);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        mHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        if (getDrawable() != null) {
            mDrawableWidth = getDrawable().getIntrinsicWidth();
            mDrawableHeight = getDrawable().getIntrinsicHeight();

            if (mDrawableWidth * mHeight > mDrawableHeight * mWidth) {
                mOrientation = ORIENTATION_HORIZONTAL;
                float imgScale = (float) mHeight / (float) mDrawableHeight;
                mMaxOffset = Math.abs((mDrawableWidth * imgScale - mWidth) * 0.5f);
            } else if(mDrawableWidth * mHeight < mDrawableHeight * mWidth) {
                mOrientation = ORIENTATION_VERTICAL;
                float imgScale = (float) mWidth / (float) mDrawableWidth;
                mMaxOffset = Math.abs((mDrawableHeight * imgScale - mHeight) * 0.5f);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handler = false;
        if (!mIsGyroscopeEnable) {
            handler = mGestureDetector.onTouchEvent(event);
        }
        return handler || super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mEnablePanoramaMode || getDrawable() == null || isInEditMode()) {
            super.onDraw(canvas);
            return;
        }

        // Draw image
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            float currentOffsetX = mMaxOffset * mProgress;
            canvas.save();
            canvas.translate(currentOffsetX, 0);
            super.onDraw(canvas);
            canvas.restore();
        } else if (mOrientation == ORIENTATION_VERTICAL) {
            float currentOffsetY = mMaxOffset * mProgress;
            canvas.save();
            canvas.translate(0, currentOffsetY);
            super.onDraw(canvas);
            canvas.restore();
        }

        // Draw scrollbar
        if (mEnableScrollbar) {
            switch (mOrientation) {
                case ORIENTATION_HORIZONTAL: {
                    float barBgWidth = mWidth * 0.9f;
                    float barWidth = barBgWidth * mWidth / mDrawableWidth;

                    float barBgStartX = (mWidth - barBgWidth) / 2;
                    float barBgEndX = barBgStartX + barBgWidth;
                    float barStartX = barBgStartX + (barBgWidth - barWidth) / 2 * (1 - mProgress);
                    float barEndX = barStartX + barWidth;
                    float barY = mHeight * 0.95f;

                    mScrollbarPaint.setAlpha(100);
                    canvas.drawLine(barBgStartX, barY, barBgEndX, barY, mScrollbarPaint);
                    mScrollbarPaint.setAlpha(255);
                    canvas.drawLine(barStartX, barY, barEndX, barY, mScrollbarPaint);
                    break;
                }
                case ORIENTATION_VERTICAL: {
                    float barBgHeight = mHeight * 0.9f;
                    float barHeight = barBgHeight * mHeight / mDrawableHeight;

                    float barBgStartY = (mHeight - barBgHeight) / 2;
                    float barBgEndY = barBgStartY + barBgHeight;
                    float barStartY = barBgStartY + (barBgHeight - barHeight) / 2 * (1 - mProgress);
                    float barEndY = barStartY + barHeight;
                    float barX = mWidth * 0.95f;

                    mScrollbarPaint.setAlpha(100);
                    canvas.drawLine(barX, barBgStartY, barX, barBgEndY, mScrollbarPaint);
                    mScrollbarPaint.setAlpha(255);
                    canvas.drawLine(barX, barStartY, barX, barEndY, mScrollbarPaint);
                    break;
                }
            }
        }
    }

    private float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    public void setEnablePanoramaMode(boolean enable) {
        mEnablePanoramaMode = enable;
    }

    public boolean isPanoramaModeEnabled() {
        return mEnablePanoramaMode;
    }

    public void setInvertScrollDirection(boolean invert) {
        if (mInvertScrollDirection != invert) {
            mInvertScrollDirection = invert;
        }
    }

    public boolean isInvertScrollDirection() {
        return mInvertScrollDirection;
    }

    public void setEnableScrollbar(boolean enable) {
        if (mEnableScrollbar != enable){
            mEnableScrollbar = enable;
            if (mEnableScrollbar) {
                initScrollbarPaint();
            } else {
                mScrollbarPaint = null;
            }
        }
    }

    public boolean isScrollbarEnabled() {
        return mEnableScrollbar;
    }

    public byte getOrientation() {
        return mOrientation;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        /**
         * Do nothing because PanoramaImageView only
         * supports {@link scaleType.CENTER_CROP}
         */
    }

    /**
     * Interface definition for a callback to be invoked when the image is scrolling
     */
    public interface OnPanoramaScrollListener {
        /**
         * Call when the image is scrolling
         *
         * @param view the panoramaImageView shows the image
         *
         * @param offsetProgress value between (-1, 1) indicating the offset progress.
         *                 -1 means the image scrolls to show its left(top) bound,
         *                 1 means the image scrolls to show its right(bottom) bound.
         */
        void onScrolled(PanoramaImageView view, float offsetProgress);
    }

    public void setOnPanoramaScrollListener(OnPanoramaScrollListener listener) {
        mOnPanoramaScrollListener = listener;
    }

    private Handler mHandler;

    public void startAutoScrolling() {
        HandlerThread handlerThread = new HandlerThread("AutoScrollingThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                autoUpdateProgress();
                mHandler.sendEmptyMessageDelayed(0, 33);
            }
        };
        mHandler.sendEmptyMessage(0);
    }

    public void stopAutoScrolling() {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //Log.d("<DEBUG>", "stopAutoScrolling");
                    mHandler.removeMessages(0);
                    mHandler.getLooper().quit();
                    mHandler = null;
                }
            });
        }
    }

    private boolean mIsLeftSide = true;

    private void autoUpdateProgress() {
        float progressUnit = 0.01F;
        float MAX = 1F;
        float MIN = -1F;

        if (mIsLeftSide) {
            if (mProgress < MAX) {
                mProgress += progressUnit;
                if (mProgress > MAX) mProgress = MAX;
            } else {
                mIsLeftSide = false;
            }
        }

        if (!mIsLeftSide) {
            if (mProgress > MIN) {
                mProgress -= progressUnit;
                if (mProgress < MIN) mProgress = MIN;
            } else {
                mIsLeftSide = true;
            }
        }

        updateProgressPostInvalidate(mProgress);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoScrolling();
    }
}

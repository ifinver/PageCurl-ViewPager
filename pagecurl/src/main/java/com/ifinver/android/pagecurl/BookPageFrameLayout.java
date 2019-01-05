package com.ifinver.android.pagecurl;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by iFinVer on 2018/9/30 下午1:55.
 * ilzq@foxmail.com
 *
 * 默认不响应任何事件，需要外部设置{@link #setTouchEventAvailable(boolean)}为true
 * Will not answer any touch event, unless set {@link #setTouchEventAvailable(boolean)}
 */
public class BookPageFrameLayout extends FrameLayout {

    private static final int MAX_SHADOW_ALPHA = 180;// out of 255
    private static final int MAX_SHINE_ALPHA = 100;// out of 255

    private float mFlipAngle;
    private int mHalfWidth;
    private Rect mRightRect = new Rect(0, 0, 0, 0);
    private Rect mRightRectDynamic = new Rect(0, 0, 0, 0);
    private Rect mLeftRect = new Rect(0, 0, 0, 0);
    private Rect mLeftRectDynamic = new Rect(0, 0, 0, 0);
    private View mChildView = null;
    // used for transforming the canvas
    private Camera mCamera = new Camera();
    private Matrix mMatrix = new Matrix();
    // paints drawn above views when flipping
    private Paint mShadowPaint = new Paint();
    private Paint mShinePaint = new Paint();
    private float mFlipPosition;
    private boolean mTouchEventAvailable = false;

    public BookPageFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public BookPageFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BookPageFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setStyle(Paint.Style.FILL);
        mShinePaint.setColor(Color.WHITE);
        mShinePaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 左边不动，右边翻起来
     */
    public void flip(float position) {
        mFlipPosition = position;
        invalidate();
    }

    public void cancelFlip() {
        if (mFlipPosition != 0) {
            mFlipPosition = 0;
            invalidate();
        }
    }

    @Override
    public void invalidate() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.invalidate();
        } else {
            postInvalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mHalfWidth = getWidth() / 2;

        mLeftRect.top = 0;
        mLeftRect.left = 0;
        mLeftRect.right = mHalfWidth;
        mLeftRect.bottom = getHeight();

        mLeftRectDynamic.top = 0;
        mLeftRectDynamic.left = 0;
        mLeftRectDynamic.right = mHalfWidth;
        mLeftRectDynamic.bottom = getHeight();

        mRightRect.top = 0;
        mRightRect.left = mHalfWidth;
        mRightRect.right = getWidth();
        mRightRect.bottom = getHeight();

        mRightRectDynamic.top = 0;
        mRightRectDynamic.left = mHalfWidth;
        mRightRectDynamic.right = getWidth();
        mRightRectDynamic.bottom = getHeight();

        if (getChildCount() > 1) {
            throw new IllegalStateException("PageFrameLayout只能有一个孩子。如果想有多个，请添加一层ViewGroup");
        }
        mChildView = getChildAt(0);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (checkStatus()) {
//            if (mChildView != null) {
//                setDrawWithLayer(mChildView, false);
//            }
            super.dispatchDraw(canvas);
        } else {
//            //计时
//            long spend = SystemClock.elapsedRealtime();
            //时间戳
            final long drawingTime = getDrawingTime();
            //计算角度
            mFlipAngle = mFlipPosition * 180;
            //画正在翻起来的一边
            drawFlipSide(canvas, drawingTime);
            //画不动的一边
            drawBaseSide(canvas, drawingTime);
//            spend = SystemClock.elapsedRealtime() - spend;
//            LogHelper.d("BUG","draw spend : "+spend);//大部分0毫秒，偶尔1毫秒，开启硬件加速的情况下。
        }
    }

    private void drawFlipSide(Canvas canvas, long drawingTime) {
        if (mFlipAngle < -90 || mFlipAngle > 90) return;
        //压栈
        canvas.save();
        mCamera.save();
        //指定区域
        if (mFlipAngle < 0) {
            //画了左边，这里要旋转右边
            canvas.clipRect(mRightRect);
        } else {
            canvas.clipRect(mLeftRect);
        }
        //计算旋转矩阵
        mCamera.rotateY(mFlipAngle);
        mCamera.getMatrix(mMatrix);
        normalizeMatrix();
        //旋转canvas
        canvas.concat(mMatrix);
        //绘制内容
//        setDrawWithLayer(mChildView, true);
        drawChild(canvas, mChildView, drawingTime);
        //绘制白色遮罩，使之变亮
        final int alpha = (int) ((Math.abs(mFlipAngle) / 90f) * MAX_SHINE_ALPHA);
        mShinePaint.setAlpha(alpha);
        if (mFlipAngle < 0) {
            //画了左边，这里要旋转右边
            canvas.drawRect(mRightRect,mShinePaint);
        } else {
            canvas.drawRect(mLeftRect,mShinePaint);
        }
        //出栈
        mCamera.restore();
        canvas.restore();
    }

    private void drawBaseSide(Canvas canvas, long drawingTime) {
        canvas.save();
//        setDrawWithLayer(mChildView, true);

//        LogHelper.d("BUG", "hex:" + Integer.toHexString(System.identityHashCode(this)) + ",angle:" + mFlipAngle + ",position:" + mFlipPosition);

        if (mFlipAngle < -90) {
            //开始把左边的页面滑动至屏幕中央（夹角的绝对值小于90）,或者把右边的页面滑动至屏幕中央（夹角的绝对值大于90）：
            //本页面左边处于折叠状态，这时候，本页面只需要绘制最左边极小的一个小矩形，也不需要绘制正在翻转的侧页
            mLeftRectDynamic.right = (int) ((float) mHalfWidth * (1f - Math.cos(Math.toRadians(180f + mFlipAngle))));
            canvas.clipRect(mLeftRectDynamic);
        } else if (mFlipAngle >= -90 && mFlipAngle < 0) {
            //本页面已占主导地位，绘制出不动的左边一半。（此时已经绘制出正在翻转的右边一半的侧页了）
            canvas.clipRect(mLeftRect);
        } else if (mFlipAngle >= 0 && mFlipAngle <= 90) {//mFlipAngle==0 的情况是不存在的
            //本页面已占主导地位，绘制出不动的右边一半。（此时已经绘制出正在翻转的左边一半的侧页了）
            canvas.clipRect(mRightRect);
        } else {//mFlipAngle > 90
            //开始把右边的页面滑动至屏幕中央（夹角的绝对值小于90）,或者把左边的页面滑动至屏幕中央（夹角的绝对值大于90）：
            //本页面右边处于折叠状态，这时候，本页面只需要绘制最右边极小的一个小矩形，也不需要绘制正在翻转的侧页
            mRightRectDynamic.left = (int) ((float) mHalfWidth * (1f + Math.cos(Math.toRadians(180f - mFlipAngle))));
            canvas.clipRect(mRightRectDynamic);
        }

        drawChild(canvas, mChildView, drawingTime);

        //绘制阴影，使绘制的小矩形变暗
        float absAngle = Math.abs(mFlipAngle);
        if(absAngle > 90 && absAngle < 180){
            final int alpha = (int) (((absAngle - 90) / 90f) * MAX_SHADOW_ALPHA);
            mShadowPaint.setAlpha(alpha);
            canvas.drawPaint(mShadowPaint);
        }

        canvas.restore();
    }

    private void normalizeMatrix() {
        mMatrix.preScale(0.25f, 0.25f);
        mMatrix.postScale(4.0f, 4.0f);
        mMatrix.preTranslate((float) -getWidth() / 2, (float) -getHeight() / 2);
        mMatrix.postTranslate((float) getWidth() / 2, (float) getHeight() / 2);
    }

    private boolean checkStatus() {
        return mFlipPosition == 0.0f
                || mFlipPosition < -1
                || mFlipPosition > 1
                || mLeftRect.right == 0
                || mRightRect.right == 0
                || mChildView == null;
    }

//    /**
//     * Enable a hardware layer for the view.
//     */
//    private void setDrawWithLayer(View v, boolean drawWithLayer) {
//        if (isHardwareAccelerated()) {
//            if (v.getLayerType() != LAYER_TYPE_HARDWARE && drawWithLayer) {
//                v.setLayerType(LAYER_TYPE_HARDWARE, null);
//            } else if (v.getLayerType() != LAYER_TYPE_NONE && !drawWithLayer) {
//                v.setLayerType(LAYER_TYPE_NONE, null);
//            }
//        }
//    }

    public void setTouchEventAvailable(boolean available) {
        this.mTouchEventAvailable = available;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mTouchEventAvailable) {
            return super.dispatchTouchEvent(ev);
        }else{
            return false;//不拦截（即：不消费），也不进行子view的分发，本BookPageFrameLayout就被“过滤”了。
        }
    }
}

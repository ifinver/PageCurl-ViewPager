package com.ifinver.android.pagecurl;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by iFinVer on 2018/9/30 下午5:18.
 * ilzq@foxmail.com
 */

public class BookPageScroller extends Scroller {
    public BookPageScroller(Context context) {
        super(context);
    }

    public BookPageScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public BookPageScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    private int mScrollDuration = -1;//-1 means disabled

    /**
     * @param duration 毫秒
     */
    public void setScrollDuration(int duration){
        if(duration >= 0) {
            this.mScrollDuration = duration;
        }
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        if (mScrollDuration >= 0) {
            duration = mScrollDuration;
        }
        super.startScroll(startX, startY, dx, dy, duration);
    }
}

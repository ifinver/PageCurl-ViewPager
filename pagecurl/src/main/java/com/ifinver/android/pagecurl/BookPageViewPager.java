package com.ifinver.android.pagecurl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by iFinVer on 2018/9/30 下午5:13.
 * ilzq@foxmail.com
 *
 * 自定义滚动插值器，如先加速后减速
 * 优先级的页面状态监听，比如指定某个Listener优先被回调
 * 设置滚动阈值
 * 初始化时，会对监听器回调onPageSelected(0),系统默认的不会.
 * Notice : Wi
 */
public class BookPageViewPager extends ViewPager {
    //配置项
    private static final int mMinimumVelocity = -1;//触发翻页的最小加速度。设为-1时，只判断mFlingDistance
    private static final int mFlingDistance = 60;//滑动翻页阈值，单位：dp
    private Interpolator interpolator = new AccelerateDecelerateInterpolator();//插值器

    //成员变量
    private BookPageScroller mScroller;//自定义的支持先加速后减速的滑动器
    private ArrayList<OnPageChangeListener> mOnPageChangeListeners;//用于实现带优先级的页面状态监听
    private boolean isFirstSetCurrentItem = true;//如果是第一次设置监听器，就立即进行onPageSelected(0)回调。


    public BookPageViewPager(Context context) {
        super(context);
        init();
    }

    public BookPageViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        //设置插值器
        try {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            mScroller = new BookPageScroller(getContext(), interpolator);//先加速后减速
            mScroller.setScrollDuration(500);
            scroller.set(this, mScroller);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                throw new IllegalStateException("检查v4包，升级导致里面的变量名变了！需要更改这里的变量名");
            }
        }

        /*
         * 详见{@link ViewPager#determineTargetPage()}
         */
        try {
            Field dis = ViewPager.class.getDeclaredField("mFlingDistance");
            dis.setAccessible(true);
            int value = (int) (mFlingDistance * getResources().getDisplayMetrics().density);
            dis.set(this, value);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                throw new IllegalStateException("检查v4包，升级导致里面的变量名变了！需要更改这里的变量名");
            }
        }

        /*
         * 详见{@link ViewPager#determineTargetPage()}
         */
        try {
            Field dis = ViewPager.class.getDeclaredField("mMinimumVelocity");
            dis.setAccessible(true);
            dis.set(this, mMinimumVelocity);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                throw new IllegalStateException("检查v4包，升级导致里面的变量名变了！需要更改这里的变量名");
            }
        }


    }

    @Override
    public void addOnPageChangeListener(@NonNull OnPageChangeListener listener) {
        super.addOnPageChangeListener(listener);
        if (mOnPageChangeListeners == null) {
            mOnPageChangeListeners = new ArrayList<>();
        }
        mOnPageChangeListeners.add(listener);
    }

    @Override
    public void removeOnPageChangeListener(@NonNull OnPageChangeListener listener) {
        super.removeOnPageChangeListener(listener);
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.remove(listener);
        }
    }

    @Override
    public ArrayList<View> getTouchables() {
        return super.getTouchables();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Throwable ignored) {
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Throwable ignored) {
        }
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (Throwable ignored) {
            return true;
        }
    }

    @Override
    public void clearOnPageChangeListeners() {
        super.clearOnPageChangeListeners();
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.clear();
        }
    }

    public void addOnPageChangeListenerToFirst(OnPageChangeListener listener) {
        if (listener != null) {
            super.clearOnPageChangeListeners();
            super.addOnPageChangeListener(listener);
            if (mOnPageChangeListeners != null) {
                for (OnPageChangeListener mOnPageChangeListener : mOnPageChangeListeners) {
                    super.addOnPageChangeListener(mOnPageChangeListener);
                }
            } else {
                mOnPageChangeListeners = new ArrayList<>();
            }
            mOnPageChangeListeners.add(listener);
        }
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
        if (isFirstSetCurrentItem) {
            if (item == 0 && getCurrentItem() == 0) {
                if (mOnPageChangeListeners != null) {
                    for (OnPageChangeListener mOnPageChangeListener : mOnPageChangeListeners) {
                        mOnPageChangeListener.onPageSelected(0);
                    }
                }
            }
        }
        isFirstSetCurrentItem = false;
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        try {
            super.setCurrentItem(item, smoothScroll);
            if (isFirstSetCurrentItem) {
                isFirstSetCurrentItem = false;
                if (item == 0 && getCurrentItem() == 0) {
                    if (mOnPageChangeListeners != null) {
                        for (OnPageChangeListener mOnPageChangeListener : mOnPageChangeListeners) {
                            mOnPageChangeListener.onPageSelected(0);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }


    /**
     * @param duration 毫秒
     */
    public void setScrollDuration(int duration) {
        if (mScroller != null) {
            mScroller.setScrollDuration(duration);
        }
    }

}

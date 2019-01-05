package com.ifinver.android.pagecurl;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by iFinVer on 2018/9/28 下午8:21.
 * ilzq@foxmail.com
 *
 * 二级缓存：(Two level cache:)
 * 离屏不显示的Fragment缓存在mFragments中. ('mFragments' holds off screen pages)
 * 离屏进行DestroyItem的Fragment缓存在mCache中 ('mCache' holds destroyed pages)
 *
 * For multi type : {@link #getItemType(int)}
 */
public abstract class BookPageAdapter<T> extends PagerAdapter {

    private static final String TAG = "BookPageAdapter";

    protected static final int DEFAULT_TYPE = 0;

    private List<T> mDataList;
    private OnPageClickListener mOnPageClickListener;
    private ViewPager mBookViewPager;

    //for Adapter
    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;
    private ArrayList<Fragment.SavedState> mSavedState = new ArrayList<>();
    private ArrayList<PageFragment> mFragments = new ArrayList<>(); //伴随数列，大部分元素为null，不会占用不必要内存
    private PageFragment mCurrentPrimaryItem = null;
    private final SparseArray<Queue<PageFragment>> mCache = new SparseArray<>(3);

    @SuppressWarnings("WeakerAccess")
    public BookPageAdapter(FragmentManager fm) {
        mFragmentManager = fm;
    }

    public void setData(List<T> pages) {
        mDataList = pages;
        mCache.clear();
        notifyDataSetChanged();
    }

    @SuppressWarnings("WeakerAccess")
    public void setOnPageClickListener(OnPageClickListener l) {
        this.mOnPageClickListener = l;
    }

    @SuppressWarnings("WeakerAccess")
    public void setBookViewPager(BookPageViewPager bookPageContainer) {
        this.mBookViewPager = bookPageContainer;
        bookPageContainer.addOnPageChangeListenerToFirst(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateCurrentItem();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void updateCurrentItem() {
        if (mBookViewPager != null) {
            updateCurrentItem(getItem(mBookViewPager.getCurrentItem()));
        }
    }

    protected abstract int getItemType(int position);

    private PageFragment getItem(int position) {

        if (mFragments.size() > position) {
            PageFragment f = mFragments.get(position);
            if (f != null) {
                return f;
            }
        }

        final int itemType = getItemType(position);
        final Queue<PageFragment> cacheQueue = mCache.get(itemType);
        if (cacheQueue != null && cacheQueue.size() > 3) {//超过3个缓存的时候再使用，避免刚刚进入缓存，生命周期还没有变更时又上战场了。
            final PageFragment cachePageFragment = cacheQueue.poll();
            if (cachePageFragment != null) {
                return cachePageFragment;
            }
        }

        //没有cache走到这里
        return createItem(position,itemType);
    }

    @NonNull
    protected abstract PageFragment createItem(int position, int itemType);

    @SuppressWarnings("WeakerAccess")
    public PageFragment getCurrentPage() {
        return mCurrentPrimaryItem;
    }

    public T getItemData(int position) {
        if (mDataList != null && position >= 0 && position < mDataList.size()) {
            return mDataList.get(position);
        }
        return null;
    }

    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    @NonNull
    @SuppressLint("CommitTransaction")
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (mFragments.size() > position) {
            PageFragment f = mFragments.get(position);
            if (f != null) {
                performBindData(position, f);
                return f;
            }
        }

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        PageFragment fragment = getItem(position);

        try {
            if (mSavedState.size() > position) {
                Fragment.SavedState fss = mSavedState.get(position);
                if (fss != null) {
                    fragment.setInitialSavedState(fss);
                }
            }
        } catch (Throwable ignored) {
            // fragment.setInitialSavedState(fss) ： Fragment already active
        }

        while (mFragments.size() <= position) {
            mFragments.add(null);
        }
        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);
        mFragments.set(position, fragment);

        performBindData(position, fragment);

        mCurTransaction.add(mBookViewPager.getId(), fragment);

        return fragment;
    }

    private void performBindData(int position, PageFragment fragment) {
        fragment.bindData(position, getCount(), getItemData(position), mOnPageClickListener);
    }


    @Override
    @SuppressLint("CommitTransaction")
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        PageFragment fragment = (PageFragment) object;
        fragment.markDestroy();

        while (mSavedState.size() <= position) {
            mSavedState.add(null);
        }

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        try {
            mSavedState.set(position, fragment.isAdded()
                    ? mFragmentManager.saveFragmentInstanceState(fragment) : null);
        } catch (Throwable ignored) {
            //大量瞬间滑动时，可能会爆异常
            //mFragmentManager.saveFragmentInstanceState(fragment) : is not currently in the FragmentManager
        }

        while (mFragments.size() <= position) {
            mFragments.add(null);
        }
        mFragments.set(position, null);

        //进行缓存
        int itemType = getItemType(position);
        Queue<PageFragment> cacheQueue = mCache.get(itemType);
        if (cacheQueue == null) {
            cacheQueue = new LinkedList<>();
            mCache.put(itemType, cacheQueue);
        }
        cacheQueue.offer(fragment);

        mCurTransaction.remove(fragment);
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    private void updateCurrentItem(PageFragment fragment) {
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
                mCurrentPrimaryItem.setTouchesAvailable(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
                fragment.setTouchesAvailable(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        Bundle state = null;
        if (mSavedState.size() > 0) {
            state = new Bundle();
            Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size()];
            mSavedState.toArray(fss);
            state.putParcelableArray("states", fss);
        }
        for (int i = 0; i < mFragments.size(); i++) {
            Fragment f = mFragments.get(i);
            if (f != null && f.isAdded()) {
                if (state == null) {
                    state = new Bundle();
                }
                String key = "f" + i;
                mFragmentManager.putFragment(state, key, f);
            }
        }
        return state;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle) state;
            bundle.setClassLoader(loader);
            Parcelable[] fss = bundle.getParcelableArray("states");
            mSavedState.clear();
            mFragments.clear();
            if (fss != null) {
                for (Parcelable fs : fss) {
                    mSavedState.add((Fragment.SavedState) fs);
                }
            }
            Iterable<String> keys = bundle.keySet();
            for (String key : keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    PageFragment f = (PageFragment) mFragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        while (mFragments.size() <= index) {
                            mFragments.add(null);
                        }
                        f.setMenuVisibility(false);
                        mFragments.set(index, f);
                    } else {
                        Log.w(TAG, "Bad fragment at key " + key);
                    }
                }
            }
        }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        PageFragment pageFragment = object instanceof PageFragment ? ((PageFragment) object) : null;
        if (pageFragment != null) {
            T data = null;
            try {
                //noinspection unchecked
                data = (T) pageFragment.getPageData();
            }catch (Throwable ignored){}
            if (data != null && mDataList != null) {
                return mDataList.indexOf(data);
            }
        }
        return PagerAdapter.POSITION_NONE;
    }

    public void destroy() {
        mSavedState.clear();
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        for (PageFragment f : mFragments) {
            try {
                mCurTransaction.remove(f);
            } catch (Throwable ignored) {
            }

        }
        mFragments.clear();

        for (int i = 0; i < mCache.size(); i++) {
            final Queue<PageFragment> pageFragments = mCache.valueAt(i);
            if (pageFragments != null) {
                for (PageFragment fragment : pageFragments) {
                    try {
                        mCurTransaction.remove(fragment);
                    } catch (Throwable ignored) {
                    }
                }
                pageFragments.clear();
            }
        }
        mCache.clear();

        try {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        } catch (Throwable ignored) {
        }
    }

    public interface OnPageClickListener {

        void onPageClick(PageFragment page);

    }
}

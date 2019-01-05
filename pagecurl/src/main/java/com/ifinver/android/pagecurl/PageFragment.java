package com.ifinver.android.pagecurl;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by iFinVer on 2018/9/29 上午9:47.
 * ilzq@foxmail.com
 */
public abstract class PageFragment<T> extends Fragment {

    protected boolean isViewCreated = false;
    protected boolean isDataPassed = false;
    protected int mPageIndex = -1;
    private int mTotalCount;
    protected T mPageData;
    private final Object mDataLock = new Object();//bindData()有可能比onCreateView()还要早，故加此锁。
    private BookPageFrameLayout mRootView;
    protected BookPageAdapter.OnPageClickListener mOnPageClickListener;
    private boolean mTouchesAvailable = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView != null) {
            ViewGroup parent = mRootView.getParent() instanceof ViewGroup ? ((ViewGroup) mRootView.getParent()) : null;
            if (parent != null) {
                parent.removeView(mRootView);
            }
        } else {
            final Context context = getContext();
            if (context == null) {
                return null;
            }
            mRootView = new BookPageFrameLayout(context);
            mRootView.setTouchEventAvailable(mTouchesAvailable);
//            mRootView.setTag(this);
            View view = onInflateView(container);
            if (view != null) {
                mRootView.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }
        isViewCreated = true;
        performBindData();
        return mRootView;
    }

    public void updatePageIndicator(int totalCount){
        this.mTotalCount = totalCount;
        synchronized (mDataLock) {
            if (isViewCreated && isDataPassed) {
                showPageNumber(true, mPageIndex,totalCount);
            }
        }
    }

    public void bindData(int position, int totalCount, T itemData, BookPageAdapter.OnPageClickListener listener){
        this.mPageIndex = position;
        this.mTotalCount = totalCount;
        this.mPageData = itemData;
        this.mOnPageClickListener = listener;
        this.isDataPassed = true;
        performBindData();
    }

    private void performBindData() {
        synchronized (mDataLock) {
            if (isViewCreated && isDataPassed) {
                onBindData(mPageIndex, mPageData);
                showPageNumber(true, mPageIndex, mTotalCount);
            }
        }
    }

    public void setTouchesAvailable(boolean available) {
        this.mTouchesAvailable = available;
        if (mRootView != null) {
            mRootView.setTouchEventAvailable(available);
        }
    }

    public T getPageData() {
        return mPageData;
    }

    @Nullable
    protected abstract View onInflateView(ViewGroup container);

    protected abstract void onBindData(int position, T pageData);

    public abstract void showPageNumber(boolean show, int position, int total);

    public void markDestroy() {
        isViewCreated = false;
        isDataPassed = false;
    }

}

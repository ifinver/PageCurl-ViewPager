package com.ifinver.android.pagecurlsample;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.ifinver.android.pagecurl.BookPageAdapter;
import com.ifinver.android.pagecurl.PageFragment;

/**
 * Created by iFinVer on 2019/1/3.
 * ilzq@foxmail.com
 */
public class MultiTypeBookPageAdapter<T> extends BookPageAdapter<T> {
    public MultiTypeBookPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    protected int getItemType(int position) {
        return DEFAULT_TYPE;
    }

    @NonNull
    @Override
    protected PageFragment createItem(int position, int itemType) {
        return new PicturePageFragment();
    }
}

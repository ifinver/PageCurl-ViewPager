package com.ifinver.android.pagecurlsample;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ifinver.android.pagecurl.PageFragment;


/**
 * Created by iFinVer on 2018/9/29 上午9:47.
 * ilzq@foxmail.com
 */
public class PicturePageFragment extends PageFragment<ImageVO> {

    private final String TAG = "ContentPageFragment";

    public final static int DISPLAY_STATUS_NONE = 0;
    public final static int DISPLAY_STATUS_PROCESSING = 1;
    public final static int DISPLAY_STATUS_SHOWN = 2;
    public final static int DISPLAY_STATUS_ERROR = 3;
    public final static int DISPLAY_STATUS_CANCELED = 4;

    @IntDef({DISPLAY_STATUS_NONE, DISPLAY_STATUS_PROCESSING, DISPLAY_STATUS_SHOWN, DISPLAY_STATUS_ERROR, DISPLAY_STATUS_CANCELED})
    @interface IMG_DISPLAY_STATUS {
    }

    private ImageView ivPicture;
    public TextView tvPageNumber;

    @IMG_DISPLAY_STATUS
    public int displayStatus = DISPLAY_STATUS_NONE;

    @Nullable
    @Override
    protected View onInflateView(ViewGroup container) {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_picture, container, false);
        ivPicture = rootView.findViewById(R.id.iv_picture);
        tvPageNumber = rootView.findViewById(R.id.tv_page_number);
        return rootView;
    }

    @Override
    public void onBindData(final int position, ImageVO pageData) {
        displayStatus = DISPLAY_STATUS_PROCESSING;
        //不用对比已显示的图片的url是否等于将要显示的url，而跳过加载，这样可以规避一个图片缩放异常的bug
        final ObjectAnimator anim = ObjectAnimator.ofInt(ivPicture, "ImageLevel", 0, 100);
        anim.setDuration(800);
        anim.setRepeatCount(ObjectAnimator.INFINITE);
        anim.start();
        Glide.with(this).clear(ivPicture);//cancel previous
        Glide.with(this)
                .load(pageData.url)
                .apply(new RequestOptions().placeholder(R.drawable.loading_rotate))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        displayStatus = DISPLAY_STATUS_ERROR;
                        anim.cancel();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        displayStatus = DISPLAY_STATUS_SHOWN;
                        anim.cancel();
                        return false;
                    }
                })
                .into(ivPicture);
    }

    @Override
    public void showPageNumber(boolean show, int position, int total) {
        if (show) {
            tvPageNumber.setVisibility(View.VISIBLE);
            final String text = (position + 1) + "/" + total;
            tvPageNumber.setText(text);
        } else {
            tvPageNumber.setVisibility(View.GONE);
        }
    }
}

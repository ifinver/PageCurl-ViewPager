package com.ifinver.android.pagecurl;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by iFinVer on 2018/9/29 下午7:38.
 * ilzq@foxmail.com
 * <p>
 * {@link ViewPager#setPageTransformer(boolean, ViewPager.PageTransformer)}
 * <p>
 * *第一个参数为false时 (When first argument set to false)：
 * [-Infinity,-1) :离屏的左边的页 (Left page out of screen)
 * [-1,0) : 两个可见的页中，左边的页 (Left page)
 * [0,1) : 两个可见的页中，右边的页 (Right page)
 * [1,+Infinity] :离屏的右边的页  (Right page out of screen)
 * *第一个参数为true时：
 * [-Infinity,-1) :离屏的左边的页
 * [-1,0] : 两个可见的页中，左边的页
 * (0,1] : 两个可见的页中，右边的页
 * (1,+Infinity] :离屏的右边的页
 * <p>
 * 将左边的页面滑动到屏幕中时（即手指按下后向右移动）：(When swipe left page into screen)
 * 左边的页：从-1到0 （包含-1，不包含0）  ( Left page : from -1 to 0 )
 * 右边的页：从0到1 （包含0，不包含1）    ( Right page :from 0 to 1 )
 * <p>
 * 将右边的页面滑动到屏幕中时（即手指按下后向左移动）：
 * 左边的页：从0到-1 （不包含0，包含-1）
 * 右边的页：从1到0 （不包含1，包含0）
 * <p>
 * 实现思想：(How it work)
 * 滑动时： (When swipe)
 * 0. 每个页面看做左右两个部分。（Each page is considered to have two parts left and right)
 * 1. 左边的页只绘制左边一半，右边的页只绘制右边一半 (Left page only draw left part， and so was right page)
 * 2. 再绘制旋转的书页。(Then, draw the flying part)
 * 3. 停止时,隐藏其它页面 (When animation is end, unset the translation)
 * <p>
 * <p>
 * 警告：
 * {@link ViewPager#setPageTransformer(boolean, ViewPager.PageTransformer)}
 * 第一个参数必须为false,因为本类和{@link BookPageFrameLayout}都是依照false的区间设计的
 *
 * Warning:
 * {@link ViewPager#setPageTransformer(boolean, ViewPager.PageTransformer)}
 * First argument must set to be false.
 * If you want it to be true,
 */
public class BookPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(@NonNull View view, float position) {
        //must guarantee that the 'view' is derived from BookPageFrameLayout
        final BookPageFrameLayout page = (BookPageFrameLayout) view;
        if (position <= -1 || position >= 1) {//边界
            if (page.getTranslationX() != 0) {
                //取消固定 (unset the position,so the page will be disappear)
                page.setTranslationX(0);
                //取消特殊绘制 (cancel page curl drawing)
                page.cancelFlip();
            }
        } else {
            //让页面们堆叠显示 （Set all pages in the same position)
            page.setTranslationX(page.getWidth() * -position);
            //绘制 (Draw different parts for each page)
            page.flip(position);
        }
    }
}

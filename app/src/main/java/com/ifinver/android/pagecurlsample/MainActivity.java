package com.ifinver.android.pagecurlsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ifinver.android.pagecurl.BookPageAdapter;
import com.ifinver.android.pagecurl.BookPageTransformer;
import com.ifinver.android.pagecurl.BookPageViewPager;
import com.ifinver.android.pagecurl.PageFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BookPageViewPager vpBook;
    private MultiTypeBookPageAdapter<ImageVO> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vpBook = findViewById(R.id.vp_book);
        mAdapter = new MultiTypeBookPageAdapter<>(getSupportFragmentManager());
        vpBook.setPageTransformer(false,new BookPageTransformer());//step 1
        vpBook.setAdapter(mAdapter);//step 2
        mAdapter.setBookViewPager(vpBook);//step 3

        //load data
        List<ImageVO> voList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ImageVO vo = new ImageVO();
            vo.url = "file:///android_asset/" + (i%8) + ".jpg";
            voList.add(vo);
        }
        mAdapter.setData(voList);
//        ImageCrawler.fetch(30, new ImageCrawler.Listener() {
//            @Override
//            public void onSuccess(List<ImageVO> voList) {
//                if (mAdapter != null) {
//                    mAdapter.setData(voList);
//                }
//            }
//
//            @Override
//            public void onFailed(Exception e) {
//
//            }
//        });

        findViewById(R.id.fab_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //jump to next over two pages
                if (vpBook != null) {
                    int target = vpBook.getCurrentItem() + 2;
                    if (target >= mAdapter.getCount()) {
                        target = mAdapter.getCount() - 1;
                    }
                    vpBook.setCurrentItem(target, true);
                }

            }
        });
        findViewById(R.id.fab_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //jump to previous over two pages
                if (vpBook != null) {
                    int target = vpBook.getCurrentItem()-2;
                    if (target < 0) {
                        target = 0;
                    }
                    vpBook.setCurrentItem(target, true);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

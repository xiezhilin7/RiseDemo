package com.example.risedemo.viewpager;

import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.risedemo.R;
import com.viewpagerindicator.CirclePageIndicator;


public class ViewPagerActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private SlidesAdapter slidesAdapter;
    private LinearLayout dotsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);

        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);

        slidesAdapter = new SlidesAdapter(this);
        viewPager.setAdapter(slidesAdapter);

        CirclePageIndicator mPageIndicator = (CirclePageIndicator) findViewById(R.id.circle_page_indicator);
        mPageIndicator.setViewPager(viewPager);

        viewPager.addOnPageChangeListener(viewListener);

        start();
    }

    private int mCurrentIndex = 0;
    private final int PLAY = 0x123;
    private long mDelayTime = 2000;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == PLAY) {
                viewPager.setCurrentItem(mCurrentIndex);
                start();
            }
        }
    };

    private void start() {
        mCurrentIndex++;
        mCurrentIndex = mCurrentIndex % slidesAdapter.getCount();
        mHandler.sendEmptyMessageDelayed(PLAY, mDelayTime);
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {
        }

        @Override
        public void onPageSelected(int position) {
            mCurrentIndex = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

}

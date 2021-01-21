package com.example.risedemo.viewpager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.risedemo.R;

import java.util.ArrayList;

public class SlidesAdapter extends PagerAdapter {

    Context context;
    View childView1, childView2, childView3;
    ArrayList<View> mView;

    public SlidesAdapter(Context context) {
        this.context = context;
        mView = new ArrayList<>();
        childView1 = View.inflate(context, R.layout.slide_childview1, null);
        childView2 = View.inflate(context, R.layout.slide_childview2, null);
        childView3 = View.inflate(context, R.layout.slide_childview3, null);
        mView.add(childView1);
        mView.add(childView2);
        mView.add(childView3);
    }

    @Override
    public int getCount() {
        return mView.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View childView = mView.get(position);
        ViewPager parent = (ViewPager) childView.getParent();
        if (parent != null) {
            parent.removeView(childView);
        }
        container.addView(childView);
        return childView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);
    }

}

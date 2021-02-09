package com.example.tracker;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tracker.time.TimeFragment;
import com.example.tracker.weight.WeightFragment;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 2;

    public ScreenSlidePagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new WeightFragment();
        } else {
            return new TimeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}

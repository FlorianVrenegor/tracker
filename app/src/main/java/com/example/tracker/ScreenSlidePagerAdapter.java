package com.example.tracker;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tracker.time.TimeFragment;
import com.example.tracker.todo.TodoFragment;
import com.example.tracker.weight.WeightFragment;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 3;

    public ScreenSlidePagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new WeightFragment();
        } else if (position == 1) {
            return new TimeFragment();
        } else {
            return new TodoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}

package com.example.tracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private var viewPager: ViewPager2? = null
    private var pagerAdapter: FragmentStateAdapter? = null

    private val tabNames = arrayOf("Weight", "Time", "Todo")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.view_pager)
        viewPager!!.setUserInputEnabled(false)
        pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager!!.setAdapter(pagerAdapter)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager!!) { tab: TabLayout.Tab, position: Int -> tab.setText(tabNames[position]) }.attach()
    }

    override fun onBackPressed() {
        if (viewPager!!.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager!!.currentItem -= 1
        }
    }
}

package com.lmx.autoscaningapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MainFragment() // Main screen fragment
            1 -> ScanResultsFragment() // Fragment to display the .txt file content
            else -> MainFragment()
        }
    }
}

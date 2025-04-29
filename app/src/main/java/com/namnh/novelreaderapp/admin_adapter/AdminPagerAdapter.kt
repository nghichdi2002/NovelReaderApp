package com.namnh.novelreaderapp.admin_adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.namnh.novelreaderapp.admin.ManageChaptersFragment
import com.namnh.novelreaderapp.admin.ManageStoriesFragment

class AdminPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> ManageStoriesFragment()
            1 -> ManageChaptersFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Quản lý truyện"
            1 -> "Quản lý chương"
            2 -> "Quản lý tài khoản"
            else -> null
        }
    }
}
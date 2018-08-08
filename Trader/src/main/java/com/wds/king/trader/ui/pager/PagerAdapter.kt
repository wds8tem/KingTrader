package com.wds.king.trader.ui.pager

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.wds.king.trader.ui.fragment.TabFragment

class PagerAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {

    var fragmentList : ArrayList<TabFragment> = arrayListOf()
    var groupId = 0
    var curIndex = 0

    override fun getItem(position: Int): TabFragment? {

        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }
}
package com.wds.king.trader.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wds.king.trader.MainActivity
import com.wds.king.trader.R
import com.wds.king.trader.adapter.HomeInterestSubjectAdapter
import com.wds.king.trader.model.InterestGroup
import com.wds.king.trader.model.InterestSubject
import kotlinx.android.synthetic.main.home_tab_fragment.view.*

class TabFragment() : Fragment() {

    var group: InterestGroup = InterestGroup()
    var homeInterSubAdapter: HomeInterestSubjectAdapter = HomeInterestSubjectAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val activity = arguments!!.getSerializable("main") as MainActivity

        val view = inflater?.inflate(R.layout.home_tab_fragment, container, false)
        homeInterSubAdapter.setContext(activity)

        homeInterSubAdapter.setOnClickListener(onClickListener = object : HomeInterestSubjectAdapter.OnClickListener {
            override fun onItem(interSub : InterestSubject?) {
                activity.showDataView(interSub)
            }

            override fun onHeader(title: String) {

            }
        })
        view.home_rv.adapter = homeInterSubAdapter
        homeInterSubAdapter.notifyDataSetChanged()
        return view
    }
}
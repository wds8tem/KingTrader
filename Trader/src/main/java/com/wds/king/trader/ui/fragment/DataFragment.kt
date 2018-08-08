package com.wds.king.trader.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wds.king.trader.R
import com.wds.king.trader.model.InterestSubject
import com.wds.king.trader.ui.view.DataView
import kotlinx.android.synthetic.main.data_fragment.view.*

const val DATA_TAG = "DataFragment"

class DataFragment : BaseFragment() {

    var dataView : DataView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onActivityCreated(savedInstanceState)

        val rootView: View = inflater.inflate(R.layout.data_fragment, container, false)
                ?: return null

        val item = arguments!!.getSerializable("item") as InterestSubject?

        dataView = DataView(activity!!,item)
        dataView!!.initView(activity!!)
        rootView.data_content.addView(dataView)
        return rootView
    }

    override fun onDetach() {
        super.onDetach()
        dataView?.releaseView()
    }
}
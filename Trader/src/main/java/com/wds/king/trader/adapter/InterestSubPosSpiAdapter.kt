package com.wds.king.trader.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.wds.king.trader.R

class InterestSubPosSpiAdapter(context: Context, itemList: Array<String>) : BaseAdapter() {

    internal val context: Context = context
    internal val itemList: Array<String> = itemList

    override fun getCount(): Int {
        return itemList.size
    }

    override fun getItem(position: Int): Any {
        return itemList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var convertView = convertView

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.interested_subject_pos_spi, parent, false)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.setMargins(10, 20, 0, 0)
            convertView.layoutParams = lp
        }

        val title = itemList[position]

        val tvAddType = convertView!!.findViewById<TextView>(R.id.add_inter_sub_sel_type_title)
        tvAddType.text = title

        return convertView
    }
}

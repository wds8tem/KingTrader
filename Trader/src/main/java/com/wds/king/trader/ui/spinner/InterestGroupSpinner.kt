package com.wds.king.trader.ui.spinner

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.wds.king.trader.model.InterestGroup
import com.wds.king.trader.ui.fragment.TabFragment

class InterestGroupSpinner(context: Context) : Spinner(context), AdapterView.OnItemSelectedListener {

    private var tabFragmentList: ArrayList<TabFragment>? = null

    private val groupListAdapter: GroupListAdapter
    private var interestGroup: InterestGroup? = null

    private val ITEM_HEIGHT: Int
    private val PADDING_H: Int
    private val ITEM_CODE_WIDTH: Int

    private var listener: OnItemCodeSelectedListener? = null

    init {

        val metrics = context.resources.displayMetrics
        ITEM_HEIGHT = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, metrics).toInt()
        PADDING_H = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, metrics).toInt()
        ITEM_CODE_WIDTH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, metrics).toInt()

        groupListAdapter = GroupListAdapter()

        prompt = "관심그룹 편집"
        onItemSelectedListener = this
    }

    fun setOnItemCodeSelectedListener(listener: OnItemCodeSelectedListener) {
        this.listener = listener
    }

    fun setGroupList(array: ArrayList<TabFragment>) {

        tabFragmentList = array
        interestGroup = null
        adapter = groupListAdapter
        groupListAdapter.notifyDataSetChanged()
    }

    fun notifyDataSetChanged()
    {
       groupListAdapter.notifyDataSetChanged()
    }


    fun clearInstance() {

        adapter = null

        if (tabFragmentList != null) {
            tabFragmentList = null
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

        val obj = getItemAtPosition(position) ?: return

        interestGroup = obj as InterestGroup

        if (listener != null) {
            listener!!.onItemSelected(position)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private inner class GroupListAdapter : BaseAdapter(), SpinnerAdapter {

        override fun getCount(): Int {
            return if (tabFragmentList == null) 0 else tabFragmentList!!.size
        }

        override fun getItem(nPos: Int): InterestGroup? {
            if (tabFragmentList == null) return null
            return if (nPos < 0 || nPos >= tabFragmentList!!.size) null else tabFragmentList!![nPos].group
        }

        override fun getItemId(nPos: Int): Long {
            return nPos.toLong()
        }

        override fun getView(nPos: Int, convertView: View?, parent: ViewGroup): View {
            var viewItem: TextView? = convertView as? TextView

            if (viewItem == null) {
                viewItem = TextView(parent.context)
                viewItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                viewItem.setTextColor(Color.WHITE)
                viewItem.setSingleLine()
                viewItem.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
            }

            val obj = getItem(nPos)
            if (obj != null && obj is InterestGroup) {
                viewItem.text = obj.name
            } else {
                viewItem.text = ""
            }

            return viewItem
        }

        override fun getDropDownView(nPos: Int, convertView: View?, parent: ViewGroup): View {
            var viewItem: DropDownItemView? = convertView as? DropDownItemView

            if (viewItem == null) {
                viewItem = DropDownItemView(parent.context)
                viewItem.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ITEM_HEIGHT)
            }

            val obj = getItem(nPos)
            if (obj != null && obj is InterestGroup) {
                viewItem.setItemInfo((obj as InterestGroup?)!!)
            } else {
                viewItem.clearInfo()
            }

            return viewItem
        }
    }

    interface OnItemCodeSelectedListener {
        fun onItemSelected(idx: Int)
    }

    private inner class DropDownItemView constructor(context: Context) : LinearLayout(context) {

        private val mGroupName: TextView

        init {

            orientation = LinearLayout.HORIZONTAL
            setPadding(PADDING_H, 0, PADDING_H, 0)

            mGroupName = TextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                setTextColor(Color.DKGRAY)
                setSingleLine()
                gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
            }

            addView(mGroupName, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))
        }

        fun setItemInfo(group: InterestGroup) {
            mGroupName.text = group.name
        }

        fun clearInfo() {
            mGroupName.text = ""
        }
    }
}

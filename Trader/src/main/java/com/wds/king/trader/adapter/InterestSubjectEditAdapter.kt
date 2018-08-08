package com.wds.king.trader.adapter

import android.graphics.Color
import android.graphics.Color.parseColor
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.wds.king.trader.R
import com.wds.king.trader.callback.InterestSubjectEditCB
import com.wds.king.trader.callback.ItemTouchHelperAdapter
import com.wds.king.trader.callback.ItemTouchHelperViewHolder
import com.wds.king.trader.callback.OnStartDragListener
import com.wds.king.trader.model.InterestGroup
import com.wds.king.trader.model.InterestSubject
import java.util.*

class InterestSubjectEditAdapter(listener: InterestSubjectEditCB, header: InterestGroup, dragStartListener: OnStartDragListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), InterestSubject.NotifyListener, ItemTouchHelperAdapter {

    private val TYPE_HEADER = 0
    private val TYPE_BODY = 1
    var itemList: ArrayList<InterestSubject> = arrayListOf()
    private val header: InterestGroup = header
    private var groupName = ""

    private val listener: InterestSubjectEditCB = listener
    private val dragStartListener: OnStartDragListener = dragStartListener
    var lockTouchSwap = false
    var allSelected = false

    override fun onSingleDataChangedNotify(code : String, groupId : Int,pos: Int,curPrice : String) {
        notifyItemChanged(pos)
    }

    fun isAtLeastOnSelection() : Boolean{
        itemList.find { it.isChecked }.apply {
            return true
        }
        return false
    }

    fun selectAllItem() {

        itemList.map {
            it.isChecked = true
        }
        notifyDataSetChanged()
    }

    fun removeItem() {

        itemList.filter {
            it.isChecked
        }.run {
            this.map {
                itemList.remove(it)
            }
        }
        notifyDataSetChanged()
    }

    fun swapItemsUp() {

        for (i in 0 until itemList.size) {
            val item = itemList[i]
            if (item.isChecked && (i - 1) >= 0) {
                Collections.swap(itemList, i, i - 1)
            }
        }
        notifyDataSetChanged()
    }

    fun swapItemsDown() {

        for (i in itemList.size - 1 downTo 0) {
            val item = itemList[i]

            if (item.isChecked && (i + 1) < itemList.size) {
                Collections.swap(itemList, i, i + 1)
            }
        }
        notifyDataSetChanged()
    }

    fun cancelAllSelection() {
        itemList.map {
            it.isChecked = false
        }
        notifyDataSetChanged()
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {

        if (fromPosition > 1 && toPosition > 0) {
            Collections.swap(itemList, fromPosition - 1, toPosition - 1)
            notifyItemMoved(fromPosition, toPosition)
        }
    }

    override fun onItemDismiss(position: Int) {

//        itemList.removeAt(position - 1)
//        notifyItemRemoved(position)
    }

    fun setDataList(groupName: String, itemList: ArrayList<InterestSubject>) {

        this.itemList = itemList
        this.groupName = groupName
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.interested_subject_edit_header, parent, false)
            return HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.interested_subject_edit_item, parent, false)
            return ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is HeaderViewHolder) {

            if (header != null) {
                holder.interSubSelection.text = groupName
                holder.interSubAddition.text = "종목추가"

                holder.interSubSelection.setOnClickListener {
                    listener.onGroupChanged()
                }

                holder.interSubAddition.setOnClickListener {
                    listener.onGotoAddSubject()
                }
            }
        } else if (holder is ItemViewHolder) {

            if (itemList != null && itemList.size > 0) {

                holder.itemMove.setOnTouchListener { v, event ->
                    if (!lockTouchSwap && MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        dragStartListener.onStartDrag(holder)
                    }
                    false
                }

                val pos = position - 1
                val item = itemList[pos]
                holder.apply {
                    checker.isChecked = item.isChecked
                    title.text = item.name
                    curPrice.text = item.curPrice
                    todayDiffPrice.text = item.todayDiffPrice
                    tvTodayVolume.text = item.todayVolume
                    todayDiffRatio.text = if (item.todayDiffRatio != "")
                        "%.2f".format(item.todayDiffRatio.toFloat()) + "%" else ""

                    val iTodayDiffPrice = if (item.todayDiffPrice != "") item.todayDiffPrice.toInt() else 0
                    if (iTodayDiffPrice > 0) {
                        curPrice.setTextColor(parseColor("#FA2A05"))
                        todayDiffPrice.setTextColor(parseColor("#FA2A05"))
                        todayDiffRatio.setTextColor(parseColor("#FA2A05"))
                        todayDiffRatioShape.text = "\u25B2"
                        todayDiffRatioShape.setTextColor(parseColor("#FA2A05"))
                    } else if (iTodayDiffPrice < 0) {
                        curPrice.setTextColor(parseColor("#084EE5"))
                        todayDiffPrice.setTextColor(parseColor("#084EE5"))
                        todayDiffRatio.setTextColor(parseColor("#084EE5"))
                        todayDiffRatioShape.text = "\u25BC"
                        todayDiffRatioShape.setTextColor(parseColor("#084EE5"))
                    } else {
                        curPrice.setTextColor(parseColor("#D1D2D5"))
                        todayDiffPrice.setTextColor(parseColor("#D1D2D5"))
                        todayDiffRatio.setTextColor(parseColor("#D1D2D5"))
                        todayDiffRatioShape.text = ""
                    }

                    checker.setOnClickListener {
                        itemList[position - 1].isChecked = (it as CheckBox).isChecked

                        var checked = false
                        for (item in itemList) {
                            if (item.isChecked) {
                                checked = true
                                break
                            }
                        }
                        lockTouchSwap = checked
                        listener.onItemViewClicked(checked)
                    }
                }
            }
        }
    }

    fun isCheckedWithinItems(): Boolean {
        var checked = false
        for (item in itemList) {
            if (item.isChecked) {
                checked = true
                break
            }
        }
        return checked
    }

    override fun getItemCount() = itemList.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_HEADER
        } else {
            TYPE_BODY
        }
    }

    private inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view), ItemTouchHelperViewHolder {

        val checker: CheckBox = view.findViewById(R.id.inter_sub_edit_item_checker)
        val title: TextView = view.findViewById(R.id.inter_sub_edit_title)
        val curPrice: TextView = view.findViewById(R.id.inter_sub_edit_cur_price)
        val todayDiffPrice: TextView = view.findViewById(R.id.inter_sub_edit_today_diff_price)
        val tvTodayVolume: TextView = view.findViewById(R.id.inter_sub_edit_today_volume)
        val todayDiffRatio: TextView = view.findViewById(R.id.inter_sub_edit_today_diff_ratio)
        val todayDiffRatioShape: TextView = view.findViewById(R.id.inter_sub_edit_diff_ratio_shape)
        val itemMove: ImageView = view.findViewById(R.id.inter_sub_edit_item_move)

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(0)
        }
    }

    private inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val interSubSelection: TextView = view.findViewById(R.id.inter_sub_edit_sel)
        val interSubAddition: TextView = view.findViewById(R.id.inter_sub_edit_add)
    }
}

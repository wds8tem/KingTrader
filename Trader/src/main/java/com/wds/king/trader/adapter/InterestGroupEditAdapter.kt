package com.wds.king.trader.adapter

import android.content.Context
import android.graphics.Color
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TextView
import com.wds.king.trader.R
import com.wds.king.trader.callback.InterestGroupEditCB
import com.wds.king.trader.callback.ItemTouchHelperAdapter
import com.wds.king.trader.callback.ItemTouchHelperViewHolder
import com.wds.king.trader.callback.OnStartDragListener
import com.wds.king.trader.model.InterestSubject
import com.wds.king.trader.ui.fragment.TabFragment
import java.util.*

class InterestGroupEditAdapter(context : Context, listener: InterestGroupEditCB, dragStartListener: OnStartDragListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), InterestSubject.NotifyListener, ItemTouchHelperAdapter {

    private val TYPE_HEADER = 0
    private val TYPE_BODY = 1
    var itemList: ArrayList<TabFragment> = arrayListOf()
    private var header: String = ""
    private val context = context

    private val listener: InterestGroupEditCB = listener
    private val dragStartListener: OnStartDragListener = dragStartListener
    var lockTouchSwap = false
    var allSelected = false

    override fun onSingleDataChangedNotify(code : String, groupId : Int, pos: Int,curPrice : String) {
        notifyItemChanged(pos)
    }

    fun clearAllItem() {
        itemList.clear()
    }

    fun selectAllItem() {

        itemList.map {
            it.group.isChecked = true
        }
        notifyDataSetChanged()
    }

    fun removeItem() {

        itemList.filter {
            it.group.isChecked
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
            if (item.group.isChecked && ((i - 1) > 0)) {

                Collections.swap(itemList, i, i - 1)
            }
        }
        notifyDataSetChanged()
    }

    fun swapItemsDown() {

        for (i in itemList.size - 1 downTo 0) {
            val item = itemList[i]
            if (item.group.isChecked && (i + 1) < itemList.size) {

                Collections.swap(itemList, i, i + 1)
            }
        }
        notifyDataSetChanged()
    }

    fun cancelAllSelection() {
        itemList.map {
            it.group.isChecked = false
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
    }

    fun addDataList(itemList: ArrayList<TabFragment>) {

        //this.itemList = itemList
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    fun addData(item: TabFragment) {

        this.itemList.add(item)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.interested_group_edit_header, parent, false)
            return HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.interested_group_edit_item, parent, false)
            return ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is HeaderViewHolder) {

            if (header != null) {
                holder.interGroupSelection.text = context.getString(R.string.interested_group) + "(" + itemList.size + ")"


                holder.interGroupAddition.setOnClickListener {
                    listener.onAddGroup()
                }
            }
        } else if (holder is ItemViewHolder) {

            if (itemList != null && itemList.size > 0) {

                holder.handleView.setOnTouchListener { v, event ->
                    if (!lockTouchSwap && MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        dragStartListener.onStartDrag(holder)
                    }
                    false
                }

                val pos = position - 1
                val item = itemList[pos]
                holder.apply {
                    checker.isChecked = item.group.isChecked
                    title.text = item.group.name

                    checker.setOnClickListener {
                        itemList[position - 1].group.isChecked = (it as CheckBox).isChecked

                        var checked = false
                        for (item in itemList) {
                            if (item.group.isChecked) {
                                checked = true
                                break
                            }
                        }
                        lockTouchSwap = checked
                        listener.onItemViewClicked(checked)
                    }

                    checker.setOnClickListener {
                        itemList[position - 1].group.isChecked = (it as CheckBox).isChecked

                        var checked = false
                        for (item in itemList) {
                            if (item.group.isChecked) {
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
            if (item.group.isChecked) {
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

        val handleView: FrameLayout = view.findViewById(R.id.inter_group_edit_item_view)
        val checker: CheckBox = view.findViewById(R.id.inter_group_edit_item_checker)
        val title: TextView = view.findViewById(R.id.inter_group_edit_title)

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(0)
        }
    }

    private inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val interGroupSelection: TextView = view.findViewById(R.id.inter_group_edit_sel)
        val interGroupAddition: TextView = view.findViewById(R.id.inter_group_edit_add)
    }
}

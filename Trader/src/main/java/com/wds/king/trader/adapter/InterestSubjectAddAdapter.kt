package com.wds.king.trader.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.SectionIndexer
import android.widget.TextView
import com.wds.king.trader.R
import com.wds.king.trader.adapter.InterestSubjectAddAdapter.ViewHolder
import com.wds.king.trader.callback.InterestSubjectCB
import com.wds.king.trader.model.InterestSubject
import kotlinx.android.synthetic.main.interested_subject_selection_layout.view.*
import java.util.*
import kotlin.collections.ArrayList

class InterestSubjectAddAdapter(context: Context, listener: InterestSubjectCB, itemList: ArrayList<InterestSubject>)
    : RecyclerView.Adapter<ViewHolder>(), SectionIndexer {

    var itemList: ArrayList<InterestSubject> = itemList
    var selItemList: ArrayList<InterestSubject> = ArrayList()
    var selItemUIList: ArrayList<LinearLayout> = ArrayList()
    private var filteredItemList: ArrayList<InterestSubject> = ArrayList()
    private var sectionPosList: ArrayList<Int> = java.util.ArrayList(26)
    private var listener: InterestSubjectCB = listener
    private val context: Context = context

    private val sections = java.util.ArrayList<String>(26)
    private val sectionList = arrayListOf(
            "가", "나", "다", "라", "마", "바", "사", "아", "자", "카", "차", "타", "파", "하"
            , "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")

    init {
        filteredItemList.addAll(itemList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.interested_subject_add_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {

        val interestSubject = itemList[position]

        if (holder != null) {
            interestSubject.apply {
                holder.itemChecker.isChecked = isChecked
                holder.itemCode.text = code
                holder.itemName.text = name
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getSectionForPosition(position: Int): Int {
        return 0
    }

    override fun getSections(): Array<String> {

        var i = 0
        val size = itemList.size
        while (i < size) {
            val section: String = itemList.get(i).name[0].toUpperCase().toString()
            if (!sections.contains(section) && sectionList.contains(section)) {
                sections.add(section)
                sectionPosList!!.add(i)
            }
            i++
        }
        return sections.toTypedArray()
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return sectionPosList!!.get(sectionIndex)
    }

    fun filter(charText: String) {

        var charText = charText
        charText = charText.toLowerCase(Locale.getDefault())
        itemList.clear()
        if (charText.length == 0) {
            itemList.addAll(filteredItemList)
        } else {
            for (subject in filteredItemList) {
                val name = subject.name
                if (name.toLowerCase().contains(charText)) {
                    itemList.add(subject)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun removeSelItem(tag: Any)
    {
        selItemList.find {
            it.code==tag
        }?.let {
            selItemList.remove(it)
        }

        selItemUIList.filter {
            it.tag==tag
        }.forEach {

            val selLayout = it
            selItemUIList.remove(selLayout)
            this.listener?.onViewClicked(it, false)
        }
    }

    fun initAllItem()
    {
        //itemList?.clear()
        itemList.map {
            it.isChecked = false
        }
        notifyDataSetChanged()

        selItemList?.clear()
        selItemUIList?.clear()
        //filteredItemList?.clear()
        sectionPosList?.clear()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val itemChecker: CheckBox = view.findViewById(R.id.add_inter_sub_item_checker)
        val itemCode: TextView = view.findViewById(R.id.add_inter_sub_item_code)
        val itemName: TextView = view.findViewById(R.id.add_inter_sub_item_name)

        init {
            itemChecker.setOnClickListener(this)
            itemCode.setOnClickListener(this)
            itemName.setOnClickListener(this)
            view.setOnClickListener(this)
        }

        override fun onClick(view: View) {

            var layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var curSelItemUI: LinearLayout = layoutInflater.inflate(R.layout.interested_subject_selection_layout, null) as LinearLayout
            curSelItemUI.add_inter_sub_sel_item_name.text = itemName.text
            curSelItemUI.tag = itemCode.text

            if(view.id!=R.id.add_inter_sub_item_checker) {
                itemChecker.isChecked = !itemChecker.isChecked
            }

            var dup = false
            var selectedItem: LinearLayout = curSelItemUI
            for (item in selItemUIList) {
                if (item.tag==curSelItemUI.tag) {
                    dup = true
                    selectedItem = item
                    break
                }
            }

            if (dup) {
                selItemUIList.remove(selectedItem)
                var curSelItem : InterestSubject? = itemList.find{
                    it.code?.equals(itemCode.text) ?: false
                }

                curSelItem?.let {
                    selItemList.remove(it)
                }
            } else {
                selectedItem.setOnClickListener { v ->

                    removeSelItem(v.tag)
                    itemChecker.isChecked = false

                }

                selItemUIList.add(selectedItem)

                var curSelItem : InterestSubject? = itemList.find{
                    it.code?.equals(itemCode.text) ?: false
                }

                curSelItem?.let {
                    selItemList.add(it)
                }
            }

            this@InterestSubjectAddAdapter.listener?.onViewClicked(selectedItem, !dup)
        }
    }
}

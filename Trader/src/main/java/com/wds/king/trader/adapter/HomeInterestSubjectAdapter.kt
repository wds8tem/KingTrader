package com.wds.king.trader.adapter

import android.content.Context
import android.graphics.Color.parseColor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.TextView
import com.wds.king.trader.R
import com.wds.king.trader.database.MyProfitRepo
import com.wds.king.trader.model.InterestSubject
import com.wds.king.trader.model.MyProfit
import com.wds.king.trader.ui.fragment.myProfitDaoEx
import com.wds.king.trader.ui.util.SubjectComparator.sequenceComparator
import com.yuanta.smartnet.proc.SmartNetRealProc
import com.yuanta.smartnet.proc.SmartNetTranProc
import java.util.*

class HomeInterestSubjectAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), InterestSubject.NotifyListener {

    private val TYPE_HEADER = 0
    private val TYPE_BODY = 1

    val MAX_ITEM = 50
    private var context: Context? = null

    val itemList: ArrayList<InterestSubject> = arrayListOf()
    val editableItemList: ArrayList<InterestSubject> = arrayListOf()
    private var header: MyProfit? = null

    private var onClickListener: OnClickListener? = null

    interface OnClickListener {
        fun onItem(item: InterestSubject?)
        fun onHeader(title: String)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    fun setContext(context : Context)
    {
        this.context = context
    }

    override fun onSingleDataChangedNotify(code: String, groupId: Int, pos: Int, curPrice: String) {
        notifyItemChanged(pos)
    }

    fun clearAllItemList() {
        clearItemList()
        clearEditableItemList()
        notifyDataSetChanged()
    }

    private fun clearItemList() {
        itemList.clear()
    }

    fun clearEditableItemList() {
        editableItemList.clear()
    }

    fun addUnique(pItem: InterestSubject): Boolean {

        itemList.apply {
            var isDup = false
            for (item in this) {
                if (pItem.code == item.code) {
                    isDup = true
                    item.pos = pItem.pos
                }
            }
            if (!isDup) {

                if (pItem.pos == -1) {
                    pItem.pos = size
                }

                add(pItem)
                return true
            }
        }
        return false
    }

    fun sort() {
        itemList.sortWith(sequenceComparator)
        notifyDataSetChanged()
    }

    fun addDataList(context: Context, itemList: ArrayList<InterestSubject>) {

        itemList.map {
            addData(context, it)
        }
        notifyDataSetChanged()
    }

    fun addData(context: Context, item: InterestSubject) {

        if (addUnique(item)) {
            itemList.apply {
                for (item in this) {
                    item.run {
                    tranCurrPrice = SmartNetTranProc(context)
                    realCurrPrice = SmartNetRealProc(context)

                        setOnSingleDataChangedNotify(object : InterestSubject.NotifyListener {
                            override fun onSingleDataChangedNotify(code: String, groupId: Int, pos: Int, curPrice: String) {
                                curPriceChanged = true
                                notifyItemChanged(pos + 1)

                                val myProfitRepo = MyProfitRepo().apply {
                                    this.code = code
                                    this.groupId = groupId
                                    this.curPrice = curPrice.toInt()
                                }

                                myProfitRepo.let {
                                    val runnable = Runnable {
                                        myProfitDaoEx.replace(it.code, it.groupId, it.curPrice)
                                    }
                                    val thread = Thread(runnable)
                                    thread.start()
                                }
                            }
                        })
                        initPrice()
                    }
                }
            }
        }
    }

    fun setHeader(header: MyProfit) {
        this.header = header
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_interested_subject_header, parent, false)
            return HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_interested_subject_item, parent, false)
            return ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is HeaderViewHolder) {

            if (header != null) {
                holder.mHeader = header

                holder.tvMyProfitsRatio.text = header!!.myProfitsRatio
                holder.tvMyProfitsPrice.text = header!!.myProfitsPrice
                holder.tvMyPurchasePrice.text = header!!.myPurchasePrice
                holder.tvMyEstimationPrice.text = header!!.myEstimationPrice
            }
        } else if (holder is ItemViewHolder) {

            if (itemList != null && itemList.size > 0) {

                val pos = position - 1
                val item = itemList[pos]
                holder.run {

                    title.text = item.name
                    interSub = item
                    curPrice.text = item.curPrice

                    val iTodayDiffPrice = if (item.todayDiffPrice != "") item.todayDiffPrice.toInt() else 0

                    if (item.curPriceChanged) {
                        val animation = AlphaAnimation(0f, 1f)
                        animation.duration = 1000
                        when {
                            iTodayDiffPrice > 0 -> curPriceBorder.background = context!!.getDrawable(R.drawable.price_border_red)
                            iTodayDiffPrice < 0 -> curPriceBorder.background = context!!.getDrawable(R.drawable.price_border_blue)
                            else -> curPriceBorder.background = context!!.getDrawable(R.drawable.price_border_black)
                        }

                        curPriceBorder.layoutParams.width = curPrice.paint.measureText(curPrice.text.toString()).toInt()
                        curPriceBorder.animation = animation
                        curPriceBorder.visibility = View.GONE
                        item.curPriceChanged = false
                    }

                    todayDiffPrice.text = item.todayDiffPrice
                    myDiffRatio.text = item.myDiffRatio
                    myDiffPrice.text = item.myDiffPrice
                    tvTodayVolume.text = item.todayVolume
                    todayDiffRatio.text = if (item.todayDiffRatio != "")
                        "%.2f".format(item.todayDiffRatio.toFloat()) + "%" else ""
                    myVolume.text = item.myVolume
                    myAvgPrice.text = item.myAvgPrice


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
                }
            }
        }
    }

    override fun getItemCount() = itemList.size + 1

    override fun getItemViewType(position: Int) = if (position == 0) TYPE_HEADER else TYPE_BODY

    private inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val title: TextView = view.findViewById(R.id.home_inter_sub_title)
        var interSub: InterestSubject? = null
        val curPrice: TextView = view.findViewById(R.id.home_inter_sub_cur_price)
        val curPriceBorder: TextView = view.findViewById(R.id.home_inter_sub_cur_price_border)
        val todayDiffPrice: TextView = view.findViewById(R.id.home_inter_sub_today_diff_price)
        val myDiffRatio: TextView = view.findViewById(R.id.home_inter_sub_my_diff_ratio)
        val myDiffPrice: TextView = view.findViewById(R.id.home_inter_sub_my_diff_price)
        val tvTodayVolume: TextView = view.findViewById(R.id.home_inter_sub_today_volume)
        val todayDiffRatio: TextView = view.findViewById(R.id.home_inter_sub_today_diff_ratio)
        val myVolume: TextView = view.findViewById(R.id.home_inter_sub_my_volume)
        val myAvgPrice: TextView = view.findViewById(R.id.home_inter_sub_my_avg_price)
        val todayDiffRatioShape: TextView = view.findViewById(R.id.home_inter_sub_diff_ratio_shape)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onClickListener?.onItem(interSub)
        }
    }

    private inner class HeaderViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {

        val tvMyProfitsRatio: TextView = mView.findViewById(R.id.home_my_profits_ratio)
        val tvMyProfitsPrice: TextView = mView.findViewById(R.id.home_my_profits_price)
        val tvMyPurchasePrice: TextView = mView.findViewById(R.id.home_my_purchase_price)
        val tvMyEstimationPrice: TextView = mView.findViewById(R.id.home_my_estimation_price)
        var mHeader: MyProfit? = null
    }
}

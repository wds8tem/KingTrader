package com.wds.king.trader.ui.spinner

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.wds.king.trader.ui.util.Util
import java.util.*

class AccountSelectSpinner(context: Context) : Spinner(context), AdapterView.OnItemSelectedListener {

    private var arrayAccounts: ArrayList<AccInfo> = ArrayList()
    private val accAdapter: AccountListAdapter = AccountListAdapter()
    private var curIndex = -1
    private var listener: OnAccountSelectedListener? = null

    var accountNo: String?
        get() {
            if (arrayAccounts == null) return ""
            return if (curIndex < 0 || curIndex >= arrayAccounts!!.size) "" else arrayAccounts!![curIndex].strAccNo

        }
        set(strNo) {
            if (arrayAccounts == null) return

            for (i in arrayAccounts!!.indices) {
                if (arrayAccounts!![i].strAccNo == strNo) {
                    curIndex = i
                    setSelection(i)
                    return
                }
            }
        }

    init {
        onItemSelectedListener = this
        prompt = "계좌 선택"
        adapter = accAdapter
    }

    fun clearInstance() {
        arrayAccounts.clear()
    }

    fun setOnAccountSelectedListener(listener: OnAccountSelectedListener) {
        this.listener = listener
    }

    fun resetAccountList() {
        arrayAccounts.clear()
        accAdapter.notifyDataSetChanged()
    }

    fun addAccountList(strAccNo: String, strAccName: String) {
        val infoAcc = AccInfo()

        infoAcc.strAccNo = strAccNo
        infoAcc.strAccName = strAccName

        arrayAccounts.add(infoAcc)

        accAdapter.notifyDataSetChanged()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {

        if (arrayAccounts != null) {
            if (pos >= 0 && pos < arrayAccounts.size) {
                curIndex = pos

                if (listener != null)
                    listener!!.onAccountSelected(arrayAccounts!![pos].strAccNo)
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    interface OnAccountSelectedListener {
        fun onAccountSelected(strNo: String?)
    }

    inner class AccInfo : Any() {
        var strAccNo: String? = null
        var strAccName: String? = null
        val accNoText: String
            get() {
                if (strAccNo!!.isEmpty()) return ""

                return if (strAccNo!!.length == 12)
                    strAccNo!!.substring(0, 4) + "-" + strAccNo!!.substring(4, 8) + "-" + strAccNo!!.substring(8)
                else
                    strAccNo!!.substring(0, 3) + "-" + strAccNo!!.substring(3, 5) + "-" + strAccNo!!.substring(5)
            }
    }

    private inner class AccountListAdapter : BaseAdapter(), SpinnerAdapter {

        override fun getItem(pos: Int): Any? {
            return if (pos < 0 || pos >= arrayAccounts.size) null else arrayAccounts[pos]
        }

        override fun getItemId(arg0: Int): Long {
            return arg0.toLong()
        }

        override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {

            var viewItem: TextView? = convertView as? TextView

            if (viewItem == null) {
                viewItem = TextView(parent.context).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                    setTextColor(Color.DKGRAY)
                    setSingleLine()
                    setPadding(20, 0, 0, 20)
                    gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                }
            }

            val obj = getItem(pos)
            if (obj != null && obj is AccInfo) {

                viewItem.text = obj.accNoText
            } else {
                viewItem.text = ""
            }

            return viewItem
        }

        override fun isEmpty(): Boolean {
            return arrayAccounts.size <= 0
        }

        override fun getDropDownView(pos: Int, convertView: View?, viewParent: ViewGroup): View {

            var viewItem: TextView? = convertView as? TextView

            if (viewItem == null) {
                viewItem = TextView(viewParent.context).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                    setTextColor(Color.DKGRAY)
                    setSingleLine()
                    setPadding(20, 20, 20, 20)
                    gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                }

                viewItem.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Util.dipToPixels(viewParent.context, 40f).toInt())
            }

            val obj = getItem(pos)
            if (obj != null && obj is AccInfo) {

                viewItem.text = obj.accNoText
            } else {
                viewItem.text = ""
            }

            return viewItem
        }

        override fun getCount(): Int {
            return arrayAccounts.size

        }
    }
}

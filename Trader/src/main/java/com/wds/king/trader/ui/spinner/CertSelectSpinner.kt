package com.wds.king.trader.ui.spinner

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.lumensoft.ks.KSCertificate
import java.util.*

class CertSelectSpinner : Spinner, AdapterView.OnItemSelectedListener {

    private var arrayCert: ArrayList<KSCertificate>? = null
    private var mAdapter: CertListAdapter = CertListAdapter()
    private var currIndex = -1
    private var listener: OnCertSelectedListener? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var certDN: String
        get() {
            if (arrayCert == null) return ""
            return if (currIndex < 0 || currIndex >= arrayCert!!.size) "" else arrayCert!![currIndex].subjectDn

        }
        set(strDN) {
            if (arrayCert == null) return

            for (i in arrayCert!!.indices) {
                if (arrayCert!![i].subjectDn == strDN) {
                    currIndex = i
                    setSelection(i)
                    return
                }
            }
        }

    init {
        onItemSelectedListener = this
        prompt = "인증서 선택"
    }


    fun setOnCertSelectedListener(listener: OnCertSelectedListener) {
        this.listener = listener
    }

    fun setCertList(array: ArrayList<KSCertificate>) {
        arrayCert = array
        adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    fun clearInstance() {
        if (arrayCert != null) {
            arrayCert!!.clear()
            arrayCert = null
        }
    }

    override fun setEnabled(isEnabled: Boolean) {
        super.setEnabled(isEnabled)

        mAdapter.notifyDataSetChanged()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (arrayCert != null) {
            if (pos >= 0 && pos < arrayCert!!.size) {
                currIndex = pos

                if (listener != null)
                    listener!!.onCertSelected(arrayCert!![pos].subjectDn)
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    interface OnCertSelectedListener {
        fun onCertSelected(strDN: String)
    }

    private inner class CertListAdapter : BaseAdapter(), SpinnerAdapter {

        override fun getItem(arg0: Int): Any? {
            if (arrayCert == null) return null
            return if (arg0 < 0 || arg0 >= arrayCert!!.size) null else arrayCert!![arg0]

        }

        override fun getItemId(arg0: Int): Long {
            return arg0.toLong()
        }

        override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {

            var viewItem: TextView? = convertView as? TextView

            if (viewItem == null) {
                viewItem = TextView(parent.context)
                viewItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                viewItem.setTextColor(if (this@CertSelectSpinner.isEnabled) Color.BLACK else Color.LTGRAY)
                viewItem.setSingleLine()
            }

            val obj = getItem(pos)
            if (obj != null && obj is KSCertificate) {
                viewItem.text = obj.subjectDn
            } else {
                viewItem.text = ""
            }

            return viewItem
        }

        override fun isEmpty(): Boolean {
            return arrayCert == null || arrayCert!!.size < 0
        }

        override fun getDropDownView(pos: Int, convertView: View?, viewParent: ViewGroup): View {

            var viewItem: TextView? = convertView as TextView?

            if (viewItem == null) {
                viewItem = TextView(viewParent.context)
                viewItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                viewItem.setPadding(20, 20, 20, 20)
                viewItem.setTextColor(if (this@CertSelectSpinner.isEnabled) Color.BLACK else Color.LTGRAY)
            }

            val obj = getItem(pos)
            if (obj != null && obj is KSCertificate) {
                viewItem.text = obj.subjectDn
            } else {
                viewItem.text = ""
            }

            return viewItem
        }

        override fun getCount(): Int {
            return if (arrayCert == null) 0 else arrayCert!!.size
        }
    }
}

package com.wds.king.trader.ui.util

import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import java.text.DecimalFormat

object Util {
    var m_formatIntValue = DecimalFormat("###,###,##0")
    var m_formatF2Value = DecimalFormat("##0.00")
    var m_formatF4Value = DecimalFormat("##0.0000")

    fun dipToPixels(context: Context, dipValue: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics)
    }

    fun getIntValue(strValue: String?): Int {
        var strValue: String? = strValue ?: return 0
        strValue = strValue!!.trim { it <= ' ' }
        if (strValue.isEmpty()) return 0

        try {
            return Integer.parseInt(strValue)
        } catch (exNF: NumberFormatException) {

        }

        return 0
    }

    fun getFloatValue(strValue: String?): Float {
        var strValue: String? = strValue ?: return 0f
        strValue = strValue!!.trim { it <= ' ' }
        if (strValue.isEmpty()) return 0f

        try {
            return java.lang.Float.parseFloat(strValue)
        } catch (exNF: NumberFormatException) {

        }

        return 0f
    }

    fun setIntValue(view: TextView, strValue: String, strPrefix: String, strPostfix: String) {
        setIntValue(view, strValue, 0, strPrefix, strPostfix)
    }

    @JvmOverloads
    fun setIntValue(view: TextView, strValue: String, nAttr: Int = 0, strPrefix: String = "", strPostfix: String = "") {
        val nValue = getIntValue(strValue)

        view.text = strPrefix + m_formatIntValue.format(nValue.toLong()) + strPostfix
        view.setTextColor(getValueColor(nAttr))
    }

    @JvmOverloads
    fun setIntValue(view: TextView, strValue: String, strValue2: String, nAttr: Int = 0, strPrefix: String = "", strPostfix: String = "") {
        val nValue = getIntValue(strValue)
        val nValue2 = getIntValue(strValue2)

        val temp = (1 - (nValue.toFloat()/nValue2))*100
        val ratio = (Math.round(temp*100).toFloat()/100)

        view.setLines(2)
        view.setSingleLine(false)
        view.text = strPrefix + m_formatIntValue.format(nValue.toLong()) + strPostfix + "\n" + "${ratio}%"
        view.setTextColor(getValueColor(nAttr))
    }

    @JvmOverloads
    fun setF2Value(view: TextView, strValue: String, nAttr: Int, strPrefix: String = "", strPostfix: String = "") {
        val fValue = Math.round(getFloatValue(strValue) * 100).toFloat() / 100

        view.text = strPrefix + m_formatF2Value.format(fValue.toDouble()) + strPostfix
        view.setTextColor(getValueColor(nAttr))
    }

    //소수점 4자리
    fun setF4Value(view: TextView, strValue: String) {
        val fValue = Math.round(getFloatValue(strValue) * 10000).toFloat() / 10000

        view.text = m_formatF4Value.format(fValue.toDouble())
    }

    @JvmOverloads
    fun setF4Value(view: TextView, strValue: String, nAttr: Int, strPrefix: String = "", strPostfix: String = "") {
        val fValue = Math.round(getFloatValue(strValue) * 10000).toFloat() / 10000

        view.text = strPrefix + m_formatF4Value.format(fValue.toDouble()) + strPostfix
        view.setTextColor(getValueColor(nAttr))
    }

    fun setSignValue(view: TextView, nAttr: Int) {
        view.text = getValueSign(nAttr)
        view.setTextColor(getValueColor(nAttr))
    }

    fun getValueColor(nAttr: Int): Int {
        when (nAttr) {
            0x01 -> return ColorUtil.RISE    // 상한
            0x02 -> return ColorUtil.RISE    // 상승
            0x03 -> return ColorUtil.STAY    // 보합
            0x04 -> return ColorUtil.FALL        // 하한
            0x05 -> return ColorUtil.FALL    // 하락

        // 유안타에 있는 속성인지 모름, 기존 소스가 이상함
            0x06 -> return ColorUtil.RISE    // 기세상한
            0x07 -> return ColorUtil.RISE    // 기세상승
            0x09 -> return ColorUtil.FALL    // 기세하한
            0x0A -> return ColorUtil.FALL    // 기세하락
        }

        return ColorUtil.STAY
    }

    fun getValueSign(nAttr: Int): String {
        when (nAttr) {
            0x01 -> return "↑"    // 상한
            0x02 -> return "▲"    // 상승
            0x03 -> return ""    // 보합
            0x04 -> return "↓"    // 하한
            0x05 -> return "▼"    // 하락

        // 유안타에 있는 속성인지 모름, 기존 소스가 이상함
            0x06 -> return "↑↑"    // 기세상한
            0x07 -> return "▲▲"    // 기세상승
            0x09 -> return "↓↓"    // 기세하한
            0x0A -> return "▼▼"    // 기세하락
        }

        return ""
    }
}//소수점 4자리

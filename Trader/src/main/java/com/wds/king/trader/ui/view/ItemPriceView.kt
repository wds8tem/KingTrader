package com.wds.king.trader.ui.view

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.wds.king.trader.MainActivity
import com.wds.king.trader.R
import com.wds.king.trader.model.InterestSubject
import com.wds.king.trader.ui.util.Util

class ItemPriceView(context: Context, interSub: InterestSubject?) : LinearLayout(context) {

    private val myRatioOfReturn: TextView        // 현재가
    private val viewPrice: TextView        // 현재가
    private val viewSign: TextView            // 등락기호
    private val viewDiff: TextView            // 등락
    private val viewDiffRatio: TextView    // 등락율

    private val viewUpjong: TextView        // 업종
    private val viewTradeAmount: TextView    // 거래량
    private val viewTradeMoney: TextView    // 거래대금

    init {

        orientation = LinearLayout.VERTICAL

        val layoutLine1 = LinearLayout(context)
        layoutLine1.orientation = LinearLayout.HORIZONTAL

        myRatioOfReturn = makeTextView(context, 20).apply {
            text = context.getString(R.string.my_ratio_of_return_note)
            setOnClickListener { v ->
                (context as MainActivity).showMyReturnView(interSub)
            }
        }

        viewPrice = makeTextView(context, 20)
        viewSign = makeTextView(context, 12)
        viewDiff = makeTextView(context, 14)
        viewDiffRatio = makeTextView(context, 14)

        layoutLine1.addView(myRatioOfReturn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 20f))
        layoutLine1.addView(viewPrice, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 35f))
        layoutLine1.addView(viewSign, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 10f))
        layoutLine1.addView(viewDiff, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 15f))
        layoutLine1.addView(viewDiffRatio, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 20f))

        viewUpjong = makeTextView(context, 12)
        viewUpjong.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL

        viewTradeAmount = makeTextView(context, 12)
        viewTradeMoney = makeTextView(context, 12)

        val layoutLine2 = LinearLayout(context)
        layoutLine2.orientation = LinearLayout.HORIZONTAL

        layoutLine2.addView(viewUpjong, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 2f))
        layoutLine2.addView(viewTradeAmount, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3f))
        layoutLine2.addView(viewTradeMoney, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3f))

        addView(layoutLine1, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 30f))
        addView(layoutLine2, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 20f))
    }

    // 종목 현재가 정보 ( 현재가, 등락, 등락률)
    fun setItemPrice(strPrice: String, nPriceAttr: Int, strDiff: String, nDiffAttr: Int, strDiffRatio: String) {
        Util.setIntValue(viewPrice, strPrice, nPriceAttr)
        Util.setSignValue(viewSign, nPriceAttr)
        Util.setIntValue(viewDiff, strDiff, nDiffAttr)
        Util.setF2Value(viewDiffRatio, strDiffRatio, nDiffAttr, "", " %")
    }

    // 종목 거래 정보( 거래량, 거래대금)
    fun setTradeInfo(strTradeAmount: String, strTradeMoney: String) {
        Util.setIntValue(viewTradeAmount, strTradeAmount, "", " 주")
        Util.setIntValue(viewTradeMoney, strTradeMoney, "", " ")
    }

    // 종목 정보 ( 업종 )
    fun setItemInfo(strUpjong: String) {
        viewUpjong.text = strUpjong
    }

    private fun makeTextView(context: Context, nTextDip: Int): TextView {
        val viewText = TextView(context)
        viewText.setTextColor(Color.BLACK)
        viewText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, nTextDip.toFloat())
        viewText.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
        viewText.setSingleLine()

        return viewText
    }
}

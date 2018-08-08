package com.wds.king.trader.ui.view

import android.content.Context
import android.graphics.Color
import android.support.design.widget.TabLayout
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wds.king.trader.R
import com.wds.king.trader.R.drawable.minus
import com.wds.king.trader.R.drawable.plus
import com.wds.king.trader.disposables
import com.wds.king.trader.model.InterestSubject
import com.wds.king.trader.ui.spinner.AccountSelectSpinner
import com.wds.king.trader.ui.util.Util
import com.yuanta.smartnet.intrf.IRealDataListener
import com.yuanta.smartnet.intrf.ITranDataListener
import com.yuanta.smartnet.proc.SmartNetMng
import com.yuanta.smartnet.proc.SmartNetRealProc
import com.yuanta.smartnet.proc.SmartNetTranProc

class DataView(context: Context, interSub : InterestSubject?) :
        BaseView(context), ITranDataListener, IRealDataListener, View.OnClickListener {

    private val interSub = interSub

    private var viewItemPrice: ItemPriceView? = null                    // 종목 현재가 정보
    private var viewItemHoga: ItemHogaView? = null                        // 종목 호가 (5호가)

    private var spinnerAccount: AccountSelectSpinner? = null                // 계좌 선택
    private var editAccountPwd: EditText? = null                // 계좌 비번

    private var editTradeUnitPrice: EditText? = null                // 거래단가
    private var editTradeNo: EditText? = null                        // 주문번호
    private var totalOrderPriceNo: TextView? = null                        // 주문번호

    private var tranCurrPrice: SmartNetTranProc? = null                // 주식 현재가 조회
    private var tranCurrHoga: SmartNetTranProc? = null                // 주식 호가 조회

    private var realCurrPrice: SmartNetRealProc? = null                // 주식 체결 실시간
    private var realCurrHoga: SmartNetRealProc? = null                // 주식 호가 실시간

    private var tranStockBuy: SmartNetTranProc? = null                // 주식 매수 주문
    private var tranStockSell: SmartNetTranProc? = null                // 주식 매도 주문
    private var tranStockModify: SmartNetTranProc? = null            // 정정 주문
    private var tranStockCancel: SmartNetTranProc? = null            // 취소 주문

    private var orderCountEditValue: EditText? = null        // 거래수량
    private var orderPriceEditValue: EditText? = null

    private var orderCount = 0
    private var orderPrice = 0

    private var strPrice = "0"

    private var actMain: Context? = null

    private var TITLE_WIDTH = 100
    private var INPUT_WIDTH = 200
    private var PADDING_H = 10
    private var PADDING_V = 10

    interface ID {
        companion object {
            val CTRL_ID_BUY = 100
            val CTRL_ID_SELL = 101
            val CTRL_ID_MODIFY = 102
            val CTRL_ID_CANCEL = 103

            val CTRL_ID_ORDER_PRICE_PLUS = 104
            val CTRL_ID_ORDER_PRICE_MINUS = 105
            val CTRL_ID_ORDER_COUNT_PLUS = 106
            val CTRL_ID_ORDER_COUNT_MINUS = 107
        }
    }

    init {

        setBackgroundColor(Color.WHITE)

        TITLE_WIDTH = Util.dipToPixels(context, 80f).toInt()
        INPUT_WIDTH = Util.dipToPixels(context, 200f).toInt()
        PADDING_H = Util.dipToPixels(context, 10f).toInt()
        PADDING_V = Util.dipToPixels(context, 10f).toInt()

        tranCurrPrice = SmartNetTranProc(context)
        tranCurrPrice!!.initInstance(this)
        tranCurrPrice!!.setQueryFile("DSO300001")
        tranCurrPrice!!.tranId = context.getString(R.string.stock_price_tran)

        tranCurrHoga = SmartNetTranProc(context)
        tranCurrHoga!!.initInstance(this)
        tranCurrHoga!!.setQueryFile("DSO300002")
        tranCurrHoga!!.tranId = context.getString(R.string.stock_hoga_tran)

        tranStockBuy = SmartNetTranProc(context)
        tranStockBuy!!.initInstance(this)
        tranStockBuy!!.setQueryFile("DSO100001")
        tranStockBuy!!.tranId = context.getString(R.string.stock_buy_request)
        tranStockBuy!!.setCertType(2)

        tranStockSell = SmartNetTranProc(context)
        tranStockSell!!.initInstance(this)
        tranStockSell!!.setQueryFile("DSO100002")
        tranStockSell!!.tranId = context.getString(R.string.stock_sell_request)
        tranStockSell!!.setCertType(2)

        tranStockModify = SmartNetTranProc(context)
        tranStockModify!!.initInstance(this)
        tranStockModify!!.setQueryFile("DSO100003")
        tranStockModify!!.tranId = context.getString(R.string.stock_modify_request)
        tranStockModify!!.setCertType(2)

        tranStockCancel = SmartNetTranProc(context)
        tranStockCancel!!.initInstance(this)
        tranStockCancel!!.setQueryFile("DSO100004")
        tranStockCancel!!.tranId = context.getString(R.string.stock_cancel_request)
        tranStockCancel!!.setCertType(2)

        realCurrPrice = SmartNetRealProc(context)
        realCurrPrice!!.initInstance(this)
        realCurrPrice!!.setQueryFile("AUTO11")
        realCurrPrice!!.realId = context.getString(R.string.stock_price_real)

        realCurrHoga = SmartNetRealProc(context)
        realCurrHoga!!.initInstance(this)
        realCurrHoga!!.setQueryFile("AUTO12")
        realCurrHoga!!.realId = context.getString(R.string.stock_hoga_real)

        requestCurrPrice()
    }

    fun initView(actMain: Context) {

        this.actMain = actMain
        this.orientation = LinearLayout.VERTICAL

        val viewScroll = ScrollView(context)

        val hogaLayoutFrame = LinearLayout(context)
        hogaLayoutFrame.orientation = LinearLayout.VERTICAL

        viewItemPrice = ItemPriceView(context, interSub)
        viewItemPrice!!.setPadding(PADDING_H, 0, PADDING_H, 0)


        viewItemHoga = ItemHogaView(context)
        viewItemHoga!!.setOnHogaViewListener(object : ItemHogaView.OnHogaViewListener {
            override fun onHogaClicked(strPrice: String) {
                if (editTradeUnitPrice != null)
                    editTradeUnitPrice!!.setText(strPrice)
            }
        })

        hogaLayoutFrame.addView(viewItemHoga, LinearLayout.LayoutParams(Util.dipToPixels(context, (180).toFloat()).toInt(),
                Util.dipToPixels(context, (70 * 10).toFloat()).toInt()))


        val layoutAccount = makeAccountInput()

        val layoutTrade = makeTradeInput()

        viewScroll.addView(hogaLayoutFrame, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        val mainLayoutFrame = LinearLayout(context)
        mainLayoutFrame.orientation = LinearLayout.HORIZONTAL
        mainLayoutFrame.addView(viewScroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        val tradeLayoutFrame = LinearLayout(context)
        tradeLayoutFrame.orientation = LinearLayout.VERTICAL
        tradeLayoutFrame.addView(layoutAccount, LinearLayout.LayoutParams(Util.dipToPixels(context, (240).toFloat()).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT))
        tradeLayoutFrame.addView(layoutTrade, LinearLayout.LayoutParams(Util.dipToPixels(context, (240).toFloat()).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT))
        mainLayoutFrame.addView(tradeLayoutFrame, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        addView(viewItemPrice, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Util.dipToPixels(context, 70f).toInt()))
        addView(mainLayoutFrame, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

    }

    fun releaseView() {

        removeAllViews()

        tranCurrPrice?.apply {
            clearInstance()
            tranCurrPrice = null
        }

        tranCurrHoga?.apply {
            clearInstance()
            tranCurrHoga = null
        }

        tranStockBuy?.apply {
            clearInstance()
            tranStockBuy = null
        }

        tranStockBuy?.apply {
            clearInstance()
            tranStockBuy = null
        }

        tranStockSell?.apply {
            clearInstance()
            tranStockSell = null
        }

        tranStockModify?.apply {
            clearInstance()
            tranStockModify = null
        }

        tranStockCancel?.apply {
            clearInstance()
            tranStockCancel = null
        }

        realCurrPrice?.apply {
            clearInstance()
            realCurrPrice = null
        }

        realCurrHoga?.apply {
            clearInstance()
            realCurrHoga = null
        }
    }

    private fun makeAccountInput(): LinearLayout {

        val textCode = makeTextView(context.getString(R.string.stock_my_account), 16)
        val textCodeLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        textCodeLayoutParams.setMargins(20, 20, 0, 0)
        textCode.layoutParams = textCodeLayoutParams

        spinnerAccount = AccountSelectSpinner(context)
        val spinnerAccountLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        spinnerAccountLayoutParams.setMargins(20, 20, 0, 0)
        spinnerAccount!!.layoutParams = spinnerAccountLayoutParams

        spinnerAccount!!.resetAccountList()

        val mngNet = SmartNetMng.getInstance()

        val nAccCount = mngNet.accountSize

        for (i in 0 until nAccCount) {

            val tempAcnt = mngNet.getAccountInfo(mngNet.getAccountNo(i), "productcode")

            for (str in stockAcctType) {
                if (tempAcnt.contains(str)) {
                    spinnerAccount!!.addAccountList(mngNet.getAccountNo(i), mngNet.getAccountName(i))
                }
            }
        }

        spinnerAccount!!.setOnAccountSelectedListener(object : AccountSelectSpinner.OnAccountSelectedListener {

            override fun onAccountSelected(strNo: String?) {
                if (editAccountPwd != null) {
                    editAccountPwd!!.setText("")
                    editAccountPwd!!.requestFocus()
                }
            }
        })

        editAccountPwd = makeEditView("",Gravity.LEFT)
        setEditText(editAccountPwd!!, false, true)
        editAccountPwd!!.setOnEditorActionListener(TextView.OnEditorActionListener { view, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                SmartNetMng.getInstance().checkAccPwd(spinnerAccount!!.accountNo, editAccountPwd!!.text.toString()
                ) { isSuccess, strAccNo, strMessage ->
                    if (isSuccess) {
                        Toast.makeText(actMain, context.getString(R.string.stock_account_check_complete), Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(actMain, strMessage, Toast.LENGTH_LONG).show()
                        editAccountPwd!!.setText("")
                    }
                }

                return@OnEditorActionListener true
            }

            false
        })

        val layoutAccount = LinearLayout(context)
        layoutAccount.orientation = LinearLayout.VERTICAL

        layoutAccount.addView(textCode, LinearLayout.LayoutParams(TITLE_WIDTH, FrameLayout.LayoutParams.MATCH_PARENT))
        layoutAccount.addView(spinnerAccount, LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT, 5f))
        layoutAccount.addView(editAccountPwd, LinearLayout.LayoutParams(Util.dipToPixels(context, (120).toFloat()).toInt(), FrameLayout.LayoutParams.MATCH_PARENT, 2f))

        layoutAccount.setPadding(PADDING_H, PADDING_V, PADDING_H, PADDING_V)

        return layoutAccount
    }

    private fun makeTradeInput(): LinearLayout {

        val orderCountPlusButton = Button(context)
        orderCountPlusButton.background = context.getDrawable(plus)
        orderCountPlusButton.id = ID.CTRL_ID_ORDER_COUNT_PLUS
        orderCountPlusButton.gravity = Gravity.CENTER_VERTICAL
        orderCountPlusButton.setOnClickListener(this)

        orderCountEditValue = EditText(context).apply {
            setRawInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
            gravity = Gravity.CENTER
            disposables?.add(RxTextView.afterTextChangeEvents(this).subscribe {
                val data = it.editable().toString()
                if(!data.isEmpty()) {
                    orderCount = data.toInt()
                    val totalPrice = orderPrice * orderCount
                    totalOrderPriceNo?.text = "$totalPrice 원"
                }
            })
        }

        val orderCountMinusButton = Button(context)
        orderCountMinusButton.id = ID.CTRL_ID_ORDER_COUNT_MINUS
        orderCountMinusButton.background = context.getDrawable(minus)
        orderCountMinusButton.gravity = Gravity.CENTER_VERTICAL
        orderCountMinusButton.setOnClickListener(this)

        val layoutOrderCountPickerLayout = LinearLayout(context)
        layoutOrderCountPickerLayout.orientation = HORIZONTAL

        layoutOrderCountPickerLayout.addView(orderCountPlusButton, 75, 75)
        layoutOrderCountPickerLayout.addView(orderCountEditValue, LayoutParams(300, 150))
        layoutOrderCountPickerLayout.addView(orderCountMinusButton, 75, 75)
        layoutOrderCountPickerLayout.gravity = Gravity.CENTER
        layoutOrderCountPickerLayout.setPadding(PADDING_H, PADDING_V, PADDING_H, PADDING_V)

        val orderPricePlusButton = Button(context)
        orderPricePlusButton.id = ID.CTRL_ID_ORDER_PRICE_PLUS
        orderPricePlusButton.background = context.getDrawable(plus)
        orderPricePlusButton.setOnClickListener(this)

        orderPriceEditValue = EditText(context).apply {
            setRawInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
            gravity = Gravity.CENTER
            disposables?.add(RxTextView.afterTextChangeEvents(this).subscribe {

                val data = it.editable().toString()
                if(!data.isEmpty()) {
                    orderPrice = data.toInt()
                    val totalPrice = orderPrice * orderCount
                    totalOrderPriceNo?.text = "$totalPrice 원"
                }
            })
        }

        val orderPriceMinusButton = Button(context)
        orderPriceMinusButton.id = ID.CTRL_ID_ORDER_PRICE_MINUS
        orderPriceMinusButton.background = context.getDrawable(minus)
        orderPriceMinusButton.setOnClickListener(this)

        val layoutOrderPricePickerLayout = LinearLayout(context)
        layoutOrderPricePickerLayout.orientation = HORIZONTAL

        layoutOrderPricePickerLayout.addView(orderPricePlusButton, 75, 75)
        layoutOrderPricePickerLayout.addView(orderPriceEditValue, LayoutParams(300, 150))
        layoutOrderPricePickerLayout.addView(orderPriceMinusButton, 75, 75)
        layoutOrderPricePickerLayout.gravity = Gravity.CENTER
        layoutOrderPricePickerLayout.setPadding(PADDING_H, PADDING_V, PADDING_H, PADDING_V)

        val totalOrderPrice = makeTextView(context.getString(R.string.stock_order_total_price), 16)
        totalOrderPriceNo = makeEditView("",Gravity.RIGHT)
        totalOrderPriceNo!!.inputType = InputType.TYPE_CLASS_NUMBER

        val textNo = makeTextView(context.getString(R.string.stock_order_number), 16)

        editTradeNo = makeEditView("",Gravity.RIGHT)
        editTradeNo!!.inputType = InputType.TYPE_CLASS_NUMBER

        val buttonBuy = makeCtrlButton(ID.CTRL_ID_BUY, context.getString(R.string.stock_order_buy))
        buttonBuy.setOnClickListener(this)

        val buttonSell = makeCtrlButton(ID.CTRL_ID_SELL, context.getString(R.string.stock_order_sell))
        buttonSell.setOnClickListener(this)

        val buttonModify = makeCtrlButton(ID.CTRL_ID_MODIFY, context.getString(R.string.stock_order_modify))
        buttonModify.setOnClickListener(this)

        val buttonCancel = makeCtrlButton(ID.CTRL_ID_CANCEL, context.getString(R.string.stock_order_cancel))
        buttonCancel.setOnClickListener(this)

        val buttonLayoutFrame = LinearLayout(context)
        buttonLayoutFrame.orientation = VERTICAL
        buttonLayoutFrame.setPadding(0, PADDING_V, 0, 0)

        val layoutLine2 = LinearLayout(context)
        layoutLine2.orientation = HORIZONTAL
        layoutLine2.setPadding(0, PADDING_V, 0, 0)

        val tabLayout = makeTabLayout()

        tabLayout.let {

            it.addTab(it.newTab().setText(context.getString(R.string.stock_order_buy)))
            it.addTab(it.newTab().setText(context.getString(R.string.stock_order_sell)))
            it.addTab(it.newTab().setText(context.getString(R.string.stock_order_modify) + "/" + context.getString(R.string.stock_order_cancel)))

            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabSelected(tab: TabLayout.Tab?) {

                    orderPrice = 0
                    orderCount = 0
                    orderPriceEditValue?.setText("")
                    orderCountEditValue?.setText("")
                    totalOrderPriceNo?.text = ""

                    tab?.let {
                        when (it.position) {
                            0 -> {
                                buttonBuy.visibility = View.VISIBLE
                                buttonSell.visibility = View.GONE
                                buttonModify.visibility = View.GONE
                                buttonCancel.visibility = View.GONE
                            }
                            1 -> {
                                buttonBuy.visibility = View.GONE
                                buttonSell.visibility = View.VISIBLE
                                buttonModify.visibility = View.GONE
                                buttonCancel.visibility = View.GONE
                            }
                            2 -> {
                                buttonBuy.visibility = View.GONE
                                buttonSell.visibility = View.GONE
                                buttonModify.visibility = View.VISIBLE
                                buttonCancel.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            })
        }

        tabLayout.getTabAt(1)?.select()
        tabLayout.getTabAt(0)?.select()

        buttonLayoutFrame.addView(tabLayout, LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))

        layoutLine2.addView(buttonBuy, LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        layoutLine2.addView(buttonSell, LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        layoutLine2.addView(buttonModify, LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        layoutLine2.addView(buttonCancel, LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))

        val layoutTrade = LinearLayout(context)
        layoutTrade.orientation = VERTICAL

        val layoutLine3 = LinearLayout(context)
        layoutLine3.orientation = HORIZONTAL

        layoutLine3.addView(layoutOrderCountPickerLayout, LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))

        val layoutLine4 = LinearLayout(context)
        layoutLine4.orientation = HORIZONTAL

        layoutLine4.addView(layoutOrderPricePickerLayout, LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))

        val layoutLine5 = LinearLayout(context)
        layoutLine5.orientation = HORIZONTAL

        layoutLine5.addView(totalOrderPrice, LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT))

        val totalOrderPriceNoParam = LayoutParams(
                Util.dipToPixels(context, (120).toFloat()).toInt(), FrameLayout.LayoutParams.MATCH_PARENT).apply {
            setMargins(20, 0, 0, 30)
        }
        layoutLine5.addView(totalOrderPriceNo, totalOrderPriceNoParam)

        val layoutLine6 = LinearLayout(context)
        layoutLine6.orientation = HORIZONTAL

        layoutLine6.addView(textNo, LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT))

        val editTradeNoParam = LayoutParams(
                Util.dipToPixels(context, (120).toFloat()).toInt(), FrameLayout.LayoutParams.MATCH_PARENT).apply {
            setMargins(20, 0, 0, 30)
        }

        layoutLine6.addView(editTradeNo, editTradeNoParam)

        val layout3Param = LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        layout3Param.topMargin = 10
        layout3Param.gravity = Gravity.CENTER

        buttonLayoutFrame.addView(layoutLine3, layout3Param)
        buttonLayoutFrame.addView(layoutLine4, LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        buttonLayoutFrame.addView(layoutLine5, LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        buttonLayoutFrame.addView(layoutLine6, LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        buttonLayoutFrame.addView(layoutLine2, LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))

        layoutTrade.addView(buttonLayoutFrame, LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))

        layoutTrade.setPadding(PADDING_H, PADDING_V, PADDING_H, PADDING_V)

        return layoutTrade
    }

    private fun requestCurrPrice() {

        if (realCurrPrice != null) realCurrPrice!!.cancelRealData()
        if (realCurrHoga != null) realCurrHoga!!.cancelRealData()

        if (tranCurrPrice != null) {
            tranCurrPrice!!.clearInputData()
            tranCurrPrice!!.clearOutputData()

            tranCurrPrice!!.setSingleData(0, 0, interSub!!.marketTypeCode)
            tranCurrPrice!!.setSingleData(0, 1, interSub!!.code)

            tranCurrPrice!!.requestData()
        }

        if (tranCurrHoga != null) {
            tranCurrHoga!!.clearInputData()
            tranCurrHoga!!.clearOutputData()

            tranCurrHoga!!.setSingleData(0, 0, interSub!!.marketTypeCode)
            tranCurrHoga!!.setSingleData(0, 1, interSub!!.code)

            tranCurrHoga!!.requestData()
        }
    }

    private fun requestCurrPriceReal() {
        if (tranCurrPrice == null) return

        val strItemCode = tranCurrPrice!!.getSingleData(0, 1).trim { it <= ' ' }    // 현재가 요청한 종목코드

        realCurrPrice!!.setRealData(0, 0, strItemCode)
        realCurrPrice!!.requestReal()
    }

    private fun requestCurrHogaReal() {
        if (tranCurrHoga == null) return

        val strItemCode = tranCurrHoga!!.getSingleData(0, 1).trim { it <= ' ' }    // 호가 데이터 요청한 종목코드

        realCurrHoga!!.setRealData(0, 0, strItemCode)
        realCurrHoga!!.requestReal()
    }

    private fun requestOrderBuy() {

        if (tranStockBuy == null) return

        val strAccountNo = spinnerAccount!!.accountNo
        val strAccountPwd = SmartNetMng.getInstance().getAccountInfo(strAccountNo, "pwd")
        val strAccountId = SmartNetMng.getInstance().getAccountInfo(strAccountNo, "accountid")
        val strAccountProductCode = SmartNetMng.getInstance().getAccountInfo(strAccountNo, "productcode")

        val strOrderCount = orderCountEditValue!!.text.toString()
        val strOrderPrice = orderPriceEditValue!!.text.toString()

        tranStockBuy!!.clearInputData()
        tranStockBuy!!.clearOutputData()

        tranStockBuy!!.setSingleData(0, 0, strAccountId)        // 계좌식별번호
        tranStockBuy!!.setSingleData(0, 1, strAccountProductCode)    // 계좌상품구분코드
        tranStockBuy!!.setSingleData(0, 2, strAccountPwd)        // 계좌비번
        tranStockBuy!!.setSingleData(0, 3, "01")                // 매매구분
        // 01:보통, 05:시장가, 06:조건부지정가, 12:최유리지정가, 13:최우선지정가, 71:시간외종가, 81:시간외단일가

        tranStockBuy!!.setSingleData(0, 4, interSub!!.code)        // 종목코드
        tranStockBuy!!.setSingleData(0, 5, interSub!!.marketTypeCode)        // 장내장외구분 0:코스피, 1:코스닥, 2:else
        tranStockBuy!!.setSingleData(0, 6, strOrderCount)        // 주문수량
        tranStockBuy!!.setSingleData(0, 7, strOrderPrice)        // 주문가격
        tranStockBuy!!.setSingleData(0, 8, "00")                // 주문조건 00:일반, 01:IOC, 02:FOK
        tranStockBuy!!.setSingleData(0, 9, "0")                // 시스템트레이딩 0:일반, 1:system trading
        tranStockBuy!!.setSingleData(0, 10, "000000")            // 주문계획번호 000000 : default

        tranStockBuy!!.requestData()
    }

    private fun requestOrderSell() {

        if (tranStockSell == null) return

        val strAccountNo = spinnerAccount!!.accountNo
        val strAccountPwd = SmartNetMng.getInstance().getAccountInfo(strAccountNo, "pwd")
        val strAccountId = SmartNetMng.getInstance().getAccountInfo(strAccountNo, "accountid")
        val strAccountProductCode = SmartNetMng.getInstance().getAccountInfo(strAccountNo, "productcode")

        val strOrderCount = orderCountEditValue!!.text.toString()
        val strOrderPrice = orderPriceEditValue!!.text.toString()

        tranStockSell!!.clearInputData()
        tranStockSell!!.clearOutputData()

        tranStockSell!!.setSingleData(0, 0, strAccountId)        // 계좌식별번호
        tranStockSell!!.setSingleData(0, 1, strAccountProductCode)    // 계좌상품구분코드
        tranStockSell!!.setSingleData(0, 2, strAccountPwd)        // 계좌비번
        tranStockSell!!.setSingleData(0, 3, "01")                // 매매구분
        // 01:보통, 05:시장가, 06:조건부지정가, 12:최유리지정가, 13:최우선지정가, 71:시간외종가, 81:시간외단일가

        tranStockSell!!.setSingleData(0, 4, interSub!!.code)        // 종목코드
        tranStockSell!!.setSingleData(0, 5, interSub!!.marketTypeCode)        // 장내장외구분 0:코스피, 1:코스닥, 2:else
        tranStockSell!!.setSingleData(0, 6, strOrderCount)        // 주문수량
        tranStockSell!!.setSingleData(0, 7, strOrderPrice)        // 주문가격
        tranStockSell!!.setSingleData(0, 8, "00")                // 주문조건 00:일반, 01:IOC, 02:FOK
        tranStockSell!!.setSingleData(0, 9, "0")                // 시스템트레이딩 0:일반, 1:system trading
        tranStockSell!!.setSingleData(0, 10, "000000")            // 주문계획번호 000000 : default

        tranStockSell!!.requestData()
    }

    private fun requestOrderModify() {

        if (tranStockModify == null) return

        val strAccountNo = spinnerAccount!!.accountNo
        val strAccountPwd = SmartNetMng.getInstance().getAccountInfo(strAccountNo, "pwd")
        val strAccountId = SmartNetMng.getInstance().getAccountInfo(strAccountNo, "accountid")
        val strAccountProductCode = SmartNetMng.getInstance().getAccountInfo(strAccountNo, "productcode")

        val strItemCode = interSub!!.code
        val strItemType = interSub!!.marketTypeCode

        val strOrderCount = orderCountEditValue!!.text.toString()
        val strOPrice = orderPriceEditValue!!.text.toString()

        val strOrgOrderNo = editTradeNo!!.text.toString()

        tranStockModify!!.clearInputData()
        tranStockModify!!.clearOutputData()

        tranStockModify!!.setSingleData(0, 0, strAccountId)        // 계좌식별번호
        tranStockModify!!.setSingleData(0, 1, strAccountProductCode)    // 계좌상품구분코드
        tranStockModify!!.setSingleData(0, 2, strAccountPwd)        // 계좌비번
        tranStockModify!!.setSingleData(0, 3, strOrgOrderNo)        // 원주문번호
        tranStockModify!!.setSingleData(0, 4, "2")                    // 정정/취소 구분 2:전부정정, 1:일부정정

        tranStockModify!!.setSingleData(0, 5, strItemCode)            // 종목코드
        tranStockModify!!.setSingleData(0, 6, strItemType)            // 장내장외구분 0:코스피, 1:코스닥, 2:else
        tranStockModify!!.setSingleData(0, 7, strOrderCount)        // 정정수량
        tranStockModify!!.setSingleData(0, 8, strOPrice)        // 주문가격
        tranStockModify!!.setSingleData(0, 9, "01")                // 매매구분
        // 01:보통, 05:시장가, 06:조건부지정가, 12:최유리지정가, 13:최우선지정가, 71:시간외종가, 81:시간외단일가
        tranStockModify!!.setSingleData(0, 10, "00")                // 주문조건 00:일반, 01:IOC, 02:FOK

        tranStockModify!!.requestData()
    }

    // 주식 취소 주문
    private fun requestOrderCancel() {
        if (tranStockCancel == null) return

        val strAccountNo = spinnerAccount!!.accountNo
        val strAccountPwd = SmartNetMng.getInstance().getAccountInfo(strAccountNo, "pwd")
        val strAccountId = SmartNetMng.getInstance().getAccountInfo(strAccountNo, "accountid")
        val strAccountProductCode = SmartNetMng.getInstance().getAccountInfo(strAccountNo, "productcode")
        val strOrderCount = orderCountEditValue!!.text.toString()

        val strOrgOrderNo = editTradeNo!!.text.toString()

        tranStockCancel!!.clearInputData()
        tranStockCancel!!.clearOutputData()

        tranStockCancel!!.setSingleData(0, 0, strAccountId)        // 계좌식별번호
        tranStockCancel!!.setSingleData(0, 1, strAccountProductCode)    // 계좌상품구분코드
        tranStockCancel!!.setSingleData(0, 2, strAccountPwd)        // 계좌비번
        tranStockCancel!!.setSingleData(0, 3, strOrgOrderNo)        // 원주문번호
        tranStockCancel!!.setSingleData(0, 4, "1")                    // 시간구분 1:장중, 2:시간외
        tranStockCancel!!.setSingleData(0, 5, "4")                    // 정정/취소 구분 4:전부취소, 3:일부취소

        tranStockCancel!!.setSingleData(0, 6, interSub!!.code)            // 종목코드
        tranStockCancel!!.setSingleData(0, 7, interSub!!.marketTypeCode)            // 장내장외구분 0:코스피, 1:코스닥, 2:else
        tranStockCancel!!.setSingleData(0, 8, strOrderCount)        // 취소수량

        tranStockCancel!!.requestData()
    }

    override fun onTranDataReceived(sTranID: String, sMsgCode: String, sMsgText: String) {

        Log.d("wang", String.format("onTranDataReceived [%s][%s][%s]", sTranID, sMsgCode, sMsgText))

        if (sTranID == context.getString(R.string.stock_price_tran)) {

            tranCurrPrice?.run {
                strPrice = getSingleData(1, 1)

                val nPriceAttr = getSingleDataAttr(1, 1)
                val strDiff = getSingleData(1, 2)
                val nDiffAttr = getSingleDataAttr(1, 2)
                val strDiffRatio = getSingleData(1, 3)

                val strUpjong = getSingleData(1, 4)
                val strTradeAmount = getSingleData(1, 10)
                val strTradeMoney = getSingleData(1, 15)

                viewItemPrice!!.setItemPrice(strPrice, nPriceAttr, strDiff, nDiffAttr, strDiffRatio)
                viewItemPrice!!.setTradeInfo(strTradeAmount, strTradeMoney)
                viewItemPrice!!.setItemInfo(strUpjong)


            }

            requestCurrPriceReal()
        } else if (sTranID == context.getString(R.string.stock_hoga_tran)) {

            viewItemHoga?.apply {
                resetVolume()
                tranCurrHoga?.apply {
                    for (nDataIndex in 0 until 10) {

                        val strMaesuRemains = getMultiData(1, 3, nDataIndex)
                        val strMaedoRemains = getMultiData(1, 1, nDataIndex)
                        totalMaedoVolume += strMaedoRemains.toInt()
                        totalMaesuVolume += strMaesuRemains.toInt()
                    }

                    for (nDataIndex in 0 until 10) {
                        // 매도 호가
                        val strMaedoHoga = getMultiData(1, 0, nDataIndex)
                        val nMaedoHogaAttr = getMultiDataAttr(1, 0, nDataIndex)
                        val strMaedoRemains = getMultiData(1, 1, nDataIndex)

                        // 매수호가
                        val strMaesuHoga = getMultiData(1, 2, nDataIndex)
                        val nMaesuHogaAttr = getMultiDataAttr(1, 2, nDataIndex)
                        val strMaesuRemains = getMultiData(1, 3, nDataIndex)

                        setHoga(true, nDataIndex, strMaedoHoga, strPrice, nMaedoHogaAttr, strMaedoRemains)
                        setHoga(false, nDataIndex, strMaesuHoga, strPrice, nMaesuHogaAttr, strMaesuRemains)
                    }
                }
            }

            requestCurrHogaReal()
        } else if (sTranID == context.getString(R.string.stock_buy_request)) {
            val strMessage = tranStockBuy!!.getSingleData(1, 0)

            if (tranStockBuy!!.dataMsgCode == "0") {
                // 성공
                Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, tranStockBuy!!.dataMsgText, Toast.LENGTH_LONG).show()
            }
        } else if (sTranID == context.getString(R.string.stock_sell_request)) {
            val strMessage = tranStockSell!!.getSingleData(1, 0)

            if (tranStockSell!!.dataMsgCode == "0") {
                // 성공
                Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, tranStockSell!!.dataMsgText, Toast.LENGTH_LONG).show()
            }
        } else if (sTranID == context.getString(R.string.stock_modify_request)) {
            val strMessage = tranStockModify!!.getSingleData(1, 0)

            if (tranStockModify!!.dataMsgCode == "0") {
                // 성공
                Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, tranStockModify!!.dataMsgText, Toast.LENGTH_LONG).show()
            }
        } else if (sTranID == context.getString(R.string.stock_cancel_request)) {
            val strMessage = tranStockCancel!!.getSingleData(1, 0)

            if (tranStockCancel!!.dataMsgCode == "0") {
                // 성공
                Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, tranStockCancel!!.dataMsgText, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onTranTimeout(sTranID: String) {
        Log.e("onTranTimeout", String.format("TranId:%s ", sTranID))
    }

    override fun onRealDataReceived(strRealId: String) {

        if (strRealId == context.getString(R.string.stock_price_real)) {

            realCurrPrice?.run {

                val strPrice = getRealData(1, 3)
                val nPriceAttr  = getRealDataAttr(1, 3)
                val strDiff = getRealData(1, 16)
                val nDiffAttr = getRealDataAttr(1, 16)
                val strDiffRatio = getRealData(1, 12)
                val strTradeAmount = getRealData(1, 10)
                val strTradeMoney = getRealData(1, 11)

                viewItemPrice!!.setItemPrice(strPrice, nPriceAttr, strDiff, nDiffAttr, strDiffRatio)
                viewItemPrice!!.setTradeInfo(strTradeAmount, strTradeMoney)

            }

        } else if (strRealId == context.getString(R.string.stock_hoga_real)) {

            viewItemHoga?.apply {
                resetVolume()
                realCurrHoga?.apply {
                    for (nDataIndex in 0 until 10) {
                        val strMaedoRemains = getRealMultiData(2, 3, nDataIndex)
                        val strMaesuRemains = getRealMultiData(2, 4, nDataIndex)

                        totalMaedoVolume += strMaedoRemains.toInt()
                        totalMaesuVolume += strMaesuRemains.toInt()
                    }

                    var contractIntensity = Math.round(totalMaesuVolume.toFloat() / totalMaedoVolume * 100).toFloat() / 100

                    invalidate()
                    for (nDataIndex in 0 until 10) {

                        val strMaedoHoga = getRealMultiData(2, 0, nDataIndex)
                        val nMaedoHogaAttr = getRealMultiDataAttr(2, 0, nDataIndex)
                        val strMaedoRemains = getRealMultiData(2, 3, nDataIndex)

                        val strMaesuHoga = getRealMultiData(2, 1, nDataIndex)
                        val nMaesuHogaAttr = getRealMultiDataAttr(2, 1, nDataIndex)
                        val strMaesuRemains = getRealMultiData(2, 4, nDataIndex)

                        viewItemHoga!!.setHoga(true, nDataIndex, strMaedoHoga, strPrice, nMaedoHogaAttr, strMaedoRemains)
                        viewItemHoga!!.setHoga(false, nDataIndex, strMaesuHoga, strPrice, nMaesuHogaAttr, strMaesuRemains)
                    }
                }
            }
        }
    }

    override fun onClick(view: View) {

        when (view.id) {
            ID.CTRL_ID_BUY -> {
                requestOrderBuy()
            }
            ID.CTRL_ID_SELL -> {
                requestOrderSell()
            }
            ID.CTRL_ID_MODIFY -> {
                requestOrderModify()
            }
            ID.CTRL_ID_CANCEL -> {
                requestOrderCancel()
            }
            ID.CTRL_ID_ORDER_PRICE_PLUS -> {
                orderPrice += 50
                orderPriceEditValue?.setText("$orderPrice")
                val totalPrice = orderPrice * orderCount
                totalOrderPriceNo?.text = "$totalPrice 원"
            }
            ID.CTRL_ID_ORDER_PRICE_MINUS -> {
                if (orderPrice >= 50) {
                    orderPrice -= 50
                }
                orderPriceEditValue?.setText("$orderPrice")
                val totalPrice = orderPrice * orderCount
                totalOrderPriceNo?.text = "$totalPrice 원"
            }
            ID.CTRL_ID_ORDER_COUNT_PLUS -> {
                orderCount++
                orderCountEditValue?.setText("$orderCount")
                val totalPrice = orderPrice * orderCount
                totalOrderPriceNo?.text = "$totalPrice 원"
            }
            ID.CTRL_ID_ORDER_COUNT_MINUS -> {
                if (orderCount >= 1) {
                    orderCount--
                }
                orderCountEditValue?.setText("$orderCount")
                val totalPrice = orderPrice * orderCount
                totalOrderPriceNo?.text = "$totalPrice 원"
            }
        }
    }
}

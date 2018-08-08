package com.wds.king.trader.model

import android.util.Log
import com.yuanta.smartnet.intrf.IRealDataListener
import com.yuanta.smartnet.intrf.ITranDataListener
import com.yuanta.smartnet.proc.SmartNetRealProc
import com.yuanta.smartnet.proc.SmartNetTranProc
import java.io.Serializable

class InterestSubject(pName: String, pCode: String, pMarketTypeCode: String, pGroupId : Int,
                      pPos : Int, pCurPrice: String, pTodayDiffPrice: String, pMyDiffRatio: String,
                      pMyDiffPrice: String, pTodayVolume: String, pTodayDiffRatio : String, pMyVolume: String, pMyAvgPrice: String
) : ITranDataListener, IRealDataListener, Serializable {

    internal var groupId: Int = pGroupId

    internal var pos: Int = pPos

    internal var name : String = pName

    internal var code : String = pCode

    internal var marketTypeCode : String = pMarketTypeCode

    internal var curPrice: String = pCurPrice

    internal var todayDiffPrice: String = pTodayDiffPrice

    internal var myDiffRatio: String = pMyDiffRatio

    internal var myDiffPrice: String = pMyDiffPrice

    internal var todayVolume: String = pTodayVolume

    internal var todayDiffRatio: String = pTodayDiffRatio

    internal var myVolume: String = pMyVolume

    internal var myAvgPrice: String = pMyAvgPrice

    var isChecked: Boolean = false

    internal var from: Int = -1
    internal var to: Int = -1
    internal var fragIdxFrom: Int = -1
    internal var fragIdxTo: Int = -1
    internal var title: String = ""
    internal var tranCurrPrice: SmartNetTranProc? = null
    internal var realCurrPrice: SmartNetRealProc? = null
    var listener: NotifyListener? = null

    internal var curPriceChanged = false

    interface NotifyListener {
        fun onSingleDataChangedNotify(code : String, groupId : Int, pos: Int,curPrice: String)
    }

    constructor()
            : this("","","",-1,-1,"","","","","","","","")

    fun setOnSingleDataChangedNotify(listener: NotifyListener) {

        this.listener = listener
    }

    fun initPrice() {

        tranCurrPrice?.let {
            it.initInstance(this)
            it.setQueryFile("DSO300001")
            it.tranId = "주식현재가"
            it.clearInputData()
            it.clearOutputData()
            it.setSingleData(0, 0, marketTypeCode)
            it.setSingleData(0, 1, code)
            it.requestData()
        }

        realCurrPrice?.let {
            it.initInstance(this)
            it.setQueryFile("AUTO11")
            it.realId = "주식현재가실시간"
        }
    }

    override fun onTranTimeout(sTranID: String) {
        Log.e("onTranTimeout", String.format("TranId:%s ", sTranID))
    }

    override fun onRealDataReceived(strRealId: String) {

        if (strRealId == "주식현재가실시간") {
            realCurrPrice?.run {
                curPrice = getRealData(1, 3)
                val nPriceAttr = getRealDataAttr(1, 3)
                todayDiffPrice = getRealData(1, 16)
                val nDiffAttr = getRealDataAttr(1, 16)
                todayDiffRatio = getRealData(1, 12)

                todayVolume = getRealData(1, 10)
                val strTradeMoney = getRealData(1, 11)

                listener?.onSingleDataChangedNotify(code, groupId, pos, curPrice)
            }
        }
    }

    override fun onTranDataReceived(sTranID: String, sMsgCode: String, sMsgText: String) {

        if (sTranID == "주식현재가") {
            tranCurrPrice?.run {
                curPrice = getSingleData(1, 1)

                val nPriceAttr = getSingleDataAttr(1, 1)
                todayDiffPrice = getSingleData(1, 2)
                val nDiffAttr = getSingleDataAttr(1, 2)
                todayDiffRatio = getSingleData(1, 3)

                val strUpjong = getSingleData(1, 4)
                todayVolume = getSingleData(1, 10)
                val strTradeMoney = getSingleData(1, 15)

                listener?.onSingleDataChangedNotify(code, groupId, pos, curPrice)
            }
            requestCurrPriceReal()
        }
    }

    private fun requestCurrPriceReal() {

        tranCurrPrice?.run {
            val strItemCode = getSingleData(0, 1).trim { it <= ' ' }
            realCurrPrice?.run {
                setRealData(0, 0, strItemCode)
                requestReal()
            }
        }
    }

    fun clearInstance()
    {
        tranCurrPrice?.apply {
            clearInstance()
            tranCurrPrice = null
        }
        realCurrPrice?.apply {
            clearInstance()
            realCurrPrice = null
        }
    }
}
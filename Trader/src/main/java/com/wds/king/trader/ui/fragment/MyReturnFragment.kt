package com.wds.king.trader.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.wds.king.trader.R
import com.wds.king.trader.database.MyProfitRepo
import com.wds.king.trader.disposables
import com.wds.king.trader.model.InterestSubject
import com.wds.king.trader.rxextensions.runOnIoScheduler
import kotlinx.android.synthetic.main.my_ratio_of_return_dlg.*
import kotlinx.android.synthetic.main.my_return_fragment.view.*

const val MY_RETURN_TAG = "MyReturnFragment"

class MyReturnFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onActivityCreated(savedInstanceState)

        val rootView: View = inflater.inflate(R.layout.my_return_fragment, container, false)
                ?: return null

        val interSub = arguments!!.getSerializable("item") as InterestSubject?

        rootView.my_return_subject_name!!.text = interSub!!.name

        rootView.my_return_input_buy.setOnClickListener {
            rootView.my_return_input_buy.visibility = View.GONE
            rootView.my_return_menu.visibility = View.VISIBLE
        }

        val dialog = MaterialDialog.Builder(activity!!)

                .customView(R.layout.my_ratio_of_return_dlg, true)
                .positiveText(R.string.check)
                .negativeText(R.string.cancel)
                .onPositive { dialog1, which ->
                    val (volume, price) = setMyProfitSetting(dialog1, rootView, interSub!!.curPrice)

                    disposables?.add(runOnIoScheduler {

                        val myProfitRepo = MyProfitRepo()
                        myProfitRepo.code = interSub!!.code
                        myProfitRepo.groupId = interSub!!.groupId
                        myProfitRepo.volume = volume
                        myProfitRepo.myPrice = price
                        myProfitRepo.curPrice = interSub!!.curPrice.toInt()
                        myProfitDaoEx.add(myProfitRepo)
                    })
                }.build()

        val runnable = Runnable {

            myProfitDaoEx.get(interSub!!.code, interSub!!.groupId)?.let {

                dialog.my_return_edit_buy_count.setText(it.volume.toString())
                dialog.my_return_edit_buy_price.setText(it.myPrice.toString())

                setMyProfitSetting(rootView, it.volume, it.myPrice, interSub!!.curPrice)

                activity!!.runOnUiThread{
                    rootView.my_return_input_buy.visibility = View.GONE
                    rootView.my_return_menu.visibility = View.VISIBLE
                }
            }
        }
        val thread = Thread(runnable)
        thread.start()

        rootView.my_return_modify!!.setOnClickListener {
            dialog.show()
        }

        rootView.my_return_remove!!.setOnClickListener {

            rootView.my_return_menu.visibility = View.GONE
            rootView.my_return_input_buy.visibility = View.VISIBLE

            disposables?.add(runOnIoScheduler {
                val myProfitRepo = MyProfitRepo()
                myProfitRepo.code = interSub!!.code
                myProfitRepo.groupId = interSub!!.groupId
                myProfitDaoEx.delete(myProfitRepo)
            })
        }

        return rootView
    }

    private fun setMyProfitSetting(rootView: View, volume: Int, price: Int, curPrice: String): Pair<Int, Int> {

        val totalPrice = volume * price
        rootView.my_return_buy_count.text = volume.toString()
        rootView.my_return_buy_price.text = price.toString()
        rootView.my_return_total_buy_price.text = totalPrice.toString()

        val temp = (1 - (price.toFloat() / curPrice.toInt())) * 100
        val profitRatio = (Math.round(temp * 100).toFloat() / 100)
        val profitPrice = (curPrice.toInt() * volume) - (price * volume)

        rootView.my_return_profit_ratio.text = "$profitRatio %"
        rootView.my_return_profit_price.text = profitPrice.toString()
        return Pair(volume, price)
    }

    private fun setMyProfitSetting(dialog1: MaterialDialog, rootView: View, curPrice: String): Pair<Int, Int> {

        val volume = dialog1.my_return_edit_buy_count.text.toString().toInt()
        val price = dialog1.my_return_edit_buy_price.text.toString().toInt()
        return setMyProfitSetting(rootView, volume, price, curPrice)
    }

    override fun onDetach() {
        super.onDetach()
    }
}
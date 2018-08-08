package com.wds.king.trader.ui.util

enum class Sort(i: Int, s: String) {

    NO_SORT(0, "no_sort"),
    DIFF_RATIO(1, "diff_ratio"),
    DIFF_PRICE(2, "diff_price"),
    TRADING_PRICE(3, "trading_price"),
    TRADING_VOLUME(4, "trading_volume"),
    PRICE(5, "price"),
    PROFIT(6, "profit"),
    PROFIT_RATIO(7, "profit_ratio"),
    PURCHASED_PRICE(8, "purchased_price");

    var idx: Int = 0
        internal set
    var type: String
        internal set

    init {

        idx = i
        type = s
    }

    companion object {

        fun fromIdx(idx: Int): Sort {
            when (idx) {
                0 -> return NO_SORT
                1 -> return DIFF_RATIO
                2 -> return DIFF_PRICE
                3 -> return TRADING_PRICE
                4 -> return TRADING_VOLUME
                5 -> return PRICE
                6 -> return PROFIT
                7 -> return PROFIT_RATIO
                7 -> return PURCHASED_PRICE
                else -> return NO_SORT
            }
        }
    }
}

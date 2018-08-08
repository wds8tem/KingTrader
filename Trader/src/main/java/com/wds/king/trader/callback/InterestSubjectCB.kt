package com.wds.king.trader.callback

import android.widget.LinearLayout
import android.widget.TextView

interface InterestSubjectCB {
    fun onViewClicked(selectedItem : LinearLayout, isSelected : Boolean)
}

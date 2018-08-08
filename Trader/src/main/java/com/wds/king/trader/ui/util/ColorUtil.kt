package com.wds.king.trader.ui.util

import android.graphics.Color

object ColorUtil {

    val RISE = Color.RED
    val FALL = Color.BLUE
    val STAY = Color.DKGRAY

    fun getAlphaColor(nColor: Int, nAlpha: Int): Int {
        return Color.argb((255f * nAlpha.toFloat() / 100f).toInt(), Color.red(nColor), Color.green(nColor), Color.blue(nColor))
    }
}

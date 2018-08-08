package com.wds.king.trader.ui.view

import com.wds.king.trader.ui.util.ColorUtil
import com.wds.king.trader.ui.util.Util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class ItemHogaView(context: Context) : FrameLayout(context) {

    private var arrViews: Array<HogaItemView?> = arrayOfNulls(20)

    private var PADDING_H = 20
    private var LINE_WIDTH = 1
    private var linePaint: Paint = Paint()
    private var volumePaint: Paint = Paint()
    private var diffPaint: Paint = Paint()

    var totalMaedoVolume = 0
    var totalMaesuVolume = 0
    var nViewIndex = 0

    private var listener: OnHogaViewListener? = null

    init {

        PADDING_H = Util.dipToPixels(context, 20f).toInt()
        LINE_WIDTH = Util.dipToPixels(context, 1f).toInt()
        if (LINE_WIDTH < 2) LINE_WIDTH = 2

        val layoutFrame = LinearLayout(context)
        layoutFrame.orientation = LinearLayout.VERTICAL

        for (i in 0 until arrViews.size) {
            arrViews[i] = HogaItemView(context, isMaedo = i < 10)
            layoutFrame.addView(arrViews[i], LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        }

        addView(layoutFrame, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        linePaint!!.color = Color.LTGRAY
        linePaint!!.strokeWidth = LINE_WIDTH.toFloat()
        linePaint!!.style = Paint.Style.STROKE

        diffPaint.textSize = 24F

    }

    fun resetVolume() {
        totalMaedoVolume = 0
        totalMaesuVolume = 0
    }

    fun setHoga(isMaedo: Boolean, nOffset: Int, strHoga: String, currentPrice: String, nHogaAttr: Int, strRemain: String) {

        val remain = strRemain.toInt()
        if (isMaedo) {
            nViewIndex = 10 - nOffset - 1
        } else {
            nViewIndex = nOffset + 10
        }

        if (nViewIndex < 0 || nViewIndex >= arrViews.size) return

        arrViews[nViewIndex]?.setHogaInfo(strHoga, currentPrice, nHogaAttr, strRemain)
        arrViews[nViewIndex]?.apply {
            diff = volume - remain
            volume = remain
        }
    }

    fun setOnHogaViewListener(listener: OnHogaViewListener) {
        this.listener = listener
    }

    public override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        val nWidth = width
        val nHeight = height

        canvas.drawRect(0f, 0f, nWidth.toFloat(), nHeight.toFloat(), linePaint!!)

        val nX1 = nWidth / 2
        val nMaxUnit = nX1 * 0.9F
        val nXOffset = nX1 * 0.05F
        val nYOffset = 8.0F
        val nYTextOffset = 27.0F
        val nX2 = nWidth / 2

        canvas.drawLine(nX1.toFloat(), 0f, nX1.toFloat(), nHeight.toFloat(), linePaint!!)
        canvas.drawLine(nX2.toFloat(), 0f, nX2.toFloat(), nHeight.toFloat(), linePaint!!)

        val volumeHeight = nHeight / 20.0F * 0.8F

        val yUnit = nHeight / 20.0F
        for (y in 0 until 21) {
            val nY = nHeight / 20.0F * (y)

            if (y in 0..19) {
                val volumeYPos = nY + yUnit
                val volume = arrViews[y]!!.volume.toFloat()
                val diff = arrViews[y]!!.diff
                if (arrViews[y]!!.volume > 0) {

                    val ratio = volume / totalMaesuVolume * nMaxUnit
                    if (y < 10) {
                        volumePaint!!.color = ColorUtil.getAlphaColor(Color.parseColor("#87cefa"), 70)

                    } else {
                        volumePaint!!.color = ColorUtil.getAlphaColor(Color.parseColor("#ff4500"), 70)
                    }
                    canvas.drawRect(nX2 + nXOffset, volumeYPos - nYOffset, nX2 + nXOffset + ratio, volumeYPos - volumeHeight - nYOffset, volumePaint!!)

                    if (diff > 0) {
                        diffPaint.color = ColorUtil.getAlphaColor(Color.parseColor("#87cefa"), 90)
                        canvas.drawText("" + Math.abs(diff), nWidth - 85F, volumeYPos - nYTextOffset, diffPaint)
                    } else if (diff < 0) {
                        diffPaint.color = ColorUtil.getAlphaColor(Color.parseColor("#ff4500"), 90)
                        canvas.drawText("" + Math.abs(diff), nWidth - 80F, volumeYPos - nYTextOffset, diffPaint)
                    }
                }
            }

            canvas.drawLine(0f, nY, nWidth.toFloat(), nY, linePaint!!)
        }
    }

    interface OnHogaViewListener {
        fun onHogaClicked(strPrice: String)
    }

    private inner class HogaItemView(context: Context, isMaedo: Boolean) : LinearLayout(context) {

        private val viewHoga: TextView
        private val viewRemain: TextView
        var volume = 0
        var diff = 0

        init {

            orientation = LinearLayout.HORIZONTAL

            viewHoga = makeTextView(context, true, isMaedo)
            viewRemain = makeTextView(context, false, isMaedo)

            viewHoga.setPadding(PADDING_H, 0, PADDING_H, 0)
            viewRemain.setPadding(PADDING_H, 0, PADDING_H, 0)

            addView(viewHoga, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))
            addView(viewRemain, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))

            if (isMaedo) {

                viewHoga.setBackgroundColor(ColorUtil.getAlphaColor(Color.parseColor("#87cefa"), 10))
                viewRemain.setBackgroundColor(ColorUtil.getAlphaColor(Color.parseColor("#87cefa"), 10))
            } else {
                viewHoga.setBackgroundColor(ColorUtil.getAlphaColor(Color.parseColor("#ff4500"), 10))
                viewRemain.setBackgroundColor(ColorUtil.getAlphaColor(Color.parseColor("#ff4500"), 10))
            }

            viewHoga.isClickable = true
            viewHoga.setOnClickListener { view ->
                val objTag = view.tag
                if (objTag != null && objTag is String) {
                    if (listener != null)
                        listener!!.onHogaClicked(objTag)
                }
            }
        }

        fun setHogaInfo(strHoga: String, currentPrice: String, nHogaAttr: Int, strRemain: String) {

            Util.setIntValue(viewHoga, strHoga, currentPrice, nHogaAttr)
            Util.setIntValue(viewRemain, strRemain)

            viewHoga.tag = strHoga
        }

        private fun makeTextView(context: Context, isHoga: Boolean, isMaedo: Boolean): TextView {

            val viewText = TextView(context).apply {
                setTextColor(if (isHoga) if (isMaedo) Color.RED else Color.BLUE else Color.BLACK)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)

                if (isHoga) {
                    gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                } else {
                    gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                }
                setSingleLine()
            }

            return viewText
        }
    }
}

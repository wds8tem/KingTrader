package com.wds.king.trader.ui.layout

import android.content.Context
import android.support.v4.widget.DrawerLayout
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup

class FullDrawerLayout : DrawerLayout {

    private fun gravityToString(gravity: Int): String {
        if (gravity and Gravity.LEFT == Gravity.LEFT) {
            return "LEFT"
        }
        return if (gravity and Gravity.RIGHT == Gravity.RIGHT) {
            "RIGHT"
        } else Integer.toHexString(gravity)
    }

    private val MIN_DRAWER_MARGIN = 0

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        if (widthMode != View.MeasureSpec.EXACTLY || heightMode != View.MeasureSpec.EXACTLY) {
            throw IllegalArgumentException(
                    "DrawerLayout must be measured with MeasureSpec.EXACTLY.")
        }

        setMeasuredDimension(widthSize, heightSize)

        val foundDrawers = 0
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)

            if (child.visibility == View.GONE) {
                continue
            }

            val lp = child.layoutParams as DrawerLayout.LayoutParams

            if (isContentView(child)) {
                val contentWidthSpec = View.MeasureSpec.makeMeasureSpec(
                        widthSize - lp.leftMargin - lp.rightMargin, View.MeasureSpec.EXACTLY)
                val contentHeightSpec = View.MeasureSpec.makeMeasureSpec(
                        heightSize - lp.topMargin - lp.bottomMargin, View.MeasureSpec.EXACTLY)
                child.measure(contentWidthSpec, contentHeightSpec)
            } else if (isDrawerView(child)) {
                val childGravity = getDrawerViewGravity(child) and Gravity.HORIZONTAL_GRAVITY_MASK
                if (foundDrawers and childGravity != 0) {
                    throw IllegalStateException("Child drawer has absolute gravity " +
                            gravityToString(childGravity) + " but this already has a " +
                            "drawer view along that edge")
                }
                val drawerWidthSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec,
                        MIN_DRAWER_MARGIN + lp.leftMargin + lp.rightMargin,
                        lp.width)
                val drawerHeightSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec,
                        lp.topMargin + lp.bottomMargin,
                        lp.height)
                child.measure(drawerWidthSpec, drawerHeightSpec)
            } else {
                throw IllegalStateException("Child " + child + " at index " + i +
                        " does not have a valid layout_gravity - must be Gravity.LEFT, " +
                        "Gravity.RIGHT or Gravity.NO_GRAVITY")
            }
        }
    }

    internal fun isContentView(child: View): Boolean {
        return (child.layoutParams as DrawerLayout.LayoutParams).gravity == Gravity.NO_GRAVITY
    }

    internal fun isDrawerView(child: View): Boolean {
        val gravity = (child.layoutParams as DrawerLayout.LayoutParams).gravity
        val absGravity = Gravity.getAbsoluteGravity(gravity,
                child.layoutDirection)
        return absGravity and (Gravity.LEFT or Gravity.RIGHT) != 0
    }

    internal fun getDrawerViewGravity(drawerView: View): Int {
        val gravity = (drawerView.layoutParams as DrawerLayout.LayoutParams).gravity
        return Gravity.getAbsoluteGravity(gravity, drawerView.layoutDirection)
    }
}

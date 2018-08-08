package com.wds.king.trader.ui.view

import android.content.Context
import android.graphics.Color
import android.support.design.widget.TabLayout
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.TypedValue
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.*

open class BaseView(context: Context)
    : LinearLayout(context) {

    var stockAcctType = arrayOf("A01010001", "A01010002", "A01010003", "A03010001", "A03020001", "A03020002", "A03030001",
            "A03030009", "A03030013", "A03040002", "A03040003", "A03040004", "A03040005", "A03040006", "A03040007", "A03040008",
            "A03040009", "A03040010", "A03040011", "A03040012", "A03040024", "A06010001", "A06010002", "A06010003")

    @JvmOverloads
    protected fun makeTextView(strText: String, nTextDip: Int = 20, nTextColor: Int = Color.BLACK): TextView {
        val viewText = TextView(context)
        viewText.setTextColor(nTextColor)
        viewText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, nTextDip.toFloat())
        viewText.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        viewText.text = strText
        viewText.setSingleLine()

        return viewText
    }

    @JvmOverloads
    protected fun makeTabLayout(): TabLayout {
        return TabLayout(context)
    }

    protected fun makeEditView(strText: String, gravity: Int): EditText {
        val viewEdit = EditText(context)
        viewEdit.setTextColor(Color.BLACK)
        viewEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        viewEdit.gravity = gravity
        viewEdit.setText(strText)
        viewEdit.setSingleLine()

        return viewEdit
    }

    protected fun setEditText(viewEdit: EditText?, isNormal: Boolean, isNumber: Boolean) {

        viewEdit?.apply {
            isLongClickable = false
            isFocusableInTouchMode = true
            isFocusable = true
            ellipsize = null
            privateImeOptions = "defaultInputmode=english;"
            imeOptions = EditorInfo.IME_ACTION_DONE
            if (isNormal) {
                if (isNumber)
                    inputType = InputType.TYPE_CLASS_NUMBER
                else
                    inputType = InputType.TYPE_TEXT_VARIATION_URI or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                if (isNumber)
                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_TEXT_VARIATION_PASSWORD
                else
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                viewEdit.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
    }

    protected fun makeCtrlButton(nCtrlId: Int, strText: String): Button {
        val button = Button(context)
        button.setTextColor(Color.BLACK)
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        button.gravity = Gravity.CENTER
        button.text = strText
        button.setSingleLine()
        button.id = nCtrlId
        button.setPadding(0, 0, 0, 0)

        return button
    }

    protected fun makeCheckBox(nCtrlId: Int, strText: String): CheckBox {
        val box = CheckBox(context)

        box.text = strText
        box.id = nCtrlId
        box.setTextColor(Color.BLACK)
        box.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)

        return box
    }
}

package com.wds.king.trader.ui.fragment


import android.support.v4.app.Fragment
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.inputmethod.EditorInfo
import android.widget.EditText

open class BaseFragment : Fragment() {

    protected fun setEditText(viewEdit: EditText?, isNormal: Boolean, isNumber: Boolean) {

        if(viewEdit!=null) {
            with(viewEdit)
            {
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
    }
}
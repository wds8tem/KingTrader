package com.wds.king.trader.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wds.king.trader.R

const val INTRO_TAG = "LoginFragment"

class IntroFragment : BaseFragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.intro_fragment, container, false) ?: null
    }
}
package com.wds.king.trader.model

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class SignViewModelFactory(
        val authTokenProvider: AuthTokenProvider)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SignViewModel(authTokenProvider) as T
    }
}

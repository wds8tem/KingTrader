package com.wds.king.trader.model

import android.arch.lifecycle.ViewModel
import android.util.Log
import com.wds.king.trader.ui.util.SupportOptional
import com.wds.king.trader.ui.util.optionalOf
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class SignViewModel(val authTokenProvider: AuthTokenProvider)
    : ViewModel() {

    val accessToken: BehaviorSubject<SupportOptional<String>> = BehaviorSubject.create()

    fun loadAccessToken(): Disposable = Single.fromCallable { optionalOf(authTokenProvider.token) }
            .subscribeOn(Schedulers.io())
            .subscribe(Consumer<SupportOptional<String>> {
                Log.d("wang","authTokenProvider.token :  ${authTokenProvider.token}")
                accessToken.onNext(it)
            })

    fun requestAccessToken(token : String){
        authTokenProvider.updateToken(token)
    }

}

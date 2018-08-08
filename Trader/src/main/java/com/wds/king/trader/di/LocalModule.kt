package com.wds.king.trader.di

import android.arch.persistence.room.Room
import android.content.Context
import com.wds.king.trader.database.Database
import com.wds.king.trader.database.InterestedGroupDao
import com.wds.king.trader.database.InterestedSubjectDao
import com.wds.king.trader.database.MyProfitDao
import com.wds.king.trader.model.AuthTokenProvider
import com.wds.king.trader.model.SignViewModelFactory
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class LocalModule {

    @Provides
    @Singleton
    fun provideSignViewModelFactory(authTokenProvider: AuthTokenProvider): SignViewModelFactory
            = SignViewModelFactory(authTokenProvider)

    @Provides
    @Singleton
    fun provideAuthTokenProvider(@Named("appContext") context: Context): AuthTokenProvider
            = AuthTokenProvider(context)

    @Provides
    @Singleton
    fun provideInterSubDao(db: Database): InterestedSubjectDao = db.interestedSubjectDao()

    @Provides
    @Singleton
    fun provideInterGroupDao(db: Database): InterestedGroupDao = db.interestedGroupDao()

    @Provides
    @Singleton
    fun provideMyProfitDao(db: Database): MyProfitDao = db.myProfitGroupDao()

    @Provides
    @Singleton
    fun provideDB(@Named("appContext") context: Context): Database = Room.databaseBuilder(context.applicationContext,
            Database::class.java, "king_trader2.db")
            .build()

}
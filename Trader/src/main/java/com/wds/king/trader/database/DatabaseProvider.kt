package com.wds.king.trader.database

import android.arch.persistence.room.Room
import android.content.Context

private var instance : Database? = null

fun provideInterSubDao(context: Context): InterestedSubjectDao
        = provideDatabase(context).interestedSubjectDao()

fun provideInterGroupDao(context: Context): InterestedGroupDao
        = provideDatabase(context).interestedGroupDao()

fun provideMyProfitDao(context: Context): MyProfitDao
        = provideDatabase(context).myProfitGroupDao()

fun provideDatabase(context: Context): Database {
    if (null == instance) {
        instance = Room.databaseBuilder(context.applicationContext,
                Database::class.java, "king_trader2.db")
                .build()
    }
    return instance!!
}
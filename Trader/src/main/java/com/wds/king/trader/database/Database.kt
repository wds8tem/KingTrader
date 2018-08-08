package com.wds.king.trader.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [(InterestedSubjectRepo::class), (InterestedGroupRepo::class), (MyProfitRepo::class)], version = 1)
abstract class Database : RoomDatabase() {

    abstract fun interestedSubjectDao(): InterestedSubjectDao
    abstract fun interestedGroupDao(): InterestedGroupDao
    abstract fun myProfitGroupDao(): MyProfitDao
}
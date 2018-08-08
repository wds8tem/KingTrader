package com.wds.king.trader.database

import android.arch.persistence.room.*

import io.reactivex.Flowable

@Dao
interface InterestedGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(interestedGroupRepo: InterestedGroupRepo)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun replace(interestedGroupRepo: InterestedGroupRepo)

    @Delete()
    fun delete(interestedGroupRepo: InterestedGroupRepo)

    @Query("select * from interest_group")
    fun get(): Flowable<List<InterestedGroupRepo>>

    @Query("select * from interest_group order by group_id desc limit 1")
    fun getItem(): InterestedGroupRepo

    @Query("select count(*) from interest_group")
    fun getCount(): Int
}
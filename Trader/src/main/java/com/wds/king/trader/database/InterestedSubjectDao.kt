package com.wds.king.trader.database

import android.arch.persistence.room.*

import io.reactivex.Flowable

@Dao
interface InterestedSubjectDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(interestSubjectRepo: InterestedSubjectRepo)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun replace(interestSubjectRepo: InterestedSubjectRepo)

    @Delete()
    fun delete(interestSubjectRepo: InterestedSubjectRepo)

    @Query("delete from interest_subject where group_id=:groupId")
    fun deleteItem(groupId : Int)

    @Query("select * from interest_subject where group_id=:groupId")
    fun get(groupId : Int): Flowable<List<InterestedSubjectRepo>>

    @Query("select * from interest_subject")
    fun get(): Flowable<List<InterestedSubjectRepo>>

    @Query("delete from interest_subject")
    fun deleteAll()

    @Query("select count(*) from interest_subject")
    fun getCount(): Int
}
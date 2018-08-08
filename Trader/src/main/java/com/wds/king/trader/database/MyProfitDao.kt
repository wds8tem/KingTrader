package com.wds.king.trader.database

import android.arch.persistence.room.*
import io.reactivex.Flowable

@Dao
interface MyProfitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(myProfitRepo: MyProfitRepo)

    @Query("update my_profit set cur_price=:curPrice where code=:code and group_id=:groupId")
    fun replace(code : String, groupId : Int,curPrice : Int)

    @Query("select * from my_profit")
    fun get(): Flowable<List<MyProfitRepo>>

    @Query("select * from my_profit")
    fun getNoBind(): List<MyProfitRepo>

    @Query("select * from my_profit where code=:code and group_id=:groupId")
    fun get(code : String, groupId : Int): MyProfitRepo

    @Query("select count(*) from my_profit")
    fun getCount(): Int

    @Delete()
    fun delete(myProfitRepo: MyProfitRepo)
}
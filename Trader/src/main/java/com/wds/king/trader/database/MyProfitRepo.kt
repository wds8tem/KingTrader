package com.wds.king.trader.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity

@Entity(tableName = "my_profit",primaryKeys = ["code","group_id"])
class MyProfitRepo {

    @ColumnInfo(name = "code")
    var code: String = ""

    @ColumnInfo(name = "group_id")
    var groupId: Int = -1

    @ColumnInfo(name = "market_type_code")
    var marketTypeCode : String = ""

    @ColumnInfo(name = "volume")
    var volume: Int = 0

    @ColumnInfo(name = "my_price")
    var myPrice: Int = 0

    @ColumnInfo(name = "cur_price")
    var curPrice: Int = 0
}
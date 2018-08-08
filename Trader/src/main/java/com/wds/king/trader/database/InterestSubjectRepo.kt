package com.wds.king.trader.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity

@Entity(tableName = "interest_subject", primaryKeys = ["code","group_id"])
class InterestedSubjectRepo{

    @ColumnInfo(name = "group_id")
    var groupId: Int = -1

    @ColumnInfo(name = "pos")
    var pos: Int = -1

    @ColumnInfo(name = "name")
    var name : String = ""

    @ColumnInfo(name = "code")
    var code : String = ""

    @ColumnInfo(name = "market_type_code")
    var marketTypeCode : String = ""

    @ColumnInfo(name = "cur_price")
    var curPrice: String = ""

    @ColumnInfo(name = "today_diff_price")
    var todayDiffPrice: String = ""

    @ColumnInfo(name = "my_diff_ratio")
    var myDiffRatio: String = ""

    @ColumnInfo(name = "my_diff_price")
    var myDiffPrice: String = ""

    @ColumnInfo(name = "today_volume")
    var todayVolume: String = ""

    @ColumnInfo(name = "today_diff_ratio")
    var todayDiffRatio: String = ""

    @ColumnInfo(name = "my_volume")
    var myVolume: String = ""

    @ColumnInfo(name = "my_avg_price")
    var myAvgPrice: String = ""
}
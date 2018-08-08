package com.wds.king.trader.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "interest_group")
class InterestedGroupRepo {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "group_id")
    var groupId: Int? = null

    @ColumnInfo(name = "pos")
    var pos: Int = -1

    @ColumnInfo(name = "name")
    var name : String = ""
}
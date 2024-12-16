package com.valleapp.valletpvlib.db.entities

import androidx.room.PrimaryKey
import com.valleapp.valletpvlib.interfaces.IBaseEntity

abstract class BaseEntity(
    @PrimaryKey(autoGenerate = true) open var id: Long = 0,
)
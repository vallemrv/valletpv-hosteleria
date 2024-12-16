package com.valleapp.valletpvlib.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.valleapp.valletpvlib.db.entities.BaseEntity


interface BaseDao<T: BaseEntity> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(obj: T): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(objList: List<T>): List<Long>

    @Update
    suspend fun update(obj: T): Int

    @Update
    suspend fun update(objList: List<T>): Int // Nueva firma para lista

    @Delete
    suspend fun delete(obj: T): Int

    @Delete
    suspend fun delete(objList: List<T>): Int // Nueva firma para lista

    suspend fun getAll(): List<T>

    suspend fun getById(): T?

    suspend fun deleteById(): Int

    suspend fun removeAll(): Int
}
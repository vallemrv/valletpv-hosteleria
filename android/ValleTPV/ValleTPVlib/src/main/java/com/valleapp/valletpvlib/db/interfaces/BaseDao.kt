package com.valleapp.valletpvlib.db.interfaces

import androidx.room.*

@Dao
interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<T>)

    @Update
    suspend fun update(entity: T)

    @Delete
    suspend fun delete(entity: T)
    abstract fun getAll(): List<Any>
    abstract fun filterByCondition(cWhere: String): List<Any>

}

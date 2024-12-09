package com.valleapp.valletpvlib.db.interfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.valleapp.valletpvlib.db.entities.Camarero


@Dao
interface CamareroDao  {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(camarero: Camarero)

    @Update
    suspend fun update(camarero: Camarero)

    @Delete
    suspend fun delete(camarero: Camarero)

    @Query("SELECT * FROM camareros WHERE activo = 1")
    suspend fun getAllActive(): List<Camarero>

    @Query("UPDATE camareros SET autorizado = :autorizado WHERE id = :id")
    suspend fun setAutorizado(id: Long, autorizado: Boolean)

    @Query("SELECT * FROM camareros WHERE activo = 1 AND autorizado = :autorizado")
    suspend fun getAutorizados(autorizado: Boolean): List<Camarero>

    @Query("SELECT * FROM camareros WHERE activo = 1 AND permisos LIKE '%' || :permiso || '%'")
    suspend fun getConPermiso(permiso: String): List<Camarero>
}


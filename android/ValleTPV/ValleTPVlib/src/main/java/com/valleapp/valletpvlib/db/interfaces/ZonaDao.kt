package com.valleapp.valletpvlib.db.interfaces;

import androidx.room.*
import com.valleapp.valletpvlib.db.entities.Zona

@Dao
interface ZonaDao {

    // Obtener todas las zonas
    @Query("SELECT * FROM zonas")
    suspend fun getAll(): List<Zona>

    // Insertar o reemplazar una zona
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(zona: Zona)

    // Insertar o reemplazar múltiples zonas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(zonas: List<Zona>)

    // Eliminar una zona por ID
    @Query("DELETE FROM zonas WHERE ID = :id")
    suspend fun deleteById(id: Int)
}

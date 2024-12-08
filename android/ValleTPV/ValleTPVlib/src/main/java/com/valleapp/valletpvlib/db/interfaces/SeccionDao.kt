package com.valleapp.valletpvlib.db.interfaces

import androidx.room.*
import com.valleapp.valletpvlib.db.entities.SeccionEntity

@Dao
interface SeccionDao {

    // Obtener todas las secciones
    @Query("SELECT * FROM secciones ORDER BY Orden DESC")
    suspend fun getAll(): List<SeccionEntity>

    // Filtrar secciones
    @Query("SELECT * FROM secciones WHERE (:condition IS NULL OR :condition = '' OR :condition)")
    suspend fun filter(condition: String?): List<SeccionEntity>

    // Insertar o reemplazar una sección
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(seccion: SeccionEntity)

    // Insertar o reemplazar múltiples secciones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(secciones: List<SeccionEntity>)

    // Eliminar una sección por ID
    @Query("DELETE FROM secciones WHERE ID = :id")
    suspend fun deleteById(id: Int)
}

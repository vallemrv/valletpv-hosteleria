package com.valleapp.valletpvlib.db.dao

import androidx.room.*
import com.valleapp.valletpvlib.db.entities.Seccion

@Dao
interface SeccionDao {

    // Obtener todas las secciones
    @Query("SELECT * FROM secciones ORDER BY Orden DESC")
    suspend fun getAll(): List<Seccion>

    // Filtrar secciones
    @Query("SELECT * FROM secciones WHERE (:condition IS NULL OR :condition = '' OR :condition)")
    suspend fun filter(condition: String?): List<Seccion>

    // Insertar o reemplazar una sección
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(seccion: Seccion)

    // Insertar o reemplazar múltiples secciones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(secciones: List<Seccion>)

    // Eliminar una sección por ID
    @Query("DELETE FROM secciones WHERE ID = :id")
    suspend fun deleteById(id: Int)
}

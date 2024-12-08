package com.valleapp.valletpvlib.db.interfaces

import androidx.room.*
import com.valleapp.valletpvlib.db.entities.SubTeclaEntity

@Dao
interface SubTeclaDao {

    // Obtener todas las subteclas para una tecla específica
    @Query("SELECT * FROM subteclas WHERE IDTecla = :idTecla")
    suspend fun getAllByTecla(idTecla: Int): List<SubTeclaEntity>

    // Insertar o reemplazar una subtecla
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subTecla: SubTeclaEntity)

    // Insertar o reemplazar múltiples subteclas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subTeclas: List<SubTeclaEntity>)

    // Eliminar una subtecla por ID
    @Query("DELETE FROM subteclas WHERE ID = :id")
    suspend fun deleteById(id: Int)
}

package com.valleapp.valletpvlib.db.interfaces

import androidx.room.*
import com.valleapp.valletpvlib.db.entities.TeclaEntity

@Dao
interface TeclaDao {

    // Obtener todas las teclas por sección o subsección
    @Query("SELECT * FROM teclas WHERE IDSeccion = :idSeccion OR IDSec2 = :idSeccion ORDER BY Orden DESC")
    suspend fun getAllBySeccion(idSeccion: Int): List<TeclaEntity>

    // Buscar teclas por nombre o etiqueta con tarifa específica
    @Query("""
        SELECT * FROM teclas 
        WHERE Nombre LIKE '%' || :str || '%' OR Tag LIKE '%' || :str || '%' 
        ORDER BY Orden DESC 
        LIMIT 15
    """)
    suspend fun findLike(str: String): List<TeclaEntity>

    // Insertar o reemplazar una tecla
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tecla: TeclaEntity)

    // Insertar o reemplazar múltiples teclas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(teclas: List<TeclaEntity>)

    // Eliminar una tecla por ID
    @Query("DELETE FROM teclas WHERE ID = :id")
    suspend fun deleteById(id: Int)
}
